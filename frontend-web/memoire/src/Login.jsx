import React, { useState, useContext } from "react";
import "./css/login.css";
import mmrlogo from "./assets/mmrlogo.png";
import { Link, useNavigate } from "react-router-dom";
import { GoogleLogin, GoogleOAuthProvider } from "@react-oauth/google";
import axios from "axios";
import { useFCMToken } from "./hooks/useFCMToken"; // Import the hook

const Login = () => {
  // const [credentials, setCredentials] = useState({ username: '', password: '' });
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
      <form onSubmit={handleLogin}>
        <div>
          <label htmlFor="username">Username</label>
          <input
            id="username"
            type="text"
            placeholder="Enter your username"
            value={formData.username}
            onChange={handleInputChange}
          />
        </div>

        <div>
          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={formData.password}
            onChange={handleInputChange}
          />
        </div>

        {errorMessage && <p style={{ color: "red" }}>{errorMessage}</p>}

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
