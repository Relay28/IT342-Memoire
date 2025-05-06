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
              <div className="space-y-4">
  {filteredFriends.length > 0 ? (
    <div className={`rounded-xl ${isDark ? 'bg-gray-800' : 'bg-white'} shadow-sm overflow-hidden`}>
      {/* Header */}
      <div className={`p-4 border-b ${isDark ? 'border-gray-700' : 'border-gray-200'}`}>
        <h3 className={`text-lg font-semibold ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>
          Your Friends ({filteredFriends.length})
        </h3>
      </div>
      
      {/* Friends List */}
      <div className="divide-y divide-gray-200 dark:divide-gray-700">
        {filteredFriends.map(friend => (
          <div 
            key={friend.id} 
            className={`p-4 flex items-center justify-between ${isDark ? 'hover:bg-gray-700' : 'hover:bg-gray-50'} transition-colors`}
          >
            <Link 
              to={`/profile/${friend.id}`}
              className="flex items-center flex-1 min-w-0"
            >
              <div className="relative flex-shrink-0 mr-4">
                <div className="w-12 h-12 rounded-full overflow-hidden border-2 border-[#AF3535]/30">
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
              <div className="min-w-0">
                <p className={`font-medium truncate ${isDark ? 'text-gray-100' : 'text-gray-800'}`}>
                  {friend.username}
                </p>
                <p className={`text-sm truncate ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  {friend.email || 'No email provided'}
                </p>
              </div>
            </Link>
            
            <button 
              onClick={() => handleRemoveFriend(friend.id)}
              className={`
                ml-4 px-3 py-1.5 rounded-lg text-sm font-medium transition-all
                ${isDark ? 
                  'text-gray-300 hover:text-[#AF3535] hover:bg-gray-600' : 
                  'text-gray-600 hover:text-[#AF3535] hover:bg-gray-100'
                }
                flex items-center gap-1.5
              `}
              aria-label="Remove friend"
            >
              <FiX className="w-4 h-4 text-[#AF3535]" />
              <span className="hidden sm:inline text-[#AF3535]">Unfriend</span>
            </button>
          </div>
        ))}
      </div>
    </div>
  ) : (
    <div className={`p-8 text-center rounded-xl ${isDark ? 'bg-gray-800 text-gray-300' : 'bg-white text-gray-500'}`}>
      <div className="mx-auto w-20 h-20 bg-[#AF3535]/10 rounded-full flex items-center justify-center mb-4">
        <FiX className="h-10 w-10 text-[#AF3535]" />
      </div>
      <h3 className="text-lg font-medium mb-2">
        {searchQuery ? 'No friends found' : 'Your friends list is empty'}
      </h3>
      <p className="text-sm max-w-md mx-auto">
        {searchQuery ? 'Try searching with a different name' : 'Start by adding friends to connect'}
      </p>
      {!searchQuery && (
        <button className="mt-4 px-4 py-2 bg-[#AF3535] text-white rounded-lg text-sm font-medium hover:bg-[#AF3535]/90 transition-colors">
          Add Friends
        </button>
      )}
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