import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { TextField, InputAdornment, IconButton } from '@mui/material';
import mmrlogo from "../assets/mmrlogo.png";
import sunsetGif from "../assets/sunset.gif";
import { Visibility, VisibilityOff } from "@mui/icons-material";

const Register = () => {
  const [showPassword, setShowPassword] = useState(false);
  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
  });

  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  const handleInputChange = (e) => {
    const { id, value } = e.target;
    setFormData((prev) => ({ ...prev, [id]: value }));
  };

  const validateForm = () => {
    const { username, email, password } = formData;
    if (!username || !email || !password) {
      setErrorMessage("All fields are required.");
      return false;
    }
    return true;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (!validateForm()) return;

    try {
      const response = await axios.post(
        "https://memoire-it342.as.r.appspot.com/api/auth/register",
        formData,
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 200) {
        setSuccessMessage("Registration successful! Redirecting to login...");
        setTimeout(() => navigate("/login"), 2000);
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
        "Registration failed. Please try again later."
      );
    }
  };

  return (
    <div className="flex w-screen h-screen">
      {/* Left Section - Same as login */}
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

      {/* Right Section - Register Form */}
      <div className="w-1/2 h-screen flex flex-col justify-center items-center bg-white">
        <div className="w-4/5 max-w-md">
          <h2 className="text-[28px] text-[#b22222] mb-5 text-center font-bold">REGISTER</h2>
          
          <form onSubmit={handleFormSubmit} className="flex flex-col items-start w-full">
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
              id="email"
              label="Email"
              type="email"
              placeholder="Enter your email"
              variant="outlined"
              fullWidth
              margin="normal"
              value={formData.email}
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

            <button 
              type="submit" 
              className="w-full py-2.5 bg-[#b22222] text-white border-none rounded cursor-pointer text-base"
            >
              SIGN UP
            </button>

            {errorMessage && (
              <p className="text-red-500 text-sm mt-2 w-full text-center">
                {errorMessage}
              </p>
            )}
            {successMessage && (
              <p className="text-green-500 text-sm mt-2 w-full text-center">
                {successMessage}
              </p>
            )}
          </form>

          <p className="mt-10 text-xs text-center">
            Already have an account?{" "}
            <Link to="/login" className="text-[#b22222] no-underline font-bold hover:underline">
              Login here.
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;