import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const BASE_URL = `${API_BASE_URL}/api/users`;

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
        BASE_URL,
        getAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch user:', error);
      throw error;
    }
  },

  async getPublicProfile(userId) {
    try {
      const response = await axios.get(
        '${API_BASE_URL}/api/profiles/view/${userId}',
        getAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Failed to fetch user:', error);
      throw error;
    }
  },

  // Update user details (with or without profile image)
  async updateProfile(userData) {
      try {
        const response = await axios.put(
          BASE_URL,
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
        `${BASE_URL}/profile-picture`,
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

// Add this to your profileService object
async changePassword(currentPassword, newPassword) {
  try {
    const response = await axios.post(
      `${BASE_URL}/change-password`,
      {
        currentPassword,
        newPassword
      },
      getAuthHeaders()
    );
    return response.data;
  } catch (error) {
    console.error('Failed to change password:', error);
    
    // Enhanced error handling
    let errorMessage = 'Password change failed';
    if (error.response) {
      errorMessage = error.response.data?.message || 
                    (error.response.status === 401 ? 'Current password is incorrect' : 
                    error.response.status === 400 ? 'New password does not meet requirements' : 
                    'Server error');
    }
    throw new Error(errorMessage);
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
        `${BASE_URL}/disable`,
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
 // Get Profile Pic with safe fallback
async getProfilePicture() {
  try {
    const response = await axios.get(
      `${BASE_URL}/profile-picture?t=${Date.now()}`,
      getImageConfig()
    );

    if (!response || !response.data || !response.headers['content-type']) {
      console.warn('Empty profile picture response');
      return null; // Or a fallback image URL if you prefer
    }

    const base64 = btoa(
      new Uint8Array(response.data).reduce(
        (data, byte) => data + String.fromCharCode(byte),
        ''
      )
    );

    return `data:${response.headers['content-type']};base64,${base64}`;
  } catch (error) {
    console.warn('No profile picture found or failed to fetch:', error?.response?.status || error.message);
    return null; // Ensure null is returned on error to avoid further issues
  }
}


};