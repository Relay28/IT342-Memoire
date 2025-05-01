import React, { useState, useEffect } from 'react';
import { FiSearch, FiX } from 'react-icons/fi';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { useAuth } from './AuthProvider';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import friendshipService from '../services/FriendshipService';

const FriendsPage = () => {
  const { isDark } = useThemeMode();
  const { user, token } = useAuth();
  const [searchQuery, setSearchQuery] = useState('');
  const [friends, setFriends] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const friendsList = await friendshipService.getFriendsList(token);
        setFriends(friendsList);
      } catch (err) {
        console.error('Error fetching friends:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchFriends();
  }, [token]);

  const handleRemoveFriend = async (friendId) => {
    try {
      const friendship = await friendshipService.findByUsers(friendId, token);
      if (friendship) {
        await friendshipService.deleteFriendship(friendship.id, token);
        setFriends(friends.filter(f => f.id !== friendId));
      }
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
              
              <div className="max-w-6xl mx-auto">
                {filteredFriends.length === 0 ? (
                  <div className={`text-center py-16 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                    <div className="inline-block p-6 rounded-full bg-gray-100 dark:bg-gray-700/50 mb-4">
                      <svg 
                        xmlns="http://www.w3.org/2000/svg" 
                        className="h-12 w-12 text-gray-400 dark:text-gray-500" 
                        fill="none" 
                        viewBox="0 0 24 24" 
                        stroke="currentColor"
                      >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                      </svg>
                    </div>
                    <h3 className="text-xl font-medium mb-2">No friends yet</h3>
                    <p className="max-w-md mx-auto">
                      {searchQuery ? 'No friends match your search' : 'Add some friends to see them here'}
                    </p>
                  </div>
                ) : (
                  filteredFriends.map(friend => (
                    <div 
                      key={friend.id} 
                      className={`rounded-xl p-4 transition-all ${isDark ? 'bg-gray-700/50 border border-gray-600' : 'bg-white border border-gray-200'}`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4">
                          <img 
                            src={friend.profilePicture || ProfilePictureSample} 
                            alt={friend.username}
                            className="h-12 w-12 rounded-full border-2 border-white dark:border-gray-600 shadow-sm"
                          />
                          <div>
                            <h3 className={`font-medium ${isDark ? 'text-white' : 'text-gray-900'}`}>
                              {friend.username}
                            </h3>
                            <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                              {friend.email}
                            </p>
                          </div>
                        </div>
                        
                        <button 
                          className={`px-4 py-2 rounded-full text-sm flex items-center ${
                            isDark 
                              ? 'bg-red-600 hover:bg-red-700 text-white' 
                              : 'bg-red-500 hover:bg-red-600 text-white'
                          }`}
                          onClick={() => handleRemoveFriend(friend.id)}
                        >
                          <FiX className="mr-2 h-4 w-4" />
                          Remove
                        </button>
                      </div>
                    </div>
                  ))
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