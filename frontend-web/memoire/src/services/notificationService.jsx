// NotificationService.js
import webSocketService from './websocketService';

class NotificationService {
  constructor() {
    this.username = null;
    this.notificationHandlers = [];
    this.countHandlers = [];
    this.connectionSubscription = null;
    this.notificationSubscription = null;
    this.countSubscription = null;
    this.endpoint = "/ws-notifications";
  }

  connect(username, token) {
    this.username = username;
    console.log(`Connecting notification service for ${username} to ${this.endpoint}`);
    
    return webSocketService.connect(token, this.endpoint)
      .then(() => {
        // Subscribe to personal notification channel
        this.notificationSubscription = webSocketService.subscribe(
          `/topic/notifications/${this.username}`, 
          this.onNotificationReceived.bind(this),
          {},
          this.endpoint
        );
        
        // Subscribe to notification count updates
        this.countSubscription = webSocketService.subscribe(
          `/topic/notifications/count/${this.username}`, 
          this.onCountReceived.bind(this),
          {},
          this.endpoint
        );
        
        // Subscribe to user-specific channel for connection confirmation
        this.connectionSubscription = webSocketService.subscribe(
          `/user/${this.username}/topic/notifications/connect`, 
          this.onConnectionConfirmed.bind(this),
          {},
          this.endpoint
        );
        
        // Send connection message to get initial data
        return webSocketService.send("/app/notifications/connect", {}, {}, this.endpoint);
      })
      .catch(error => {
        console.error("Failed to connect notification service:", error);
        throw error;
      });
  }
  
  onConnectionConfirmed(data) {
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
  
  onNotificationReceived(notification) {
    console.log('New notification received:', notification);
    
    // Notify all handlers
    this.notificationHandlers.forEach(handler => handler(notification));
    
    // Play sound
    this.playNotificationSound();
  }
  
  onCountReceived(data) {
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
  
  disconnect() {
    if (this.notificationSubscription) {
      webSocketService.unsubscribe(this.notificationSubscription);
      this.notificationSubscription = null;
    }
    
    if (this.countSubscription) {
      webSocketService.unsubscribe(this.countSubscription);
      this.countSubscription = null;
    }
    
    if (this.connectionSubscription) {
      webSocketService.unsubscribe(this.connectionSubscription);
      this.connectionSubscription = null;
    }
    
    // We don't disconnect from the WebSocketService here since other services might be using it
    // The WebSocketService will handle disconnection when reference count reaches 0
    webSocketService.disconnect(this.endpoint);
    console.log('Disconnected from notification service');
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
const notificationService = new NotificationService();
export default notificationService;