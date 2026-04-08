import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, BehaviorSubject, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Client, Message } from '@stomp/stompjs';

export interface ChatMessage {
  id?: number;
  chatRoomId: number;
  senderId: number;
  senderName: string;
  senderAvatar: string;
  content: string;
  createdAt: string;
  isRead: boolean;
  senderType: 'USER' | 'SHOP';
  messageType?: 'TEXT' | 'ORDER_INFO' | 'PRODUCT_INFO' | 'IMAGE';
  parsedOrderInfo?: any;
  parsedProductInfo?: any;
}

export interface ChatRoom {
  id: number;
  shopId: number;
  shopName: string;
  shopLogo: string;
  userId: number;
  userName: string;
  userAvatar: string;
  lastMessage?: string;
  lastMessageTime?: string;
  unreadCount: number;
}

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = `${environment.apiUrl}/chat`;
  private stompClient: Client | null = null;
  private messageSubject = new Subject<ChatMessage>();
  private connected$ = new BehaviorSubject<boolean>(false);

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // ==================== REST API ====================

  getChatRooms(): Observable<ChatRoom[]> {
    return this.http.get<ChatRoom[]>(`${this.apiUrl}/rooms`);
  }

  getShopChatRooms(): Observable<ChatRoom[]> {
    return this.http.get<ChatRoom[]>(`${this.apiUrl}/rooms/shop`);
  }

  getChatHistory(roomId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/rooms/${roomId}/messages`);
  }

  createOrGetRoom(shopId: number): Observable<ChatRoom> {
    return this.http.post<ChatRoom>(`${this.apiUrl}/rooms`, { shopId });
  }

  markAsRead(roomId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/rooms/${roomId}/read`, {});
  }

  uploadImageMessage(chatRoomId: number, file: File): Observable<ChatMessage> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ChatMessage>(`${this.apiUrl}/rooms/${chatRoomId}/image`, formData);
  }

  // ==================== WebSocket STOMP ====================

  connect(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const token = localStorage.getItem('access_token');
    if (!token) return;

    // Use current hostname to support non-localhost access
    const host = window.location.hostname;
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    
    // In dev, if using localhost:4200, backend is usually on :8080
    const port = host === 'localhost' ? '8080' : window.location.port;

    this.stompClient = new Client({
      brokerURL: `${protocol}//${host}:${port}/ws`,
      connectHeaders: {
        Authorization: `Bearer ${token}`
      },
      debug: (str) => console.log('STOMP: ' + str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = (frame) => {
      console.log('Connected to WebSocket');
      this.connected$.next(true);
    };

    this.stompClient.onDisconnect = () => {
      console.log('Disconnected from WebSocket');
      this.connected$.next(false);
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
      this.connected$.next(false);
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.connected$.next(false);
    }
  }

  subscribeToChatRoom(roomId: number): Observable<ChatMessage> {
    const subject = new Subject<ChatMessage>();
    
    const trySub = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.subscribe(`/topic/chat/${roomId}`, (message: Message) => {
          const chatMsg: ChatMessage = JSON.parse(message.body);
          subject.next(chatMsg);
          this.messageSubject.next(chatMsg);
        });
        return true;
      }
      return false;
    };

    if (!trySub()) {
      const checkConnect = setInterval(() => {
        if (trySub()) clearInterval(checkConnect);
      }, 100);
      setTimeout(() => clearInterval(checkConnect), 10000);
    }

    return subject.asObservable();
  }

  subscribeToUserNotifications(userId: number): Observable<ChatMessage> {
    const subject = new Subject<ChatMessage>();
    
    const trySub = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.subscribe(`/topic/user/${userId}`, (message: Message) => {
          const chatMsg: ChatMessage = JSON.parse(message.body);
          subject.next(chatMsg);
        });
        return true;
      }
      return false;
    };

    if (!trySub()) {
      const checkConnect = setInterval(() => {
        if (trySub()) clearInterval(checkConnect);
      }, 100);
      setTimeout(() => clearInterval(checkConnect), 10000);
    }

    return subject.asObservable();
  }

  sendMessage(chatRoomId: number, content: string): void {
    const trySend = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.publish({
          destination: '/app/chat.send',
          body: JSON.stringify({ chatRoomId, content, messageType: 'TEXT' })
        });
        return true;
      }
      return false;
    };

    if (!trySend()) {
      const checkConnect = setInterval(() => {
        if (trySend()) clearInterval(checkConnect);
      }, 100);
      setTimeout(() => {
        clearInterval(checkConnect);
        if (!this.stompClient || !this.stompClient.connected) {
          console.warn('STOMP client not connected, message not sent');
        }
      }, 5000);
    }
  }

  sendOrderInfo(chatRoomId: number, orderId: number): void {
    const trySend = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.publish({
          destination: '/app/chat.sendOrder',
          body: JSON.stringify({ chatRoomId, orderId })
        });
        return true;
      }
      return false;
    };

    if (!trySend()) {
      const checkConnect = setInterval(() => {
        if (trySend()) clearInterval(checkConnect);
      }, 100);
      setTimeout(() => {
        clearInterval(checkConnect);
        if (!this.stompClient || !this.stompClient.connected) {
          console.warn('STOMP client not connected, order info not sent');
        }
      }, 5000);
    }
  }

  sendProductInfo(chatRoomId: number, productId: number): void {
    const trySend = () => {
      if (this.stompClient && this.stompClient.connected) {
        this.stompClient.publish({
          destination: '/app/chat.sendProduct',
          body: JSON.stringify({ chatRoomId, productId })
        });
        return true;
      }
      return false;
    };

    if (!trySend()) {
      const checkConnect = setInterval(() => {
        if (trySend()) clearInterval(checkConnect);
      }, 100);
      setTimeout(() => {
        clearInterval(checkConnect);
        if (!this.stompClient || !this.stompClient.connected) {
          console.warn('STOMP client not connected, product info not sent');
        }
      }, 5000);
    }
  }

  getGlobalMessages(): Observable<ChatMessage> {
    return this.messageSubject.asObservable();
  }
}
