import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";

function App() {
  
  return (
    <GoogleOAuthProvider>
      <BrowserRouter>
        <AppRoutes />

      </BrowserRouter>
    </GoogleOAuthProvider>

    
  );
}

export default App;
