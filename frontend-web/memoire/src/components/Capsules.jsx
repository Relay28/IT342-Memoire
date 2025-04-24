import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../components/AuthProvider';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { format } from 'date-fns';
import LockDateModal from '../components/modals/LockDateModal';
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
  const [activeFilter, setActiveFilter] = useState('all');
  const [openMenuId, setOpenMenuId] = useState(null);
  const [isLockModalOpen, setIsLockModalOpen] = useState(false);
  const [selectedCapsuleId, setSelectedCapsuleId] = useState(null);
  const menuRef = useRef(null);
  const navigate = useNavigate();

  useEffect(() => {
    fetchCapsulesByFilter(activeFilter);
  }, [authToken, activeFilter]);

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
    }
  };

  const handleDeleteCapsule = async (id) => {
    if (window.confirm('Are you sure you want to delete this time capsule? This action cannot be undone.')) {
      try {
        await deleteTimeCapsule(id);
        fetchCapsulesByFilter(activeFilter);
        setOpenMenuId(null);
      } catch (err) {
        console.error('Failed to delete time capsule:', err);
      }
    }
  };

  const handleUnlockCapsule = async (id) => {
    try {
      await unlockTimeCapsule(id);
      fetchCapsulesByFilter(activeFilter);
      setOpenMenuId(null);
    } catch (err) {
      console.error('Failed to unlock time capsule:', err);
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
          return 'bg-blue-900/50 text-blue-300 border-blue-700';
        case 'CLOSED':
          return 'bg-amber-900/50 text-amber-300 border-amber-700';
        case 'PUBLISHED':
          return 'bg-emerald-900/50 text-emerald-300 border-emerald-700';
        case 'ARCHIVED':
          return 'bg-gray-700/50 text-gray-300 border-gray-600';
        default:
          return 'bg-gray-700/50 text-gray-300';
      }
    } else {
      switch (status) {
        case 'UNPUBLISHED':
          return 'bg-blue-50 text-blue-800 border-blue-200';
        case 'CLOSED':
          return 'bg-amber-50 text-amber-800 border-amber-200';
        case 'PUBLISHED':
          return 'bg-emerald-50 text-emerald-800 border-emerald-200';
        case 'ARCHIVED':
          return 'bg-gray-50 text-gray-800 border-gray-200';
        default:
          return 'bg-gray-50 text-gray-800';
      }
    }
  };

  const handleCapsuleClick = (capsule) => {
    if (capsule.status === 'CLOSED' || capsule.status === 'PUBLISHED' || capsule.status === 'ARCHIVED') {
      navigate(`/view/${capsule.id}`);
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
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className={`flex-1 p-8 overflow-y-auto flex items-center justify-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className={`text-center ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
              <div className="animate-pulse flex space-x-4">
                <div className="flex-1 space-y-4 py-1">
                  <div className="h-4 bg-gray-300 dark:bg-gray-600 rounded w-3/4 mx-auto"></div>
                  <div className="space-y-2">
                    <div className="h-4 bg-gray-300 dark:bg-gray-600 rounded"></div>
                    <div className="h-4 bg-gray-300 dark:bg-gray-600 rounded w-5/6"></div>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <section className={`flex-1 p-8 overflow-y-auto flex items-center justify-center ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className={`p-4 rounded-lg ${isDark ? 'bg-gray-700 text-red-400' : 'bg-white text-red-600'} shadow-md`}>
              Error: {error}
            </div>
          </section>
        </div>
      </div>
    );
  }

  return (
    <div className={`min-h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
      <div className="flex flex-col h-screen">
        <Header />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className={`flex-1 p-6 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
            <div className="max-w-6xl mx-auto">
              <div className="mb-8">
                <h1 className={`text-2xl font-bold mb-2 ${isDark ? 'text-white' : 'text-gray-900'}`}>My Time Capsules</h1>
                <p className={`text-sm ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  {activeFilter === 'all' ? 'All your time capsules' : `Showing ${activeFilter} capsules`}
                </p>
              </div>

              {/* Status Filter Tabs */}
              <div className={`mb-6 rounded-lg p-1 ${isDark ? 'bg-gray-700' : 'bg-gray-100'}`}>
                <nav className="flex space-x-1">
                  {tabs.map((tab) => (
                    <button
                      key={tab.id}
                      onClick={() => setActiveFilter(tab.id)}
                      className={`py-2 px-4 rounded-md text-sm font-medium transition-colors ${
                        activeFilter === tab.id
                          ? `${isDark ? 'bg-gray-600 text-white shadow' : 'bg-white text-gray-900 shadow-sm'}`
                          : `${isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-600 hover:bg-gray-200'}`
                      }`}
                    >
                      {tab.label}
                    </button>
                  ))}
                </nav>
              </div>

              {/* Capsules List */}
              <div className={`rounded-xl  ${isDark ? 'bg-gray-700' : 'bg-white'} shadow-sm border ${isDark ? 'border-gray-600' : 'border-gray-200'}`}>
                {/* Header Row */}
                <div className={`grid grid-cols-12 items-center gap-4 p-4 border-b ${isDark ? 'border-gray-600 bg-gray-700' : 'border-gray-200 bg-gray-50'} font-medium text-xs uppercase tracking-wider ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                  <div className="col-span-5">Name</div>
                  <div className="col-span-2 text-center">Owner</div>
                  <div className="col-span-2 text-center">Modified</div>
                  <div className="col-span-2 text-center">Status</div>
                  <div className="col-span-1"></div>
                </div>

                {capsules.length === 0 ? (
                  <div className={`p-8 text-center ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                    <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <h3 className="mt-2 text-sm font-medium">No capsules found</h3>
                    <p className="mt-1 text-sm">
                      {activeFilter === 'all' ? "Create your first time capsule to get started" : `No ${activeFilter} capsules available`}
                    </p>
                  </div>
                ) : (
                  capsules.map((capsule) => (
                    <div 
                      key={capsule.id} 
                      className={`grid grid-cols-12 items-center gap-4 p-4 border-b ${isDark ? 'border-gray-600 hover:bg-gray-700/70' : 'border-gray-200 hover:bg-gray-50'} transition-colors cursor-pointer`}
                      onClick={() => handleCapsuleClick(capsule)}
                    >
                      <div className="col-span-5 flex items-center">
                        <div className={`flex-shrink-0 h-10 w-10 rounded-lg ${isDark ? 'bg-gray-600' : 'bg-gray-200'} flex items-center justify-center mr-3`}>
                          <svg className={`h-5 w-5 ${isDark ? 'text-gray-400' : 'text-gray-500'}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                          </svg>
                        </div>
                        <div>
                          <h3 className={`font-medium truncate ${isDark ? 'text-white' : 'text-gray-900'}`}>
                            {capsule.title}
                          </h3>
                          <p className={`text-xs truncate ${isDark ? 'text-gray-400' : 'text-gray-500'}`}>
                            Created {formatDate(capsule.createdAt)}
                          </p>
                        </div>
                      </div>
                      <div className="col-span-2 flex justify-center">
                        <div className="flex items-center">
                          <img 
                            src={ProfilePictureSample} 
                            alt="Owner" 
                            className="h-8 w-8 rounded-full object-cover border-2 border-transparent hover:border-blue-400 transition-colors"
                          />
                        </div>
                      </div>
                      <div className={`col-span-2 text-center text-sm ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                        {formatDate(capsule.updatedAt || capsule.createdAt)}
                      </div>
                      <div className="col-span-2 text-center">
                        <span className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-medium border ${getStatusStyle(capsule.status)}`}>
                          {getStatusLabel(capsule.status)}
                        </span>
                      </div>
                      <div className="col-span-1 flex justify-end relative" ref={openMenuId === capsule.id ? menuRef : null}>
                        <button 
                          className={`p-1 rounded-full ${isDark ? 'text-gray-400 hover:text-gray-300 hover:bg-gray-600' : 'text-gray-500 hover:text-gray-700 hover:bg-gray-200'} transition-colors`}
                          onClick={(e) => handleToggleMenu(e, capsule.id)}
                        >
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                            <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                          </svg>
                        </button>
                        
                        {/* Dropdown Menu */}
                        {openMenuId === capsule.id && (
                          <div className={`absolute right-0 top-8 mt-2 w-56 rounded-lg shadow-lg z-10 ring-1 ring-opacity-5 ${
                            isDark ? 'bg-gray-700 ring-gray-600' : 'bg-white ring-gray-200'
                          }`}>
                            <div className="py-1">
                              <button
                                className={`w-full flex items-center px-4 py-2 text-sm ${
                                  isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                }`}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  navigate(`/view/${capsule.id}`);
                                }}
                              >
                                <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                </svg>
                                View Details
                              </button>
                              
                              {/* Only show Edit option if capsule is not CLOSED, PUBLISHED or ARCHIVED */}
                              {capsule.status !== 'CLOSED' && capsule.status !== 'PUBLISHED' && capsule.status !== 'ARCHIVED' && (
                                <button
                                  className={`w-full flex items-center px-4 py-2 text-sm ${
                                    isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                  }`}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    navigate(`/edit/${capsule.id}`);
                                  }}
                                >
                                  <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                  </svg>
                                  Edit
                                </button>
                              )}
                              
                              {/* Show Unlock option only for CLOSED capsules */}
                              {capsule.status === 'CLOSED' && (
                                <button
                                  className={`w-full flex items-center px-4 py-2 text-sm ${
                                    isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                  }`}
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleUnlockCapsule(capsule.id);
                                  }}
                                >
                                  <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z" />
                                  </svg>
                                  Unlock
                                </button>
                              )}
                              
                              {/* Only show Lock option if capsule is not CLOSED, PUBLISHED or ARCHIVED */}
                              {capsule.status !== 'CLOSED' && capsule.status !== 'PUBLISHED' && capsule.status !== 'ARCHIVED' && (
                                <button
                                  className={`w-full flex items-center px-4 py-2 text-sm ${
                                    isDark ? 'text-gray-300 hover:bg-gray-600' : 'text-gray-700 hover:bg-gray-100'
                                  }`}
                                  onClick={(e) => handleOpenLockModal(e, capsule.id)}
                                >
                                  <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                                  </svg>
                                  Lock
                                </button>
                              )}
                              
                              <button
                                className={`w-full flex items-center px-4 py-2 text-sm ${
                                  isDark ? 'text-red-400 hover:bg-gray-600' : 'text-red-600 hover:bg-gray-100'
                                }`}
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleDeleteCapsule(capsule.id);
                                }}
                              >
                                <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                                </svg>
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
            </div>
          </section>
        </div>
      </div>

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