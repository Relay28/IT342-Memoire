// src/services/timeCapsuleService.js
import axios from 'axios';

const API_URL = 'https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/timecapsules';

class TimeCapsuleService {
  /**
   * Creates a new time capsule
   * @param {Object} timeCapsuleData - The time capsule data to create
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the created time capsule
   */
  async createTimeCapsule(timeCapsuleData, authToken) {
    try {
      const response = await axios.post(API_URL, timeCapsuleData, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error creating time capsule', error);
      throw error;
    }
  }

  /**
   * Retrieves a specific time capsule by ID
   * @param {number} id - The ID of the time capsule
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the time capsule data
   */
  async getTimeCapsule(id, authToken) {
    try {
      const response = await axios.get(`${API_URL}/${id}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching time capsule with ID ${id}`, error);
      throw error;
    }
  }

  /**
   * Gets all time capsules for the authenticated user
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the user's time capsules
   */
  async getUserTimeCapsules(authToken) {
    try {
      const response = await axios.get(`${API_URL}/user`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching user time capsules', error);
      throw error;
    }
  }

  /**
   * Gets all UNPUBLISHED time capsules
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing unpublished time capsules
   */
  async getUnpublishedTimeCapsules(authToken) {
    try {
      const response = await axios.get(`${API_URL}/status/unpublished`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching unpublished time capsules', error);
      throw error;
    }
  }

  /**
   * Gets all CLOSED time capsules
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing closed time capsules
   */
  async getClosedTimeCapsules(authToken) {
    try {
      const response = await axios.get(`${API_URL}/status/closed`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching closed time capsules', error);
      throw error;
    }
  }

  /**
   * Gets all PUBLISHED time capsules
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing published time capsules
   */
  async getPublishedTimeCapsules(authToken) {
    try {
      const response = await axios.get(`${API_URL}/status/published`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching published time capsules', error);
      throw error;
    }
  }

  // Add this to your TimeCapsuleService
async publishTimeCapsule(id, authToken) {
  try {
    return await axios.patch(`${API_URL}/${id}/publish`, {}, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
  } catch (error) {
    this.handleError(`Error publishing time capsule with ID ${id}`, error);
    throw error;
  }
}

  /**
   * Gets all ARCHIVED time capsules
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing archived time capsules
   */
  async getArchivedTimeCapsules(authToken) {
    try {
      const response = await axios.get(`${API_URL}/status/archived`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching archived time capsules', error);
      throw error;
    }
  }

  /**
   * Gets all time capsules with pagination
   * @param {number} page - The page number (0-based)
   * @param {number} size - The page size
   * @param {string} sortBy - The field to sort by
   * @param {string} sortDirection - The sort direction (ASC or DESC)
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing paginated time capsules
   */
  async getAllTimeCapsules(page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'DESC', authToken) {
    try {
      const response = await axios.get(API_URL, {
        params: {
          page,
          size,
          sortBy,
          sortDirection
        },
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching all time capsules', error);
      throw error;
    }
  }

  /**
   * Updates an existing time capsule
   * @param {number} id - The ID of the time capsule to update
   * @param {Object} timeCapsuleData - The updated time capsule data
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the updated time capsule
   */
  async updateTimeCapsule(id, timeCapsuleData, authToken) {
    try {
      const response = await axios.put(`${API_URL}/${id}`, timeCapsuleData, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error updating time capsule with ID ${id}`, error);
      throw error;
    }
  }

  /**
   * Deletes a time capsule
   * @param {number} id - The ID of the time capsule to delete
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when deletion is complete
   */
  async deleteTimeCapsule(id, authToken) {
    try {
      return await axios.delete(`${API_URL}/${id}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
    } catch (error) {
      this.handleError(`Error deleting time capsule with ID ${id}`, error);
      throw error;
    }
  }

  /**
   * Locks a time capsule with a specified open date
   * @param {number} id - The ID of the time capsule to lock
   * @param {string} openDate - The date when the time capsule should be unlocked (ISO format)
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when locking is complete
   */
  async lockTimeCapsule(id, openDate, authToken) {
    try {
      const lockRequest = { openDate };
      return await axios.patch(`${API_URL}/${id}/lock`, lockRequest, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
    } catch (error) {
      this.handleError(`Error locking time capsule with ID ${id}`, error);
      throw error;
    }
  }

  // In TimeCapsuleService.js
async archiveTimeCapsule(id, authToken) {
  try {
    return await axios.patch(`${API_URL}/${id}/archive`, {}, {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    });
  } catch (error) {
    this.handleError(`Error archiving time capsule with ID ${id}`, error);
    throw error;
  }
}
  /**
   * Unlocks a time capsule
   * @param {number} id - The ID of the time capsule to unlock
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when unlocking is complete
   */
  async unlockTimeCapsule(id, authToken) {
    try {
      return await axios.patch(`${API_URL}/${id}/unlock`, {}, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
    } catch (error) {
      this.handleError(`Error unlocking time capsule with ID ${id}`, error);
      throw error;
    }
  }
  /**
   * Standardized error handling for API requests
   * @param {string} message - The error message
   * @param {Error} error - The error object
   */
  handleError(message, error) {
    console.error(`${message}:`, error);
    
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error('Response data:', error.response.data);
      console.error('Response status:', error.response.status);
    } else if (error.request) {
      // The request was made but no response was received
      console.error('No response received:', error.request);
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('Error setting up request:', error.message);
    }
  }
}



// Create and export a singleton instance
const timeCapsuleService = new TimeCapsuleService();
export default timeCapsuleService;