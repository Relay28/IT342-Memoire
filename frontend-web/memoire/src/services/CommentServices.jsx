// src/services/CommentServices.js
import axios from 'axios';

const API_URL = 'https://20250428t092311-dot-memoire-it342.as.r.appspot.com/api/comments';

class CommentServices {
  /**
   * Creates a new comment for a time capsule
   * @param {number} capsuleId - The ID of the time capsule
   * @param {string} text - The comment text
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the created comment
   */
  async createComment(capsuleId, text, authToken) {
    try {
      const response = await axios.post(`${API_URL}/capsule/${capsuleId}`, { text }, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError('Error creating comment', error);
      throw error;
    }
  }

  /**
   * Updates an existing comment
   * @param {number} commentId - The ID of the comment to update
   * @param {string} text - The updated comment text
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the updated comment
   */
  async updateComment(commentId, text, authToken) {
    try {
      const response = await axios.put(`${API_URL}/${commentId}`, { text }, {
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error updating comment with ID ${commentId}`, error);
      throw error;
    }
  }

  /**
   * Deletes a comment
   * @param {number} commentId - The ID of the comment to delete
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise resolving when deletion is complete
   */
  async deleteComment(commentId, authToken) {
    try {
      return await axios.delete(`${API_URL}/${commentId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
    } catch (error) {
      this.handleError(`Error deleting comment with ID ${commentId}`, error);
      throw error;
    }
  }

  /**
   * Retrieves a specific comment by ID
   * @param {number} commentId - The ID of the comment
   * @param {string} authToken - The authentication token
   * @returns {Promise} - Promise containing the comment data
   */
  async getCommentById(commentId, authToken) {
    try {
      const response = await axios.get(`${API_URL}/${commentId}`, {
        headers: {
          'Authorization': `Bearer ${authToken}`
        }
      });
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching comment with ID ${commentId}`, error);
      throw error;
    }
  }

  /**
   * Gets all comments for a specific time capsule
   * @param {number} capsuleId - The ID of the time capsule
   * @returns {Promise} - Promise containing the capsule's comments
   */
  async getCommentsByCapsule(capsuleId) {
    try {
      const response = await axios.get(`${API_URL}/capsule/${capsuleId}`);
      return response.data;
    } catch (error) {
      this.handleError(`Error fetching comments for capsule ID ${capsuleId}`, error);
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
const commentServices = new CommentServices();
export default commentServices;