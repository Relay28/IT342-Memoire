import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";
import { PersonalInfoProvider } from './components/PersonalInfoContext'; 
function App() {
  
  return (
    <PersonalInfoProvider>
    <GoogleOAuthProvider>
      <BrowserRouter>
        <AppRoutes />

      </BrowserRouter>
    </GoogleOAuthProvider>
    </PersonalInfoProvider>
    
  );
}

export default App;
