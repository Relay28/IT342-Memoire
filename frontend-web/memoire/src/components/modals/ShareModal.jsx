import { useState, useEffect, useRef } from 'react';
import { FiX, FiSearch, FiUser, FiLock, FiPlus } from 'react-icons/fi';
import { useAuth } from '../../components/AuthProvider';
import apiService from '../Profile/apiService';
import capsuleAccessService from '../../services/capsuleAccessService';
import { useNavigate } from 'react-router-dom';

const ShareModal = ({ title, onClose, capsuleId }) => {
  const { user, authToken } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [isSearching, setIsSearching] = useState(false);
  const [searchError, setSearchError] = useState(null);
  const [showSearchResults, setShowSearchResults] = useState(false);
  const searchRef = useRef(null);
  const [collaborators, setCollaborators] = useState([
    { 
      id: user.id, 
      username: user.username, 
      name: user.name, 
      profilePicture: user.profilePicture,
      role: 'Owner' 
    }
  ]);

  useEffect(() => {
    if (!capsuleId) return;
  
    const fetchAccesses = async () => {
      try {
        const accesses = await capsuleAccessService.getAccesses(capsuleId, authToken);
  
        const collaboratorsData = [];
  
        // Add the owner (already included in initial state)
        collaboratorsData.push({
          id: user.id,
          username: user.username,
          name: user.name,
          profilePicture: user.profilePicture,
          role: 'Owner',
        });
  
        // Fetch user details for each access entry, excluding the owner (already included)
        for (const access of accesses) {
          const userId = access.userId;
          if (userId !== user.id) { // Skip the owner
            const userResponse = await apiService.get(`/api/profiles/view/${userId}`);
            const userData = userResponse.data;
            
            let profilePicture = null;
            if (userData.profilePicture) {
              if (typeof userData.profilePicture === 'string') {
                profilePicture = `data:image/jpeg;base64,${userData.profilePicture}`;
              }  
            }
            
            collaboratorsData.push({
              id: userId,
              username: userData.username,
              name: userData.name,
              profilePicture,
              role: access.role,
              accessId: access.id,
              accessDate: access.createdAt,
            });
          }
        }
  
        setCollaborators(collaboratorsData);
      } catch (e) {
        console.error('Failed to fetch accesses:', e);
      }
    };
  
    fetchAccesses();
  }, [capsuleId, authToken]);

  // Search users effect
  useEffect(() => {
    const searchUsers = async () => {
      try {
        setIsSearching(true);
        setSearchError(null);
        const response = await apiService.get(
          `/api/profiles/search?query=${encodeURIComponent(searchQuery)}`
        );
        setSearchResults(response.data.results);
      } catch (err) {
        console.error('Search error:', err);
        setSearchError('Failed to load search results');
        setSearchResults([]);
      } finally {
        setIsSearching(false);
      }
    };

    if (searchQuery.trim()) {
      setShowSearchResults(true);
      const debounceTimer = setTimeout(searchUsers, 300);
      return () => clearTimeout(debounceTimer);
    } else {
      setShowSearchResults(false);
      setSearchResults([]);
    }
  }, [searchQuery]);

  const addCollaborator = async (user) => {
    const userId = user.userId || user.id;
    if (collaborators.some(c => c.id === userId)) {
      return; // User already added
    }

    try {
      const access = await capsuleAccessService.grantAccess({
        capsuleId,
        userId,
        role: 'EDITOR'
      }, authToken);
      setCollaborators(prev => [...prev, {
        id: access.user.id,
        username: access.user.username,
        name: access.user.name,
        profilePicture: access.user.profilePicture,
        role: access.role,
      }]);
      setSearchQuery('');
      setSearchResults([]);
      setShowSearchResults(false);
    } catch (e) {
      // handle error
    }
  };

  const removeCollaborator = async (userId) => {
    const collaborator = collaborators.find(c => c.id === userId);
    if (!collaborator) return;
    try {
      await capsuleAccessService.removeAccess(collaborator.accessId, authToken);
      setCollaborators(prev => prev.filter(c => c.id !== userId));
    } catch (e) {
      // handle error
    }
  };

  const filteredResults = searchResults.filter(result => {
    const resultId = (result.userId || result.id).toString();
    return (
      resultId !== user.id.toString() &&
      !collaborators.some(c => c.id.toString() === resultId)
    );
  });

  return (
    <div className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-xl w-full max-w-md max-h-[90vh] overflow-hidden shadow-2xl">
        {/* Modal Header */}
        <div className="flex justify-between items-center p-6 border-b sticky top-0 bg-white z-10">
          <div>
            <h3 className="font-bold text-xl text-gray-900">Share '{title}'</h3>
            <p className="text-sm text-gray-500 mt-1">Add collaborators to this content</p>
          </div>
          <button 
            onClick={onClose} 
            className="p-2 rounded-full hover:bg-gray-100 transition-colors text-gray-500 hover:text-gray-700"
          >
            <FiX size={20} />
          </button>
        </div>

        {/* Search Input */}
        <div className="p-6 pb-0 relative" ref={searchRef}>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
              <FiSearch className="text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search by name or username..."
              className="w-full pl-12 pr-4 py-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent transition-all"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => searchQuery && setShowSearchResults(true)}
            />
            {searchQuery && (
              <button 
                onClick={() => {
                  setSearchQuery('');
                  setSearchResults([]);
                }}
                className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-600 transition-colors"
              >
                <FiX size={16} />
              </button>
            )}
          </div>

          {/* Search Results Dropdown */}
          {showSearchResults && (
            <div className="absolute left-6 right-6 mt-2 bg-white border border-gray-200 rounded-lg shadow-lg z-20 overflow-hidden">
              {isSearching ? (
                <div className="p-4 text-center text-gray-500 flex items-center justify-center space-x-2">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-gray-400"></div>
                  <span>Searching...</span>
                </div>
              ) : searchError ? (
                <div className="p-4 text-center text-red-500">
                  {searchError}
                </div>
              ) : filteredResults.length === 0 ? (
                <div className="p-4 text-center text-gray-500">
                  No matching users found
                </div>
              ) : (
                <div className="max-h-60 overflow-y-auto divide-y divide-gray-100">
                  {filteredResults.map((user) => (
                    <div 
                      key={user.userId || user.id}
                      className="flex items-center justify-between p-3 hover:bg-gray-50 cursor-pointer transition-colors"
                      onClick={() => addCollaborator(user)}
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 rounded-full bg-gray-100 overflow-hidden flex-shrink-0 flex items-center justify-center">
                          {user.profilePicture ? (
                            <img 
                              src={user.profilePicture} 
                              alt={user.username}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <FiUser className="w-5 h-5 text-gray-400" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="font-medium text-gray-900 truncate">{user.username}</p>
                          {user.name && <p className="text-sm text-gray-500 truncate">{user.name}</p>}
                        </div>
                      </div>
                      <FiPlus className="text-gray-400 hover:text-red-600 transition-colors" />
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Collaborators List */}
        <div className="p-6 overflow-y-auto max-h-[40vh]">
          <h4 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
            People with access ({collaborators.length})
          </h4>
          <ul className="space-y-4">
            {collaborators.map(person => (
              <li key={person.id} className="flex justify-between items-center group">
                <div className="flex items-center space-x-4 min-w-0">
                  <div className="w-12 h-12 rounded-full bg-gray-100 overflow-hidden flex-shrink-0 flex items-center justify-center">
                    {person.profilePicture ? (
                      <img 
                        src={person.profilePicture} 
                        alt={person.username}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <FiUser className="w-5 h-5 text-gray-400" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium text-gray-900 truncate">{person.username}</p>
                    <p className="text-sm text-gray-500 truncate">{person.name || ''}</p>
                  </div>
                </div>
                <div className="flex items-center space-x-3">
                  {person.role === 'Owner' ? (
                    <span className="text-xs font-medium bg-gray-100 text-gray-600 px-2 py-1 rounded-full flex items-center">
                      <FiLock className="mr-1" size={12} /> Owner
                    </span>
                  ) : (
                    <span className="text-xs font-medium bg-gray-100 text-gray-600 px-2 py-1 rounded-full flex items-center">
                      {person.role === 'EDITOR' && 'Editor'}
                    </span>
                  )}
                  {person.role !== 'Owner' && (
                    <button 
                      onClick={() => removeCollaborator(person.id)}
                      className="w-8 h-8 flex items-center justify-center rounded-full hover:bg-gray-100 text-gray-400 hover:text-red-600 transition-colors"
                    >
                      <FiX size={16} />
                    </button>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </div>

        {/* Modal Footer */}
        <div className="p-6 border-t flex justify-end items-center">
          <button 
            onClick={onClose} 
            className="bg-gray-200 text-gray-700 py-2 px-4 rounded-md hover:bg-gray-300 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShareModal;
