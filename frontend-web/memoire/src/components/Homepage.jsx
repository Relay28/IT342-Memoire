import React, {useState, useRef, useEffect, useContext} from 'react';
import mmrlogo from '../assets/mmrlogo.png';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';
import { Link, useNavigate } from "react-router-dom";
import { PersonalInfoContext } from '../components/PersonalInfoContext'; // Adjust the path as needed

const Homepage = () => {
  const navigate = useNavigate();
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isReportOpen, setIsReportOpen] = useState(false);
  const reportRef = useRef(null);
const { personalInfo } = useContext(PersonalInfoContext);
  const userData = personalInfo || {
      username: "",
      email: "",
      bio: "",
      profilePicture: ProfilePictureSample
    };
  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (reportRef.current && !reportRef.current.contains(event.target)) {
        setIsReportOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);
  
  const toggleProfile = () => {
    setIsProfileOpen(!isProfileOpen);
  };
  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen">
        {/* Header */}
        <header className="flex items-center justify-between p-4 bg-white shadow-md">
          <button className="flex items-center space-x-2">
            <img src={mmrlogo} alt="Mémoire Logo" className="h-10 w-10" />
            <div className="text-2xl font-bold text-red-700">MÉMOIRE</div>
          </button>

          <div className="flex items-center px-4 py-2 bg-gray-100 rounded-full w-1/3">
            <FaSearch className="text-red-700 mr-2" />
            <input 
              type="text" 
              placeholder="Search here..." 
              className="bg-transparent border-none outline-none w-full"
            />
          </div>

          <div className="flex items-center space-x-4">
          <button className="p-2 rounded-full hover:bg-red-100">
            <FaMoon size={24} className="text-[#AF3535]" />
          </button>
          <button className="p-2 rounded-full hover:bg-red-100">
            <FaBell size={24} className="text-[#AF3535]" />
          </button>
          <div className="relative">
  <button 
    className="p-1 focus:outline-none transition-all duration-200 hover:ring-2 hover:ring-[#AF3535]/30 rounded-full"
    onClick={toggleProfile}
    aria-label="User menu"
  >
    <div className="relative">
      <img 
        src={ProfilePictureSample} 
        alt="User profile" 
        className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover hover:brightness-95 transition-all duration-200"
      />
      {isProfileOpen && (
        <div className="absolute inset-0 rounded-full bg-[#AF3535]/20 animate-pulse"></div>
      )}
    </div>
  </button>

  {/* Dropdown Menu */}
  {isProfileOpen && (
    <div className="absolute right-0 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-lg bg-white shadow-lg ring-1 ring-black/5 focus:outline-none z-50 animate-enter">
      <div className="px-1 py-1">
        <div className="flex items-center gap-3 px-4 py-3">
          <img 
            src={userData.profilePicture || ProfilePictureSample} 
            alt="User" 
            className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover"
          />
          <div>
            <p className="text-sm font-medium text-gray-900">{userData.username || "Loading..."}</p>
            <p className="text-xs text-gray-500">{userData.email || "loading@example.com"}</p>
          </div>
        </div>
      </div>
      <div className="px-1 py-1">
        <button
          className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
          onClick={() => {
            // Handle profile navigation
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
          onClick={() => {
            // Handle logout
            setIsProfileOpen(false);
          }}
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

        <div className="flex flex-1 h-screen overflow-hidden">
          {/* Sidebar */}
          <aside className="w-64 p-4 shadow-md h-[calc(100vh)]">
            <Link 
            to="/create" 
            className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
            >
            <FaPlus className="text-red-700 mr-3" size={20} />
            <span>Create your capsule</span>
            </Link>

            <hr className="my-2" />

           <Link 
            to="/homepage" 
            className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
            >
            <FaHome className="text-red-700 mr-3" size={20} />
            <span>Home</span>
            </Link>

            <Link 
            to="/capsules" 
            className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
            >
            <FaStar className="text-red-700 mr-3" size={20} />
            <span>Capsules</span>
            </Link>

            <Link 
            to="/archived_capsules" 
            className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
            >
            <FaShareAlt className="text-red-700 mr-3" size={20} />
            <span>Archived Capsules</span>
            </Link>

            <hr className="my-2" />

            <div className="flex justify-between items-center p-3">
              <h4 className="text-lg font-semibold">Friends</h4>
              <Link 
                to="/friends" 
                className="text-sm text-blue-600 hover:text-blue-800 hover:underline"
              >
                See more...
              </Link>
            </div>
           
          </aside>

          {/* Main Capsule Content */}
          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-md overflow-hidden">
              {/* Ellipsis Button and Dropdown */}
              <div className="relative">
              <button 
                className="absolute top-4 right-4 p-2 rounded-full hover:bg-[#AF3535]/10 text-gray-500 hover:text-[#AF3535] transition-colors"
                onClick={() => setIsReportOpen(!isReportOpen)}
                aria-label="More options"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                </svg>
              </button>

              {/* Report Dropdown Menu */}
              {isReportOpen && (
                <div 
                  className="absolute right-0 top-12 w-48 origin-top-right rounded-lg bg-white shadow-lg ring-1 ring-black/5 focus:outline-none z-50 animate-enter"
                  ref={reportRef}
                >
                  <div className="px-1 py-1">
                    <button
                      className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
                      onClick={() => {
                        // Handle report action
                        setIsReportOpen(false);
                      }}
                    >
                      <svg className="h-5 w-5 text-gray-400 group-hover:text-[#AF3535]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                      </svg>
                      Report
                    </button>
                  </div>
                </div>
              )}
           
                <div className="absolute right-4 top-12 bg-white shadow-lg rounded-md p-2 hidden">
                  <button className="w-full text-left px-4 py-2 hover:bg-gray-100">Report</button>
                </div>
              </div>

              <div className="p-6">
                <div className="flex items-center mb-4">
                  <img src={ProfilePictureSample} alt="user" className="h-12 w-12 rounded-full mr-4" />
                  <div>
                    <strong className="block">Georgia Santos</strong>
                    <p className="text-sm text-gray-500">Opened on February 24, 2025</p>
                  </div>
                </div>

                <div className="mb-4">
                  <p className="mb-4">Its been a year...</p>
                  <hr className="my-2" />
                  <div className="my-4">
                    <div className="text-xl font-semibold">Memories of 2024</div>
                    <div className="text-sm text-gray-500">Created on February 24, 2025</div>
                  </div>
                  <p>Hi Self! Open this after a year to reminisce wompwomp</p>
                </div>

                <div className="grid grid-cols-3 gap-2 mt-4">
                  <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                  <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                  <img src={bgmemoire} alt="memory" className="h-32 w-full object-cover rounded" />
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Homepage;