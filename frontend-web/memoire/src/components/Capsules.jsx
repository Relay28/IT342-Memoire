import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/AuthProvider';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { format } from 'date-fns';
import LockDateModal from '../components/modals/LockDateModal'; // Import the LockDateModal component
import { useThemeMode } from '../context/ThemeContext';

const Capsules = () => {
  const { isDark } = useThemeMode();
  const { authToken } = useAuth();
  const { 
    loading, 
    error, 
    getUserTimeCapsules,
    getUnpublishedTimeCapsules,
    getClosedTimeCapsules,
    getPublishedTimeCapsules,
    getArchivedTimeCapsules,
    deleteTimeCapsule,
    unlockTimeCapsule
  } = useTimeCapsule();
  
  const [capsules, setCapsules] = useState([]);
  const [activeFilter, setActiveFilter] = useState('all'); // 'all', 'unpublished', 'closed', 'published', 'archived'
  const [openMenuId, setOpenMenuId] = useState(null);
  const [isLockModalOpen, setIsLockModalOpen] = useState(false);
  const [selectedCapsuleId, setSelectedCapsuleId] = useState(null);
  const menuRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCapsulesByFilter(activeFilter);
  }, [authToken, activeFilter]);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event) {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setOpenMenuId(null);
      }
    }
    
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [menuRef]);

  const fetchCapsulesByFilter = async (filter) => {
    if (!authToken) return;
    
    try {
      let data;
      switch (filter) {
        case 'unpublished':
          data = await getUnpublishedTimeCapsules();
          break;
        case 'closed':
          data = await getClosedTimeCapsules();
          break;
        case 'published':
          data = await getPublishedTimeCapsules();
          break;
        case 'archived':
          data = await getArchivedTimeCapsules();
          break;
        case 'all':
        default:
          data = await getUserTimeCapsules();
          break;
      }
      setCapsules(data);
    } catch (err) {
      console.error(`Failed to fetch ${filter} capsules:`, err);
      // Error is already handled in the hook
    }
  };

  const handleDeleteCapsule = async (id) => {
    if (window.confirm('Are you sure you want to delete this time capsule? This action cannot be undone.')) {
      try {
        await deleteTimeCapsule(id);
        // Refresh the list after deletion
        fetchCapsulesByFilter(activeFilter);
        setOpenMenuId(null);
      } catch (err) {
        console.error('Failed to delete time capsule:', err);
        // Error is handled in the hook
      }
    }
  };

  const handleUnlockCapsule = async (id) => {
    try {
      await unlockTimeCapsule(id);
      // Refresh the list after unlocking
      fetchCapsulesByFilter(activeFilter);
      setOpenMenuId(null);
    } catch (err) {
      console.error('Failed to unlock time capsule:', err);
      // Error is handled in the hook
    }
  };

  const handleToggleMenu = (e, id) => {
    e.stopPropagation();
    setOpenMenuId(openMenuId === id ? null : id);
  };

  const handleOpenLockModal = (e, id) => {
    e.stopPropagation();
    setSelectedCapsuleId(id);
    setIsLockModalOpen(true);
    setOpenMenuId(null);
  };

  const handleLockSuccess = () => {
    // Refresh the list after locking
    fetchCapsulesByFilter(activeFilter);
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return format(new Date(dateString), 'MMMM d, yyyy');
  };

  const getStatusLabel = (status) => {
    switch (status) {
      case 'UNPUBLISHED':
        return 'Draft';
      case 'CLOSED':
        return 'Locked';
      case 'PUBLISHED':
        return 'Published';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return status;
    }
  };

  const getStatusStyle = (status) => {
    if (isDark) {
      switch (status) {
        case 'UNPUBLISHED':
          return 'bg-blue-900 text-blue-200';
        case 'CLOSED':
          return 'bg-yellow-900 text-yellow-200';
        case 'PUBLISHED':
          return 'bg-green-900 text-green-200';
        case 'ARCHIVED':
          return 'bg-gray-700 text-gray-200';
        default:
          return 'bg-gray-700 text-gray-200';
      }
    } else {
      switch (status) {
        case 'UNPUBLISHED':
          return 'bg-blue-100 text-blue-800';
        case 'CLOSED':
          return 'bg-yellow-100 text-yellow-800';
        case 'PUBLISHED':
          return 'bg-green-100 text-green-800';
        case 'ARCHIVED':
          return 'bg-gray-100 text-gray-800';
        default:
          return 'bg-gray-100 text-gray-800';
      }
    }
  };

  const handleCapsuleClick = (capsule) => {
    // Prevent editing for CLOSED capsules
    if (capsule.status === 'CLOSED') {
      alert('This capsule is locked and cannot be edited');
      return;
    }
    navigate(`/edit/${capsule.id}`);
  };

  const tabs = [
    { id: 'all', label: 'All Capsules' },
    { id: 'unpublished', label: 'Drafts' },
    { id: 'closed', label: 'Locked' },
    { id: 'published', label: 'Published' },
    { id: 'archived', label: 'Archived' }
  ];

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className="flex-1 p-8 overflow-y-auto flex items-center justify-center">
            <div className="text-center">Loading your capsules...</div>
          </section>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-100">
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className="flex-1 p-8 overflow-y-auto flex items-center justify-center">
            <div className="text-red-500">Error: {error}</div>
          </section>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-100'}`}>
      <div className="flex flex-col h-screen">
        <Header />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className={`flex-1 p-8 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-100'}`}>
            <div className="max-w-4xl mx-auto">
              {/* Status Filter Tabs */}
              <div className={`mb-6 border-b ${isDark ? 'border-gray-700' : 'border-gray-200'}`}>
                <nav className="flex space-x-8">
                  {tabs.map((tab) => (
                    <button
                      key={tab.id}
                      onClick={() => setActiveFilter(tab.id)}
                      className={`py-3 px-1 border-b-2 font-medium text-sm ${
                        activeFilter === tab.id
                          ? `${isDark ? 'border-blue-400 text-blue-400' : 'border-blue-500 text-blue-600'}`
                          : `${isDark ? 'border-transparent text-gray-400 hover:text-gray-300 hover:border-gray-500' : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'}`
                      }`}
                    >
                      {tab.label}
                    </button>
                  ))}
                </nav>
              </div>

              {/* Header Row */}
              <div className={`grid grid-cols-6 items-center gap-4 font-bold pb-4 mb-2 border-b ${isDark ? 'border-gray-700 text-gray-300' : 'border-gray-200 text-gray-900'}`}>
                <div className="col-span-2 pl-4">Name</div>
                <div className="text-center">Owner</div>
                <div className="text-center">Modified last</div>
                <div className="text-center">Status</div>
                <div className="w-8"></div>
              </div>

              {capsules.length === 0 ? (
                <div className={`text-center py-8 ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  No {activeFilter !== 'all' ? activeFilter : ''} capsules found. 
                  {activeFilter === 'all' && " Create your first one!"}
                </div>
              ) : (
                capsules.map((capsule) => (
                  <div 
                    key={capsule.id} 
                    className={`grid grid-cols-6 items-center gap-4 p-4 mb-3 rounded-lg shadow-md hover:bg-gray-50 transition-colors cursor-pointer ${
                      isDark ? 'bg-gray-700 hover:bg-gray-600' : 'bg-white hover:bg-gray-50'
                    }`}
                    onClick={() => handleCapsuleClick(capsule)}
                  >
                    <div className={`col-span-2 font-semibold truncate pl-4 ${isDark ? 'text-gray-200' : 'text-gray-900'}`}>
                      {capsule.title}
                    </div>
                    <div className="flex justify-center">
                      <img 
                        src={ProfilePictureSample} 
                        alt="Owner" 
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    </div>
                    <div className={`text-center ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                      {formatDate(capsule.updatedAt || capsule.createdAt)}
                    </div>
                    <div className="text-center">
                      <span className={`px-2 py-1 rounded-full text-xs ${getStatusStyle(capsule.status)} ${
                        isDark ? capsule.status === 'UNPUBLISHED' ? 'bg-blue-900 text-blue-200' :
                                capsule.status === 'CLOSED' ? 'bg-yellow-900 text-yellow-200' :
                                capsule.status === 'PUBLISHED' ? 'bg-green-900 text-green-200' :
                                'bg-gray-900 text-gray-200' : ''
                      }`}>
                        {getStatusLabel(capsule.status)}
                      </span>
                    </div>
                    <div className="flex justify-center relative" ref={openMenuId === capsule.id ? menuRef : null}>
                      <button 
                        className={`${isDark ? 'text-gray-400 hover:text-gray-300' : 'text-gray-400 hover:text-gray-600'} focus:outline-none`}
                        onClick={(e) => handleToggleMenu(e, capsule.id)}
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                          <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                        </svg>
                      </button>
                      
                      {/* Dropdown Menu */}
                      {openMenuId === capsule.id && (
                        <div className={`absolute right-0 top-6 mt-2 w-48 rounded-md shadow-lg z-10 ${
                          isDark ? 'bg-gray-700 border border-gray-600' : 'bg-white'
                        }`}>
                          <div className="py-1">
                            {/* View Option */}
                            <button
                              className={`w-full text-left px-4 py-2 text-sm ${
                                isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                              }`}
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/view/${capsule.id}`);
                              }}
                            >
                              View Details
                            </button>
                            
                            {/* Edit Option - Don't show for locked capsules */}
                            {capsule.status !== 'CLOSED' && (
                              <button
                                className={`w-full text-left px-4 py-2 text-sm ${
                                  isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                }`}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  navigate(`/edit/${capsule.id}`);
                                }}
                              >
                                Edit
                              </button>
                            )}
                            
                            {/* Lock/Unlock Option */}
                            {capsule.status === 'CLOSED' ? (
                              <button
                                className={`w-full text-left px-4 py-2 text-sm ${
                                  isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                }`}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleUnlockCapsule(capsule.id);
                                }}
                              >
                                Unlock
                              </button>
                            ) : (
                              <button
                                className={`w-full text-left px-4 py-2 text-sm ${
                                  isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                }`}
                                onClick={(e) => handleOpenLockModal(e, capsule.id)}
                              >
                                Lock
                              </button>
                            )}
                            
                            {/* Delete Option */}
                            <button
                              className={`w-full text-left px-4 py-2 text-sm ${
                                isDark ? 'text-red-400 hover:bg-gray-600' : 'text-red-600 hover:bg-gray-100'
                              }`}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteCapsule(capsule.id);
                              }}
                            >
                              Delete
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </section>
        </div>
      </div>

      {/* Lock Date Modal */}
      <LockDateModal
        isOpen={isLockModalOpen}
        onClose={() => setIsLockModalOpen(false)}
        timeCapsuleId={selectedCapsuleId}
        onSuccess={handleLockSuccess}
      />
    </div>
  );
};

export default Capsules;