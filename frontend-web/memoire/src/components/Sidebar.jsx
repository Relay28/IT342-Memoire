// src/components/Sidebar.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';
import { useTimeCapsule } from '../hooks/useTimeCapsule';
import { useThemeMode } from '../context/ThemeContext';

const Sidebar = () => {
  const [isFirstModalOpen, setIsFirstModalOpen] = useState(false);
  const [isSecondModalOpen, setIsSecondModalOpen] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const navigate = useNavigate();
  const { createTimeCapsule, loading, error } = useTimeCapsule();
  const { mode, isDark } = useThemeMode();

  const handleCreateClick = () => {
    setIsFirstModalOpen(true);
  };

  const handleFirstModalConfirm = () => {
    setIsFirstModalOpen(false);
    setIsSecondModalOpen(true);
  };

  const handleSecondModalConfirm = async () => {
    try {
      const capsuleData = { title, description };
      const response = await createTimeCapsule(capsuleData);
      navigate(`/edit/${response.id}`);
    } catch (err) {
      console.error('Failed to create capsule:', err);
    }
    setIsSecondModalOpen(false);
  };

  return (
    <>
      <aside className={`w-64 p-4 shadow-md overflow-y-auto ${isDark ? 'bg-gray-900 text-white' : 'bg-white text-gray-900'}`}>
        {/* Create Capsule Button */}
        <div 
          onClick={handleCreateClick}
          className={`flex items-center p-3 rounded-lg ${isDark ? 'hover:bg-gray-700' : 'hover:bg-gray-100'} cursor-pointer`}
        >
          <FaPlus className="text-[#AF3535] mr-3" size={20} />
          <span>Create your capsule</span>
        </div>

        <hr className={`my-2 ${isDark ? 'border-gray-700' : 'border-gray-200'}`} />

        <Link 
          to="/homepage" 
          className={`flex items-center p-3 rounded-lg ${isDark ? 'hover:bg-gray-700' : 'hover:bg-gray-100'} cursor-pointer`}
        >
          <FaHome className="text-[#AF3535] mr-3" size={20} />
          <span>Home</span>
        </Link>

        <Link 
          to="/capsules" 
          className={`flex items-center p-3 rounded-lg ${isDark ? 'hover:bg-gray-700' : 'hover:bg-gray-100'} cursor-pointer`}
        >
          <FaStar className="text-[#AF3535] mr-3" size={20} />
          <span>Capsules</span>
        </Link>

        <Link 
          to="/archived_capsules" 
          className={`flex items-center p-3 rounded-lg ${isDark ? 'hover:bg-gray-700' : 'hover:bg-gray-100'} cursor-pointer`}
        >
          <FaShareAlt className="text-[#AF3535] mr-3" size={20} />
          <span>Archived Capsules</span>
        </Link>
        
        <hr className={`my-2 ${isDark ? 'border-gray-700' : 'border-gray-200'}`} />

        <div className="flex justify-between items-center p-3">
          <h4 className="text-lg font-semibold">Friends</h4>
          <Link 
            to="/friends" 
            className={`text-sm ${isDark ? 'text-[#AF3535] hover:text-red-300' : 'text-[#AF3535] hover:text-red-800'} hover:underline`}
          >
            See more...
          </Link>
        </div>
      </aside>

      {/* First Modal - Confirmation */}
      {isFirstModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
          <div className={`rounded-lg p-6 w-full max-w-md ${isDark ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'}`}>
            <h2 className="text-xl font-semibold mb-6">Create new time capsule</h2>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setIsFirstModalOpen(false)}
                className={`px-4 py-2 border ${isDark ? 'border-gray-600 hover:bg-gray-700' : 'border-gray-300 hover:bg-gray-100'} rounded transition-colors`}
              >
                Cancel
              </button>
              <button
                onClick={handleFirstModalConfirm}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Second Modal - Title and Description */}
      {isSecondModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
          <div className={`rounded-lg p-6 w-full max-w-md ${isDark ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'}`}>
            <h2 className="text-xl font-semibold mb-6">Enter capsule details</h2>
            
            <div className="space-y-4 mb-6">
              <div>
                <label className={`block text-sm font-medium mb-1 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>Title</label>
                <input
                  type="text"
                  className={`w-full p-2 border rounded focus:outline-none focus:ring-1 focus:ring-red-500 focus:border-red-500 ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'border-gray-300'}`}
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Enter capsule title"
                />
              </div>
              
              <div>
                <label className={`block text-sm font-medium mb-1 ${isDark ? 'text-gray-300' : 'text-gray-700'}`}>Description</label>
                <textarea
                  className={`w-full p-2 border rounded focus:outline-none focus:ring-1 focus:ring-red-500 focus:border-red-500 ${isDark ? 'bg-gray-700 border-gray-600 text-white' : 'border-gray-300'}`}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Enter capsule description"
                  rows={3}
                />
              </div>
            </div>
            
            {error && <div className="text-red-500 text-sm mb-4">{error}</div>}
            
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setIsSecondModalOpen(false)}
                className={`px-4 py-2 border ${isDark ? 'border-gray-600 hover:bg-gray-700' : 'border-gray-300 hover:bg-gray-100'} rounded transition-colors`}
              >
                Cancel
              </button>
              <button
                onClick={handleSecondModalConfirm}
                disabled={loading}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors disabled:opacity-50"
              >
                {loading ? 'Creating...' : 'Create Capsule'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Sidebar;
