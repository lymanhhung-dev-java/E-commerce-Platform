import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatRoom, ChatMessage } from '../../core/services/chat.service';
import { UserService } from '../../core/services/user.service';

@Component({
  selector: 'app-merchant-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './merchant-chat.html',
  styleUrls: ['./merchant-chat.css']
})
export class MerchantChatComponent implements OnInit, OnDestroy {
  @ViewChild('messageContainer') messageContainer!: ElementRef;

  chatRooms: ChatRoom[] = [];
  selectedRoom: ChatRoom | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  currentUserId: number = 0;
  loading = false;
  private roomSubscription: any = null;
  private globalSubscription: any = null;

  constructor(
    private chatService: ChatService,
    private userService: UserService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.userService.getMyProfile().subscribe((profile: any) => {
        this.currentUserId = profile.id;
        if (profile.shopId || profile.isShopOwner) {
          this.subscribeToGlobalNotifications();
        }
      });

      this.chatService.connect();
      this.loadChatRooms();
    }
  }

  ngOnDestroy(): void {
    if (this.roomSubscription) {
      this.roomSubscription.unsubscribe();
    }
    if (this.globalSubscription) {
      this.globalSubscription.unsubscribe();
    }
    this.chatService.disconnect();
  }

  loadChatRooms(): void {
    this.chatService.getShopChatRooms().subscribe(rooms => {
      this.chatRooms = rooms;
    });
  }

  subscribeToGlobalNotifications(): void {
    this.globalSubscription = this.chatService.subscribeToUserNotifications(this.currentUserId).subscribe(msg => {
      const room = this.chatRooms.find(r => r.id === msg.chatRoomId);
      if (room) {
        room.lastMessage = msg.content;
        room.lastMessageTime = msg.createdAt;
        if (this.selectedRoom?.id !== room.id) {
          room.unreadCount++;
        }
        
        // Move room to top
        this.chatRooms = [room, ...this.chatRooms.filter(r => r.id !== room.id)];
      } else {
        // If room not in list, reload rooms
        this.loadChatRooms();
      }
    });
  }

  selectRoom(room: ChatRoom): void {
    if (this.selectedRoom?.id === room.id) return;

    if (this.roomSubscription) {
      this.roomSubscription.unsubscribe();
    }

    this.selectedRoom = room;
    this.messages = [];
    this.loading = true;

    this.chatService.getChatHistory(room.id).subscribe(messages => {
      this.messages = messages;
      this.loading = false;
      this.scrollToBottom();

      // Mark as read
      this.chatService.markAsRead(room.id).subscribe();
      room.unreadCount = 0;
    });

    // Subscribe to real-time messages
    this.roomSubscription = this.chatService.subscribeToChatRoom(room.id).subscribe(msg => {
      if (msg && msg.chatRoomId === room.id) {
        if (!this.messages.find(m => m.id === msg.id)) {
          this.messages.push(msg);
          this.scrollToBottom();
        }
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedRoom) return;

    this.chatService.sendMessage(this.selectedRoom.id, this.newMessage.trim());
    this.newMessage = '';
  }

  onKeyPress(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  scrollToBottom(): void {
    setTimeout(() => {
      if (this.messageContainer) {
        const el = this.messageContainer.nativeElement;
        el.scrollTop = el.scrollHeight;
      }
    }, 100);
  }

  getTimeString(dateStr: string): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  }
}
