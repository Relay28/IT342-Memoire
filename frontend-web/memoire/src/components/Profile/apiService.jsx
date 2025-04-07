// services/apiService.js
import axios from 'axios';
const token =sessionStorage.getItem('authToken') 
console.log("TOEKN RECEIVEVD" +token)
const apiService = axios.create({
 
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}` 
  },
});

// Add request interceptor to include auth token
apiService.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;  // <-- FIXED
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});


export default apiService;