// src/context/NotificationContext.js
import React, { createContext, useContext, useState, useEffect, useMemo  } from 'react';
import axios from 'axios';
import notificationService from '../services/notificationService';
import { useAuth } from '../components/AuthProvider';
const NotificationContext = createContext();

export const useNotifications = () => useContext(NotificationContext);

export const NotificationProvider = ({ children }) => {
  const { user, authToken, isAuthenticated } = useAuth(); // Use your auth context
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isConnected, setIsConnected] = useState(false);

  
// In NotificationContext.js
useEffect(() => {
    console.log('Notifications updated:', notifications);
  }, [notifications]);
  
  // In Header.jsx
  useEffect(() => {
    console.log('Header sees notifications:', notifications);
    console.log('Header sees unreadCount:', unreadCount);
  }, [notifications, unreadCount]);
  // Connect to WebSocket when user is authenticated
  useEffect(() => {
    if (isAuthenticated && user && authToken && !isConnected) {
      connectToNotifications(user.username, authToken);
      fetchNotifications();
      fetchUnreadCount();
    }
    
    // Disconnect when user logs out
    if (!isAuthenticated && isConnected) {
      notificationService.disconnect();
      setIsConnected(false);
      setNotifications([]);
      setUnreadCount(0);
    }

    // if (!isConnected) {
    //     connectToNotifications(user.username, authToken);
    //   }
    
    // Clean up on unmount
    return () => {
      if (isConnected) {
        notificationService.disconnect();
      }
    };
  }, [isAuthenticated, user, authToken, isConnected]);

  const connectToNotifications = (username, token) => {
    notificationService
      .onNewNotification(handleNewNotification)
      .onCountUpdate(handleCountUpdate)
      .connect(username, token);
    
    setIsConnected(true);
  };

  const handleCountUpdate = (count) => {
    console.log('Unread count updated from server:', count);
    setUnreadCount(count);
  };
  const handleNewNotification = (notification) => {
    console.log('New notification received:', notification);
    setNotifications(prev => {
      const newNotifications = [notification, ...prev];
      console.log('Updated notifications:', newNotifications);
      return newNotifications;
    });
    
    // Also update unread count for new notifications
    
    if (document.hidden) {
      showBrowserNotification(notification);
    }
  };
  const markAsRead = async (notificationId) => {
    try {
      const response = await axios.patch(
        `http://localhost:8080/api/notifications/${notificationId}/read`,
        {},
        {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update local state to mark as read (though WebSocket will handle this too)
        setNotifications(prev => 
            prev.map(n => ({ ...n, read: true }))
          );
          fetchUnreadCount();
      }
    } catch (error) {
      console.error('Error marking notification as read:', error);
    }
  };

  const markAllAsRead = async () => {
    try {
      const response = await axios.patch(
        'http://localhost:8080/api/notifications/read-all',
        {},
        {
          headers: {
            'Authorization': `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update local state to mark all as read (though WebSocket will handle this too)
     
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
      const response = await axios.get('http://localhost:8080/api/notifications', {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      
      if (response.status === 200) {
        // Remove duplicates when setting notifications
        setNotifications(prev => {
          const newNotifications = response.data.filter(newNotif => 
            !prev.some(existingNotif => existingNotif.id === newNotif.id)
          );
          return [...newNotifications, ...prev];
        });
      }
    } catch (error) {
      console.error('Error fetching notifications:', error);
    }
  };
  const fetchUnreadCount = async () => {
    if (!isAuthenticated || !authToken) return;
    
    try {
      const response = await axios.get('http://localhost:8080/api/notifications/unread-count', {
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