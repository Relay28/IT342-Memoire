import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/user';

// Helper function to get auth headers
const getAuthHeaders = () => {
  const token = sessionStorage.getItem('authToken');
  return {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'multipart/form-data'
    }
  };
};

const getJsonAuthHeaders = () => {
  const token = sessionStorage.getItem('authToken');
  return {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };
};

export const profileService = {
  async updateProfile(userData, profileImage = null) {
    try {
      const token = sessionStorage.getItem('authToken');
      const config = {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': profileImage ? undefined : 'application/json' // Let axios set multipart content-type
        }
      };

      let response;
      
      if (profileImage) {
        // Case 1: With image (multipart form)
        const formData = new FormData();
        formData.append('profileImg', profileImage);
        formData.append('newUserDetails', new Blob([JSON.stringify(userData)], {
          type: 'application/json'
        }));
        
        response = await axios.put(
          API_BASE_URL,
          formData,
          config
        );
      } else {
        // Case 2: Without image (pure JSON)
        response = await axios.put(
          API_BASE_URL,
          userData,
          config
        );
      }
      
      return response.data;
    } catch (error) {
      console.error('Profile update failed:', error);
      if (error.response) {
        console.error('Server response:', error.response.data);
      }
      throw error;
    }
  },
  // Disable/deactivate account
  async deactivateAccount() {
    try {
      const response = await axios.patch(
        `${API_BASE_URL}/disable`,
        {},
        getJsonAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Account deactivation failed:', error);
      throw error;
    }
  },

  // Upload profile picture only
  async uploadProfilePicture(imageFile) {
    try {
      const formData = new FormData();
      formData.append('profileImg', imageFile);

      // Include empty user data as required by your backend
      const emptyUserData = {};
      const userDataBlob = new Blob([JSON.stringify(emptyUserData)], {
        type: 'application/json'
      });
      formData.append('newUserDetails', userDataBlob);

      const response = await axios.put(
        API_BASE_URL,
        formData,
        getAuthHeaders()
      );
      return response.data;
    } catch (error) {
      console.error('Profile picture upload failed:', error);
      throw error;
    }
  },

  // Update user details without changing profile picture
  async updateUserDetails(userData) {
    try {
      // 1. Create FormData
      const formData = new FormData();
      
      // 2. Prepare the EXACT structure backend expects
      const backendUserData = {
        email: userData.email,
        name: userData.name,
        biography: userData.biography,
        // Include ALL fields your backend processes
      };
  
      // 3. Create Blob with proper JSON formatting
      const userDataBlob = new Blob(
        [JSON.stringify(backendUserData)], 
        { type: 'application/json' }
      );
  
      // 4. Append with EXACT field name backend expects
      formData.append('newUserDetails', userDataBlob);
  
      // 5. Get auth headers (without Content-Type)
      const token = sessionStorage.getItem('authToken');
      const config = {
        headers: {
          'Authorization': `Bearer ${token}`
          // Let browser set Content-Type automatically
        }
      };
  
      // 6. Debug the request payload
      console.log('Sending update:', backendUserData);
  
      const response = await axios.put(
        API_BASE_URL,
        formData,
        config
      );
  
      // 7. Verify response
      console.log('Update response:', response.data);
      return response.data;
      
    } catch (error) {
      console.error('Update failed:', {
        message: error.message,
        response: error.response?.data
      });
      throw error;
    }
  }
};