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

  return (
    <header className={`flex items-center justify-between p-4 ${
      mode === 'dark' 
        ? 'bg-gray-900 shadow-lg shadow-gray-950/10' 
        : 'bg-white shadow-md shadow-gray-200/50'
    } relative`}>
      {connectionStatusIndicator()}
      
      <Link to="/homepage" className="flex items-center space-x-2">
        <img src={mode === 'dark' ? logolight : mmrlogo}  alt="Mémoire Logo" className="h-10 w-10" />
        <div className="text-2xl font-bold text-[#AF3535] dark:text-red-500">MÉMOIRE</div>
      </Link>

      <form onSubmit={handleSearch} className="flex items-center px-4 py-2 bg-gray-100 dark:bg-gray-700 rounded-full w-1/3">
  <FaSearch className="text-[#AF3535] dark:text-red-500 mr-2" />
  <input 
    type="text" 
    placeholder="Search by username or name..." 
    className="bg-transparent border-none outline-none w-full text-gray-800 dark:text-gray-200 dark:placeholder-gray-400"
    value={searchQuery}
    onChange={(e) => setSearchQuery(e.target.value)}
  />
</form>

      <div className="flex items-center space-x-4">
        <button 
          className="p-2 rounded-full hover:bg-red-100 dark:hover:bg-red-900/30" 
          onClick={toggleTheme}
          aria-label="Toggle dark mode"
        >
          {mode === 'light' ? (
            <FaMoon size={24} className="text-[#AF3535] dark:text-red-400" />
          ) : (
            <FaSun size={24} className="text-yellow-400" />
          )}
        </button>
        
        {/* Notifications dropdown */}
        <div className="relative" ref={notificationRef}>
          <button 
            className="p-2 rounded-full hover:bg-red-100 dark:hover:bg-red-900/30 relative"
            onClick={() => {
              if (isNotificationsOpen) {
                setIsNotificationsOpen(false);
              } else {
                handleOpenNotifications();
              }
            }}
            aria-label="Notifications"
          >
            <FaBell size={24} className="text-[#AF3535] dark:text-red-400" />
            {unreadCount > 0 && (
              <span className="absolute top-0 right-0 bg-red-600 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </button>
          
          {isNotificationsOpen && (
            <div className="absolute right-0 mt-2 w-80 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-50 max-h-96 overflow-y-auto">
              <div className="p-3 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
                <h3 className="font-semibold text-gray-800 dark:text-gray-200">Notifications</h3>
                {unreadCount > 0 && (
                  <button 
                    onClick={(e) => {
                      e.stopPropagation();
                      if (unreadCount > 0) {
                        markAllAsRead();
                      }
                    }}
                    className="text-xs text-blue-500 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300"
                  >
                    Mark all as read
                  </button>
                )}
              </div>
              
              {!isAuthenticated ? (
                <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                  Please log in to view notifications
                </div>
              ) : showNotificationsLoading ? (
                <div className="p-6 flex justify-center">
                  <div className="w-6 h-6 border-2 border-[#AF3535] dark:border-red-500 border-t-transparent rounded-full animate-spin"></div>
                </div>
              ) : showConnectingState ? (
                <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                  Connecting to notification service...
                </div>
              ) : showEmptyState ? (
                <div className="p-4 text-center text-gray-500 dark:text-gray-400">
                  No notifications
                </div>
              ) : hasNotifications ? (
                <ul>
                  {memoizedNotifications.map(notification => (
                    <li 
                      key={notification.id} 
                      className={`p-3 border-b border-gray-100 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700 cursor-pointer ${!notification.read ? 'bg-blue-50 dark:bg-blue-900/30' : ''}`}
                      onClick={() => {
                        if (!notification.read) {
                          markAsRead(notification.id);
                        }
                        
                        // Handle notification navigation based on type
                        if (notification.linkUrl) {
                          navigate(notification.linkUrl);
                          setIsNotificationsOpen(false);
                        }
                      }}
                    >
                      <div className="flex justify-between items-start">
                        <div className="flex-1">
                          <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{notification.text}</p>
                          <p className="text-xs text-gray-500 dark:text-gray-400">
                            {formatNotificationTime(notification.createdAt)}
                          </p>
                        </div>
                        {!notification.read && (
                          <span className="inline-block h-2 w-2 rounded-full bg-blue-500 flex-shrink-0 ml-2 mt-1"></span>
                        )}
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <div className="p-4 text-center text-red-500 dark:text-red-400">
                  Error loading notifications
                </div>
              )}
            </div>
          )}
        </div>
        
        {/* Profile Dropdown */}
        <div className="relative">
          <button 
            className="p-1 focus:outline-none transition-all duration-200 hover:ring-2 hover:ring-[#AF3535]/30 dark:hover:ring-red-500/30 rounded-full"
            onClick={() => setIsProfileOpen(!isProfileOpen)}
            aria-label="User menu"
          >
            <div className="relative">
              {isLoading ? (
                <div className="h-10 w-10 rounded-full border-2 border-[#AF3535] dark:border-red-500 bg-gray-200 dark:bg-gray-600 animate-pulse"></div>
              ) : (
                <img 
                  src={profilePicture || user?.profilePicture || ProfilePictureSample} 
                  alt="User profile" 
                  className="h-10 w-10 rounded-full border-2 border-[#AF3535] dark:border-red-500 object-cover hover:brightness-95 transition-all duration-200"
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.src = ProfilePictureSample;
                  }}
                />
              )}
              {isProfileOpen && (
                <div className="absolute inset-0 rounded-full bg-[#AF3535]/20 dark:bg-red-500/30 animate-pulse"></div>
              )}
            </div>
          </button>

          {isProfileOpen && (
            <div className="absolute right-0 mt-2 w-56 origin-top-right divide-y divide-gray-100 dark:divide-gray-700 rounded-lg bg-white dark:bg-gray-800 shadow-lg ring-1 ring-black/5 dark:ring-white/10 focus:outline-none z-50 animate-enter">
              <div className="px-1 py-1">
                <div className="flex items-center gap-3 px-4 py-3">
                  <img 
                    src={profilePicture || user?.profilePicture || ProfilePictureSample} 
                    alt="User" 
                    className="h-10 w-10 rounded-full border-2 border-[#AF3535] dark:border-red-500 object-cover"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = ProfilePictureSample;
                    }}
                  />
                  <div>
                    <p className="text-sm font-medium text-gray-900 dark:text-gray-100">{user?.username || "Loading..."}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">{user?.email || "loading@example.com"}</p>
                  </div>
                </div>
              </div>
              <div className="px-1 py-1">
                <button
                  className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 dark:text-gray-100 hover:bg-[#AF3535]/10 dark:hover:bg-red-900/30 hover:text-[#AF3535] dark:hover:text-red-400 transition-colors"
                  onClick={() => {
                    setIsProfileOpen(false);
                    navigate('/profile');
                  }}
                >
                  <svg className="h-5 w-5 text-gray-400 dark:text-gray-500 group-hover:text-[#AF3535] dark:group-hover:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span className="text-sm">Profile</span>
                </button>
              </div>
              <div className="px-1 py-1">
                <button
                  className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 dark:text-gray-100 hover:bg-[#AF3535]/10 dark:hover:bg-red-900/30 hover:text-[#AF3535] dark:hover:text-red-400 transition-colors"
                  onClick={handleLogout}
                >
                  <svg className="h-5 w-5 text-gray-400 dark:text-gray-500 group-hover:text-[#AF3535] dark:group-hover:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
                  </svg>
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