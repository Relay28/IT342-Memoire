import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";
import { AuthProvider } from './components/AuthProvider';
import { NotificationProvider } from "./context/NotificationContext";

function App() {
  
  return (
    <AuthProvider>
       <NotificationProvider>
    <GoogleOAuthProvider>
   
      <BrowserRouter>
        <AppRoutes />

      </BrowserRouter>
    
    </GoogleOAuthProvider>
    </NotificationProvider>
    </AuthProvider>
    
  );
}

export default App;
