import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import capsuleContentService from '../services/capsuleContentService';
import { useAuth } from '../components/AuthProvider';

const API_BASE_URL = 'https://memoire-it342.as.r.appspot.com/api/capsule-content';

const CapsuleContentContext = createContext();

export const CapsuleContentProvider = ({ children }) => {
  const { authToken, isAuthenticated, user } = useAuth();
  const [contents, setContents] = useState({});
  const [isConnected, setIsConnected] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // WebSocket connection management
  useEffect(() => {
    if (isAuthenticated && authToken) {
      const unsubscribeStatus = capsuleContentService.onStatusChange((status) => {
        setIsConnected(status === 'connected');
        setConnectionStatus(status);
      });
     
      return () => {
        unsubscribeStatus();
        capsuleContentService.unsubscribeAll();
      };
    }
  }, [isAuthenticated, authToken]);

  const getAuthHeaders = useCallback(() => {
    return {
      headers: {
        Authorization: `Bearer ${authToken}`
      }
    };
  }, [authToken]);
  
  const fetchCapsuleContents = useCallback(async (capsuleId) => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${API_BASE_URL}/${capsuleId}`,
        getAuthHeaders()
      );
      
      // Transform the response to include downloadable URLs
      const transformedData = response.data.map(item => ({
        ...item,
        url: `${API_BASE_URL}/${item.id}/download`
      }));
      
      setLoading(false);
      return transformedData;
    } catch (err) {
      setError(err.message || 'Failed to fetch capsule contents');
      setLoading(false);
      throw err;
    }
  }, [getAuthHeaders]);

  const getContentTypeHeaders = useCallback(() => {
    return {
      headers: {
        Authorization: `Bearer ${authToken}`,
        'Content-Type': 'multipart/form-data'
      }
    };
  }, [authToken]);

  // WebSocket subscription handlers
  const connectToCapsule = useCallback(async (capsuleId) => {
    if (!isAuthenticated || !authToken || !user?.username) {
      setError('Authentication required');
      throw new Error('Authentication required');
    }
    
    setLoading(true);
    
    try {
      await capsuleContentService.subscribeToCapsule(
        capsuleId,
        (update) => handleUpdate(capsuleId, update),
        (initialContents) => handleInitialContents(capsuleId, initialContents)
      );
      
      setLoading(false);
      return { success: true };
    } catch (error) {
      console.error("Failed to connect to capsule:", error);
      setError(error.message || 'Failed to connect to capsule');
      setLoading(false);
      
      // Fallback to REST API if WebSocket fails
      try {
        const contents = await fetchCapsuleContents(capsuleId);
        handleInitialContents(capsuleId, contents);
        return { success: false, fallback: true };
      } catch (fallbackError) {
        setError(fallbackError.message || 'Failed to fetch fallback contents');
        throw fallbackError;
      }
    }
  }, [authToken, isAuthenticated, fetchCapsuleContents, user?.username]);

  const handleUpdate = useCallback((capsuleId, update) => {
    console.log("Received WebSocket update:", update);
    setContents((prev) => {
      const current = { ...(prev[capsuleId] || {}) };
      const contentId = update.id || update.contentId;
  
      if (!contentId) return prev;
  
      if (update.action === 'delete') {
        const { [contentId]: _, ...remaining } = current;
        return { ...prev, [capsuleId]: remaining };
      }
  
      // Handle add/update with url for binary data access
      return {
        ...prev,
        [capsuleId]: {
          ...current,
          [contentId]: {
            ...(current[contentId] || {}),
            ...update,
            id: contentId,
            // Add direct download URL
            url: update.contentUrl || `${API_BASE_URL}/${contentId}/download`
          }
        }
      };
    });
  }, []);

  const handleInitialContents = useCallback((capsuleId, initialContents) => {
    if (!Array.isArray(initialContents)) return;
    
    const contentsMap = initialContents.reduce((acc, item) => {
      if (item?.id) {
        // Add download URL for accessing binary data
        acc[item.id] = {
          ...item,
          url: item.contentUrl || `${API_BASE_URL}/${item.id}/download`
        };
      }
      return acc;
    }, {});

    setContents(prev => ({
      ...prev,
      [capsuleId]: contentsMap
    }));
  }, []);

  const disconnectFromCapsule = useCallback((capsuleId) => {
    capsuleContentService.unsubscribeFromCapsule(capsuleId);
    setContents((prev) => {
      const newContents = { ...prev };
      delete newContents[capsuleId];
      return newContents;
    });
  }, []);

  const fetchRenderableContents = useCallback(async (capsuleId) => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${API_BASE_URL}/renderable/${capsuleId}`,
        getAuthHeaders()
      );
      
      // Transform to add direct download URLs
      const renderableWithUrls = response.data.map(item => ({
        ...item,
        url: item.contentUrl || `${API_BASE_URL}/${item.id}/download`
      }));
      
      setLoading(false);
      return renderableWithUrls;
    } catch (err) {
      setError(err.message || 'Failed to fetch renderable contents');
      setLoading(false);
      throw err;
    }
  }, [getAuthHeaders]);

  const fetchMediaContent = useCallback(async (capsuleId) => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${API_BASE_URL}/${capsuleId}`,
        getAuthHeaders()
      );
      
      const mediaContents = response.data
        .filter(content => 
          content.contentType?.startsWith('image/') || 
          content.contentType?.startsWith('video/')
        )
        .map(content => ({
          ...content,
          // Direct URL to access binary data
          url: `${API_BASE_URL}/${content.id}/download`
        }));
      
      setLoading(false);
      return mediaContents;
    } catch (err) {
      setError(err.message || 'Failed to fetch media content');
      setLoading(false);
      throw err;
    }
  }, [getAuthHeaders]);

  const uploadContent = useCallback(async (capsuleId, file) => {
    setLoading(true);
    const formData = new FormData();
    formData.append('file', file);
    
    try {
      const response = await axios.post(
        `${API_BASE_URL}/${capsuleId}/upload`,
        formData,
        getContentTypeHeaders()
      );
      
      // Manually trigger WebSocket-like update
      // Add download URL to the uploaded content
      const uploadedContent = {
        ...response.data,
        url: `${API_BASE_URL}/${response.data.id}/download`
      };
      
      handleUpdate(capsuleId, {
        ...uploadedContent,
        action: 'add',
        id: uploadedContent.id
      });
      
      setLoading(false);
      return uploadedContent;
    } catch (err) {
      setError(err.message || 'Failed to upload content');
      setLoading(false);
      throw err;
    }
  }, [getContentTypeHeaders, handleUpdate]);

  const downloadContent = useCallback(async (contentId) => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${API_BASE_URL}/${contentId}/download`,
        {
          ...getAuthHeaders(),
          responseType: 'blob'
        }
      );
      setLoading(false);
      return response.data;
    } catch (err) {
      setError(err.message || 'Failed to download content');
      setLoading(false);
      throw err;
    }
  }, [getAuthHeaders]);

  const deleteContent = useCallback(async (contentId) => {
    setLoading(true);
    try {
      await axios.delete(
        `${API_BASE_URL}/${contentId}`,
        getAuthHeaders()
      );
      
      // Find which capsule this content belongs to
      let foundCapsuleId = null;
      for (const [capsuleId, capsuleContents] of Object.entries(contents)) {
        if (capsuleContents[contentId]) {
          foundCapsuleId = capsuleId;
          break;
        }
      }
      
      // If we found the capsule, manually trigger a WebSocket-like update
      if (foundCapsuleId) {
        handleUpdate(foundCapsuleId, {
          id: contentId,
          action: 'delete'
        });
      }
      
      setLoading(false);
      return true;
    } catch (err) {
      setError(err.message || 'Failed to delete content');
      setLoading(false);
      throw err;
    }
  }, [getAuthHeaders, contents, handleUpdate]);

  const getCapsuleContents = useCallback((capsuleId) => {
    return contents[capsuleId] ? Object.values(contents[capsuleId]) : [];
  }, [contents]);

  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const value = {
    connectToCapsule,
    disconnectFromCapsule,
    isConnected,
    connectionStatus,
    contents,
    getCapsuleContents,
    fetchCapsuleContents,
    fetchRenderableContents,
    fetchMediaContent,
    uploadContent,
    downloadContent,
    deleteContent,
    loading,
    error,
    clearError
  };

  return (
    <CapsuleContentContext.Provider value={value}>
      {children}
    </CapsuleContentContext.Provider>
  );
};

export const useCapsuleContent = () => {
  const context = useContext(CapsuleContentContext);
  if (!context) {
    throw new Error('useCapsuleContent must be used within a CapsuleContentProvider');
  }
  return context;
};