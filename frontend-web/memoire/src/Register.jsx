import React from "react";
import "./css/register.css";
import mmrlogo from './assets/mmrlogo.png';
import googleLogo from './assets/google.png';
import { Link } from "react-router-dom";

const Register = () => {
  return (
    <div className="register-container">
      {/* Left Section */}
      <div className="register-info">
        <div className="logo-wrapper">
          <img src={mmrlogo} alt="Mémoire Logo" className="logo-img" />
          <div className="title">MÉMOIRE</div>
        </div>
        <p className="desc">
          CREATE A DIGITAL TIME CAPSULE AND RELIVE MOMENTS WHEN THE TIME IS RIGHT.
        </p>
      </div>

      {/* Right Section */}
      <div className="register-form">
        <h2>REGISTER</h2>
        <form>
            <label>Username</label>
            <input type="text" placeholder="Enter your username" />

          <label>Email</label>
          <input type="email" placeholder="Enter your email" />

          <label>Password</label>
          <input type="password" placeholder="Enter your password" />
          
          <button type="submit" className="register-button">SIGN UP</button>
        </form>

        <p className="or">OR SIGN UP WITH</p>
        <div className="social-register">
          <button className="social-btn">
            <img src={googleLogo} alt="Google" className="social-logo" />
            <span className="google-text">Sign up with Google</span>
          </button>
        </div>

        {/* login Link */}
        <p className="login-text">
          Have an account? <Link to="/login" className="login-link">Login here.</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
