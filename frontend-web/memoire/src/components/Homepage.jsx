import React, { useState, useEffect, useRef, useContext, useCallback } from 'react';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { useNavigate } from "react-router-dom";
import { useAuth } from '../components/AuthProvider';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useThemeMode } from '../context/ThemeContext';
import ServiceReportCapsule from '../services/ServiceReportCapsule';
import TimeCapsuleService from '../services/TimeCapsuleService';
import { useCapsuleContent } from '../context/CapsuleWebContextProvider';

const Homepage = () => {
  // Context and hooks
  const { isDark } = useThemeMode();
  const navigate = useNavigate();
  const { user, logout, isAuthenticated, authToken } = useAuth();
  const { fetchMediaContent } = useCapsuleContent();
  
  // Refs
  const dropdownRef = useRef(null);
  const modalRef = useRef(null);
  
  // State for capsules
  const [publishedCapsules, setPublishedCapsules] = useState([]);
  const [loadingCapsules, setLoadingCapsules] = useState(true);
  const [error, setError] = useState(null);
  const [mediaContent, setMediaContent] = useState({});
  
  // State for reporting
  const [isReportDropdownOpen, setIsReportDropdownOpen] = useState(false);
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [reportType, setReportType] = useState('TimeCapsule');
  const [reportSuccess, setReportSuccess] = useState(false);
  const [currentReportCapsuleId, setCurrentReportCapsuleId] = useState(null);
  
  // State for archiving
  const [isArchiving, setIsArchiving] = useState(false);
  const [archiveError, setArchiveError] = useState(null);
  const [archiveSuccess, setArchiveSuccess] = useState(false);
  
  // State for comments and reactions
  const [comments, setComments] = useState({});
  const [newComment, setNewComment] = useState({});
  const [commentLoading, setCommentLoading] = useState(false);
  
  // Services
  const reportCapsule = ServiceReportCapsule();

  // Authentication check
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  // Fetch published capsules with media
  const fetchCapsules = useCallback(async () => {
    if (!isAuthenticated || !authToken) return;
    
    try {
      setLoadingCapsules(true);
      setError(null);
      
      const capsules = await TimeCapsuleService.getPublishedTimeCapsules(authToken);
      setPublishedCapsules(capsules || []);
      
      // Initialize comments storage for each capsule
      const initialComments = {};
      capsules.forEach(capsule => {
        initialComments[capsule.id] = capsule.comments || [];
      });
      setComments(initialComments);
      
      // Initialize new comment state for each capsule
      const initialNewComments = {};
      capsules.forEach(capsule => {
        initialNewComments[capsule.id] = '';
      });
      setNewComment(initialNewComments);
      
      // Fetch media content for each capsule
      const mediaPromises = capsules.map(async (capsule) => {
        try {
          const media = await fetchMediaContent(capsule.id);
          // Transform media URLs to include the full path if they're relative
          const processedMedia = media.map(item => ({
            ...item,
            url: item.url.startsWith('http') ? item.url : `${API_BASE_URL}/${item.id}/download`
          }));
          return { capsuleId: capsule.id, media: processedMedia };
        } catch (err) {
          console.error(`Error fetching media for capsule ${capsule.id}:`, err);
          return { capsuleId: capsule.id, media: [] };
        }
      });
      
      const mediaResults = await Promise.all(mediaPromises);
      const mediaMap = mediaResults.reduce((acc, { capsuleId, media }) => {
        acc[capsuleId] = media;
        return acc;
      }, {});
      
      setMediaContent(mediaMap);
    } catch (err) {
      console.error('Error fetching capsules:', err);
      setError(err.message || 'Failed to load time capsules');
    } finally {
      setLoadingCapsules(false);
    }
  }, [isAuthenticated, authToken, fetchMediaContent]);

  useEffect(() => {
    fetchCapsules();
  }, [fetchCapsules]);

  // Close dropdowns/modals when clicking outside
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

  // Report submission handler
  const handleReportSubmit = async (e) => {
    e.preventDefault();
    if (!currentReportCapsuleId) return;
    
    try {
      await reportCapsule.createReport(
        currentReportCapsuleId,
        reportType,
        authToken
      );
      setReportSuccess(true);
      setIsReportModalOpen(false);
      setReportType('TimeCapsule');
      setCurrentReportCapsuleId(null);
      setTimeout(() => setReportSuccess(false), 3000);
    } catch (error) {
      console.error('Error submitting report:', error);
      setError(error.message || 'Failed to submit report');
    }
  };

  // Archive capsule handler
  const handleArchiveCapsule = async (capsuleId) => {
    if (!authToken) return;
    
    try {
      setIsArchiving(true);
      setArchiveError(null);
      
      await TimeCapsuleService.archiveTimeCapsule(capsuleId, authToken);
      
      setPublishedCapsules(prev => prev.filter(c => c.id !== capsuleId));
      setArchiveSuccess(true);
      setTimeout(() => setArchiveSuccess(false), 3000);
    } catch (error) {
      console.error('Error archiving capsule:', error);
      setArchiveError(error.message || 'Failed to archive time capsule');
    } finally {
      setIsArchiving(false);
    }
  };

  // Comment handlers
  const handleCommentChange = (capsuleId, value) => {
    setNewComment(prev => ({
      ...prev,
      [capsuleId]: value
    }));
  };

  const handleCommentSubmit = async (capsuleId) => {
    if (!newComment[capsuleId]?.trim() || !authToken) return;
    
    setCommentLoading(true);
    
    try {
      // Here you would normally call an API to save the comment
      // Mock implementation for now
      const commentPayload = {
        id: Date.now().toString(),
        text: newComment[capsuleId],
        user: {
          id: user.id,
          username: user.username,
          fullName: user.fullName,
          profilePicture: user.profilePicture
        },
        createdAt: new Date().toISOString(),
        reactions: { like: [], love: [] }
      };
      
      // Update local state
      setComments(prev => ({
        ...prev,
        [capsuleId]: [...(prev[capsuleId] || []), commentPayload]
      }));
      
      // Clear input
      setNewComment(prev => ({
        ...prev,
        [capsuleId]: ''
      }));
    } catch (error) {
      console.error('Error submitting comment:', error);
      setError(error.message || 'Failed to submit comment');
    } finally {
      setCommentLoading(false);
    }
  };

  // Handle reactions to comments
  const handleReaction = (capsuleId, commentId, reactionType) => {
    setComments(prev => {
      const capsuleComments = [...(prev[capsuleId] || [])];
      const commentIndex = capsuleComments.findIndex(c => c.id === commentId);
      
      if (commentIndex === -1) return prev;
      
      const comment = {...capsuleComments[commentIndex]};
      const reactions = {...comment.reactions};
      
      // Check if user already reacted
      const userReacted = reactions[reactionType]?.includes(user.id);
      
      if (userReacted) {
        // Remove reaction
        reactions[reactionType] = reactions[reactionType].filter(id => id !== user.id);
      } else {
        // Add reaction
        reactions[reactionType] = [...(reactions[reactionType] || []), user.id];
      }
      
      comment.reactions = reactions;
      capsuleComments[commentIndex] = comment;
      
      return {
        ...prev,
        [capsuleId]: capsuleComments
      };
    });
  };

  // Open report modal
  const openReportModal = (capsuleId) => {
    setCurrentReportCapsuleId(capsuleId);
    setIsReportModalOpen(true);
    setIsReportDropdownOpen(false);
  };

  // Fallback user data
  const userData = user || {
    username: "Guest",
    email: "",
    bio: "",
    profilePicture: ProfilePictureSample,
    fullName: "Guest User"
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

          <main className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
            {/* Status messages */}
            {reportSuccess && (
              <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-green-800 text-green-200' : 'bg-green-100 text-green-800'}`}>
                Report submitted successfully!
              </div>
            )}
            
            {archiveSuccess && (
              <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-green-800 text-green-200' : 'bg-green-100 text-green-800'}`}>
                Capsule archived successfully!
              </div>
            )}

            {error && (
              <div className={`mb-4 p-4 rounded-lg ${isDark ? 'bg-red-800 text-red-200' : 'bg-red-100 text-red-800'}`}>
                {error}
              </div>
            )}

            {/* Content */}
            {loadingCapsules ? (
              <div className="flex justify-center items-center h-64">
                <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'border-gray-300' : 'border-gray-600'}`}></div>
              </div>
            ) : publishedCapsules.length === 0 ? (
              <div className={`text-center py-12 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                <p className="text-xl mb-2">No published time capsules yet</p>
                <p>When time capsules reach their opening date, they'll appear here.</p>
              </div>
            ) : (
              <div className="space-y-6 max-w-2xl mx-auto">
                {publishedCapsules.map((capsule) => {
                  const capsuleMedia = mediaContent[capsule.id] || [];
                  const capsuleComments = comments[capsule.id] || [];
                  
                  return (
                    <article 
                      key={capsule.id} 
                      className={`rounded-lg shadow-md overflow-hidden ${isDark ? 'bg-gray-700 text-white' : 'bg-white text-gray-900'}`}
                    >
                      {/* Capsule options dropdown */}
                      <div className="relative">
                        <button 
                          className={`absolute top-4 right-4 p-2 rounded-full ${isDark ? 'hover:bg-gray-600 text-gray-300 hover:text-[#AF3535]' : 'hover:bg-[#AF3535]/10 text-gray-500 hover:text-[#AF3535]'} transition-colors`}
                          onClick={() => {
                            setIsReportDropdownOpen(prev => prev && currentReportCapsuleId === capsule.id ? false : true);
                            setCurrentReportCapsuleId(capsule.id);
                          }}
                          aria-label="More options"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                          </svg>
                        </button>

                        {isReportDropdownOpen && currentReportCapsuleId === capsule.id && (
                          <div 
                            ref={dropdownRef}
                            className={`absolute right-0 mt-2 w-48 rounded-md shadow-lg z-10 ${isDark ? 'bg-gray-700' : 'bg-white'}`}
                          >
                            <div className="py-1">
                              <button
                                onClick={() => openReportModal(capsule.id)}
                                className={`block w-full text-left px-4 py-2 text-sm ${isDark ? 'text-red-400 hover:bg-gray-600' : 'text-red-600 hover:bg-gray-100'}`}
                              >
                                Report This Capsule
                              </button>
                              <button
                                onClick={() => handleArchiveCapsule(capsule.id)}
                                disabled={isArchiving}
                                className={`block w-full text-left px-4 py-2 text-sm ${isDark ? 
                                  isArchiving ? 'text-gray-500' : 'text-yellow-400 hover:bg-gray-600' : 
                                  isArchiving ? 'text-gray-400' : 'text-yellow-600 hover:bg-gray-100'}`}
                              >
                                {isArchiving ? 'Archiving...' : 'Archive Capsule'}
                              </button>
                            </div>
                          </div>
                        )}
                      </div>

                      {/* Capsule content */}
                      <div className="p-6">
                        <header className="flex items-center mb-4">
                          <img 
                            src={capsule.user?.profilePicture || ProfilePictureSample} 
                            alt="user" 
                            className="h-12 w-12 rounded-full mr-4" 
                            onError={(e) => {
                              e.target.src = ProfilePictureSample;
                            }}
                          />
                          <div>
                            <h2 className="font-bold">{capsule.user?.fullName || capsule.user?.username || 'Unknown User'}</h2>
                            <time className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                              Opened on {new Date(capsule.openDate).toLocaleDateString()}
                            </time>
                          </div>
                        </header>

                        <div className="mb-4">
                          <p className="mb-4">{capsule.description || "This time capsule doesn't have a description."}</p>
                          <hr className={`my-2 ${isDark ? 'border-gray-600' : 'border-gray-200'}`} />
                          <div className="my-4">
                            <h3 className="text-xl font-semibold">{capsule.title || `Memories from ${new Date(capsule.createdAt).getFullYear()}`}</h3>
                            <time className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                              Created on {new Date(capsule.createdAt).toLocaleDateString()}
                            </time>
                          </div>
                          {capsule.message && <p>{capsule.message}</p>}
                        </div>

                        {/* Media gallery */}
                        {capsuleMedia.length > 0 && (
                          <div className="grid grid-cols-3 gap-2 mt-4">
                            {capsuleMedia.slice(0, 3).map((media, index) => {
                              const mediaUrl = media.url || bgmemoire;
                              const isImage = media.contentType?.startsWith('image/') || 
                                            mediaUrl.match(/\.(jpeg|jpg|gif|png|webp)$/i) !== null;
                              
                              return isImage ? (
                                <img 
                                  key={media.id || index} 
                                  src={mediaUrl} 
                                  alt={`Capsule content ${index + 1}`} 
                                  className="h-32 w-full object-cover rounded"
                                  onError={(e) => {
                                    e.target.src = bgmemoire;
                                    e.target.onerror = null; // Prevent infinite loop if fallback fails
                                  }}
                                  loading="lazy"
                                />
                              ) : (
                                <div key={media.id || index} className="h-32 w-full bg-gray-200 rounded flex items-center justify-center">
                                  <span className="text-gray-500">Media Preview</span>
                                </div>
                              );
                            })}
                          </div>
                        )}

                        {/* Comments section */}
                        <div className="mt-6">
                          <hr className={`my-4 ${isDark ? 'border-gray-600' : 'border-gray-200'}`} />
                          <h4 className={`font-medium mb-4 ${isDark ? 'text-gray-200' : 'text-gray-700'}`}>
                            Comments ({capsuleComments.length})
                          </h4>
                          
                          {/* Comment input */}
                          <div className="flex mb-4">
                            <img 
                              src={userData.profilePicture || ProfilePictureSample} 
                              alt="Your profile" 
                              className="h-8 w-8 rounded-full mr-2"
                              onError={(e) => {
                                e.target.src = ProfilePictureSample;
                              }}
                            />
                            <div className="flex-1 relative">
                              <input
                                type="text"
                                placeholder="Add a comment..."
                                value={newComment[capsule.id] || ''}
                                onChange={(e) => handleCommentChange(capsule.id, e.target.value)}
                                className={`w-full py-2 px-3 rounded-full ${
                                  isDark ? 'bg-gray-600 text-white placeholder-gray-400 border-gray-600' 
                                  : 'bg-gray-100 text-gray-800 placeholder-gray-500 border-gray-200'
                                } border focus:outline-none focus:ring-2 ${
                                  isDark ? 'focus:ring-blue-600' : 'focus:ring-blue-400'
                                }`}
                                onKeyPress={(e) => {
                                  if (e.key === 'Enter') {
                                    handleCommentSubmit(capsule.id);
                                  }
                                }}
                              />
                              <button
                                disabled={commentLoading || !newComment[capsule.id]?.trim()}
                                onClick={() => handleCommentSubmit(capsule.id)}
                                className={`absolute right-3 top-1/2 transform -translate-y-1/2 text-blue-500 hover:text-blue-700 ${
                                  (!newComment[capsule.id]?.trim() || commentLoading) ? 'opacity-50 cursor-not-allowed' : ''
                                }`}
                              >
                                Post
                              </button>
                            </div>
                          </div>
                          
                          {/* Comments list */}
                          <div className="space-y-4">
                            {capsuleComments.map(comment => (
                              <div key={comment.id} className="flex">
                                <img
                                  src={comment.user?.profilePicture || ProfilePictureSample}
                                  alt={comment.user?.username || "User"}
                                  className="h-8 w-8 rounded-full mr-2"
                                  onError={(e) => {
                                    e.target.src = ProfilePictureSample;
                                  }}
                                />
                                <div className="flex-1">
                                  <div className={`${isDark ? 'bg-gray-600' : 'bg-gray-100'} rounded-2xl px-4 py-2`}>
                                    <p className="font-medium text-sm">
                                      {comment.user?.fullName || comment.user?.username || "Unknown User"}
                                    </p>
                                    <p className="text-sm">{comment.text}</p>
                                  </div>
                                  
                                  {/* Reactions and timestamp */}
                                  <div className="flex items-center mt-1 ml-2">
                                    <div className="flex space-x-2 text-xs">
                                      <button 
                                        onClick={() => handleReaction(capsule.id, comment.id, 'like')}
                                        className={`${
                                          comment.reactions?.like?.includes(user.id) ? 
                                            (isDark ? 'text-blue-400' : 'text-blue-600') : 
                                            (isDark ? 'text-gray-400 hover:text-blue-400' : 'text-gray-500 hover:text-blue-600')
                                        }`}
                                      >
                                        Like {comment.reactions?.like?.length > 0 && `(${comment.reactions.like.length})`}
                                      </button>
                                      <button 
                                        onClick={() => handleReaction(capsule.id, comment.id, 'love')}
                                        className={`${
                                          comment.reactions?.love?.includes(user.id) ? 
                                            (isDark ? 'text-red-400' : 'text-red-600') : 
                                            (isDark ? 'text-gray-400 hover:text-red-400' : 'text-gray-500 hover:text-red-600')
                                        }`}
                                      >
                                        Love {comment.reactions?.love?.length > 0 && `(${comment.reactions.love.length})`}
                                      </button>
                                    </div>
                                    <span className={`text-xs ml-auto ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                                      {new Date(comment.createdAt).toLocaleDateString()} {new Date(comment.createdAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                    </span>
                                  </div>
                                </div>
                              </div>
                            ))}
                            
                            {capsuleComments.length === 0 && (
                              <p className={`text-center py-2 text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                                No comments yet. Be the first to comment!
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                    </article>
                  );
                })}
              </div>
            )}
          </main>
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
                      required
                    >
                      <option value="TimeCapsule">Time Capsule Issue</option>
                      <option value="Comment">Comment Issue</option>
                    </select>
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
                      disabled={reportCapsule.loading}
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