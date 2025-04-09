import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../components/AuthProvider';
class NotificationService {
  constructor() {
    this.stompClient = null;
    this.connected = false;
    this.username = null;
    this.notificationHandlers = [];
    this.countHandlers = [];
  }

  connect(username, token) {
    this.username = username;
    
    const socket = new SockJS('http://localhost:8080/ws-notifications');
    this.stompClient = Stomp.over(socket);
    
    // Configure STOMP client
    const headers = {
      'Authorization': `Bearer ${token}`
    };
    
    this.stompClient.connect(headers, this.onConnected.bind(this), this.onError.bind(this));
    return this; // For chaining
  }
  
  onConnected() {
    this.connected = true;
    console.log('Connected to notification websocket');
    
    // Subscribe to personal notification channel
    this.stompClient.subscribe(`/topic/notifications/${this.username}`, this.onNotificationReceived.bind(this));
    
    // Subscribe to notification count updates
    this.stompClient.subscribe(`/topic/notifications/count/${this.username}`, this.onCountReceived.bind(this));
    
    // Subscribe to user-specific channel for connection confirmation
    this.stompClient.subscribe(`/user/topic/notifications/connect`, this.onConnectionConfirmed.bind(this));
    
    // Send connection message to get initial data
    this.stompClient.send("/app/notifications/connect", {}, JSON.stringify({}));
  }
  
  onConnectionConfirmed(response) {
    const data = JSON.parse(response.body);
    console.log('Initial notification data:', data);
    
    // Update notification count
    this.countHandlers.forEach(handler => handler(data.count));
    
    // Process initial notifications
    if (data.notifications && data.notifications.length > 0) {
      data.notifications.forEach(notification => {
        this.notificationHandlers.forEach(handler => handler(notification));
      });
    }
  }
  
  onNotificationReceived(payload) {
    const notification = JSON.parse(payload.body);
    console.log('New notification received:', notification);
    
    // Notify all handlers
    this.notificationHandlers.forEach(handler => handler(notification));
    
    // Play sound
    this.playNotificationSound();
  }
  
  onCountReceived(payload) {
    const data = JSON.parse(payload.body);
    console.log('Notification count update:', data);
    
    // Notify count handlers
    this.countHandlers.forEach(handler => handler(data.count));
  }
  
  playNotificationSound() {
    // Play notification sound if available
    try {
      const sound = new Audio('/sounds/notification.mp3');
      sound.play();
    } catch (e) {
      console.log('Could not play notification sound', e);
    }
  }
  
  onError(error) {
    console.error('WebSocket connection error:', error);
    this.connected = false;
    
    // Try to reconnect after 5 seconds
    setTimeout(() => {
      if (this.username) {
        this.connect(this.username, sessionStorage.getItem('authToken'));
      }
    }, 5000);
  }
  
  disconnect() {
    if (this.stompClient !== null) {
      this.stompClient.disconnect();
      this.connected = false;
      console.log('Disconnected from notification service');
    }
  }
  
  // Add handler for new notifications
  onNewNotification(handler) {
    this.notificationHandlers.push(handler);
    return this; // For chaining
  }
  
  // Add handler for count updates
  onCountUpdate(handler) {
    this.countHandlers.push(handler);
    return this; // For chaining
  }
  
  // Remove notification handler
  removeNotificationHandler(handler) {
    const index = this.notificationHandlers.indexOf(handler);
    if (index !== -1) {
      this.notificationHandlers.splice(index, 1);
    }
    return this; // For chaining
  }
  
  // Remove count handler
  removeCountHandler(handler) {
    const index = this.countHandlers.indexOf(handler);
    if (index !== -1) {
      this.countHandlers.splice(index, 1);
    }
    return this; // For chaining
  }
}

// Create a singleton instance
const notificationServiceInstance = new NotificationService();
export default notificationServiceInstance;

