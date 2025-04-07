// components/Header.jsx
import React, { useState,useEffect, useContext } from 'react';
import { FaSearch, FaMoon, FaBell } from 'react-icons/fa';
import { Link, useNavigate } from 'react-router-dom';
import mmrlogo from '../assets/mmrlogo.png';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { profileService } from '../components/ProfileFunctionalities';
import { PersonalInfoContext } from './PersonalInfoContext';
const Header = ({ userData }) => {
  const { personalInfo, setPersonalInfo } = useContext(PersonalInfoContext);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [profilePicture, setProfilePicture] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const navigate = useNavigate();

  console.log(profilePicture)
  // Fetch profile picture on component mount
  useEffect(() => {
    const fetchProfilePicture = async () => {
      try {
        setIsLoading(true);
        const picture = await profileService.getProfilePicture();
        
        setProfilePicture(picture);
      } catch (error) {
        console.error('Error fetching profile picture:', error);
        // Keep the default placeholder on error
      } finally {
        setIsLoading(false);
      }
    };

    // Only fetch if the user is logged in (check for auth token)
    if (sessionStorage.getItem('authToken')) {
      fetchProfilePicture();
    } else {
      setIsLoading(false);
    }
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery)}`);
    }
  };

  const handleLogout = async () => {
    try {
      await axios.post('/api/auth/logout', {}, {
        headers: {
          'Authorization': `Bearer ${sessionStorage.getItem('authToken')}`
        }
      });
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear client-side tokens and state
      setPersonalInfo(null),
      sessionStorage.removeItem('authToken');
      // Clear profile picture cache
      setProfilePicture(null);
      // Redirect to login
      navigate('/login');
    }
  };
  return (
    <header className="flex items-center justify-between p-4 bg-white shadow-md">
      <Link to="/homepage" className="flex items-center space-x-2">
        <img src={mmrlogo} alt="Mémoire Logo" className="h-10 w-10" />
        <div className="text-2xl font-bold text-red-700">MÉMOIRE</div>
      </Link>

      <form onSubmit={handleSearch} className="flex items-center px-4 py-2 bg-gray-100 rounded-full w-1/3">
        <FaSearch className="text-red-700 mr-2" />
        <input 
          type="text" 
          placeholder="Search by username or name..." 
          className="bg-transparent border-none outline-none w-full"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </form>

      <div className="flex items-center space-x-4">
        <button className="p-2 rounded-full hover:bg-red-100">
          <FaMoon size={24} className="text-[#AF3535]" />
        </button>
        <button className="p-2 rounded-full hover:bg-red-100">
          <FaBell size={24} className="text-[#AF3535]" />
        </button>
        
        {/* Profile Dropdown (integrated) */}
        <div className="relative">
          <button 
            className="p-1 focus:outline-none transition-all duration-200 hover:ring-2 hover:ring-[#AF3535]/30 rounded-full"
            onClick={() => setIsProfileOpen(!isProfileOpen)}
            aria-label="User menu"
          >
            <div className="relative">
              {isLoading ? (
                <div className="h-10 w-10 rounded-full border-2 border-[#AF3535] bg-gray-200 animate-pulse"></div>
              ) : (
                <img 
                  src={profilePicture || userData?.profilePicture || ProfilePictureSample} 
                  alt="User profile" 
                  className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover hover:brightness-95 transition-all duration-200"
                  onError={(e) => {
                    e.target.onerror = null; // Prevent infinite loop
                    e.target.src = ProfilePictureSample; // Fallback image
                  }}
                />
              )}
              {isProfileOpen && (
                <div className="absolute inset-0 rounded-full bg-[#AF3535]/20 animate-pulse"></div>
              )}
            </div>
          </button>

          {isProfileOpen && (
            <div className="absolute right-0 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-lg bg-white shadow-lg ring-1 ring-black/5 focus:outline-none z-50 animate-enter">
              <div className="px-1 py-1">
                <div className="flex items-center gap-3 px-4 py-3">
                  <img 
                    src={profilePicture || userData?.profilePicture || ProfilePictureSample} 
                    alt="User" 
                    className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover"
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = ProfilePictureSample;
                    }}
                  />
                  <div>
                    <p className="text-sm font-medium text-gray-900">{userData?.username || "Loading..."}</p>
                    <p className="text-xs text-gray-500">{userData?.email || "loading@example.com"}</p>
                  </div>
                </div>
              </div>
              <div className="px-1 py-1">
                <button
                  className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
                  onClick={() => {
                    setIsProfileOpen(false);
                    navigate('/profile');
                  }}
                >
                  <svg className="h-5 w-5 text-gray-400 group-hover:text-[#AF3535]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span className="text-sm">Profile</span>
                </button>
              </div>
              <div className="px-1 py-1">
                <button
                  className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
                  onClick={handleLogout}
                >
                  <svg className="h-5 w-5 text-gray-400 group-hover:text-[#AF3535]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
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

export  default Header;