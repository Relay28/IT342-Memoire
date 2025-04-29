import React, { useEffect } from "react";
import { toast } from "react-toastify";
import { requestForToken, onMessageListener } from '../Firebase/FirebaseIntializer'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const FCMNotificationHandler = () => {
  useEffect(() => {
    // 1. Request permission & get FCM token
    const fetchToken = async () => {
      const token = await requestForToken();
      if (token) {
        console.log("FCM Token:", token);
        // Send token to your Spring Boot backend
        await fetch(`${API_BASE_URL}/api/fcm/update-token`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ userId: "6", fcmToken: token }),
        });
      }
    };

    fetchToken();

    // 2. Listen for incoming foreground notifications
    const setupForegroundListener = async () => {
      const payload = await onMessageListener();
      if (payload) {
        toast.info(payload.notification?.body || "New notification!", {
          position: "top-right",
          autoClose: 5000,
        });
      }
    };

    setupForegroundListener();
  }, []);

  return null; // This component doesn't render anything
};

export default FCMNotificationHandler;