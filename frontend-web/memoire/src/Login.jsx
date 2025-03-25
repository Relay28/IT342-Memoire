import React from "react";
import "./css/login.css";
import mmrlogo from "./assets/mmrlogo.png";
import { Link, useNavigate } from "react-router-dom";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";

const Login = () => {
  const navigate = useNavigate();

  // Handle successful Google Login
  const handleGoogleLoginSuccess = async (response) => {
    console.log("Google Login Successful!");
  
    const idToken = response.credential; // Extract idToken from the Google response
  
    try {
      // Send the ID token as a query parameter
      const res = await fetch(`http://localhost:8080/api/auth/verify-token?idToken=${idToken}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include", // Ensure cookies are sent if needed
      });
  
      if (!res.ok) {
        throw new Error("Failed to verify token");
      }
  
      const data = await res.json();
  
      // Handle successful login
      console.log("Backend Response:", data);
      localStorage.setItem("token", data.token); // Save JWT token to localStorage
      alert("Login successful!");
      navigate("/homepage"); // Navigate to the homepage
    } catch (error) {
      console.error("Error verifying token:", error);
      alert("Google login failed. Please try again.");
    }
  };
  

  // Handle Google Login failure
  const handleGoogleLoginError = () => {
    console.error("Google Login Failed");
    alert("Google login failed. Please try again.");
  };

  return (
    <div className="login-container">
      {/* Left Section */}
      <div className="login-info">
        <div className="logo-wrapper">
          <img src={mmrlogo} alt="Mémoire Logo" className="logo-img" />
          <div className="title">MÉMOIRE</div>
        </div>
        <p className="desc">
          CREATE A DIGITAL TIME CAPSULE AND RELIVE MOMENTS WHEN THE TIME IS RIGHT.
        </p>
      </div>

      {/* Right Section */}
      <div className="login-form">
        <h2>WELCOME!</h2>
        <form>
          <label htmlFor="username">Username</label>
          <input id="username" type="text" placeholder="Enter your username" />

          <label htmlFor="password">Password</label>
          <input id="password" type="password" placeholder="Enter your password" />

          <a href="#" className="forgot-password">
            Forgot Password?
          </a>

          <button type="submit" className="login-button">
            LOGIN
          </button>
        </form>

        <p className="or">OR SIGN IN WITH</p>
        <div className="social-login">
          <GoogleOAuthProvider clientId="624781060268-t3uuq6d7rtfshkp43vpsb85bj7ohbmqp.apps.googleusercontent.com">
            <GoogleLogin
              onSuccess={handleGoogleLoginSuccess}
              onError={handleGoogleLoginError}
            />
          </GoogleOAuthProvider>
        </div>

        {/* Register Link */}
        <p className="register-text">
          Don't have an account?{" "}
          <Link to="/register" className="register-link">
            Register here.
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
