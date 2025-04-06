import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";

function App() {
  const clientId = "500063994752-5graisegq8sp2t5mfkai2lm9a48k0kb8.apps.googleusercontent.com"; // Your Google Client ID

  return (
   
    <GoogleOAuthProvider clientId={clientId}>
      <BrowserRouter>
        <AppRoutes />

      </BrowserRouter>
    </GoogleOAuthProvider>

    
  );
}

export default App;
