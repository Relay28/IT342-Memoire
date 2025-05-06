import React, { useState, useEffect } from 'react';
import { FiSearch, FiX } from 'react-icons/fi';
import { Link } from 'react-router-dom';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { useAuth } from './AuthProvider';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import FriendshipService from '../services/FriendshipService';

const FriendsPage = () => {
  const { isDark } = useThemeMode();
  const { user, authToken } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [friends, setFriends] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const friendsList = await FriendshipService.getFriendsList(authToken);
        setFriends(friendsList); // Get ALL friends, not just first 4
      } catch (error) {
        console.error('Error fetching friends:', error);
      } finally {
        setLoading(false); // Fixed: Changed from setLoadingFriends to setLoading
      }
    };

    if (authToken) {
      fetchFriends();
    }
  }, [authToken]);

  const handleRemoveFriend = async (friendId) => {
    try {
      await FriendshipService.deleteFriendship(friendId, authToken);
      setFriends(friends.filter(f => f.id !== friendId));
    } catch (err) {
      console.error('Error removing friend:', err);
    }
  };

  const filteredFriends = friends.filter(friend =>
    friend.username.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (loading) {
    return (
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header user={user} />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <main className={`flex-1 p-8 overflow-y-auto flex items-center justify-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'border-gray-300' : 'border-gray-600'}`}></div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
      <div className="flex flex-col h-screen">
        <Header user={user} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          
          <main className={`flex-1 p-4 md:p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className="max-w-6xl mx-auto">
              {/* Header with search */}
              <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8 gap-4">
                <h1 className={`text-2xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Friends
                </h1>
                
                <div className="relative w-full md:w-64">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiSearch className={`h-5 w-5 ${isDark ? 'text-gray-400' : 'text-gray-500'}`} />
                  </div>
                  <input
                    type="text"
                    placeholder="Search friends..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className={`w-full py-2 pl-10 pr-4 rounded-full border focus:outline-none focus:ring-2 ${
                      isDark 
                        ? 'bg-gray-700 border-gray-600 text-white focus:ring-[#AF3535]' 
                        : 'bg-white border-gray-300 text-gray-800 focus:ring-[#AF3535]'
                    }`}
                  />
                </div>
              </div>

              {/* Friends List */}
              <div className="space-y-2">
                {filteredFriends.length > 0 ? (
                  filteredFriends.map(friend => (
                    <div 
                      key={friend.id} 
                      className={`flex items-center justify-between p-3 rounded-lg transition-all ${
                        isDark 
                          ? 'bg-gray-700/50 hover:bg-gray-700' 
                          : 'bg-white hover:bg-gray-50'
                      }`}
                    >
                      <Link 
                        to={`/profile/${friend.id}`}
                        className="flex items-center flex-1"
                      >
                        <div className="relative mr-3">
                          <div className="w-10 h-10 rounded-full overflow-hidden border-2 border-[#AF3535]">
                            {friend.profilePicture ? (
                              <img 
                                src={
                                  typeof friend.profilePicture === 'string' && friend.profilePicture.startsWith('data:image') 
                                    ? friend.profilePicture 
                                    : `data:image/jpeg;base64,${friend.profilePicture}`
                                }
                                alt={friend.username}
                                className="w-full h-full object-cover"
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.src = ProfilePictureSample;
                                }}
                              />
                            ) : (
                              <img 
                                src={ProfilePictureSample}
                                alt={friend.username}
                                className="w-full h-full object-cover"
                              />
                            )}
                          </div>
                
                        </div>
                        <div>
                          <p className={`font-medium ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>
                            {friend.username}
                          </p>
                          <p className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            {friend.email || 'No email provided'}
                          </p>
                        </div>
                      </Link>
                      
                      <button 
                        onClick={() => handleRemoveFriend(friend.id)}
                        className={`ml-4 p-2 rounded-full transition-colors ${
                          isDark 
                            ? 'text-red-400 hover:text-red-300 hover:bg-gray-600' 
                            : 'text-red-500 hover:text-red-600 hover:bg-gray-100'
                        }`}
                        aria-label="Remove friend"
                      >
                        <FiX className="h-4 w-4" />
                      </button>
                    </div>
                  ))
                ) : (
                  <div className={`p-4 text-center rounded-lg ${
                    isDark ? 'bg-gray-700/50 text-gray-300' : 'bg-white text-gray-500'
                  }`}>
                    {searchQuery ? 'No friends match your search' : 'You have no friends yet'}
                  </div>
                )}
              </div>
            </div>
          </main>
        </div>
      </div>
    </div>
  );
};

export default FriendsPage;