// components/Header.jsx
import React, { useState, useEffect, useRef, useMemo } from 'react';
import { FaSearch, FaMoon, FaBell, FaSun } from 'react-icons/fa';
import { Link, useNavigate } from 'react-router-dom';
import mmrlogo from '../assets/mmrlogo.png';
import logolight from '../assets/logolight.png';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { profileService } from '../components/ProfileFunctionalities';
import { useAuth } from './AuthProvider'; // Import the useAuth hook
import { useNotifications } from '../context/NotificationContext'; // Import the notifications hook
import { useThemeMode } from '../context/ThemeContext';
import apiService from './Profile/apiService';
const Header = () => {
  // Use the auth context
  const { 
    user, 
    authToken,
    isAuthenticated,
    logout
  } = useAuth();

  // Use the notification context
  const {
    notifications,
    unreadCount,
    markAsRead,
    markAllAsRead,
    fetchNotifications,
    fetchUnreadCount,
    isConnected
  } = useNotifications();

  const { mode, toggleTheme } = useThemeMode(); // 👈 Use toggleTheme from context
  
  const memoizedNotifications = useMemo(() => notifications, [notifications]);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [profilePicture, setProfilePicture] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isNotificationsLoading, setIsNotificationsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isNotificationsOpen, setIsNotificationsOpen] = useState(false);
  const notificationRef = useRef(null);
  const navigate = useNavigate();

  // Handle clicks outside notification dropdown
  useEffect(() => {
    function handleClickOutside(event) {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setIsNotificationsOpen(false);
      }
    }
    
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // Fetch initial notifications data
  useEffect(() => {
    // Only fetch if the user is authenticated
    if (isAuthenticated && authToken) {
      // Fetch profile picture
      const fetchProfilePicture = async () => {
        try {
          setIsLoading(true);
          const picture = await profileService.getProfilePicture();
          setProfilePicture(picture);
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

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  const handleLogout = async () => {
    try {
      await logout(); // Use the logout function from auth context
      setProfilePicture(null);
      navigate('/login');
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  const handleOpenNotifications = async () => {
    setIsNotificationsOpen(true);
    if (isAuthenticated) {
      setIsNotificationsLoading(true);
      try {
        await fetchNotifications();
      } catch (error) {
        console.error('Error fetching notifications:', error);
      } finally {
        setIsNotificationsLoading(false);
      }
    }
  };

  const formatNotificationTime = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) {
      return 'Just now';
    } else if (diffMins < 60) {
      return `${diffMins} minute${diffMins === 1 ? '' : 's'} ago`;
    } else if (diffHours < 24) {
      return `${diffHours} hour${diffHours === 1 ? '' : 's'} ago`;
    } else if (diffDays < 7) {
      return `${diffDays} day${diffDays === 1 ? '' : 's'} ago`;
    } else {
      return date.toLocaleDateString();
    }
  };

  console.log("MODE CHECK "+mode)
  // Show connection indicator for debugging - you can remove this in production
  const connectionStatusIndicator = () => {
    if (!isAuthenticated) return null;
    
    return (
      <div className="hidden absolute bottom-2 right-2 text-xs px-2 py-1 rounded">
        {isConnected ? 
          <span className="text-green-700 dark:text-green-500">●</span> : 
          <span className="text-red-700 dark:text-red-500">●</span>}
      </div>
    );
  };

  // Determine loading state for notifications
  const showNotificationsLoading = isAuthenticated && isNotificationsLoading;
  
  // Determine if we're connecting but not yet connected
  const showConnectingState = isAuthenticated && !isConnected && !isNotificationsLoading;
  
  // Determine if we have notifications to show
  const hasNotifications = isAuthenticated && isConnected && memoizedNotifications.length > 0;
  
  // Determine if we should show the empty state
  const showEmptyState = isAuthenticated && isConnected && memoizedNotifications.length === 0 && !isNotificationsLoading;

  // const handleNotificationClick = (notification) => async () => {
  //   if (!notification.read) {
  //     markAsRead(notification.id);
  //   }
  
  //   if (notification.type === 'FRIEND_REQUEST') {
  //     try {
  //       // Get senderId from notification or extract from text
  //       const senderId = notification.senderId;
  //       const username = notification.text.split(' ')[0]; // "Username sent you..."
        
  //       if (senderId) {
  //         navigate(`/profile/${senderId}`, {
  //           state: { 
  //             incomingFriendRequest: true,
  //             senderUsername: username
  //           }
  //         });
  //       } else {
  //         // Fallback to your existing profile view flow
  //         navigate(`/profile/?username=${username}`, {
  //           state: { fromNotification: true }
  //         });
  //       }
  //     } catch (error) {
  //       console.error('Error handling notification:', error);
  //       navigate('/profile'); // Fallback to profiles list
  //     }
  //   }
  //   setIsNotificationsOpen(false);
  // };
  const handleNotificationClick = (notification) => async () => {
    if (!notification.read) {
      markAsRead(notification.id);
    }
  
    setIsNotificationsOpen(false);
  
    if (notification.type === 'FRIEND_REQUEST') {
      try {
        // Extract username from notification text
        const username = extractUsernameFromNotification(notification.text);
        
        if (!username) {
          navigate('/profile');
          return;
        }
  
        // Use search endpoint as fallback
        const searchResponse = await apiService.get(`/api/profiles/search?query=${encodeURIComponent(username)}`);
        
        if (searchResponse.data?.results?.length > 0) {
          // Take the first matching user
          const user = searchResponse.data.results[0];
          navigate(`/profile/${user.userId}`, {
            state: { 
              profile: user,
              incomingFriendRequest: true
            }
          });
        } else {
          // Fallback to search by username
          navigate(`/profile/?username=${encodeURIComponent(username)}`, {
            state: { fromNotification: true }
          });
        }
      } catch (error) {
        console.error('Error handling notification:', error);
        navigate('/profile');
      }
    }
  };
  
  // Helper function to extract username from different notification formats
  const extractUsernameFromNotification = (text) => {
    if (!text) return null;
    
    // Try different patterns
    const patterns = [
      /^(\S+)\s+sent you/,      // "Username sent you"
      /^\[(\S+)\]/,             // "[Username]"
      /from (\S+)/i,            // "from Username"
      /^(\S+)\s+wants to/       // "Username wants to"
    ];
    
    for (const pattern of patterns) {
      const match = text.match(pattern);
      if (match && match[1]) return match[1];
    }
    
    // Fallback: first word
    return text.split(' ')[0];
  };
  
  return (
    <header className={`sticky top-0 z-40 flex items-center justify-between p-4 ${mode === 'dark' ? 'bg-gray-900 border-b border-gray-800' : 'bg-white border-b border-gray-200'}`}>
      <div className="flex items-center space-x-100">
        {/* Logo Section - Side by side with italic subtitle */}
        <Link to="/homepage" className="flex items-center space-x-3 hover:opacity-90 transition-opacity">
  {/* Logo Image */}
  <img 
    src={mode === 'dark' ? logolight : mmrlogo} 
    alt="Mémoire Logo" 
    className="h-12 w-12 flex-shrink-0" 
  />
  
  {/* Text Container */}
  <div className="flex flex-col space-y-1">
    {/* Main Title */}
    <div className="flex items-baseline space-x-3">
      <h1 className="text-2xl font-bold text-[#AF3535] dark:text-red-400 tracking-tight">
        MÉMOIRE
      </h1>
      
      {/* Tagline - now properly aligned and styled */}
      <span className={`text-xs italic ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'} transform translate-y-0.5`}>
        Preserve moments, influence memories
      </span>
    </div>
    
    {/* Optional subtle decorative element */}
    
  </div>
</Link>
        {/* Search Bar - Styled to match sidebar */}
        <form onSubmit={handleSearch} className={`flex items-center px-4 py-2 rounded-lg ${mode === 'dark' ? 'bg-gray-800' : 'bg-gray-100'} w-96`}>
          <FaSearch className={`mr-3 ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'}`} />
          <input 
            type="text" 
            placeholder="Search by username or name..." 
            className={`bg-transparent w-full focus:outline-none ${mode === 'dark' ? 'text-gray-200 placeholder-gray-400' : 'text-gray-800 placeholder-gray-500'}`}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </form>
      </div>

      <div className="flex items-center space-x-4">
        {/* Theme Toggle - Styled to match sidebar */}
        <button 
          onClick={toggleTheme}
          className={`p-2 rounded-lg ${mode === 'dark' ? 'hover:bg-gray-800 text-gray-300' : 'hover:bg-gray-100 text-gray-600'}`}
          aria-label="Toggle theme"
        >
          {mode === 'light' ? (
            <FaMoon size={18} className="text-[#AF3535]" />
          ) : (
            <FaSun size={18} className="text-yellow-400" />
          )}
        </button>
        
        {/* Notifications - Redesigned dropdown */}
        <div className="relative" ref={notificationRef}>
          <button 
            onClick={() => isNotificationsOpen ? setIsNotificationsOpen(false) : handleOpenNotifications()}
            className={`p-2 rounded-lg relative ${mode === 'dark' ? 'hover:bg-gray-800 text-gray-300' : 'hover:bg-gray-100 text-gray-600'}`}
            aria-label="Notifications"
          >
            <FaBell size={18} />
            {unreadCount > 0 && (
              <span className="absolute top-0 right-0 bg-[#AF3535] text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>
          
          {isNotificationsOpen && (
            <div className={`absolute right-0 mt-2 w-80 rounded-lg shadow-xl border ${mode === 'dark' ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'}`}>
              <div className={`p-3 border-b ${mode === 'dark' ? 'border-gray-700' : 'border-gray-200'} flex justify-between items-center`}>
                <h3 className={`font-medium ${mode === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>Notifications</h3>
                {unreadCount > 0 && (
                  <button 
                    onClick={(e) => {
                      e.stopPropagation();
                      markAllAsRead();
                    }}
                    className={`text-xs ${mode === 'dark' ? 'text-[#d15e5e]' : 'text-[#AF3535]'}`}
                  >
                    Mark all as read
                  </button>
                )}
              </div>
              
              {/* Keep all notification states exactly as is */}
              {!isAuthenticated ? (
                <div className={`p-4 text-center ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>
                  Please log in to view notifications
                </div>
              ) : showNotificationsLoading ? (
                <div className="p-6 flex justify-center">
                  <div className={`w-6 h-6 border-2 ${mode === 'dark' ? 'border-red-500' : 'border-[#AF3535]'} border-t-transparent rounded-full animate-spin`}></div>
                </div>
              ) : showConnectingState ? (
                <div className={`p-4 text-center ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>
                  Connecting...
                </div>
              ) : showEmptyState ? (
                <div className={`p-4 text-center ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>
                  No notifications
                </div>
              ) : hasNotifications ? (
                <ul className="max-h-96 overflow-y-auto">
                  {memoizedNotifications.map(notification => (
                    <li 
                      key={notification.id} 
                      className={`p-3 border-b ${mode === 'dark' ? 'border-gray-700 hover:bg-gray-700' : 'border-gray-200 hover:bg-gray-50'} ${!notification.read ? (mode === 'dark' ? 'bg-blue-900/20' : 'bg-blue-50') : ''}`}
                      onClick={handleNotificationClick(notification)}
                    >
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <p className={`text-sm ${mode === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>{notification.text}</p>
                          <p className={`text-xs mt-1 ${mode === 'dark' ? 'text-gray-500' : 'text-gray-400'}`}>
                            {formatNotificationTime(notification.createdAt)}
                          </p>
                        </div>
                        {!notification.read && (
                          <span className={`inline-block h-2 w-2 rounded-full ${mode === 'dark' ? 'bg-blue-400' : 'bg-blue-500'}`}></span>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className={`p-4 text-center ${mode === 'dark' ? 'text-red-400' : 'text-red-500'}`}>
                  Error loading notifications
                </div>
              )}
            </div>
          )}
        </div>
        
        {/* Profile Dropdown - Redesigned to match sidebar */}
        <div className="relative">
          <button 
            onClick={() => setIsProfileOpen(!isProfileOpen)}
            className="flex items-center space-x-2 focus:outline-none"
          >
            {isLoading ? (
              <div className={`h-9 w-9 rounded-full border-2 ${mode === 'dark' ? 'border-gray-700 bg-gray-800' : 'border-gray-200 bg-gray-100'} animate-pulse`}></div>
            ) : (
              <img 
                src={profilePicture || user?.profilePicture || ProfilePictureSample} 
                alt="Profile" 
                className="h-9 w-9 rounded-full border-2 border-[#AF3535] object-cover"
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.src = ProfilePictureSample;
                }}
              />
            )}
          </button>

          {isProfileOpen && (
            <div className={`absolute right-0 mt-2 w-56 rounded-lg shadow-xl border ${mode === 'dark' ? 'bg-gray-800 border-gray-700' : 'bg-white border-gray-200'}`}>
              <div className="p-4 border-b border-gray-200 dark:border-gray-700">
                <div className="flex items-center space-x-3">
                  <img 
                    src={profilePicture || user?.profilePicture || ProfilePictureSample} 
                    alt="Profile" 
                    className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover"
                  />
                  <div>
                    <p className={`font-medium ${mode === 'dark' ? 'text-gray-200' : 'text-gray-800'}`}>{user?.username || "User"}</p>
                    <p className={`text-xs ${mode === 'dark' ? 'text-gray-400' : 'text-gray-500'}`}>{user?.email || "user@example.com"}</p>
                  </div>
                </div>
              </div>
              <div className="p-1">
                <button
                  onClick={() => {
                    setIsProfileOpen(false);
                    navigate('/profile');
                  }}
                  className={`flex items-center w-full px-4 py-2 text-sm rounded-md ${mode === 'dark' ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'}`}
                >
                  <FaUser className="mr-3" size={14} />
                  Profile
                </button>
                <button
                  onClick={handleLogout}
                  className={`flex items-center w-full px-4 py-2 text-sm rounded-md ${mode === 'dark' ? 'text-gray-300 hover:bg-gray-700' : 'text-gray-700 hover:bg-gray-100'}`}
                >
                  <FaSignOutAlt className="mr-3" size={14} />
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;