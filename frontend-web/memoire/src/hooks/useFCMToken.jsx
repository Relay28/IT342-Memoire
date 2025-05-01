import { requestForToken } from "../components/Firebase/FirebaseIntializer";
import { useEffect } from "react";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

export const useFCMToken = (userId, jwtToken) => {
    useEffect(() => {
        console.log("TESTTT"+userId+jwtToken)
      if (!userId || !jwtToken) return;
  
      const updateToken = async () => {
        try {
          const fcmToken = await requestForToken();
          console.log("FIREBASE TOKEN:", fcmToken);
          
          if (fcmToken) {
            const params = new URLSearchParams();
            params.append("userId", Number(userId).toString()); // Ensures userId is sent as a numeric string
            params.append("fcmToken", String(fcmToken));          // Ensures fcmToken is sent as a string

            const response = await fetch(`${API_BASE_URL}/api/fcm/update-token?${params.toString()}`, {
                method: "POST",
                headers: { 
                "Authorization": `Bearer ${jwtToken}`  // JWT token header remains as before
                }
            });
  
            if (!response.ok) {
              throw new Error(`HTTP error! status: ${response.status}`);
            }
  
            console.log("FCM token updated for user:", userId);
          }
        } catch (error) {
          console.error("Failed to update FCM token:", error);
        }
      };
  
      updateToken();
    }, [userId, jwtToken]);  // Added jwtToken to dependencies
  };