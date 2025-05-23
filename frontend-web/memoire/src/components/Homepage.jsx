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
import CommentServices from "../services/CommentServices";
import CommentReactionService from "../services/CommentReactionService";
import MediaCarousel from './MediaShower/MediaCarousel';
import { profileService } from '../components/ProfileFunctionalities';
import apiService from './Profile/apiService';

const Homepage = () => {
  // Context and hooks
  const { isDark } = useThemeMode();
  const navigate = useNavigate();
  const { user, logout, isAuthenticated, authToken } = useAuth();
  const { fetchMediaContent } = useCapsuleContent();
  const [profilePicture, setProfilePicture] = useState(ProfilePictureSample);
  const [loadingProfile, setLoadingProfile] = useState(true);
  
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
  const [reactionLoading, setReactionLoading] = useState({});
  const [expandedComments, setExpandedComments] = useState({});
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editCommentText, setEditCommentText] = useState('');
  const [commentDropdownOpen, setCommentDropdownOpen] = useState({});
  const [capsuleOwners, setCapsuleOwners] = useState({});
  // Services
  const reportCapsule = ServiceReportCapsule();
  

  // Authentication check
  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login');
    }
  }, [isAuthenticated, navigate]);

  const toggleComments = (capsuleId) => {
    setExpandedComments(prev => ({
      ...prev,
      [capsuleId]: !prev[capsuleId]
    }));
  };

  useEffect(() => {
    if (isAuthenticated && authToken) {
      const fetchProfilePicture = async () => {
        try {
          setLoadingProfile(true);
          const userData = await profileService.getCurrentUser();
          const picture = userData.profilePicture;
  
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
            if (imageUrl) {
              setProfilePicture(imageUrl);
            }
          }
        } catch (error) {
          console.error('Error fetching profile picture:', error);
        } finally {
          setLoadingProfile(false);
        }
      };
      
      fetchProfilePicture();
    } else {
      setLoadingProfile(false);
    }
  }, [isAuthenticated, authToken]);


  const fetchCapsuleOwnerProfile = async (userId) => {
 
    try {
    
      const ownerData = await apiService.get(`/api/profiles/view/${userId}`);
      
      // Process profile picture if it exists
      let profilePictureUrl = ProfilePictureSample; // Default picture
      if (ownerData.data.profilePicture) {
        if (typeof ownerData.data.profilePicture === 'string') {
          profilePictureUrl = ownerData.data.profilePicture.startsWith('data:image')
            ? ownerData.data.profilePicture
            : `data:image/jpeg;base64,${ownerData.data.profilePicture}`;
        } else if (Array.isArray(ownerData.data.profilePicture)) {
          const binaryString = String.fromCharCode.apply(null, ownerData.data.profilePicture);
          profilePictureUrl = `data:image/jpeg;base64,${btoa(binaryString)}`;
        }
      }
  
      return {
        ...ownerData.data,
        profilePicture: profilePictureUrl
      };
    } catch (error) {
      console.error('Error fetching owner profile:', error);
      return null;
    }
  };


  // Fetch published capsules with media
