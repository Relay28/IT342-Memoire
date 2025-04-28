// src/context/NotificationContext.js
import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import notificationService from '../services/notificationService';
import webSocketService from '../services/websocketService';
import { useAuth } from '../components/AuthProvider';

const NotificationContext = createContext();

export const useNotifications = () => useContext(NotificationContext);

export const NotificationProvider = ({ children }) => {
  const { user, authToken, isAuthenticated } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isConnected, setIsConnected] = useState(false);

  useEffect(() => {
    console.log('Notifications updated:', notifications);
  }, [notifications]);

  // Connect to WebSocket when user is authenticated
  useEffect(() => {
    if (isAuthenticated && user && authToken) {
      console.log("Setting up notifications for authenticated user:", user.username);
      
      // Set up connection state change handler
      const handleConnectionStateChange = (state) => {
        console.log(`WebSocket connection state changed: ${state}`);
        setIsConnected(state === 'connected');
      };
      
      // Register connection state handler
      const cleanup = webSocketService.onConnectionStateChange(
        handleConnectionStateChange, 
        "/ws-notifications"
      );
      
      // Register notification handlers first
      notificationService
        .onNewNotification(handleNewNotification)
        .onCountUpdate(handleCountUpdate);
      
      // Then connect
      notificationService
        .connect(user.username, authToken)
        .then(() => {
          console.log('Successfully connected to notification service');
          // No need to set isConnected here as it will be set by the connection state handler
          
          // Fetch initial data
          fetchNotifications();
          fetchUnreadCount();
        })
        .catch(err => {
          console.error('Failed to connect to notification service:', err);
          setIsConnected(false);
        });
      
      // Clean up on unmount
      return () => {
        notificationService.disconnect();
        cleanup(); // Remove connection state handler
        setIsConnected(false);
      };
    } else if (!isAuthenticated) {
      // User logged out, clear state
      setNotifications([]);
      setUnreadCount(0);
      setIsConnected(false);
    }
  }, [isAuthenticated, user, authToken]);

  const handleCountUpdate = (count) => {
    console.log('Unread count updated from server:', count);
    setUnreadCount(count);
  };
  
  const handleNewNotification = (notification) => {
    console.log('New notification received:', notification);
    setNotifications(prev => {
      // Check if notification already exists to avoid duplicates
      const exists = prev.some(n => n.id === notification.id);
      if (exists) {
        return prev;
      }
      const newNotifications = [notification, ...prev];
      console.log('Updated notifications:', newNotifications);
      return newNotifications;
    });
    
    // If the notification is unread, increment the unread count
    if (!notification.read) {
      setUnreadCount(prevCount => prevCount + 1);
    }
    
    // Show browser notification if page is not visible
    if (document.hidden) {
      showBrowserNotification(notification);
    }
  };
  
  const markAsRead = async (notificationId) => {
    try {
      const response = await axios.patch(
        `https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/notifications/${notificationId}/read`,
        {},
        {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update local state to mark as read and decrement unread count if it was previously unread
        setNotifications(prev => {
          const wasUnread = prev.find(n => n.id === notificationId && !n.read);
          if (wasUnread) {
            setUnreadCount(prevCount => Math.max(0, prevCount - 1));
          }
          return prev.map(n => n.id === notificationId ? { ...n, read: true } : n);
        });
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const response = await axios.patch(
        'https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/notifications/read-all',
        {},
        {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update local state to mark all as read
        setNotifications(prev => 
          prev.map(n => ({ ...n, read: true }))
        );
        // Reset unread count to zero
        setUnreadCount(0);
      }
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
    }
  };

  const fetchNotifications = async () => {
    if (!isAuthenticated || !authToken) return;
    
    try {
      const response = await axios.get('https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/notifications', {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      
      if (response.status === 200) {
        // Remove duplicates when setting notifications
        setNotifications(prev => {
          const existingIds = new Set(prev.map(n => n.id));
          const filteredNewNotifications = response.data.filter(n => !existingIds.has(n.id));
          return [...filteredNewNotifications, ...prev];
        });
      }
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };
  
  const fetchUnreadCount = async () => {
    if (!isAuthenticated || !authToken) return;
    
    try {
      const response = await axios.get('https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/notifications/unread-count', {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      
      if (response.status === 200) {
        setUnreadCount(response.data.count);
      }
    } catch (error) {
      console.error('Error fetching unread count:', error);
    }
  };

  const showBrowserNotification = (notification) => {
    // Check if browser notifications are supported
    if (!("Notification" in window)) {
      return;
    }
    
    // Check if permission granted
    if (Notification.permission === "granted") {
      const title = getNotificationTitle(notification.type);
      const browserNotification = new Notification(title, {
        body: notification.text,
        icon: '/images/notification-icon.png'
      });
      
      browserNotification.onclick = function() {
        window.focus();
        markAsRead(notification.id);
      };
    } 
    // If permission not granted, request it
    else if (Notification.permission !== "denied") {
      Notification.requestPermission();
    }
  };

  const getNotificationTitle = (type) => {
    switch (type) {
      case "FRIEND_REQUEST":
        return "New Friend Request";
      case "FRIEND_REQUEST_ACCEPTED":
        return "Friend Request Accepted";
      case "TIME_CAPSULE_OPEN":
        return "Time Capsule Opened";
      case "COMMENT":
        return "New Comment";
      default:
        return "New Notification";
    }
  };

  const value = {
    notifications,
    unreadCount,
    isConnected,
    markAsRead,
    markAllAsRead,
    fetchNotifications,
    fetchUnreadCount
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};