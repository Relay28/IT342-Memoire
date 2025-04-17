import React, { useState, useRef, useEffect } from 'react';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { useNavigate } from "react-router-dom";
import { useAuth } from '../components/AuthProvider';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import ServiceReportCapsule from '../services/ServiceReportCapsule';

const Homepage = () => {
  const { isDark } = useThemeMode();
  const navigate = useNavigate();
  const [isReportDropdownOpen, setIsReportDropdownOpen] = useState(false);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const dropdownRef = useRef(null);
  const modalRef = useRef(null);
  
  const { user, logout, isAuthenticated } = useAuth();
  const reportCapsule = ServiceReportCapsule();

  // State for report form
  const [reportReason, setReportReason] = useState('');
  const [reportType, setReportType] = useState('POST');
  const [reportSuccess, setReportSuccess] = useState(false);

  // Check for authentication status
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  const userData = user || {
    username: "Guest",
    email: "",
    bio: "",
    profilePicture: ProfilePictureSample,
    fullName: "Guest User"
  };

  // Close dropdown and modal when clicking outside or pressing Escape
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsReportDropdownOpen(false);
      }
      if (modalRef.current && !modalRef.current.contains(event.target)) {
        setIsReportModalOpen(false);
      }
    };

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setIsReportDropdownOpen(false);
        setIsReportModalOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, []);

  const handleReportSubmit = async (e) => {
    e.preventDefault();
    try {
      await reportCapsule.createReport({
        reportedID: 1, // Replace with actual capsule ID
        itemType: reportType,
        status: 'PENDING',
        reason: reportReason
      });
      setReportSuccess(true);
      setIsReportModalOpen(false);
      setReportReason('');
      setTimeout(() => setReportSuccess(false), 3000);
    } catch (error) {
      console.error('Error submitting report:', error);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className={`min-h-screen flex justify-center items-center ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
        <div className={`animate-pulse text-lg ${isDark ? 'text-gray-300' : 'text-gray-600'}`}>Loading...</div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
      <div className="flex flex-col h-screen">
        <Header 
          userData={userData} 
          logout={logout} 
          isAuthenticated={isAuthenticated}
        />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar 
            user={userData} 
            isAuthenticated={isAuthenticated}
          />

          {/* Main Content with dark mode */}
          <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
            {reportSuccess && (
              <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-green-800 text-green-200' : 'bg-green-100 text-green-800'}`}>
                Report submitted successfully! Our team will review it shortly.
              </div>
            )}
            
            {reportCapsule.error && (
              <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-red-800 text-red-200' : 'bg-red-100 text-red-800'}`}>
                {reportCapsule.error}
                <button 
                  onClick={reportCapsule.clearError}
                  className="ml-2 font-bold"
                >
                  ×
                </button>
              </div>
            )}

            <div className={`max-w-2xl mx-auto rounded-lg shadow-md overflow-hidden ${isDark ? 'bg-gray-700 text-white' : 'bg-white text-gray-900'}`}>
              {/* Ellipsis Button and Dropdown */}
              <div className="relative">
                <button 
                  className={`absolute top-4 right-4 p-2 rounded-full ${isDark ? 'hover:bg-gray-600 text-gray-300 hover:text-[#AF3535]' : 'hover:bg-[#AF3535]/10 text-gray-500 hover:text-[#AF3535]'} transition-colors`}
                  onClick={() => setIsReportDropdownOpen(!isReportDropdownOpen)}
                  aria-label="More options"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                  </svg>
                </button>

                {/* Dropdown Menu */}
                {isReportDropdownOpen && (
                  <div 
                    ref={dropdownRef}
                    className={`absolute right-0 mt-2 w-48 rounded-md shadow-lg z-10 ${isDark ? 'bg-gray-700' : 'bg-white'}`}
                  >
                    <div className="py-1">
                      <button
                        onClick={() => {
                          setIsReportModalOpen(true);
                          setIsReportDropdownOpen(false);
                        }}
                        className={`block w-full text-left px-4 py-2 text-sm ${isDark ? 'text-red-400 hover:bg-gray-600' : 'text-red-600 hover:bg-gray-100'}`}
                      >
                        Report This Capsule
                      </button>
                    </div>
                  </div>
                )}
              </div>

              <div className="p-6">
                <div className="flex items-center mb-4">
                  <img 
                    src={userData.profilePicture || ProfilePictureSample} 
                    alt="user" 
                    className="h-12 w-12 rounded-full mr-4" 
                  />
                  <div>
                    <strong className="block">{userData.fullName || userData.username}</strong>
                    <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                      Opened on {new Date().toLocaleDateString()}
                    </p>
                  </div>
                </div>

                <div className="mb-4">
                  <p className="mb-4">Its been a year...</p>
                  <hr className={`my-2 ${isDark ? 'border-gray-600' : 'border-gray-200'}`} />
                  <div className="my-4">
                    <div className="text-xl font-semibold">Memories of {new Date().getFullYear() - 1}</div>
                    <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                      Created on {new Date().toLocaleDateString()}
                    </p>
                  </div>
                  <p>Hi {userData.username}! Open this after a year to reminisce wompwomp</p>
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

        {/* Report Modal */}
        {isReportModalOpen && (
          <div className={`fixed inset-0 z-50 flex items-center justify-center p-4 ${isDark ? 'bg-black/70' : 'bg-black/50'}`}>
            <div 
              ref={modalRef}
              className={`w-full max-w-md rounded-lg shadow-xl ${isDark ? 'bg-gray-800' : 'bg-white'}`}
            >
              <div className="p-6">
                <div className="flex justify-between items-center mb-4">
                  <h2 className={`text-xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                    Report This Capsule
                  </h2>
                  <button
                    onClick={() => setIsReportModalOpen(false)}
                    className={`p-1 rounded-full ${isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-gray-100 text-gray-500'}`}
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
                
                <form onSubmit={handleReportSubmit}>
                  <div className="mb-4">
                    <label className={`block mb-2 text-sm font-medium ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                      What's the issue?
                    </label>
                    <select
                      className={`w-full p-2 rounded border ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'bg-white border-gray-300'}`}
                      value={reportType}
                      onChange={(e) => setReportType(e.target.value)}
                    >
                      <option value="POST">Inappropriate content</option>
                      <option value="USER">User violation</option>
                      <option value="COMMENT">Harassment or bullying</option>
                      <option value="OTHER">Other issue</option>
                    </select>
                  </div>
                  
                  <div className="mb-4">
                    <label className={`block mb-2 text-sm font-medium ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                      Additional details
                    </label>
                    <textarea
                      className={`w-full p-3 rounded border ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'bg-white border-gray-300'}`}
                      placeholder="Please describe the issue in detail..."
                      rows="4"
                      value={reportReason}
                      onChange={(e) => setReportReason(e.target.value)}
                      required
                    />
                  </div>
                  
                  <p className={`text-xs mb-4 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                    Your report will be reviewed by our moderation team. False reports may result in account restrictions.
                  </p>
                  
                  <div className="flex justify-end gap-3">
                    <button
                      type="button"
                      onClick={() => setIsReportModalOpen(false)}
                      className={`px-4 py-2 rounded ${isDark ? 'bg-gray-700 hover:bg-gray-600 text-white' : 'bg-gray-200 hover:bg-gray-300'}`}
                    >
                      Cancel
                    </button>
                    <button
                      type="submit"
                      className={`px-4 py-2 rounded bg-[#AF3535] hover:bg-[#AF3535]/90 text-white ${reportCapsule.loading ? 'opacity-70 cursor-not-allowed' : ''}`}
                      disabled={reportCapsule.loading || !reportReason.trim()}
                    >
                      {reportCapsule.loading ? (
                        <span className="flex items-center justify-center">
                          <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                          </svg>
                          Submitting...
                        </span>
                      ) : 'Submit Report'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Homepage;