// src/services/friendshipService.jsx
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const API_URL = `${API_BASE_URL}/api/friendships`;

class FriendshipService {
  /**
   * Creates a new friendship request
   * @param {number} friendId - The ID of the user to send friend request to
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the created friendship
   */
  async createFriendship(friendId, authToken) {
    try {
      const response = await axios.post(
        `${API_URL}/create`,
        { friendId },
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
            'Content-Type': 'application/json',
          },
        }
      );
      return response.data;
    } catch (error) {
      this.handleError('Error creating friendship request', error);
      throw error;
    }
  }

  /**
   * Checks if two users are friends
   * @param {number} friendId - The ID of the other user
   * @param {string} authToken - The authentication token
   * @returns {Promise<boolean>} - Promise containing true if friends, false otherwise
   */
  async areFriends(friendId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/areFriends/${friendId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error checking friendship status with user ${friendId}`, error);
      throw error;
    }
  }

  /**
   * Gets the authenticated user's friends list
   * @param {string} authToken - The authentication token
   * @returns {Promise<Array>} - Promise containing the list of friends
   */
  async getFriendsList(authToken) {
    try {
      const response = await axios.get(`${API_URL}/friends`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching friends list', error);
      throw error;
    }
  }

  /**
   * Gets a specific friendship by ID
   * @param {number} friendshipId - The ID of the friendship
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the friendship data
   */
  async getFriendshipById(friendshipId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/${friendshipId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching friendship with ID ${friendshipId}`, error);
      throw error;
    }
  }

  /**
   * Accepts a friend request
   * @param {number} friendshipId - The ID of the friendship to accept
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the updated friendship
   */
  async acceptFriendship(friendshipId, authToken) {
    try {
      const response = await axios.put(
        `${API_URL}/${friendshipId}/accept`,
        {},
        {
          headers: {
            Authorization: `Bearer ${authToken}`,
          },
        }
      );
      return response.data;
    } catch (error) {
      this.handleError(`Error accepting friendship with ID ${friendshipId}`, error);
      throw error;
    }
  }

  /**
   * Deletes a friendship or cancels a request
   * @param {number} friendshipId - The ID of the friendship to delete
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when deletion is complete
   */
  async deleteFriendship(friendshipId, authToken) {
    try {
      return await axios.delete(`${API_URL}/${friendshipId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
    } catch (error) {
      this.handleError(`Error deleting friendship with ID ${friendshipId}`, error);
      throw error;
    }
  }

  /**
   * Checks if there's a pending friend request between users
   * @param {number} friendId - The ID of the other user
   * @param {string} authToken - The authentication token
   * @returns {Promise<boolean>} - Promise containing true if pending request exists
   */
  async hasPendingRequest(friendId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/hasPendingRequest/${friendId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error checking pending request status with user ${friendId}`, error);
      throw error;
    }
  }

  /**
   * Cancels a friend request
   * @param {number} friendId - The ID of the user to cancel request with
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when cancellation is complete
   */
  async cancelRequest(friendId, authToken) {
    try {
      return await axios.delete(`${API_URL}/cancel/${friendId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
    } catch (error) {
      this.handleError(`Error canceling friend request to user ${friendId}`, error);
      throw error;
    }
  }

  /**
   * Checks if the authenticated user is the receiver of a friend request
   * @param {number} friendId - The ID of the other user
   * @param {string} authToken - The authentication token
   * @returns {Promise<boolean>} - Promise containing true if receiver
   */
  async isReceiver(friendId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/isReceiver/${friendId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error checking receiver status with user ${friendId}`, error);
      throw error;
    }
  }

  /**
   * Gets received friend requests
   * @param {string} authToken - The authentication token
   * @returns {Promise<Array>} - Promise containing list of received requests
   */
  async getReceivedFriendRequests(authToken) {
    try {
      const response = await axios.get(`${API_URL}/requests/received`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError('Error fetching received friend requests', error);
      throw error;
    }
  }

  /**
   * Finds friendship between two users
   * @param {number} friendId - The ID of the other user
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the friendship data if exists
   */
  async findByUsers(friendId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/findByUsers/${friendId}`, {
        headers: {
          Authorization: `Bearer ${authToken}`,
        },
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error finding friendship with user ${friendId}`, error);
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
      console.error('Response data:', error.response.data);
      console.error('Response status:', error.response.status);
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Error setting up request:', error.message);
    }
  }
}

// Create and export a singleton instance
const friendshipService = new FriendshipService();
export default friendshipService;