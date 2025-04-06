// components/Sidebar.jsx
import React from 'react';
import { Link } from 'react-router-dom';
import { FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';

const Sidebar = () => {
  return (
    <aside className="w-64 p-4 shadow-md overflow-y-auto">
      <Link to="/create" className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
        <FaPlus className="text-red-700 mr-3" size={20} />
        <span>Create your capsule</span>
      </Link>

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
  );
};

export default Sidebar;