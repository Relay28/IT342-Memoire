import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { TextField, InputAdornment, IconButton, Alert } from '@mui/material';
import { Visibility, VisibilityOff, AdminPanelSettings } from "@mui/icons-material";
import { useAuth } from '../AuthProvider';
import mmrlogo from "../../assets/mmrlogo.png";
import adminBackground from "../../assets/bgmemoire.jpg";
const AdminLogin = () => {
    const [showPassword, setShowPassword] = useState(false);
    const [formData, setFormData] = useState({
      username: "",
      password: "",
    });
    const [fieldErrors, setFieldErrors] = useState({
      username: "",
      password: "",
    });
    
    
    const navigate = useNavigate();
    const { Adminlogin, loading, error, user, authToken } = useAuth();
    useEffect(() => {
      // Redirect if already logged in and has admin role
      if (user && authToken && user.role === "ROLE_ADMIN") {
        navigate("/admin/dashboard");
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
        newErrors.username = "Admin username is required";
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
        // Call the adminLogin function with the form data
        await Adminlogin(formData);
      } catch (err) {
        // Handle specific error messages from backend
        const errorMessage = err.message.toLowerCase();
        
        if (errorMessage.includes("username") || errorMessage.includes("not found")) {
          setFieldErrors(prev => ({ ...prev, username: "Admin username is incorrect" }));
        } else if (errorMessage.includes("password")) {
          setFieldErrors(prev => ({ ...prev, password: "Password is incorrect" }));
        } else if (errorMessage.includes("role") || errorMessage.includes("access")) {
          setFieldErrors(prev => ({ 
            ...prev, 
            username: "Account does not have admin privileges" 
          }));
        } else {
          // Fallback for other errors
          setFieldErrors({
            username: "",
            password: "",
          });
        }
      }
    };
  
    const togglePasswordVisibility = () => {
      setShowPassword(!showPassword);
    };
  
    return (
      <div className="flex w-screen h-screen">
        {/* Left Section */}
        <div className="w-1/2 h-screen relative bg-gray-900">
          <img 
            src={adminBackground} 
            alt="Admin Background" 
            className="w-full h-full object-cover opacity-30"
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
              
              <h2 className="text-3xl font-bold text-white mb-4">
                ADMIN PORTAL
              </h2>
              
              <p className="text-xl text-white/90">
                MANAGE AND MONITOR THE MÉMOIRE TIME CAPSULE PLATFORM
              </p>
            </div>
          </div>
        </div>
  
        {/* Right Section - Admin Login Form */}
        <div className="w-1/2 h-screen flex flex-col justify-center items-center bg-white">
          <div className="w-4/5 max-w-md">
            <div className="flex items-center justify-center mb-5">
              <AdminPanelSettings sx={{ color: "#b22222", fontSize: 36, marginRight: 1 }} />
              <h2 className="text-[28px] text-[#b22222] font-bold">ADMIN LOGIN</h2>
            </div>
            
            <Alert severity="info" className="mb-4">
              Secure admin access only. Unauthorized access attempts will be logged.
            </Alert>
            
            <form onSubmit={handleLogin} className="flex flex-col items-start w-full">
              <TextField
                id="username"
                label="Admin Username"
                placeholder="Enter your admin username"
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
                  to="/admin/reset-password" 
                  className="text-[#b22222] text-sm no-underline hover:underline"
                >
                  Reset Admin Credentials
                </Link>
              </div>
  
              <button 
                type="submit" 
                className="w-full py-2.5 bg-[#b22222] text-white border-none rounded cursor-pointer text-base"
                disabled={loading}
              >
                {loading ? "AUTHENTICATING..." : "ACCESS ADMIN PORTAL"}
              </button>
  
              {error && !fieldErrors.username && !fieldErrors.password && (
                <p className="text-red-500 text-sm mt-2 w-full text-center">
                  {error}
                </p>
              )}
            </form>
  
            <p className="mt-6 text-xs text-center">
              <Link to="/" className="text-gray-600 no-underline hover:underline">
                Return to User Login
              </Link>
            </p>
            
            <p className="mt-4 text-xs text-center text-gray-500">
              For admin access requests, please contact the system administrator.
            </p>
          </div>
        </div>
      </div>
    );
  };
  
  export default AdminLogin;