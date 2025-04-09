import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";
import { AuthProvider } from './components/AuthProvider';
function App() {
  
  return (
    <AuthProvider>
    <GoogleOAuthProvider>
      <BrowserRouter>
        <AppRoutes />

      </BrowserRouter>
    </GoogleOAuthProvider>
    </AuthProvider>
    
  );
}

export default App;
