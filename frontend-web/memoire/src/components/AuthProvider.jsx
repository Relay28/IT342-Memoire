import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';

// Create the auth context
export const AuthContext = createContext();

// Custom hook for using the auth context
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

// Auth Provider component
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    // Load user data from localStorage on initial render
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  
  const [authToken, setAuthToken] = useState(() => {
    // Load token from sessionStorage on initial render
    return sessionStorage.getItem('authToken') || null;
  });
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Save user data to localStorage whenever it changes
  useEffect(() => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
  }, [user]);

  // Save auth token to sessionStorage whenever it changes
  useEffect(() => {
    if (authToken) {
      sessionStorage.setItem('authToken', authToken);
      // Configure axios default headers with the token
      axios.defaults.headers.common['Authorization'] = `Bearer ${authToken}`;
    } else {
      sessionStorage.removeItem('authToken');
      // Remove the authorization header when logged out
      delete axios.defaults.headers.common['Authorization'];
    }
  }, [authToken]);

  // Login function
  const login = async (credentials) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.post(
        "https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/auth/login",
        credentials,
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 200) {
        const { token, userId, ...userData } = response.data;
        
        setAuthToken(token);
        setUser({
          id: userId,
          username: userData.username,
          email: userData.email,
          name: userData.name || userData.username,
          biography: userData.biography || "",
          profilePicture: userData.profilePicture || ""
        });
        
        return { success: true };
      }
    } catch (error) {
      const message = error.response?.data?.message || "Login failed";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };


  // Login function
  const Adminlogin = async (credentials) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.post(
        "https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/auth/admin/login",
        credentials,
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 200) {
        const { token, userId, ...userData } = response.data;
        
        setAuthToken(token);
        setUser({
          id: userId,
          username: userData.username,
          email: userData.email,
          name: userData.name || userData.username,
          role: userData.role,
          biography: userData.biography || "",
          profilePicture: userData.profilePicture || ""
        });
        
        return { success: true };
      }
    } catch (error) {
      const message = error.response?.data?.message || "Login failed";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };


  // Google login function
  const googleLogin = async (credential) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await fetch(
        `https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/auth/verify-token?idToken=${credential}`,
        { method: "POST", credentials: "include" }
      );
      
      const data = await response.json();
      
      if (!response.ok) {
        throw new Error(data.message || "Google login failed");
      }
      
      setAuthToken(data.token);
      setUser({
        id: data.userId,
        username: data.username || data.email.split('@')[0],
        email: data.email,
        name: data.name || data.email.split('@')[0],
        biography: data.biography || "",
        profilePicture: data.picture || ""
      });
      
      return { success: true };
    } catch (error) {
      const message = error.message || "Google login failed";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  // Register function
  const register = async (userData) => {
    setLoading(true);
    setError(null);
    
    try {
      const response = await axios.post(
        "https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/auth/register",
        userData,
        { headers: { "Content-Type": "application/json" } }
      );

      if (response.status === 201) {
        return { success: true };
      }
    } catch (error) {
      const message = error.response?.data?.message || "Registration failed";
      setError(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  // Logout function
  const logout = () => {
    setUser(null);
    setAuthToken(null);
    // You could also clear other auth-related storage here
    localStorage.removeItem('user');
    sessionStorage.removeItem('authToken');
  };

  // Update user profile function - updated to match backend expectations
  const updateUserProfile = async (updatedData) => {
    if (!authToken) {
      setError("You must be logged in to update your profile");
      return { success: false, error: "Not authenticated" };
    }
    
    setLoading(true);
    setError(null);
    
    try {
      // Use PUT without user ID in the URL, as the backend gets the user from the authentication context
      const response = await axios.put(
        "https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/users",
        updatedData,
        {
          headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update local user state with the complete user object returned from the server
        setUser(response.data);
        return { success: true, user: response.data };
      }
    } catch (error) {
      const message = error.response?.data || "Profile update failed";
      setError(typeof message === 'string' ? message : JSON.stringify(message));
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  // Function to upload profile image (multipart form data)
  const uploadProfileImage = async (imageFile) => {
    if (!authToken || !user) {
      setError("You must be logged in to upload a profile image");
      return { success: false, error: "Not authenticated" };
    }
    
    setLoading(true);
    setError(null);
    
    try {
      const formData = new FormData();
      formData.append('profileImg', imageFile);
      
      const response = await axios.put(
        "https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/users/profile-picture",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
            "Authorization": `Bearer ${authToken}`
          }
        }
      );

      if (response.status === 200) {
        // Update the profile picture URL in the user state
        setUser(prev => ({
          ...prev,
          profilePicture: response.data.profilePicture || prev.profilePicture
        }));
        
        return { success: true, profilePicture: response.data.profilePicture };
      }
    } catch (error) {
      const message = error.response?.data || "Image upload failed";
      setError(typeof message === 'string' ? message : JSON.stringify(message));
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  // Check if user is authenticated
  const isAuthenticated = !!authToken && !!user;

  // Context value
  const value = {
    user,
    authToken,
    loading,
    error,
    isAuthenticated,
    Adminlogin,
    login,
    googleLogin,
    register,
    logout,
    updateUserProfile,
    uploadProfileImage,
    setUser, // Expose this in case you need direct access
    clearError: () => setError(null) // Utility to clear errors
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};