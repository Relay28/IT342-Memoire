import React, { useState, useContext } from "react";
import { Link, useNavigate } from "react-router-dom";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";
import axios from "axios";
import { useFCMToken } from "../hooks/useFCMToken";
import { TextField, InputAdornment, IconButton } from '@mui/material';
import mmrlogo from "../assets/mmrlogo.png";
import sunsetGif from "../assets/sunset.gif";
import { Visibility, VisibilityOff } from "@mui/icons-material";

const Login = () => {
  const [showPassword, setShowPassword] = useState(false);
  
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [errorMessage, setErrorMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [userId, setUserId] = useState(null);

  useFCMToken(userId, sessionStorage.getItem("authToken"));

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

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
        const { token, userId } = response.data;
        sessionStorage.setItem("authToken", token);
        setUserId(userId);
        navigate("/homepage");
      }
    } catch (error) {
      setErrorMessage(error.response?.data?.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleLoginSuccess = async (response) => {
    try {
      const res = await fetch(
        `http://localhost:8080/api/auth/verify-token?idToken=${response.credential}`,
        { method: "POST", credentials: "include" }
      );
      const data = await res.json();
      
      localStorage.setItem("token", data.token);
      setUserId(data.userId);
      navigate("/homepage");
    } catch (error) {
      alert("Google login failed");
    }
  };

  const handleGoogleLoginError = () => {
    console.error("Google Login Failed");
    alert("Google login failed. Please try again.");
  };

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };
  return (
    <div className="flex w-screen h-screen">
      {/* Left Section - Same as register */}
      <div className="w-1/2 h-screen relative">
        <img 
          src={sunsetGif} 
          alt="Sunset Animation" 
          className="w-full h-full object-cover"
          loading="lazy" 
        />
        
        <div className="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-150">
          <div className="text-center backdrop-blur-md bg-white/10 rounded-lg p-8 border border-white/20 shadow-lg pb-15">
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
              sx={{
                '& .MuiOutlinedInput-root': { borderRadius: '8px' },
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
              value={formData.password}
              onChange={handleInputChange}
              sx={{
                '& .MuiOutlinedInput-root': { borderRadius: '8px' },
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

            <div className="w-full flex justify-end mb-4">
              <Link 
                to="#" 
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

            {errorMessage && (
              <p className="text-red-500 text-sm mt-2 w-full text-center">
                {errorMessage}
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
    </div>
  );
};

export default Login;