import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";
import { TextField, InputAdornment, IconButton } from '@mui/material';
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { useFCMToken } from "../hooks/useFCMToken";
import { useAuth } from '../components/AuthProvider';
import mmrlogo from "../assets/mmrlogo.png";
import sunsetGif from "../assets/sunset.gif";
import Snackbar from '@mui/material/Snackbar';
import Alert from '@mui/material/Alert';

const Login = () => {
  const [showPassword, setShowPassword] = useState(false);
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [fieldErrors, setFieldErrors] = useState({
    username: "",
    password: "",
  });
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: '',
    severity: 'success'
  });
  
  const navigate = useNavigate();
  const { login, googleLogin, loading, error, user, authToken } = useAuth();
  
  const showSnackbar = (message, severity = 'success') => {
    setSnackbar({ open: true, message, severity });
  };

  const handleCloseSnackbar = () => {
    setSnackbar(prev => ({ ...prev, open: false }));
  };

  useFCMToken(user?.id, authToken);
  
  useEffect(() => {
    if (user && authToken) {
      navigate("/homepage");
    }
  }, [user, authToken, navigate]);
  
  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
    // Clear error when user starts typing
    if (fieldErrors[id]) {
      setFieldErrors(prev => ({ ...prev, [id]: "" }));
    }
  };

  const validateForm = () => {
    let isValid = true;
    const newErrors = { username: "", password: "" };

    if (!formData.username.trim()) {
      newErrors.username = "Username is required";
      isValid = false;
    }

    if (!formData.password) {
      newErrors.password = "Password is required";
      isValid = false;
    }

    setFieldErrors(newErrors);
    return isValid;
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    try {
      await login(formData);
    } catch (err) {
      // Handle specific error messages from backend
      const errorMessage = err.message.toLowerCase();
      
      if (errorMessage.includes("username") || errorMessage.includes("user not found")) {
        setFieldErrors(prev => ({ ...prev, username: "Username is incorrect" }));
      } else if (errorMessage.includes("password") || errorMessage.includes("incorrect password")) {
        setFieldErrors(prev => ({ ...prev, password: "Password is incorrect" }));
      } else {
        // Fallback for other errors
        setFieldErrors({
          username: "",
          password: "",
        });
        // This will show the generic error message from useAuth
      }
    }
  };

  const handleGoogleLoginSuccess = async (response) => {
    try {
      await googleLogin(response.credential);
    } catch (error) {
      showSnackbar("Google login failed. Please try again.", "error");
    }
  };

  const handleGoogleLoginError = () => {
    console.error("Google Login Failed");
    showSnackbar("Google login failed. Please try again.", "error");
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="flex w-screen h-screen">
      {/* Left Section */}
      <div className="w-1/2 h-screen relative">
        <img 
          src={sunsetGif} 
          alt="Sunset Animation" 
          className="w-full h-full object-cover"
          loading="lazy" 
        />
        
        <div className="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-150">
          <div className="text-center">
            <div className="flex justify-center mb-4">
              <img src={mmrlogo} alt="MMR Logo" className="h-60 w-auto" />
            </div>
            
            <h1 className="text-6xl font-bold text-white mb-4 tracking-wider">
              MÉMOIRE
            </h1>
            
            <p className="text-xl text-white/90">
              CREATE A DIGITAL TIME CAPSULE AND RELIVE MOMENTS WHEN THE TIME IS RIGHT.
            </p>
          </div>
        </div>
      </div>

      {/* Right Section - Login Form */}
      <div className="w-1/2 h-screen flex flex-col justify-center items-center bg-white">
        <div className="w-4/5 max-w-md">
          <h2 className="text-[28px] text-[#b22222] mb-5 text-center font-bold">LOGIN</h2>
          
          <form onSubmit={handleLogin} className="flex flex-col items-start w-full">
            <TextField
              id="username"
              label="Username"
              placeholder="Enter your username"
              variant="outlined"
              fullWidth
              margin="normal"
              value={formData.username}
              onChange={handleInputChange}
              error={!!fieldErrors.username}
              helperText={fieldErrors.username}
              sx={{
                '& .MuiOutlinedInput-root': { borderRadius: '8px' },
                mb: fieldErrors.username ? 0 : 2
              }}
            />

            <TextField
              id="password"
              label="Password"
              type={showPassword ? "text" : "password"}
              placeholder="Enter your password"
              variant="outlined"
              fullWidth
              margin="normal"
              value={formData.password}
              onChange={handleInputChange}
              error={!!fieldErrors.password}
              helperText={fieldErrors.password}
              sx={{
                '& .MuiOutlinedInput-root': { borderRadius: '8px' },
                mb: fieldErrors.password ? 0 : 2
              }}
              InputProps={{
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      aria-label="toggle password visibility"
                      onClick={togglePasswordVisibility}
                      edge="end"
                    >
                      {showPassword ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
            />

            <div className="w-full flex justify-end mb-4">
              <Link 
                to="/forgot-password" 
                className="text-[#b22222] text-sm no-underline hover:underline"
              >
                Forgot Password?
              </Link>
            </div>

            <button 
              type="submit" 
              className="w-full py-2.5 bg-[#b22222] text-white border-none rounded cursor-pointer text-base"
              disabled={loading}
            >
              {loading ? "LOGGING IN..." : "LOGIN"}
            </button>

            {error && !fieldErrors.username && !fieldErrors.password && (
              <p className="text-red-500 text-sm mt-2 w-full text-center">
                {error}
              </p>
            )}

            <div className="w-full text-center my-4">
              <p className="text-xs text-gray-500">OR SIGN IN WITH</p>
            </div>

            <div className="w-full flex justify-center mb-4">
              <GoogleOAuthProvider clientId="624781060268-t3uuq6d7rtfshkp43vpsb85bj7ohbmqp.apps.googleusercontent.com">
                <GoogleLogin
                  onSuccess={handleGoogleLoginSuccess}
                  onError={handleGoogleLoginError}
                />
              </GoogleOAuthProvider>
            </div>
          </form>

          <p className="mt-4 text-xs text-center">
            Don't have an account?{" "}
            <Link to="/register" className="text-[#b22222] no-underline font-bold hover:underline">
              Register here.
            </Link>
          </p>
        </div>
      </div>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={2000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </div>
  );
};

export default Login;