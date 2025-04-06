import React, { useState, lazy, Suspense } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";
import { Visibility, VisibilityOff } from "@mui/icons-material";
import { TextField, InputAdornment, IconButton } from '@mui/material';
import "../css/login.css";
import mmrlogo from "../assets/mmrlogo.png";
import sunsetGif from "../assets/sunset.gif";
import { useFCMToken } from "../hooks/useFCMToken"; 

const Login = () => {
 
  const [showPassword, setShowPassword] = useState(false);
const navigate = useNavigate();
 
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const [userId, setUserId] = useState(null);
  console.log(userId);
  useFCMToken(userId,sessionStorage.getItem("authToken"));

  const handleLogin = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setLoading(true);

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/login",
        formData,
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 200) {
        const { token, userId } = response.data; // Assume backend returns userId
        sessionStorage.setItem("authToken", token);
        setUserId(userId); // Trigger FCM token update
        navigate("/homepage");
      }
    } catch (error) {
      setErrorMessage(error.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };


  // Handle successful Google Login
  const handleGoogleLoginSuccess = async (response) => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/auth/verify-token?idToken=${response.credential}`,
        { method: "POST", credentials: "include" }
      );
      const data = await res.json();
      
      localStorage.setItem("token", data.token);
      setUserId(data.userId); // Trigger FCM token update
      navigate("/homepage");
    } catch (error) {
      alert("Google login failed");
    }
  };


  // Handle Google Login failure
  const handleGoogleLoginError = () => {
    console.error("Google Login Failed");
    alert("Google login failed. Please try again.");
  };

  // Toggle password visibility
  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="flex w-screen h-screen">
      {/* Left Section - GIF with text overlay */}
<div className="w-1/2 h-screen relative">
  {/* Sunset Background */}
  <img 
    src={sunsetGif} 
    alt="Sunset Animation" 
    className="w-full h-full object-cover"
    loading="lazy" 
  />
  
  {/* Text Overlay */}
  <div className="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-150 ">
  <div className="text-center backdrop-blur-md bg-white/10 rounded-lg p-8 border border-white/20 shadow-lg pb-15">
    {/* Logo - Added above the title */}
    <div className="flex justify-center mb-4">
      <img 
        src={mmrlogo} 
        alt="MMR Logo" 
        className="h-60 w-auto"  // Adjust size as needed
      />
    </div>
    
    {/* Main Title */}
    <h1 className="text-6xl font-bold text-white mb-4 tracking-wider">
      MÉMOIRE
    </h1>
    
    {/* Subtitle */}
    <p className="text-xl text-white/90">
    CREATE A DIGITAL TIME CAPSULE AND RELIVE MOMENTS WHEN THE TIME IS RIGHT.
    </p>
  </div>
</div>
</div>

      {/* Right Section - All content */}
      <div className="w-1/2 h-screen flex flex-col justify-center items-center bg-white">
        <div className="w-4/5 max-w-md">
         
          
        <h2 className="text-[28px] text-[#b22222] mb-5 text-center font-bold">WELCOME!</h2>
          <form className="flex flex-col items-start w-full ">
  <TextField
    id="username"
    label="Username"
    placeholder="Enter your username"
    variant="outlined"
    fullWidth
    margin="normal"
    sx={{
      '& .MuiOutlinedInput-root': {
        borderRadius: '8px',
      },
      mb: 2
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
    sx={{
      '& .MuiOutlinedInput-root': {
        borderRadius: '8px',
      },
      mb: 2
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

            <a href="#" className="text-xs text-gray-600 no-underline w-full text-right mb-5">
              Forgot Password?
            </a>

            <button 
              type="submit" 
              className="w-full py-2.5 bg-[#b22222] text-white border-none rounded cursor-pointer text-base"
            >
              LOGIN
            </button>
          </form>

          <p className="text-xs my-4 text-center">OR SIGN IN WITH</p>
          <div className="w-full">
            <GoogleOAuthProvider clientId="624781060268-t3uuq6d7rtfshkp43vpsb85bj7ohbmqp.apps.googleusercontent.com">
              <GoogleLogin
                onSuccess={handleGoogleLoginSuccess}
                onError={handleGoogleLoginError}
                className="w-full"
              />
            </GoogleOAuthProvider>
          </div>

          {/* Register Link */}
          <p className="mt-10 text-xs text-center">
            Don't have an account?{" "}
            <Link to="/register" className="text-[#b22222] no-underline font-bold hover:underline">
              Register here.
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;