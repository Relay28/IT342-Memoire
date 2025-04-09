import React, { useState, useEffect } from 'react';
import { useAuth } from '../components/AuthProvider';
import timeCapsuleService from '../services/timeCapsuleService';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { useNavigate } from 'react-router-dom';

const Capsules = () => {
  const navigate = useNavigate();
  const { user, authToken } = useAuth();
  const [capsules, setCapsules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeMenu, setActiveMenu] = useState(null); // Track which menu is open

  useEffect(() => {
    const fetchCapsules = async () => {
      try {
        setLoading(true);
        setError(null);
        
        // Use the correct method to get ALL capsules
        const fetchedCapsules = await timeCapsuleService.getUserTimeCapsules(authToken);
        
        setCapsules(fetchedCapsules || []);
        
      } catch (err) {
        setError('Failed to load capsules. Please try again.');
        console.error('Error fetching capsules:', err);
      } finally {
        setLoading(false);
      }
    };

    if (authToken) {
      fetchCapsules();
    }
  }, [authToken]);

  const handleDelete = async (id) => {
    try {
      await timeCapsuleService.deleteTimeCapsule(id, authToken);
      // Remove the deleted capsule from state
      setCapsules(capsules.filter(capsule => capsule.id !== id));
    } catch (error) {
      setError('Failed to delete capsule');
      console.error('Error deleting capsule:', error);
    }
  };

  const toggleMenu = (id) => {
    setActiveMenu(activeMenu === id ? null : id);
  };

  // Close menu when clicking outside
  useEffect(() => {
    const handleClickOutside = () => setActiveMenu(null);
    document.addEventListener('click', handleClickOutside);
    return () => document.removeEventListener('click', handleClickOutside);
  }, []);

  // Format date helper
  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const options = { year: 'numeric', month: 'long', day: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen">
        <Header />
        
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />

          <section className="flex-1 p-8 overflow-y-auto">
            <div className="max-w-4xl mx-auto">
              {/* Header Row */}
              <div className="grid grid-cols-6 items-center gap-4 font-bold pb-4 mb-2">
                <div className="col-span-2 pl-4">Name</div>
                <div className="text-center">Owner</div>
                <div className="text-center">Modified last</div>
                <div className="text-center">Type</div>
                <div className="w-8"></div>
              </div>

              {loading ? (
                <div className="flex justify-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-red-600"></div>
                </div>
              ) : error ? (
                <div className="p-4 bg-red-100 text-red-700 rounded-lg">
                  {error}
                </div>
              ) : capsules.length === 0 ? (
                <div className="p-4 bg-gray-50 text-gray-500 rounded-lg text-center">
                  No capsules found. Create your first time capsule!
                </div>
              ) : (
                capsules.map(capsule => (
                  <div 
        key={capsule.id}
        onClick={() => {
          // Only navigate to edit if user clicks the row (not the menu)
          if (!activeMenu) {
            navigate(`/edit/${capsule.id}`);
          }
        }}
        className={`grid grid-cols-6 items-center gap-4 p-4 mb-3 bg-white rounded-lg shadow-md hover:bg-gray-50 transition-colors ${
          !activeMenu ? 'cursor-pointer' : ''
        }`}
      >
                    <div className="col-span-2 font-semibold truncate pl-4">
                      {capsule.title || 'Untitled Capsule'}
                    </div>
                    <div className="flex justify-center">
                      <img 
                        src={
                          // If current user is the owner, use auth user's picture
                          user?.id === capsule.owner?.id 
                            ? user.profilePicture 
                            : capsule.owner?.profilePicture || ProfilePictureSample
                        } 
                        alt={capsule.owner?.name || 'Owner'}  
                        className="h-8 w-8 rounded-full object-cover"
                      />
                    </div>
                    <div className="text-center">
                      {formatDate(capsule.updatedAt || capsule.createdAt)}
                    </div>
                    <div className="text-center text-gray-500">
                      {capsule.isPrivate ? 'Private' : 'Public'}
                    </div>
                    <div className="flex justify-center">
                  <div className="relative">
                    <button 
                      onClick={(e) => {
                        e.stopPropagation();
                        toggleMenu(capsule.id);
                      }}
                      className="text-gray-400 hover:text-gray-600 focus:outline-none"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                        <path d="M10 6a2 2 0 110-4 2 2 0 010 4zM10 12a2 2 0 110-4 2 2 0 010 4zM10 18a2 2 0 110-4 2 2 0 010 4z" />
                      </svg>
                    </button>
                    
                    {/* Dropdown menu */}
                    {activeMenu === capsule.id && (
                      <div className="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg z-10">
                        <div className="py-1">
                          <button
                            onClick={() => handleDelete(capsule.id)}
                            className="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
                ))
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Capsules;