import React, { useState } from "react";
import "./css/register.css";
import mmrlogo from "./assets/mmrlogo.png";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";

const Register = () => {
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
    // Add further validation if needed (e.g., email format, password strength)
    return true;
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage(""); // Reset error message before submission
    setSuccessMessage(""); // Reset success message

    if (!validateForm()) {
      return;
    }

    try {
      const response = await axios.post(
        "http://localhost:8080/api/auth/register",
        formData,
        {
          headers: { "Content-Type": "application/json" },
        }
      );

      if (response.status === 200) {
        setSuccessMessage("Registration successful! Redirecting to login...");
        setTimeout(() => navigate("/login"), 2000); // Navigate after 2 seconds
      }
    } catch (error) {
      setErrorMessage(
        error.response?.data?.message ||
          "Registration failed. Please try again later."
      );
    }
  };

  return (
    <div className="register-container">
      {/* Left Section */}
      <div className="register-info">
        <div className="logo-wrapper">
          <img src={mmrlogo} alt="Mémoire Logo" className="logo-img" />
          <div className="title">MÉMOIRE</div>
        </div>
        <p className="desc">
          CREATE A DIGITAL TIME CAPSULE AND RELIVE MOMENTS WHEN THE TIME IS
          RIGHT.
        </p>
      </div>

      {/* Right Section */}
      <div className="register-form">
        <h2>REGISTER</h2>
        <form onSubmit={handleFormSubmit}>
          <label htmlFor="username">Username</label>
          <input
            id="username"
            type="text"
            placeholder="Enter your username"
            value={formData.username}
            onChange={handleInputChange}
            aria-required="true"
            aria-label="Enter your username"
          />

          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            placeholder="Enter your email"
            value={formData.email}
            onChange={handleInputChange}
            aria-required="true"
            aria-label="Enter your email"
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={formData.password}
            onChange={handleInputChange}
            aria-required="true"
            aria-label="Enter your password"
          />

          <button type="submit" className="register-button">
            SIGN UP
          </button>

          {errorMessage && <p className="error-message">{errorMessage}</p>}
          {successMessage && <p className="success-message">{successMessage}</p>}
        </form>

        {/* Login Link */}
        <p className="login-text">
          Have an account?{" "}
          <Link to="/login" className="login-link">
            Login here.
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
