import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { FiLock, FiUnlock, FiShare2, FiUsers, FiEye, FiPlus, FiArrowLeft } from 'react-icons/fi';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import { useAuth } from '../components/AuthProvider';
import CapsuleContentGallery from './MediaShower/CapsuleContentGallery';
import { useThemeMode } from '../context/ThemeContext';
import ShareModal from './modals/ShareModal';
import LockDateModal from '../components/modals/LockDateModal';

export default function CreateCapsule() {
  const { isDark } = useThemeMode();
  const { id } = useParams();
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [capsuleData, setCapsuleData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showShareModal, setShowShareModal] = useState(false);
  const [showLockModal, setShowLockModal] = useState(false);
  
  const { isAuthenticated, user } = useAuth();
  const { getTimeCapsule, updateTimeCapsule } = useTimeCapsule();

  // Save changes automatically with debounce
  useEffect(() => {
    if (!capsuleData || !id) return;

    const timer = setTimeout(() => {
      if (title !== capsuleData.title || description !== capsuleData.description) {
        updateTimeCapsule(id, { title, description })
          .then(updatedData => {
            setCapsuleData(updatedData);
          })
          .catch(err => {
            console.error('Failed to update capsule:', err);
          });
      }
    }, 1000); // 1 second debounce

    return () => clearTimeout(timer);
  }, [title, description, id, capsuleData]);

  // Load capsule data
  useEffect(() => {
    const loadData = async () => {
      try {
        if (id) {
          const data = await getTimeCapsule(id);
          setCapsuleData(data);
          setTitle(data.title);
          setDescription(data.description);
        }
        setLoading(false);
      } catch (err) {
        setError(err.message);
        setLoading(false);
      }
    };

    if (isAuthenticated) {
      loadData();
    } else {
      setError('You must be logged in to view this page');
      setLoading(false);
    }
  }, [id, isAuthenticated]);

  const handleLockSuccess = () => {
    // Update capsule data to reflect locked status
    if (capsuleData) {
      setCapsuleData({
        ...capsuleData,
        isLocked: true
      });
    }
    // Navigate to locked capsules page
    navigate('/locked-capsules');
  };

  if (loading) {
    return (
      <div className={`flex flex-col h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <main className={`flex-1 overflow-y-auto p-6 flex items-center justify-center ${isDark ? 'text-gray-300' : ''}`}>
            <div className="animate-pulse">Loading capsule details...</div>
          </main>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className={`flex flex-col h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
        <Header />
        <div className="flex flex-1 h-screen overflow-hidden">
          <Sidebar />
          <main className={`flex-1 overflow-y-auto p-6 flex items-center justify-center ${isDark ? 'text-gray-300' : ''}`}>
            <div className="p-4 rounded-lg bg-red-100 text-red-700 dark:bg-red-900 dark:text-red-200">
              Error: {error}
            </div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className={`flex flex-col h-screen ${isDark ? 'bg-gray-900' : 'bg-gray-50'}`}>
      <Header />
      
      <div className="flex flex-1 h-screen overflow-hidden">
        <Sidebar />
        
        <main className={`flex-1 overflow-y-auto ${isDark ? 'bg-gray-800' : 'bg-gray-50'}`}>
          {/* Header Section */}
          <div className={`sticky top-1 z-10 p-6 pb-4 ${isDark ? 'bg-gray-800' : 'bg-gray-50'} shadow-sm`}>
            <div className="flex flex-col space-y-4">
              <div className="flex items-center justify-between">
                <button 
                  onClick={() => navigate('/capsules')}
                  className={`flex items-center space-x-2 p-2 rounded-lg transition-colors ${
                    isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-gray-100 text-gray-600'
                  }`}
                >
                  <FiArrowLeft className="text-lg" />
                </button>
                <input
                  type="text"
                  placeholder="Capsule Title"
                  className={`w-full text-lg font-bold p-2 focus:outline-none ${
                    isDark ? 'bg-gray-800 text-white placeholder-gray-500' : 'bg-gray-50 text-gray-900 placeholder-gray-400'
                  }`}
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  disabled={capsuleData?.isLocked} // Disable if capsule is locked
                />
                
                <div className="flex items-center space-x-3">
                  <div className={`flex items-center space-x-2 ${isDark ? 'text-gray-300' : 'text-gray-600'}`}>
                    <div className={`flex items-center space-x-1 px-2 py-1 rounded-full text-xs ${
                      isDark ? 'bg-gray-700' : 'bg-gray-100'
                    }`}>
                      <FiUsers className="text-[#AF3535] text-sm" />
                      <span>3</span>
                    </div>
                    
                    <div className={`flex items-center space-x-1 px-2 py-1 rounded-full text-xs ${
                      isDark ? 'bg-gray-700' : 'bg-gray-100'
                    }`}>
                      <FiEye className="text-[#AF3535] text-sm" />
                      <span>5</span>
                    </div>
                  </div>
                  
                  {!capsuleData?.isLocked ? (
                    <button 
                      onClick={() => setShowLockModal(true)}
                      className={`p-1.5 rounded-full transition-colors ${
                        isDark ? 'hover:bg-gray-700 text-gray-300' : 'hover:bg-gray-100 text-gray-600'
                      }`}
                      title="Lock this capsule"
                    >
                      <FiUnlock className="text-sm" />
                    </button>
                  ) : (
                    <div 
                      className={`p-1.5 rounded-full ${
                        isDark ? 'text-gray-400' : 'text-gray-500'
                      }`}
                      title="Capsule is locked"
                    >
                      <FiLock className="text-sm" />
                    </div>
                  )}
                  
                  <button 
                    onClick={() => setShowShareModal(true)}
                    className="flex items-center space-x-1 bg-gradient-to-r from-[#AF3535] to-red-600 text-white px-3 py-1.5 rounded-md text-sm hover:from-[#AF3535] hover:to-red-700 transition-all shadow-sm"
                    disabled={capsuleData?.isLocked} // Disable if capsule is locked
                  >
                    <FiShare2 className="text-xs" />
                    <span>Share</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          {/* Content Area */}
          <div className="p-6 space-y-6">
            {/* Description Section */}
            <div className={`rounded-xl p-6 ${isDark ? 'bg-gray-700' : 'bg-white'} shadow-sm`}>
              <h2 className={`text-lg font-semibold mb-4 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                Description
              </h2>
              <input
                type="text"
                placeholder="What's this capsule about?"
                className={`w-full p-3 rounded-lg border focus:outline-none focus:ring-2 focus:ring-red-500 ${
                  isDark ? 'bg-gray-600 text-white border-gray-500 placeholder-gray-400' : 'border-gray-200'
                }`}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                disabled={capsuleData?.isLocked} // Disable if capsule is locked
              />
            </div>
            
            {/* Media Gallery Section */}
            <div className={`rounded-xl overflow-hidden ${isDark ? 'bg-gray-700' : 'bg-white'} shadow-sm`}>
              <div className={`p-6 border-b ${isDark ? 'border-gray-600' : 'border-gray-200'}`}>
                <h2 className={`text-lg font-semibold ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>
                  Media Content
                </h2>
              </div>
              <div className="p-4">
                <CapsuleContentGallery capsuleId={id} isLocked={capsuleData?.isLocked} />
              </div>
            </div>
          </div>
        </main>
      </div>

      {/* Share Modal */}
      {showShareModal && capsuleData && user && (
        <ShareModal 
          title={capsuleData.title} 
          onClose={() => setShowShareModal(false)} 
          capsuleData={capsuleData}
          capsuleId = {id}
        />
      )}


      {/* Lock Modal */}
      {showLockModal && (
        <LockDateModal
          isOpen={showLockModal}
          onClose={() => setShowLockModal(false)}
          timeCapsuleId={id}
          onSuccess={handleLockSuccess}
          
        />
        
      )}
    </div>
  );
}