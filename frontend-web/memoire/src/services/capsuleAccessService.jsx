// capsuleAccessService.js
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const capsuleAccessService = {
  // Get all accesses for a capsule
  getAccesses: async (capsuleId, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/capsule/${capsuleId}`, {
      headers: { Authorization: `Bearer ${authToken}` }
    });
    if (!res.ok) throw new Error('Failed to fetch accesses');
    return res.json();
  },

  // Grant access to a user
  grantAccess: async ({ capsuleId, userId, role }, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${authToken}`
      },
      body: JSON.stringify({ capsuleId, userId, role })
    });
    if (!res.ok) throw new Error('Failed to grant access');
    return res.json();
  },

  // Remove access by accessId
  removeAccess: async (accessId, authToken) => {
    const res = await fetch(`${API_BASE_URL}/api/capsule-access/${accessId}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${authToken}` }
    });
    if (!res.ok) throw new Error('Failed to remove access');
  }
};

export default capsuleAccessService;
