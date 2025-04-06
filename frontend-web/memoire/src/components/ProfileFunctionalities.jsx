import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/users';

// Helper function to get auth headers
const getAuthHeaders = (contentType = 'application/json') => {
  const token = sessionStorage.getItem('authToken');
  return {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': contentType
    }
  };
};

export const profileService = {
  // Get current user
  async getCurrentUser() {
    try {
      const response = await axios.get(
        API_BASE_URL,
        getAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch user:', error);
      throw error;
    }
  },

  // Update user details (with or without profile image)
  async updateProfile(userData, profileImage = null) {
      try {
        const response = await axios.put(
          API_BASE_URL,
          userData,
          {
            headers: {
              'Authorization': `Bearer ${sessionStorage.getItem('authToken')}`,
              'Content-Type': 'application/json'
            }
          }
        );
        return response.data;
      } catch (error) {
        console.error('Failed to update user details:', error);
        throw error;
      }
    },
  
  // Update profile picture only
  async uploadProfilePicture(imageFile) {
    try {
      const formData = new FormData();
      formData.append('profileImg', imageFile);

      const response = await axios.put(
        `${API_BASE_URL}/profile-picture`,
        formData,
        getAuthHeaders()
      );
      
      return response.data;
    } catch (error) {
      console.error('Profile picture upload failed:', error);
      throw error;
    }
  },


  // Disable/deactivate account
  async deactivateAccount() {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/disable`,
        {},
        getAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Account deactivation failed:', error);
      throw error;
    }
  },

  // Update user details without changing profile picture
  async updateUserDetails(userData) {
    return this.updateProfile(userData); // Reuse the main update method
  }
};