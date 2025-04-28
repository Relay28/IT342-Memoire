import axios from 'axios';

const token = sessionStorage.getItem('authToken');
console.log("TOKEN RECEIVED: " + token);

const apiService = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://20250428t092311-dot-memoire-it342.as.r.appspot.com',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}` 
  },
});

// Add request interceptor to include auth token
apiService.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

// Friend Request Methods
apiService.friendships = {
  // Existing methods
  create: (data) => apiService.post('/api/friendships/create', data),
  areFriends: (friendId) => apiService.get(`/api/friendships/areFriends/${friendId}`),
  accept: (id) => apiService.put(`/api/friendships/${id}/accept`),
  delete: (id) => apiService.delete(`/api/friendships/${id}`),
  
  // New methods
  hasPendingRequest: (friendId) => apiService.get(`/api/friendships/hasPendingRequest/${friendId}`),
  cancelRequest: (friendId) => apiService.delete(`/api/friendships/cancel/${friendId}`)
};

export default apiService;