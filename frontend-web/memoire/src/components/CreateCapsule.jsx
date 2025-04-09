import React, { useState, useEffect } from 'react';
import Header from '../components/Header';
import Sidebar from '../components/Sidebar';
import { FiLock, FiShare2, FiUsers, FiEye, FiPlus, FiUnlock } from 'react-icons/fi';
import timeCapsuleService from '../services/timeCapsuleService';
import { useAuth } from '../components/AuthProvider';
import { useNavigate, useParams } from 'react-router-dom';
import ShareModal from '../components/ShareCapsule/ShareModal'; 

const CreateCapsule = ({ mode = 'create' }) => {
  const { id } = useParams();
  const [capsuleData, setCapsuleData] = useState(null);
  const { authToken } = useAuth();
  const navigate = useNavigate();
  const [showShareModal, setShowShareModal] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  // Form state
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    content: '',
    isPrivate: true,
    openDate: null
  });
  
  // UI state
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ text: '', type: '' });
  const [mediaFiles, setMediaFiles] = useState([]);
  
  useEffect(() => {
    if (mode === 'edit' && id) {
      const loadCapsule = async () => {
        try {
          const capsule = await timeCapsuleService.getTimeCapsule(id, authToken);
          setFormData({
            title: capsule.title,
            description: capsule.description,
            content: capsule.content,
            isPrivate: capsule.isPrivate,
            openDate: capsule.openDate
          });
        } catch (error) {
          console.error('Failed to load capsule:', error);
          navigate('/capsules');
        }
      };
      loadCapsule();
    }
  }, [mode, id, authToken, navigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };
  
  // In your component
const handleFileChange = (e) => {
  const files = Array.from(e.target.files);
  
  // Validate files
  const validFiles = files.filter(file => {
    const validTypes = ['image/jpeg', 'image/png', 'video/mp4'];
    const maxSize = 10 * 1024 * 1024; // 10MB
    
    if (!validTypes.includes(file.type)) {
      setError(`Invalid file type: ${file.name}`);
      return false;
    }
    
    if (file.size > maxSize) {
      setError(`File too large (max 10MB): ${file.name}`);
      return false;
    }
    
    return true;
  });

  setMediaFiles(prev => [...prev, ...validFiles]);
};
  
  const removeMediaFile = (index) => {
    setMediaFiles(prev => prev.filter((_, i) => i !== index));
  };
  
  const togglePrivacy = () => {
    setFormData(prev => ({
      ...prev,
      isPrivate: !prev.isPrivate
    }));
  };
  
  // Modify your handleSubmit to skip media upload:
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.title.trim()) {
      setError('Title is required');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      if (mode === 'create') {
        await timeCapsuleService.createTimeCapsule(formData, authToken);
        setSuccess('Capsule created successfully!');
      } else {
        // For edit, use existing methods
        await timeCapsuleService.updateTimeCapsule(id, formData, authToken);
        setSuccess('Capsule updated successfully!');
      }
      
      // After save, always go back to capsules list
      setTimeout(() => navigate('/capsules'), 1500);
      
    } catch (error) {
      setError(error.message || 'Failed to save capsule');
    } finally {
      setLoading(false);
    }
  };
  
  return (
    <div className="flex flex-col h-screen bg-gray-50">
      <Header />
      
      <div className="flex flex-1 h-screen overflow-hidden">
        <Sidebar />
        
        {/* Main Content Area */}
        <main className="flex-1 overflow-y-auto p-6">
          {/* Top Section with Title and Action Buttons */}
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold text-gray-800">Create your capsule</h1>
            
            <div className="flex items-center space-x-4">
              {/* Access Indicators */}
              <div className="flex items-center space-x-2 text-gray-600">
                <div className="flex items-center space-x-1 bg-gray-100 px-2 py-1 rounded">
                  <FiUsers className="text-red-600" />
                  <span className="text-sm">3</span>
                </div>
                
                <div className="flex items-center space-x-1 bg-gray-100 px-2 py-1 rounded">
                  <FiEye className="text-red-600" />
                  <span className="text-sm">5</span>
                </div>
              </div>
              
              {/* Privacy Toggle */}
              <button 
                onClick={togglePrivacy}
                className={`p-2 rounded-full transition-colors ${
                  formData.isPrivate 
                    ? 'text-gray-600 hover:text-red-600' 
                    : 'text-red-600 bg-red-100'
                }`}
                title={formData.isPrivate ? "Make public" : "Make private"}
              >
                {formData.isPrivate ? (
                  <FiLock className="text-lg" />
                ) : (
                  <FiUnlock className="text-lg" />
                )}
              </button>
              
              {/* Share Button */}
              <button 
                onClick={() => setShowShareModal(true)}
                className="flex items-center space-x-2 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition-colors"
                disabled={loading}
              >
                <FiShare2 />
                <span>Share</span>
              </button>

              {/* Share Modal */}
              {showShareModal && (
                <ShareModal 
                  title={formData.title || "Untitled Capsule"} 
                  onClose={() => setShowShareModal(false)} 
                />
              )}
            </div>
          </div>
          
          {/* Status Message */}
          {message.text && (
            <div className={`mb-4 p-3 rounded ${message.type === 'error' ? 'bg-red-100 text-red-700' : 'bg-green-100 text-green-700'}`}>
              {message.text}
            </div>
          )}

          <div className="border-t border-gray-200 my-4"></div>

          {/* Capsule Content */}
          <form onSubmit={handleSubmit} className="space-y-6 bg-white p-6 rounded-lg shadow-sm">
            <div>
              <input
                type="text"
                name="title"
                placeholder="Title"
                className="w-full text-2xl font-bold p-2 border-b border-gray-200 focus:outline-none focus:border-red-600"
                value={formData.title}
                onChange={handleChange}
                required
              />
            </div>
            
            <div>
              <input
                type="text"
                name="description"
                placeholder="Description"
                className="w-full text-lg p-2 border-b border-gray-200 focus:outline-none focus:border-red-600"
                value={formData.description}
                onChange={handleChange}
                required
              />
            </div>
            
            <div>
              <textarea
                name="content"
                placeholder="Content Area"
                className="w-full h-40 p-3 border border-gray-200 rounded focus:outline-none focus:border-red-600 focus:ring-1 focus:ring-red-100"
                value={formData.content}
                onChange={handleChange}
                required
              />
            </div>
            
            
            {/* Media Upload Area */}
            <div className="border border-dashed border-gray-300 rounded-lg p-4 text-center">
              <label className="flex items-center justify-center space-x-2 text-red-600 hover:text-red-700 mx-auto cursor-pointer">
                <input 
                  type="file" 
                  multiple 
                  className="hidden" 
                  onChange={handleFileChange}
                  accept="image/*,video/*,audio/*"
                />
                <FiPlus />
                <span>Add media</span>
              </label>
              <p className="text-xs text-gray-500 mt-2">Supports images, videos, and audio files</p>
            </div>
            
            {/* Media Preview */}
            {mediaFiles.length > 0 && (
              <div className="space-y-3">
                <h3 className="font-medium text-gray-700">Attached Media:</h3>
                {mediaFiles.map((file, index) => (
                  <div key={index} className="p-3 border border-gray-200 rounded bg-gray-50 flex justify-between items-center">
                    <span>{file.name}</span>
                    <button 
                      type="button"
                      onClick={() => removeMediaFile(index)}
                      className="text-gray-400 hover:text-red-600"
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
            
            {/* Save Button */}
            <div className="pt-4">
              <button 
                type="submit"
                disabled={loading}
                className={`flex items-center space-x-2 px-6 py-2 rounded-md transition-colors ${loading ? 'bg-gray-400 cursor-not-allowed' : 'bg-red-600 hover:bg-red-700 text-white'}`}
              >
                {loading ? (
                  <>
                    <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Processing...
                  </>
                ) : (
                  <>
                    <FiPlus />
                    <span>Save Capsule</span>
                  </>
                )}
              </button>
            </div>
          </form>
        </main>
      </div>
    </div>
  );
}

export default CreateCapsule;