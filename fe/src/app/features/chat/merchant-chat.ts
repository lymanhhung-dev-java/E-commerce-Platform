import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatRoom, ChatMessage } from '../../core/services/chat.service';
import { UserService } from '../../core/services/user.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-merchant-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
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
    private cdr: ChangeDetectorRef,
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

    this.chatService.getChatHistory(room.id).subscribe({
      next: (messages) => {
        this.messages = messages.map(m => {
          if (m.messageType === 'ORDER_INFO') {
            try { m.parsedOrderInfo = JSON.parse(m.content); } catch (e) {}
          } else if (m.messageType === 'PRODUCT_INFO') {
            try { m.parsedProductInfo = JSON.parse(m.content); } catch (e) {}
          }
          return m;
        });
        this.loading = false;
        this.scrollToBottom();

        // Mark as read
        this.chatService.markAsRead(room.id).subscribe();
        room.unreadCount = 0;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading chat history:', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });

    // Subscribe to real-time messages
    this.roomSubscription = this.chatService.subscribeToChatRoom(room.id).subscribe(msg => {
      if (msg && msg.chatRoomId === room.id) {
        if (!this.messages.find(m => m.id === msg.id)) {
          if (msg.messageType === 'ORDER_INFO') {
            try { msg.parsedOrderInfo = JSON.parse(msg.content); } catch (e) {}
          } else if (msg.messageType === 'PRODUCT_INFO') {
            try { msg.parsedProductInfo = JSON.parse(msg.content); } catch (e) {}
          }
          this.messages.push(msg);
          this.scrollToBottom();
          this.cdr.detectChanges();
        }
      }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.selectedRoom) return;

    this.chatService.sendMessage(this.selectedRoom.id, this.newMessage.trim());
    this.newMessage = '';
  }

  onFileSelected(event: any): void {
    if (!this.selectedRoom) return;
    const file = event.target.files[0];
    if (file) {
      if (!file.type.startsWith('image/')) {
        alert('Chỉ chấp nhận file hình ảnh');
        return;
      }
      this.chatService.uploadImageMessage(this.selectedRoom.id, file).subscribe({
        next: () => {
          event.target.value = ''; // Reset input
        },
        error: (err) => {
          console.error(err);
          alert('Upload ảnh thất bại: ' + (err.error?.message || 'Có lỗi xảy ra'));
          event.target.value = '';
        }
      });
    }
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

  formatLastMessage(content: string | undefined): string {
    if (!content) return 'Bấm để xem tin nhắn';
    try {
      if (content.startsWith('{')) {
        const obj = JSON.parse(content);
        if (obj && obj.productId) return '[Thông tin sản phẩm]';
        if (obj && obj.orderId) return '[Thông tin đơn hàng]';
      }
    } catch (e) {
      // Fallback
    }
    
    if (content.startsWith('http://') || content.startsWith('https://')) {
      return '[Hình ảnh]';
    }
    
    return content;
  }
}
