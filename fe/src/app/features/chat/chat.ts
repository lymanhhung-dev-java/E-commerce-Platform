import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Inject, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService, ChatRoom, ChatMessage } from '../../core/services/chat.service';
import { UserService } from '../../core/services/user.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './chat.html',
  styleUrls: ['./chat.css']
})
export class ChatComponent implements OnInit, OnDestroy {
  @ViewChild('messageContainer') messageContainer!: ElementRef;

  chatRooms: ChatRoom[] = [];
  selectedRoom: ChatRoom | null = null;
  messages: ChatMessage[] = [];
  newMessage = '';
  currentUserId: number = 0;
  loading = false;
  private roomSubscription: any = null;
  private globalSubscription: any = null;
  private pendingAction: { roomId: number, productId: number } | null = null;

  constructor(
    private chatService: ChatService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.userService.getMyProfile().subscribe((profile: any) => {
        this.currentUserId = profile.id;
        this.subscribeToGlobalNotifications();
      });

      this.route.queryParams.subscribe(params => {
        const roomId = params['roomId'];
        const productId = params['productId'];
        if (roomId && productId) {
          this.pendingAction = { roomId: Number(roomId), productId: Number(productId) };
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
    this.chatService.getChatRooms().subscribe(rooms => {
      this.chatRooms = rooms;

      if (this.pendingAction) {
        const pendingRoomId = this.pendingAction.roomId;
        const room = this.chatRooms.find(r => r.id === pendingRoomId);
        if (room) {
          this.selectRoom(room);
        }
      }
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
    
    // Unsubscribe from previous room
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

        // Send pending message if navigating from product detail
        if (this.pendingAction && this.pendingAction.roomId === room.id) {
          const productId = this.pendingAction.productId;
          this.pendingAction = null;
          this.router.navigate([], { queryParams: {} });
          setTimeout(() => {
            this.chatService.sendProductInfo(room.id, productId);
          }, 300); // Small buffer to ensure STOMP is subscribed
        }
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
        // Double check for duplicates
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
          // Message will be received via STOMP broadcase, no need to manually push
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

  getDateString(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN');
  }

  formatLastMessage(content: string | undefined): string {
    if (!content) return 'Bắt đầu trò chuyện...';
    try {
      if (content.startsWith('{')) {
        const obj = JSON.parse(content);
        if (obj && obj.productId) return '[Thông tin sản phẩm]';
        if (obj && obj.orderId) return '[Thông tin đơn hàng]';
      }
    } catch (e) {
      // Not JSON or parse error
    }
    
    if (content.startsWith('http://') || content.startsWith('https://')) {
      return '[Hình ảnh]';
    }
    
    return content;
  }
}
