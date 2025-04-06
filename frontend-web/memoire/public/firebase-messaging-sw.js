// /public/firebase-messaging-sw.js
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.0.0/firebase-messaging-compat.js');

const firebaseConfig = {
  apiKey: "AIzaSyBRpmpQ8PRFPd5mAlKgaVbcvLoAqgP7PlY",
  authDomain: "memoire-b5584.firebaseapp.com",
  projectId: "memoire-b5584",
  storageBucket: "memoire-b5584.appspot.com",
  messagingSenderId: "117689149240",
  appId: "1:117689149240:web:0002e222f8e8c1563c43cd",
  measurementId: "G-CR7SQ95VBY"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

// Background message handler
messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  // Customize notification here
  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body,
    icon: '/logo192.png'
  };
  self.registration.showNotification(notificationTitle, notificationOptions);
});