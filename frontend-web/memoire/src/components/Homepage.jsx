import React, {useState, useRef, useEffect, useContext} from 'react';
import mmrlogo from '../assets/mmrlogo.png';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';
import { Link, useNavigate } from "react-router-dom";
import { PersonalInfoContext } from '../components/PersonalInfoContext'; // Adjust the path as needed
import Header from '../components/Header'; // Import the Header component
import Sidebar from '../components/Sidebar'; // Import the Sidebar component

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
        <Header userData={userData} />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          {/* Use the Sidebar component */}
          <Sidebar />

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