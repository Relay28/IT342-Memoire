import React from "react";
import "./css/login.css";
import mmrlogo from './assets/mmrlogo.png';
import googleLogo from './assets/google.png';

const Login = () => {
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
          <label>Email</label>
          <input type="email" placeholder="Enter your email" />

          <label>Password</label>
          <input type="password" placeholder="Enter your password" />

          <a href="#" className="forgot-password">Forgot Password?</a>

          <button type="submit" className="login-button">LOGIN</button>
        </form>

        <p className="or">OR SIGN IN WITH</p>
        <div className="social-login">
          <button className="social-btn">
            <img src={googleLogo} alt="Google" className="social-logo" />
            <span className="google-text">Sign in with Google</span>
          </button>
        </div>

        {/* Register Link */}
        <p className="register-text">
          Don't have an account? <a href="#" className="register-link">Register here.</a>
        </p>
      </div>
    </div>
  );
};

export default Login;
