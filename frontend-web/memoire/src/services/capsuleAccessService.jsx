const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const handleResponse = async (response) => {
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    const errorMessage = errorData.message || `Request failed with status ${response.status}`;
    throw new Error(errorMessage);
  }
  return response.json();
};

const capsuleAccessService = {
  /**
   * Get all access entries for a specific capsule
   * @param {string} capsuleId - ID of the time capsule
   * @param {string} authToken - Authentication token
   * @returns {Promise<Array>} - Array of access entries
   */
  getAccesses: async (capsuleId, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/capsule/${capsuleId}`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    return handleResponse(res);
  },

  /**
   * Grant access to a specific user
   * @param {Object} params - Grant access parameters
   * @param {string} params.capsuleId - ID of the time capsule
   * @param {string} params.userId - ID of the user to grant access to
   * @param {string} params.role - Role to assign (EDITOR or VIEWER)
   * @param {string} authToken - Authentication token
   * @returns {Promise<Object>} - Created access entry
   */
  grantAccess: async ({ capsuleId, userId, role }, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`
      },
      body: JSON.stringify({ capsuleId, userId, role })
    });
    return handleResponse(res);
  },

  /**
   * Remove an access entry
   * @param {string} accessId - ID of the access entry to remove
   * @param {string} authToken - Authentication token
   * @returns {Promise<void>}
   */
  removeAccess: async (accessId, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/${accessId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${authToken}` }
    });
    await handleResponse(res);
  },

  /**
   * Restrict access to only the owner
   * @param {string} capsuleId - ID of the time capsule
   * @param {string} authToken - Authentication token
   * @returns {Promise<void>}
   */
  restrictAccessToOwner: async (capsuleId, authToken) => {
    try {
      const res = await fetch(`${API_BASE_URL}/api/capsule-access/${capsuleId}/only-me`, {
        method: 'DELETE',
        headers: { 
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      // Also update the capsule's public status to false
      await fetch(`${API_BASE_URL}/api/timecapsules/${capsuleId}/public`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ isPublic: false })
      });
      
      await handleResponse(res);
    } catch (error) {
      console.error('Error restricting access to owner:', error);
      throw error;
    }
  },

  /**
   * Grant public access to a capsule
   * @param {string} capsuleId - ID of the time capsule
   * @param {string} authToken - Authentication token
   * @returns {Promise<void>}
   */
  grantPublicAccess: async (capsuleId, authToken) => {
    try {
      // First remove all existing access
      await fetch(`${API_BASE_URL}/api/capsule-access/${capsuleId}/only-me`, {
        method: 'DELETE',
        headers: { 
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      // Then set the capsule as public
      const res = await fetch(`${API_BASE_URL}/api/capsule-access/${capsuleId}/public-access`, {
        method: 'POST',
        headers: { 
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        }
      });
      
      // Also update the capsule's public status
      await fetch(`${API_BASE_URL}/api/timecapsules/${capsuleId}/public`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${authToken}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ isPublic: true })
      });
      
      await handleResponse(res);
    } catch (error) {
      console.error('Error granting public access:', error);
      throw error;
    }
  },
  /**
   * Grant access to all friends
   * @param {string} capsuleId - ID of the time capsule
   * @param {string} role - Role to assign (EDITOR or VIEWER)
   * @param {string} authToken - Authentication token
   * @returns {Promise<Array>} - Array of created access entries
   */
  grantAccessToAllFriends: async (capsuleId, role, authToken) => {
    const res = await fetch(
      `${API_BASE_URL}/api/capsule-access/grant-to-friends/${capsuleId}?role=${role}`,
      {
        method: 'POST',
        headers: { Authorization: `Bearer ${authToken}` }
      }
    );
    return handleResponse(res);
  },

  /**
   * Update an access role
   * @param {string} accessId - ID of the access entry to update
   * @param {string} newRole - New role to assign (EDITOR or VIEWER)
   * @param {string} authToken - Authentication token
   * @returns {Promise<Object>} - Updated access entry
   */
  updateAccessRole: async (accessId, newRole, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/${accessId}/role`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`
      },
      body: JSON.stringify({ newRole })
    });
    return handleResponse(res);
  },

  /**
   * Search users
   * @param {string} query - Search query
   * @param {string} [excludeCapsuleId] - Optional capsule ID to exclude
   * @param {string} authToken - Authentication token
   * @returns {Promise<Array>} - Array of user search results
   */
  searchUsers: async (query, excludeCapsuleId, authToken) => {
    const url = new URL(`${API_BASE_URL}/api/capsule-access/search`);
    url.searchParams.append('query', query);
    if (excludeCapsuleId) {
      url.searchParams.append('excludeCapsuleId', excludeCapsuleId);
    }

    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    return handleResponse(res);
  },

  /**
   * Get all access entries for a specific user
   * @param {string} userId - ID of the user
   * @param {string} authToken - Authentication token
   * @returns {Promise<Array>} - Array of access entries
   */
  getAccessesByUser: async (userId, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/user/${userId}`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    return handleResponse(res);
  },

  /**
   * Check if a user has specific access to a capsule
   * @param {string} capsuleId - ID of the time capsule
   * @param {string} role - Role to check for
   * @param {string} authToken - Authentication token
   * @returns {Promise<boolean>} - Whether the user has the specified access
   */
  checkAccess: async (capsuleId, role, authToken) => {
    const url = new URL(`${API_BASE_URL}/api/capsule-access/check`);
    url.searchParams.append('capsuleId', capsuleId);
    url.searchParams.append('role', role);

    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    const data = await handleResponse(res);
    return data.hasAccess;
  }
};

export default capsuleAccessService;