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


// Helper function for image responses
const getImageConfig = () => {
  const token = sessionStorage.getItem('authToken');
  return {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Cache-Control': 'no-store, max-age=0' // Add this line
    },
    responseType: 'arraybuffer'
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
      formData.append("profileImg", imageFile);
  
      const response = await axios.put(
        `${API_BASE_URL}/profile-picture`,
        formData,
        {
          headers: {
            'Authorization': `Bearer ${sessionStorage.getItem('authToken')}`,
            'Content-Type': 'multipart/form-data'
          }
        }
      );
      return response.data;
    } catch (error) {
      console.error('Failed to update profile picture:', error);
      throw error;
    }
  },


  // Disable/deactivate account
  async deactivateAccount() {
    try {
      const token = sessionStorage.getItem('authToken');
      if (!token) {
        throw new Error('No authentication token found');
      }

      const response = await axios.patch(
        `${API_BASE_URL}/disable`,
        {}, // Empty body
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        }
      );
      console.log(response)

      if (response.data) {
        return response.data;
      }
      throw new Error(response.data?.message || 'Account deactivation failed');
    } catch (error) {
      console.error('Deactivation error:', {
        message: error.message,
        response: error.response?.data,
        status: error.response?.status
      });
      
      // Enhanced error message
      let errorMessage = 'Account deactivation failed';
      if (error.response) {
        errorMessage = error.response.data?.message || 
                      (error.response.status === 401 ? 'Unauthorized - Please login again' : 
                      error.response.status === 404 ? 'User not found' : 
                      'Server error');
      }
      throw new Error(errorMessage);
    }
  },

  //Get Profile Pic
  async getProfilePicture() {
    try {
      const response = await axios.get(
        `${API_BASE_URL}/profile-picture?t=${Date.now()}`, // Add timestamp
        getImageConfig()
      );
      
      // Convert arraybuffer to base64 for easy use in img src
      const base64 = btoa(
        new Uint8Array(response.data).reduce(
          (data, byte) => data + String.fromCharCode(byte),
          ''
        )
      );
      return `data:${response.headers['content-type']};base64,${base64}`;
    } catch (error) {
      console.error('Failed to fetch profile picture:', error);
      throw error;
    }
  },

  // Update user details without changing profile picture
  async updateUserDetails(userData) {
    return this.updateProfile(userData); // Reuse the main update method
  }
};