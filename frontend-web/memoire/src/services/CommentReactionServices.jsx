// src/services/CommentReactionService.js
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/comment-reactions';

class CommentReactionService {
  /**
   * Adds a reaction to a comment
   * @param {number} commentId - The ID of the comment
   * @param {string} type - The reaction type
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the created reaction
   */
  async addReaction(commentId, type, authToken) {
    try {
      const response = await axios.post(`${API_URL}/comment/${commentId}`, { type }, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error adding reaction', error);
      throw error;
    }
  }

  /**
   * Updates an existing reaction
   * @param {number} reactionId - The ID of the reaction to update
   * @param {string} type - The new reaction type
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the updated reaction
   */
  async updateReaction(reactionId, type, authToken) {
    try {
      const response = await axios.put(`${API_URL}/${reactionId}`, { type }, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error updating reaction with ID ${reactionId}`, error);
      throw error;
    }
  }

  /**
   * Deletes a reaction
   * @param {number} reactionId - The ID of the reaction to delete
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when deletion is complete
   */
  async deleteReaction(reactionId, authToken) {
    try {
      return await axios.delete(`${API_URL}/${reactionId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
    } catch (error) {
      this.handleError(`Error deleting reaction with ID ${reactionId}`, error);
      throw error;
    }
  }

  /**
   * Gets a specific reaction by ID
   * @param {number} reactionId - The ID of the reaction
   * @returns {Promise} - Promise containing the reaction data
   */
  async getReactionById(reactionId) {
    try {
      const response = await axios.get(`${API_URL}/${reactionId}`);
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching reaction with ID ${reactionId}`, error);
      throw error;
    }
  }

  /**
   * Gets all reactions for a specific comment
   * @param {number} commentId - The ID of the comment
   * @returns {Promise} - Promise containing the comment's reactions
   */
  async getReactionsByCommentId(commentId) {
    try {
      const response = await axios.get(`${API_URL}/getReaction/comment/${commentId}`);
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching reactions for comment ID ${commentId}`, error);
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
      
      // You can add custom error handling based on status codes
      if (error.response.status === 403) {
        console.error('Access denied - check permissions');
      } else if (error.response.status === 404) {
        console.error('Resource not found');
      }
    } else if (error.request) {
      console.error('No response received:', error.request);
    } else {
      console.error('Error setting up request:', error.message);
    }
  }
}

// Create and export a singleton instance
const commentReactionService = new CommentReactionService();
export default commentReactionService;