const fetchCapsules = useCallback(async () => {
  if (!isAuthenticated || !authToken) return;
  
  try {
    setLoadingCapsules(true);
    setError(null);
    
    const capsules = await TimeCapsuleService.getPublicPublishedTimeCapsules(authToken);
    setPublishedCapsules(capsules || []);
    
    // Fetch owner profiles for all capsules
    const ownerProfiles = {};
    await Promise.all(
      capsules.map(async (capsule) => {
        const ownerProfile = await fetchCapsuleOwnerProfile(capsule.createdById);
        if (ownerProfile) {
          ownerProfiles[capsule.userId] = ownerProfile;
        }
      })
    );
    
    setCapsuleOwners(ownerProfiles);
    // Initialize comments storage for each capsule
    const initialComments = {};
    const initialNewComments = {};
    const mediaPromises = [];
    
    // Process each capsule
    for (const capsule of capsules) {
      initialNewComments[capsule.id] = ''; // Initialize new comment state
      
      try {
        // Fetch comments for this capsule
        const capsuleComments = await CommentServices.getCommentsByCapsule(capsule.id);
          
        // Fetch reactions for each comment
        // In fetchCapsules function:
   // First, update the comment state initialization in fetchCapsules:
const commentsWithReactions = await Promise.all(
  (capsuleComments || []).map(async (comment) => {
    try {
      const reactions = await CommentReactionService.getReactionsByCommentId(comment.id);
      return {
        ...comment,
        userId: comment.userId,
        username: comment.username,
        userProfileImage: comment.userProfileImage,
        reactions: {
          love: reactions.filter(r => r.type === 'love').map(r => r.userId),
        },
      };
    } catch (err) {
      console.error(`Error fetching reactions for comment ${comment.id}:`, err);
      return {
        ...comment,
        userId: comment.userId,
        username: comment.username,
        userProfileImage: comment.userProfileImage,
        reactions: { love: [] },
      };
    }
  })
);
        
        initialComments[capsule.id] = commentsWithReactions;
      } catch (err) {
        console.error(`Error processing capsule ${capsule.id}:`, err);
        initialComments[capsule.id] = [];
      }
      
      // Prepare media fetch promise
      mediaPromises.push(
        fetchMediaContent(capsule.id)
          .then(media => ({
            capsuleId: capsule.id,
            media: media.map(item => ({
              ...item,
              url: item.url.startsWith('http') ? item.url : `${API_BASE_URL}/${item.id}/download`
            }))
          }))
          .catch(err => {
            console.error(`Error fetching media for capsule ${capsule.id}:`, err);
            return { capsuleId: capsule.id, media: [] };
          })
      );
    }
    
    // Set comments and new comments state
    setComments(initialComments);
    setNewComment(initialNewComments);
    
    // Process media content
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
      const comment = await CommentServices.createComment(
        capsuleId, 
        newComment[capsuleId], 
        authToken
      );
      
      // Update local state with the new comment
      // In handleCommentSubmit, update the new comment state:
      setComments(prev => ({
        ...prev,
        [capsuleId]: [...(prev[capsuleId] || []), {
          ...comment,
          userId: user.id, // Ensure userId is included
          reactions: { love: [] }
        }]
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
  const handleReaction = async (capsuleId, commentId, reactionType) => {
    if (!authToken || !user?.id) return;
  
    const loadingKey = `${commentId}-${reactionType}`;
    setReactionLoading(prev => ({ ...prev, [loadingKey]: true }));
  
    try {
      // Optimistically update UI
      setComments(prev => {
        const capsuleComments = [...prev[capsuleId]];
        const commentIndex = capsuleComments.findIndex(c => c.id === commentId);
        
        if (commentIndex === -1) return prev;
  
        const currentReactions = capsuleComments[commentIndex].reactions || {};
        const currentUserReactions = currentReactions[reactionType] || [];
        const userHasReacted = currentUserReactions.includes(user.id);
  
        // Toggle reaction
        const updatedReactions = {
          ...currentReactions,
          [reactionType]: userHasReacted
            ? currentUserReactions.filter(id => id !== user.id) // Remove reaction
            : [...currentUserReactions, user.id] // Add reaction
        };
  
        // Update the comment
        capsuleComments[commentIndex] = {
          ...capsuleComments[commentIndex],
          reactions: updatedReactions
        };
  
        return {
          ...prev,
          [capsuleId]: capsuleComments
        };
      });
  
      // Sync with server
      const reactions = await CommentReactionService.getReactionsByCommentId(commentId);
      const userReaction = reactions.find(r => 
        r.userId === user.id && 
        r.type === reactionType
      );
  
      if (userReaction) {
        // Unlike
        await CommentReactionService.deleteReaction(userReaction.reaction_id, authToken);
      } else {
        // Like
        await CommentReactionService.addReaction(commentId, reactionType, authToken);
      }
  
    } catch (error) {
      console.error('Error updating reaction:', error);
      setError(error.message || 'Failed to update reaction');
      
      // Revert optimistic update if there was an error
      setComments(prev => {
        const capsuleComments = [...prev[capsuleId]];
        const commentIndex = capsuleComments.findIndex(c => c.id === commentId);
        
        if (commentIndex === -1) return prev;
  
        // Get original reactions from server
        const originalReactions = capsuleComments[commentIndex].reactions || {};
        const originalUserReactions = originalReactions[reactionType] || [];
        const userHadReacted = originalUserReactions.includes(user.id);
  
        // Revert to original state
        const revertedReactions = {
          ...originalReactions,
          [reactionType]: userHadReacted
            ? [...originalUserReactions]
            : originalUserReactions.filter(id => id !== user.id)
        };
  
        capsuleComments[commentIndex] = {
          ...capsuleComments[commentIndex],
          reactions: revertedReactions
        };
  
        return {
          ...prev,
          [capsuleId]: capsuleComments
        };
      });
    } finally {
      setReactionLoading(prev => ({ ...prev, [loadingKey]: false }));
    }
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

  const formatTimeAgo = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now - date) / 1000);
    
    if (diffInSeconds < 60) return `${diffInSeconds}s`;
    if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m`;
    if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h`;
    if (diffInSeconds < 604800) return `${Math.floor(diffInSeconds / 86400)}d`;
    if (diffInSeconds < 2592000) return `${Math.floor(diffInSeconds / 604800)}w`;
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  if (!isAuthenticated) {
    return (
      <div className={`min-h-screen flex justify-center items-center ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
        <div className={`animate-pulse text-lg ${isDark ? 'text-gray-300' : 'text-gray-600'}`}>Loading...</div>
      </div>
    );
  }

  // Handle starting to edit a comment
const handleStartEdit = (commentId, currentText) => {
  setEditingCommentId(commentId);
  setEditCommentText(currentText);
};

const processProfileImage = (profileImage) => {
  if (!profileImage) return null;

  let imageUrl;
  if (typeof profileImage === 'string') {
    imageUrl = profileImage.startsWith('data:image') 
      ? profileImage 
      : `data:image/jpeg;base64,${profileImage}`;
  } else if (Array.isArray(profileImage)) {
    const binaryString = String.fromCharCode.apply(null, profileImage);
    imageUrl = `data:image/jpeg;base64,${btoa(binaryString)}`;
  }
  return imageUrl;
};

// Handle canceling edit
const handleCancelEdit = () => {
  setEditingCommentId(null);
  setEditCommentText('');
};

// Handle updating a comment
const handleUpdateComment = async (capsuleId, commentId) => {
  if (!editCommentText.trim() || !authToken) return;
  
  try {
    setCommentLoading(true);
    
    const updatedComment = await CommentServices.updateComment(
      commentId,
      editCommentText,
      authToken
    );
    
    // Update local state with the updated comment
    setComments(prev => ({
      ...prev,
      [capsuleId]: prev[capsuleId].map(comment => 
        comment.id === commentId 
          ? { ...comment, text: updatedComment.text } 
          : comment
      )
    }));
    
    // Reset editing state
    setEditingCommentId(null);
    setEditCommentText('');
  } catch (error) {
    console.error('Error updating comment:', error);
    setError(error.message || 'Failed to update comment');
  } finally {
    setCommentLoading(false);
  }
};

// Handle deleting a comment
const handleDeleteComment = async (capsuleId, commentId) => {
  if (!authToken) return;
  
  if (!window.confirm('Are you sure you want to delete this comment?')) {
    return;
  }
  
  try {
    setCommentLoading(true);
    
    await CommentServices.deleteComment(commentId, authToken);
    
    // Update local state by removing the deleted comment
    setComments(prev => ({
      ...prev,
      [capsuleId]: prev[capsuleId].filter(comment => comment.id !== commentId)
    }));
  } catch (error) {
    console.error('Error deleting comment:', error);
    setError(error.message || 'Failed to delete comment');
  } finally {
    setCommentLoading(false);
  }
};

return (
  <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
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

        <main className={`w-full flex-1 p-4 md:p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'} `}>
          {/* Status messages */}
          {reportSuccess && (
            <div className={`mb-4 p-4 rounded-lg border ${isDark ? 'bg-emerald-900/30 border-emerald-700 text-emerald-100' : 'bg-emerald-100 border-emerald-200 text-emerald-800'}`}>
              Report submitted successfully!
            </div>
          )}
          
          {archiveSuccess && (
            <div className={`mb-4 p-4 rounded-lg border ${isDark ? 'bg-emerald-900/30 border-emerald-700 text-emerald-100' : 'bg-emerald-100 border-emerald-200 text-emerald-800'}`}>
              Capsule archived successfully!
            </div>
          )}

          {error && (
            <div className={`mb-4 p-4 rounded-lg border ${isDark ? 'bg-rose-900/30 border-rose-700 text-rose-100' : 'bg-rose-100 border-rose-200 text-rose-800'}`}>
              {error}
            </div>
          )}

          {/* Content */}
          {loadingCapsules ? (
            <div className="flex justify-center items-center h-full w-full"> {/* Changed h-64 to h-full and added w-full */}
            <div className={`animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 ${isDark ? 'border-gray-300' : 'border-gray-600'}`}></div>
          </div>
          ) : publishedCapsules.length === 0 ? (
            <div className={`text-center py-35 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
              <div className="inline-block p-6 rounded-full bg-gray-100 dark:bg-gray-700/50 mb-4">
                <svg 
                  xmlns="http://www.w3.org/2000/svg" 
                  className="h-12 w-12 text-gray-400 dark:text-gray-500" 
                  fill="none" 
                  viewBox="0 0 24 24" 
                  stroke="currentColor"
                >
                  <path 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                    strokeWidth={1.5} 
                    d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" 
                  />
                </svg>
              </div>
              <h3 className="text-xl font-medium mb-2 text-center">
                No published time capsules yet
              </h3>
              <p className="max-w-md mx-auto text-center">
                When time capsules reach their opening date, they'll appear here for you to explore.
              </p>
            </div>
          ) : (
            <div className="space-y-6 max-w-3xl mx-auto">
              {publishedCapsules.map((capsule) => {
                const capsuleMedia = mediaContent[capsule.id] || [];
                const capsuleComments = comments[capsule.id] || [];
                
                return (
                  <article 
                    key={capsule.id} 
                    className={`rounded-xl overflow-hidden transition-all ${isDark ? 'bg-gray-700/50 text-gray-100 border border-gray-600' : 'bg-white text-gray-800 border border-gray-200'}`}
                  >
                    {/* Capsule header with options */}
                    <div className="p-5 pb-0 flex justify-between items-start">
  <div className="flex items-center space-x-3">
    <img 
      src={capsuleOwners[capsule.userId]?.profilePicture || ProfilePictureSample} 
      alt={capsuleOwners[capsule.userId]?.username || "User"} 
      className="h-10 w-10 rounded-full object-cover border-2 border-white dark:border-gray-600 shadow-sm"
      onError={(e) => {
        e.target.src = ProfilePictureSample;
      }}
    />
    <div>
      <h2 className="font-semibold">
        {capsuleOwners[capsule.userId]?.name || capsuleOwners[capsule.userId]?.username || 'Unknown User'}
      </h2>
      <time className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
        Opened on {new Date(capsule.openDate).toLocaleDateString()}
      </time>
    </div>
  </div>
                      
                      {/* Options dropdown */}
                      <div className="relative">
                        <button 
                          className={`p-1.5 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors ${isDark ? 'text-gray-300 hover:text-rose-400' : 'text-gray-500 hover:text-rose-500'}`}
                          onClick={() => {
                            setIsReportDropdownOpen(prev => prev && currentReportCapsuleId === capsule.id ? false : true);
                            setCurrentReportCapsuleId(capsule.id);
                          }}
                          aria-label="More options"
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M6 10a2 2 0 11-4 0 2 2 0 014 0zM12 10a2 2 0 11-4 0 2 2 0 014 0zM16 12a2 2 0 100-4 2 2 0 000 4z" />
                          </svg>
                        </button>

                        {isReportDropdownOpen && currentReportCapsuleId === capsule.id && (
                          <div 
                            ref={dropdownRef}
                            className={`absolute right-0 mt-1 w-48 rounded-lg shadow-lg z-10 overflow-hidden ${isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white border border-gray-200'}`}
                          >
                            <div className="py-1">
                              <button
                                onClick={() => openReportModal(capsule.id)}
                                className={`block w-full text-left px-4 py-2 text-sm ${isDark ? 'text-rose-400 hover:bg-gray-600' : 'text-rose-500 hover:bg-gray-100'}`}
                              >
                                Report This Capsule
                              </button>
                              <button
                                onClick={() => handleArchiveCapsule(capsule.id)}
                                disabled={isArchiving}
                                className={`block w-full text-left px-4 py-2 text-sm ${isDark ? 
                                  isArchiving ? 'text-gray-500' : 'text-amber-400 hover:bg-gray-600' : 
                                  isArchiving ? 'text-gray-400' : 'text-amber-600 hover:bg-gray-100'}`}
                              >
                                {isArchiving ? 'Archiving...' : 'Archive Capsule'}
                              </button>
                            </div>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Capsule content */}
                    <div className="p-5">
                      <div className="mb-4">
                        <div className={`text-xl font-bold mb-1 ${isDark ? 'text-white' : 'text-gray-900'}`}>
                          {capsule.title || 'Untitled Capsule'}
                        </div>
                        <div className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                          Created on {new Date(capsule.createdAt).toLocaleDateString()}
                        </div>
                      </div>
                      
                      <p className={`mb-4 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                        {capsule.description || 'No message content'}
                      </p>
                      
                      {capsule.message && (
                        <div className={`p-4 rounded-lg mb-4 ${isDark ? 'bg-gray-600/30 border border-gray-600' : 'bg-gray-100 border border-gray-200'}`}>
                          <p className="italic">{capsule.message}</p>
                        </div>
                      )}

                      {/* Media gallery */}
                      {capsuleMedia.length > 0 && (
                        <div className="mt-4 rounded-lg overflow-hidden border border-gray-200 dark:border-gray-600">
                          <MediaCarousel 
                            mediaItems={capsuleMedia} 
                            fallback={bgmemoire}
                          />
                        </div>
                      )}

                      {/* Comments section */}
                      <div className="mt-6">
                        <div 
                          className={`flex items-center justify-between cursor-pointer p-3 rounded-lg ${isDark ? 'hover:bg-gray-600/30' : 'hover:bg-gray-100'} transition-colors`}
                          onClick={() => toggleComments(capsule.id)}
                        >
                          <h4 className={`font-medium flex items-center ${isDark ? 'text-gray-200' : 'text-gray-700'}`}>
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clipRule="evenodd" />
                            </svg>
                            Comments ({capsuleComments.length})
                          </h4>
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            className={`h-5 w-5 transition-transform ${expandedComments[capsule.id] ? 'transform rotate-180' : ''}`}
                            viewBox="0 0 20 20"
                            fill="currentColor"
                          >
                            <path
                              fillRule="evenodd"
                              d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
                              clipRule="evenodd"
                            />
                          </svg>
                        </div>

                        {/* Comment input */}
                        {expandedComments[capsule.id] && (
                          <div className="flex mb-4 mt-3">
                           <img 
  src={profilePicture} 
  alt="Your profile" 
  className="h-8 w-8 rounded-full mr-3 flex-shrink-0 border-2 border-white dark:border-gray-600 shadow-sm"
  onError={(e) => {
    e.target.src = ProfilePictureSample;
  }}
/>
                            <div className="flex-1 relative">
                              <input
                                type="text"
                                placeholder="Share your thoughts..."
                                value={newComment[capsule.id] || ''}
                                onChange={(e) => handleCommentChange(capsule.id, e.target.value)}
                                className={`w-full py-2 px-4 rounded-full text-sm border ${
                                  isDark ? 'bg-gray-700 text-white placeholder-gray-400 border-gray-600 focus:border-[#AF3535]' 
                                  : 'bg-gray-100 text-gray-800 placeholder-gray-500 border-gray-300 focus:border-red-400'
                                } focus:outline-none focus:ring-2 ${
                                  isDark ? 'focus:ring-[#AF3535]/30' : 'focus:ring-red-400/30'
                                } transition-colors`}
                                onKeyPress={(e) => {
                                  if (e.key === 'Enter') {
                                    handleCommentSubmit(capsule.id);
                                  }
                                }}
                              />
                              <button
                                disabled={commentLoading || !newComment[capsule.id]?.trim()}
                                onClick={() => handleCommentSubmit(capsule.id)}
                                className={`absolute right-3 top-1/2 transform -translate-y-1/2 ${isDark ? 'text-red-500 hover:text-red-400' : 'text-[#AF3535] hover:text-red-600'} ${
                                  (!newComment[capsule.id]?.trim() || commentLoading) ? 'opacity-50 cursor-not-allowed' : ''
                                } transition-colors`}
                              >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-8.707l-3-3a1 1 0 00-1.414 0l-3 3a1 1 0 001.414 1.414L9 9.414V13a1 1 0 102 0V9.414l1.293 1.293a1 1 0 001.414-1.414z" clipRule="evenodd" />
                                </svg>
                              </button>
                            </div>
                          </div>
                        )}
                        
                        {/* Comments list */}
                        {expandedComments[capsule.id] && (
                          <div className="space-y-4">
                            {capsuleComments.map(comment => {
                              const isCurrentUsersComment = user?.id === user?.id;
                              
                              return (
                                <div key={comment.id} className="flex items-start group">
                                 <img
    src={processProfileImage(comment.userProfileImage) || ProfilePictureSample}
    alt={comment.username || "User"}
    className="h-8 w-8 rounded-full mr-3 flex-shrink-0 border-2 border-white dark:border-gray-600 shadow-sm"
    onError={(e) => {
      e.target.onerror = null;
      e.target.src = ProfilePictureSample;
    }}
  />
                                  
                                  <div className="flex-1 min-w-0">
          <div className="flex items-baseline">
            <span className={`font-semibold text-sm mr-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>
              {comment.username || "Unknown User"}
            </span>
            <span className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
              {formatTimeAgo(comment.createdAt)}
              {comment.createdAt !== comment.updatedAt && ' • Edited'}
            </span>
          </div>
                                    
                                    {editingCommentId === comment.id ? (
                                      <div className="mt-1 relative">
                                        <input
                                          type="text"
                                          value={editCommentText}
                                          onChange={(e) => setEditCommentText(e.target.value)}
                                          className={`w-full py-1 px-2 rounded text-sm border ${
                                            isDark ? 'bg-gray-700 text-white border-gray-600 focus:border-blue-500' 
                                            : 'bg-gray-100 text-gray-800 border-gray-300 focus:border-blue-500'
                                          } focus:outline-none focus:ring-2 ${
                                            isDark ? 'focus:ring-blue-500/30' : 'focus:ring-blue-500/30'
                                          } transition-colors`}
                                          onKeyPress={(e) => {
                                            if (e.key === 'Enter') {
                                              handleUpdateComment(capsule.id, comment.id);
                                            }
                                          }}
                                        />
                                        <div className="flex justify-end mt-1 space-x-2">
                                          <button
                                            onClick={handleCancelEdit}
                                            className={`text-xs px-2 py-1 rounded ${
                                              isDark ? 'bg-gray-600 hover:bg-gray-500 text-gray-200' : 'bg-gray-200 hover:bg-gray-300 text-gray-800'
                                            } transition-colors`}
                                          >
                                            Cancel
                                          </button>
                                          <button
                                            onClick={() => handleUpdateComment(capsule.id, comment.id)}
                                            disabled={!editCommentText.trim() || commentLoading}
                                            className={`text-xs px-2 py-1 rounded bg-[#AF3535] hover:bg-red-600 text-white ${
                                              (!editCommentText.trim() || commentLoading) ? 'opacity-50 cursor-not-allowed' : ''
                                            } transition-colors`}
                                          >
                                            {commentLoading ? 'Updating...' : 'Update'}
                                          </button>
                                        </div>
                                      </div>
                                    ) : (
                                      <p className={`text-sm mt-0.5 ${isDark ? 'text-gray-200' : 'text-gray-800'}`}>
                                        {comment.text}
                                      </p>
                                    )}
                                    
                                    <div className="flex items-center mt-1 space-x-4">
                                    {comment.reactions?.love?.length > 0 && (
                                      <span className={`text-xs ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                                        {comment.reactions.love.length} like{comment.reactions.love.length !== 1 ? 's' : ''}
                                      </span>
                                    )}
                                                                          
                                      
                                    {(user?.id) && (
                                      <div className="flex space-x-3">
                                        <button
                                          onClick={() => handleStartEdit(comment.id, comment.text)}
                                          className={`text-xs ${isDark ? 'text-gray-400 hover:text-blue-400' : 'text-gray-500 hover:text-blue-600'} transition-colors`}
                                        >
                                          Edit
                                        </button>
                                        <button
                                          onClick={() => handleDeleteComment(capsule.id, comment.id)}
                                          className={`text-xs ${isDark ? 'text-gray-400 hover:text-rose-400' : 'text-gray-500 hover:text-rose-600'} transition-colors`}
                                        >
                                          Delete
                                        </button>
                                      </div>
                                    )}
                                    </div>
                                  </div>
                                  
                                  <button 
                                    onClick={() => handleReaction(capsule.id, comment.id, 'love')}
                                    disabled={reactionLoading[`${comment.id}-love`]}
                                    className={`ml-2 p-1 ${comment.reactions?.love?.includes(user.id) ? 'opacity-100' : 'opacity-0 group-hover:opacity-100'} transition-opacity`}
                                    aria-label={comment.reactions?.love?.includes(user.id) ? "Unlike comment" : "Like comment"}
                                  >
                                    <svg 
                                      xmlns="http://www.w3.org/2000/svg" 
                                      className={`h-5 w-5 ${comment.reactions?.love?.includes(user.id) ? 
                                        'fill-rose-500 text-rose-500 hover:fill-rose-400 hover:text-rose-400' : 
                                        (isDark ? 'text-gray-400 hover:text-rose-400' : 'text-gray-500 hover:text-rose-500')
                                      } transition-colors`} 
                                      viewBox="0 0 24 24" 
                                      stroke={comment.reactions?.love?.includes(user.id) ? 'none' : 'currentColor'}
                                      strokeWidth="2"
                                      fill={comment.reactions?.love?.includes(user.id) ? 'currentColor' : 'none'}
                                    >
                                      <path 
                                        strokeLinecap="round" 
                                        strokeLinejoin="round" 
                                        d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" 
                                      />
                                    </svg>
                                  </button>
                                </div>
                              );
                            })}
                            
                            {capsuleComments.length === 0 && (
                              <div className={`text-center py-4 ${isDark ? 'text-gray-500' : 'text-gray-400'}`}>
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 mx-auto mb-2 opacity-50" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                                </svg>
                                <p className="text-sm">No comments yet. Be the first to share your thoughts!</p>
                              </div>
                            )}
                          </div>
                        )}
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
        <div className={`fixed inset-0 z-50 flex items-center justify-center p-4 ${isDark ? 'bg-black/70' : 'bg-black/50'} backdrop-blur-sm`}>
          <div 
            ref={modalRef}
            className={`w-full max-w-md rounded-xl shadow-xl overflow-hidden ${isDark ? 'bg-gray-800 border border-gray-700' : 'bg-white border border-gray-200'}`}
          >
            <div className="p-6">
              <div className="flex justify-between items-center mb-4">
                <h2 className={`text-xl font-bold ${isDark ? 'text-white' : 'text-gray-900'}`}>
                  Report This Capsule
                </h2>
                <button
                  onClick={() => setIsReportModalOpen(false)}
                  className={`p-1 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors ${isDark ? 'text-gray-300' : 'text-gray-500'}`}
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
                    className={`w-full p-3 rounded-lg border text-sm ${
                      isDark ? 'bg-gray-700 border-gray-600 text-white focus:border-[#AF3535] focus:ring-[#AF3535]/30' 
                      : 'bg-white border-gray-300 text-gray-800 focus:border-[#AF3535] focus:ring-[#AF3535]/30'
                    } focus:outline-none focus:ring-2 transition-colors`}
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
                      
                <div className="flex justify-end gap-3 pt-2">
                  <button
                    type="button"
                    onClick={() => setIsReportModalOpen(false)}
                    className={`px-4 py-2 rounded-lg text-sm font-medium ${
                      isDark ? 'bg-gray-700 hover:bg-gray-600 text-white' 
                      : 'bg-gray-200 hover:bg-gray-300 text-gray-800'
                    } transition-colors`}
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    className={`px-4 py-2 rounded-lg text-sm font-medium bg-[#AF3535] hover:bg-red-700 text-white ${
                      reportCapsule.loading ? 'opacity-70 cursor-not-allowed' : ''
                    } transition-colors`}
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