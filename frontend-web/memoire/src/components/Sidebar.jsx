// src/components/Sidebar.js
import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { FaPlus, FaHome, FaStar, FaShareAlt, FaHourglassHalf } from 'react-icons/fa';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import { useThemeMode } from '../context/ThemeContext';
import  FriendshipService  from '../services/FriendshipService';
import { useAuth } from './AuthProvider';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { profileService } from '../components/ProfileFunctionalities';

const Sidebar = () => {
  const [isFirstModalOpen, setIsFirstModalOpen] = useState(false);
  const [isSecondModalOpen, setIsSecondModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [setloading, setIsLoading] = useState('');
  const [friends, setFriends] = useState([]);
  const [loadingFriends, setLoadingFriends] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();
  const { createTimeCapsule, loading, error } = useTimeCapsule();
  const { mode, isDark } = useThemeMode();
  const { authToken, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const friendsList = await FriendshipService.getFriendsList(authToken);
        setFriends(friendsList.slice(0, 4)); // Get first 4 friends
      } catch (error) {
        console.error('Error fetching friends:', error);
      } finally {
        setLoadingFriends(false);
      }
    };

    if (authToken) {
      fetchFriends();
    }
  }, [authToken]);

useEffect(() => {
    // Only fetch if the user is authenticated
    if (isAuthenticated && authToken) {
      // Fetch profile picture
      const fetchProfilePicture = async () => {
        try {
          setIsLoading(true);
            const userData = await profileService.getCurrentUser();
          const picture = userData.profilePicture;;;
  

          if (userData.profilePicture) {
            let imageUrl;
            if (typeof userData.profilePicture === 'string') {
              imageUrl = userData.profilePicture.startsWith('data:image') 
                ? userData.profilePicture 
                : `data:image/jpeg;base64,${userData.profilePicture}`;
            } else if (Array.isArray(userData.profilePicture)) {
              const binaryString = String.fromCharCode.apply(null, userData.profilePicture);
              imageUrl = `data:image/jpeg;base64,${btoa(binaryString)}`;
            }
            if (imageUrl)
              setProfilePicture(imageUrl);
          }
         
        } catch (error) {
          console.error('Error fetching profile picture:', error);
        } finally {
          setIsLoading(false);
        }
      };
      
      fetchProfilePicture();
    } else {
      setIsLoading(false);
    }
  }, [isAuthenticated, authToken]); 

  const handleCreateClick = () => {
    setIsFirstModalOpen(true);
  };

  const handleFirstModalConfirm = () => {
    setIsFirstModalOpen(false);
    setIsSecondModalOpen(true);
  };

  const handleSecondModalConfirm = async () => {
    try {
      const capsuleData = { title, description };
      const response = await createTimeCapsule(capsuleData);
      navigate(`/edit/${response.id}`);
    } catch (err) {
      console.error('Failed to create capsule:', err);
    }
    setIsSecondModalOpen(false);
  };

  // Check active route
  const isActive = (path) => location.pathname === path;

  return (
    <>
      <aside className={`w-72 h-full p-6 border-r ${isDark ? 'bg-gray-900 border-gray-700' : 'bg-white border-gray-200'}`}>
        
        {/* Create Capsule Button */}
        <button
          onClick={handleCreateClick}
          className={`w-full flex items-center p-3.5 mb-6 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'} ${isActive('/create') ? 'bg-[#AF3535]/10 border border-[#AF3535]/30' : ''}`}
        >
          <div className={`p-2 rounded-lg mr-3 ${isActive('/create') ? 'bg-[#AF3535] text-white' : isDark ? 'bg-gray-800 text-[#AF3535]' : 'bg-gray-100 text-[#AF3535]'}`}>
            <FaPlus size={16} />
          </div>
          <span className={`font-medium ${isActive('/create') ? 'text-[#AF3535]' : isDark ? 'text-gray-200' : 'text-gray-800'}`}>Create Capsule</span>
        </button>

        <hr className={`my-4 ${isDark ? 'border-gray-800' : 'border-gray-100'}`} />

        {/* Navigation Links */}
        <nav className="space-y-1">
          <Link 
            to="/homepage" 
            className={`flex items-center p-3.5 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'} ${isActive('/homepage') ? 'bg-[#AF3535]/10 border border-[#AF3535]/30' : ''}`}
          >
            <div className={`p-2 rounded-lg mr-3 ${isActive('/homepage') ? 'bg-[#AF3535] text-white' : isDark ? 'bg-gray-800 text-[#AF3535]' : 'bg-gray-100 text-[#AF3535]'}`}>
              <FaHome size={16} />
            </div>
            <span className={`font-medium ${isActive('/homepage') ? 'text-[#AF3535]' : isDark ? 'text-gray-200' : 'text-gray-800'}`}>Dashboard</span>
          </Link>

          <Link 
            to="/capsules" 
            className={`flex items-center p-3.5 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'} ${isActive('/capsules') ? 'bg-[#AF3535]/10 border border-[#AF3535]/30' : ''}`}
          >
            <div className={`p-2 rounded-lg mr-3 ${isActive('/capsules') ? 'bg-[#AF3535] text-white' : isDark ? 'bg-gray-800 text-[#AF3535]' : 'bg-gray-100 text-[#AF3535]'}`}>
              <FaStar size={16} />
            </div>
            <span className={`font-medium ${isActive('/capsules') ? 'text-[#AF3535]' : isDark ? 'text-gray-200' : 'text-gray-800'}`}>My Capsules</span>
          </Link>

          <Link 
            to="/archived_capsules" 
            className={`flex items-center p-3.5 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'} ${isActive('/archived_capsules') ? 'bg-[#AF3535]/10 border border-[#AF3535]/30' : ''}`}
          >
            <div className={`p-2 rounded-lg mr-3 ${isActive('/archived_capsules') ? 'bg-[#AF3535] text-white' : isDark ? 'bg-gray-800 text-[#AF3535]' : 'bg-gray-100 text-[#AF3535]'}`}>
              <FaShareAlt size={16} />
            </div>
            <span className={`font-medium ${isActive('/archived_capsules') ? 'text-[#AF3535]' : isDark ? 'text-gray-200' : 'text-gray-800'}`}>Archived Capsules</span>
          </Link>

          <Link 
            to="/locked-capsules" 
            className={`flex items-center p-3.5 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'} ${isActive('/locked-capsules') ? 'bg-[#AF3535]/10 border border-[#AF3535]/30' : ''}`}
          >
            <div className={`p-2 rounded-lg mr-3 ${isActive('/locked-capsules') ? 'bg-[#AF3535] text-white' : isDark ? 'bg-gray-800 text-[#AF3535]' : 'bg-gray-100 text-[#AF3535]'}`}>
              <FaHourglassHalf size={16} />
            </div>
            <span className={`font-medium ${isActive('/locked-capsules') ? 'text-[#AF3535]' : isDark ? 'text-gray-200' : 'text-gray-800'}`}>Locked Capsules</span>
          </Link>
        </nav>
        
        <hr className={`my-6 ${isDark ? 'border-gray-800' : 'border-gray-100'}`} />

        {/* Friends Section */}
        {/* Friends Section */}
<div className="mt-6">
  <div className="flex justify-between items-center mb-3 px-2">
    <h4 className={`text-sm font-semibold ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>YOUR FRIENDS</h4>
    <Link 
      to="/friends" 
      className={`text-xs font-medium ${isDark ? 'text-[#AF3535] hover:text-[#d15e5e]' : 'text-[#AF3535] hover:text-[#8a2a2a]'} transition-colors`}
    >
      View all
    </Link>
  </div>
  
  {loadingFriends ? (
    <div className={`p-4 rounded-lg ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
      <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'} text-center`}>Loading friends...</p>
    </div>
  ) : friends.length > 0 ? (
    <div className="space-y-2">
      {friends.map(friend => (
        <Link 
          key={friend.id} 
          to={`/profile/${friend.id}`}
          className={`flex items-center p-3 rounded-lg transition-all ${isDark ? 'hover:bg-gray-800' : 'hover:bg-gray-50'}`}
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
            <p className={`font-medium ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>{friend.username}</p>
          </div>
        </Link>
      ))}
    </div>
  ) : (
    <div className={`p-4 rounded-lg text-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
      <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>No friends added yet</p>
    </div>
  )}
</div>
      </aside>

      {/* First Modal - Confirmation */}
      {isFirstModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 backdrop-blur-sm">
          <div className={`rounded-xl p-8 w-full max-w-md ${isDark ? 'bg-gray-800 text-gray-100' : 'bg-white text-gray-800'} shadow-xl`}>
            <div className="flex items-center mb-6">
              <div className="p-3 rounded-lg mr-4 bg-[#AF3535]/10">
                <FaPlus className="text-[#AF3535]" size={20} />
              </div>
              <h2 className="text-xl font-semibold">Create New Capsule</h2>
            </div>
            <p className={`mb-8 ${isDark ? 'text-gray-300' : 'text-gray-600'}`}>
              Start preserving your memories by creating a new time capsule.
            </p>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setIsFirstModalOpen(false)}
                className={`px-5 py-2.5 rounded-lg font-medium ${isDark ? 'bg-gray-700 text-gray-200 hover:bg-gray-600' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
              >
                Cancel
              </button>
              <button
                onClick={handleFirstModalConfirm}
                className="px-5 py-2.5 bg-[#AF3535] text-white rounded-lg font-medium hover:bg-[#c04a4a] transition-colors"
              >
                Continue
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Second Modal - Title and Description */}
      {isSecondModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 backdrop-blur-sm">
          <div className={`rounded-xl p-8 w-full max-w-md ${isDark ? 'bg-gray-800 text-gray-100' : 'bg-white text-gray-800'} shadow-xl`}>
            <div className="flex items-center mb-6">
              <div className="p-3 rounded-lg mr-4 bg-[#AF3535]/10">
                <FaPlus className="text-[#AF3535]" size={20} />
              </div>
              <h2 className="text-xl font-semibold">Capsule Details</h2>
            </div>
            
            <div className="space-y-6 mb-8">
              <div>
                <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>Title</label>
                <input
                  type="text"
                  className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#AF3535] ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'border-gray-300'}`}
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="e.g., Graduation 2023"
                />
              </div>
              
              <div>
                <label className={`block text-sm font-medium mb-2 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>Description</label>
                <textarea
                  className={`w-full p-3 border rounded-lg focus:outline-none focus:ring-2 focus:ring-[#AF3535] ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'border-gray-300'}`}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe what this capsule will contain..."
                  rows={4}
                />
              </div>
            </div>
            
            {error && (
              <div className={`p-3 mb-6 rounded-lg bg-[#AF3535]/10 text-[#AF3535] text-sm`}>
                {error}
              </div>
            )}
            
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setIsSecondModalOpen(false)}
                className={`px-5 py-2.5 rounded-lg font-medium ${isDark ? 'bg-gray-700 text-gray-200 hover:bg-gray-600' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
              >
                Cancel
              </button>
              <button
                onClick={handleSecondModalConfirm}
                disabled={loading}
                className="px-5 py-2.5 bg-[#AF3535] text-white rounded-lg font-medium hover:bg-[#c04a4a] transition-colors disabled:opacity-70 disabled:cursor-not-allowed flex items-center"
              >
                {loading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Creating...
                  </>
                ) : 'Create Capsule'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Sidebar;