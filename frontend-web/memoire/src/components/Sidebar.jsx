import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';

const Sidebar = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);

  return (
    <>
      <aside className="w-64 p-4 shadow-md overflow-y-auto">
        {/* Create Capsule Button - Now opens modal instead of link */}
        <div 
          onClick={() => setIsModalOpen(true)}
          className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
        >
          <FaPlus className="text-red-700 mr-3" size={20} />
          <span>Create your capsule</span>
        </div>

        <hr className="my-2" />

        <Link to="/homepage" className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
          <FaHome className="text-red-700 mr-3" size={20} />
          <span>Home</span>
        </Link>

        <Link to="/capsules" className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
          <FaStar className="text-red-700 mr-3" size={20} />
          <span>Capsules</span>
        </Link>

        <Link to="/archived_capsules" className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
          <FaShareAlt className="text-red-700 mr-3" size={20} />
          <span>Archived Capsules</span>
        </Link>
        
        <hr className="my-2" />

        <div className="flex justify-between items-center p-3">
          <h4 className="text-lg font-semibold">Friends</h4>
          <Link to="/friends" className="text-sm text-blue-600 hover:text-blue-800 hover:underline">
            See more...
          </Link>
        </div>
      </aside>

      {/* Create Capsule Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-60 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h2 className="text-xl font-semibold mb-6">Create new time capsule</h2>
            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setIsModalOpen(false)}
                className="px-4 py-2 border border-gray-300 rounded hover:bg-gray-100 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  // Add your capsule creation logic here
                  setIsModalOpen(false);
                }}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Sidebar;