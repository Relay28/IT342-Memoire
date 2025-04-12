import { useState, useEffect, useRef } from 'react';
import { FiX, FiSearch, FiUser, FiLock, FiPlus } from 'react-icons/fi';
import { useAuth } from '../../components/AuthProvider';
import apiService from '../Profile/apiService'; // Make sure you have this service set up

const ShareModal = ({ title, onClose }) => {
  const { user, authToken } = useAuth();
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

  // Click outside handler
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (searchRef.current && !searchRef.current.contains(event.target)) {
        setShowSearchResults(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

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

  const addCollaborator = (user) => {
    setCollaborators(prev => [
      ...prev,
      {
        id: user.userId || user.id,
        username: user.username,
        name: user.name,
        profilePicture: user.profilePicture,
        role: 'Collaborator',
        accessDate: new Date().toISOString()
      }
    ]);
    setSearchQuery('');
    setSearchResults([]);
    setShowSearchResults(false);
  };

  const removeCollaborator = (userId) => {
    setCollaborators(prev => prev.filter(c => c.id !== userId));
  };

  // Filter out already added collaborators and current user
  const filteredResults = searchResults.filter(result => {
    const resultId = (result.userId || result.id).toString();
    return (
      resultId !== user.id.toString() &&
      !collaborators.some(c => c.id.toString() === resultId)
    );
  });

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg w-full max-w-md max-h-[90vh] overflow-y-auto">
        {/* Modal Header */}
        <div className="flex justify-between items-center p-4 border-b sticky top-0 bg-white z-10">
          <h3 className="font-semibold text-lg">Share '{title}'</h3>
          <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
            <FiX size={20} />
          </button>
        </div>

        {/* Search Input */}
        <div className="p-4 border-b relative" ref={searchRef}>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <FiSearch className="text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search users..."
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-transparent"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onFocus={() => searchQuery && setShowSearchResults(true)}
            />
          </div>

          {/* Search Results Dropdown */}
          {showSearchResults && (
            <div className="absolute left-0 right-0 mt-1 mx-4 bg-white border border-gray-200 rounded-md shadow-lg z-20">
              {isSearching ? (
                <div className="p-3 text-center text-gray-500">
                  Searching...
                </div>
              ) : searchError ? (
                <div className="p-3 text-center text-red-500">
                  {searchError}
                </div>
              ) : filteredResults.length === 0 ? (
                <div className="p-3 text-center text-gray-500">
                  No users found
                </div>
              ) : (
                <div className="max-h-60 overflow-y-auto">
                  {filteredResults.map((user) => (
                    <div 
                      key={user.userId || user.id}
                      className="flex items-center justify-between p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                      onClick={() => addCollaborator(user)}
                    >
                      <div className="flex items-center space-x-3">
                        <div className="w-8 h-8 rounded-full bg-gray-200 overflow-hidden flex-shrink-0">
                          {user.profilePicture ? (
                            <img 
                              src={user.profilePicture} 
                              alt={user.username}
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <FiUser className="w-full h-full p-1 text-gray-600" />
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="font-medium truncate">{user.username}</p>
                          {user.name && <p className="text-xs text-gray-500 truncate">{user.name}</p>}
                        </div>
                      </div>
                      <FiPlus className="text-gray-400 hover:text-red-600 flex-shrink-0" />
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>

        {/* Collaborators List */}
        <div className="p-4">
          <h4 className="text-sm font-medium text-gray-500 mb-2">People with access</h4>
          <ul className="space-y-3">
            {collaborators.map(person => (
              <li key={person.id} className="flex justify-between items-center">
                <div className="flex items-center space-x-3 min-w-0">
                  <div className="w-10 h-10 rounded-full bg-gray-200 overflow-hidden flex-shrink-0">
                    {person.profilePicture ? (
                      <img 
                        src={person.profilePicture} 
                        alt={person.username}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <FiUser className="w-full h-full p-2 text-gray-600" />
                    )}
                  </div>
                  <div className="min-w-0">
                    <p className="font-medium truncate">{person.username}</p>
                    <p className="text-xs text-gray-500 truncate">{person.name || ''}</p>
                  </div>
                </div>
                <div className="text-xs text-gray-500 flex-shrink-0">
                  {person.role === 'Owner' ? (
                    <span className="flex items-center">
                      <FiLock className="mr-1" size={12} /> Owner
                    </span>
                  ) : (
                    <div className="flex items-center space-x-2">
                      <span className="text-gray-400 text-xs">
                        {new Date(person.accessDate).toLocaleDateString()}
                      </span>
                      <button 
                        onClick={() => removeCollaborator(person.id)}
                        className="text-gray-400 hover:text-red-600"
                      >
                        ×
                      </button>
                    </div>
                  )}
                </div>
              </li>
            ))}
          </ul>
        </div>

        {/* Modal Footer */}
        <div className="p-4 border-t flex justify-end sticky bottom-0 bg-white">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
};

export default ShareModal;