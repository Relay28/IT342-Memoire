import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import AppRoutes from "./Routes";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import FCMNotificationHandler from "./components/Firebase/FCMNotifcationHandler";
import { AuthProvider } from './components/AuthProvider';
import { NotificationProvider } from "./context/NotificationContext";
import { ThemeProvider } from "./context/ThemeContext";
import { CapsuleContentProvider } from './context/CapsuleWebContextProvider';

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <NotificationProvider>
          <GoogleOAuthProvider>
          <CapsuleContentProvider>
            <BrowserRouter>
              <AppRoutes />
              <ToastContainer 
                position="bottom-right"
                theme="colored" 
                className="dark:!bg-gray-800 dark:!text-white"
              />
            </BrowserRouter>
            </CapsuleContentProvider>
          </GoogleOAuthProvider>
        </NotificationProvider>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
