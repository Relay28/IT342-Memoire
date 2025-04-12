// src/hooks/useTimeCapsule.js
import { useState, useCallback } from 'react';
import { useAuth } from '../components/AuthProvider';
import timeCapsuleService from '../services/TimeCapsuleService';

/**
 * Custom hook for managing time capsule operations
 * @returns {Object} Time capsule operations and state
 */
export const useTimeCapsule = () => {
  const { authToken } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  /**
   * Creates a new time capsule
   * @param {Object} timeCapsuleData - The time capsule data
   * @returns {Promise} - The created time capsule
   */
  const createTimeCapsule = useCallback(async (timeCapsuleData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.createTimeCapsule(timeCapsuleData, authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create time capsule');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets a specific time capsule by ID
   * @param {number} id - The time capsule ID
   * @returns {Promise} - The requested time capsule
   */
  const getTimeCapsule = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getTimeCapsule(id, authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || `Failed to get time capsule with ID ${id}`);
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all time capsules for the current user
   * @returns {Promise} - The user's time capsules
   */
  const getUserTimeCapsules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getUserTimeCapsules(authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get your time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all UNPUBLISHED time capsules
   * @returns {Promise} - Promise containing unpublished time capsules
   */
  const getUnpublishedTimeCapsules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getUnpublishedTimeCapsules(authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get unpublished time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all CLOSED time capsules
   * @returns {Promise} - Promise containing closed time capsules
   */
  const getClosedTimeCapsules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getClosedTimeCapsules(authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get closed time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all PUBLISHED time capsules
   * @returns {Promise} - Promise containing published time capsules
   */
  const getPublishedTimeCapsules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getPublishedTimeCapsules(authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get published time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all ARCHIVED time capsules
   * @returns {Promise} - Promise containing archived time capsules
   */
  const getArchivedTimeCapsules = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getArchivedTimeCapsules(authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get archived time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Gets all time capsules with pagination
   * @param {Object} options - Pagination and sorting options
   * @returns {Promise} - Paginated time capsules
   */
  const getAllTimeCapsules = useCallback(async (options = {}) => {
    const { page = 0, size = 10, sortBy = 'createdAt', sortDirection = 'DESC' } = options;
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.getAllTimeCapsules(
        page, 
        size, 
        sortBy, 
        sortDirection, 
        authToken
      );
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to get time capsules');
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Updates an existing time capsule
   * @param {number} id - The time capsule ID
   * @param {Object} timeCapsuleData - The updated data
   * @returns {Promise} - The updated time capsule
   */
  const updateTimeCapsule = useCallback(async (id, timeCapsuleData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await timeCapsuleService.updateTimeCapsule(id, timeCapsuleData, authToken);
      setLoading(false);
      return response;
    } catch (err) {
      setError(err.response?.data?.message || `Failed to update time capsule with ID ${id}`);
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Deletes a time capsule
   * @param {number} id - The time capsule ID to delete
   * @returns {Promise} - Promise resolving when deletion is complete
   */
  const deleteTimeCapsule = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      await timeCapsuleService.deleteTimeCapsule(id, authToken);
      setLoading(false);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || `Failed to delete time capsule with ID ${id}`);
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Locks a time capsule with a specified open date
   * @param {number} id - The time capsule ID
   * @param {string} openDate - The date when the capsule should unlock
   * @returns {Promise} - Promise resolving when locking is complete
   */
  const lockTimeCapsule = useCallback(async (id, openDate) => {
    setLoading(true);
    setError(null);
    try {
      await timeCapsuleService.lockTimeCapsule(id, openDate, authToken);
      setLoading(false);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || `Failed to lock time capsule with ID ${id}`);
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Unlocks a time capsule
   * @param {number} id - The time capsule ID to unlock
   * @returns {Promise} - Promise resolving when unlocking is complete
   */
  const unlockTimeCapsule = useCallback(async (id) => {
    setLoading(true);
    setError(null);
    try {
      await timeCapsuleService.unlockTimeCapsule(id, authToken);
      setLoading(false);
      return true;
    } catch (err) {
      setError(err.response?.data?.message || `Failed to unlock time capsule with ID ${id}`);
      setLoading(false);
      throw err;
    }
  }, [authToken]);

  /**
   * Clears the current error state
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    loading,
    error,
    setLoading,  // Exporting state setters for direct use
    setError,    // Exporting state setters for direct use
    clearError,
    createTimeCapsule,
    getTimeCapsule,
    getUserTimeCapsules,
    getUnpublishedTimeCapsules,  // Added new method
    getClosedTimeCapsules,       // Added new method
    getPublishedTimeCapsules,    // Added new method
    getArchivedTimeCapsules,     // Added new method
    getAllTimeCapsules,
    updateTimeCapsule,
    deleteTimeCapsule,
    lockTimeCapsule,
    unlockTimeCapsule
  };
};