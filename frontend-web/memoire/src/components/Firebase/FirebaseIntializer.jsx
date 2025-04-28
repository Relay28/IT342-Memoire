import { initializeApp } from "firebase/app";
import { getMessaging, getToken, onMessage } from "firebase/messaging";

import { getAnalytics } from "firebase/analytics";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
// firebase-config.js
const firebaseConfig = {
  apiKey: process.env.NEXT_PUBLIC_FIREBASE_API_KEY,
  authDomain: process.env.NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN,
  projectId: process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID,
  storageBucket: process.env.NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: process.env.NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID,
  appId: process.env.NEXT_PUBLIC_FIREBASE_APP_ID
};
// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const messaging = getMessaging(app, {
  serviceWorkerRegistration: {
    // Explicitly register the service worker
    register: async () => {
      try {
        return await navigator.serviceWorker.register('/firebase-messaging-sw.js', {
          scope: '/firebase-cloud-messaging-push-scope'
        });
      } catch (error) {
        console.error('Service worker registration failed:', error);
        throw error;
      }
    }
  }
});

export const requestForToken = async () => {
    try {
      const permission = await Notification.requestPermission();
      console.log("DEBUG PERMISSION "+permission)
      if (permission === "granted") {
        const token = await getToken(messaging, { 
          vapidKey: "BNrdIAqKVpidROiLzEoeMTJfZL4Y_uJ2Ne39sCtKRkdahjigtPKqpmVQ4WzoxJS9hxf4RT20Tt8VGL-f_xvochA" // Get from Firebase Console > Cloud Messaging
        });
        console.log("FCM Token:", token);
        return token;
      }
    } catch (error) {
      console.error("Failed to get FCM token:", error);
    }
  };
  
  // Listen for incoming messages (foreground notifications)
  export const onMessageListener = () =>
    new Promise((resolve) => {
      onMessage(messaging, (payload) => {
        console.log("Foreground notification received:", payload);
        resolve(payload);
      });
    });
  
  export { messaging };