import React, {useState, useRef, useEffect} from 'react';
import mmrlogo from '../assets/mmrlogo.png';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';
import { Link } from "react-router-dom";

const ProfilePage = () => {
  const [isProfileOpen, setIsProfileOpen] = useState(false);

  const toggleProfile = () => {
    setIsProfileOpen(!isProfileOpen);
  };
  return (
    <div className="min-h-screen bg-gray-100">
      <div className="flex flex-col h-screen overflow-hidden">
        {/* Header */}
        <header className="flex items-center justify-between p-4 bg-white shadow-md">
          <button className="flex items-center space-x-2">
            <img src={mmrlogo} alt="Mémoire Logo" className="h-10 w-10" />
            <div className="text-2xl font-bold text-red-700">MÉMOIRE</div>
          </button>

          <div className="flex items-center px-4 py-2 bg-gray-100 rounded-full w-1/3">
            <FaSearch className="text-red-700 mr-2" />
            <input 
              type="text" 
              placeholder="Search here..." 
              className="bg-transparent border-none outline-none w-full"
            />
          </div>

          <div className="flex items-center space-x-4">
          <button className="p-2 rounded-full hover:bg-red-100">
            <FaMoon size={24} className="text-[#AF3535]" />
          </button>
          <button className="p-2 rounded-full hover:bg-red-100">
            <FaBell size={24} className="text-[#AF3535]" />
          </button>
          <div className="relative">
  <button 
    className="p-1 focus:outline-none transition-all duration-200 hover:ring-2 hover:ring-[#AF3535]/30 rounded-full"
    onClick={toggleProfile}
    aria-label="User menu"
  >
    <div className="relative">
      <img 
        src={ProfilePictureSample} 
        alt="User profile" 
        className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover hover:brightness-95 transition-all duration-200"
      />
      {isProfileOpen && (
        <div className="absolute inset-0 rounded-full bg-[#AF3535]/20 animate-pulse"></div>
      )}
    </div>
  </button>

  {/* Dropdown Menu */}
  {isProfileOpen && (
    <div className="absolute right-0 mt-2 w-56 origin-top-right divide-y divide-gray-100 rounded-lg bg-white shadow-lg ring-1 ring-black/5 focus:outline-none z-50 animate-enter">
      <div className="px-1 py-1">
        <div className="flex items-center gap-3 px-4 py-3">
          <img 
            src={ProfilePictureSample} 
            alt="User" 
            className="h-10 w-10 rounded-full border-2 border-[#AF3535] object-cover"
          />
          <div>
            <p className="text-sm font-medium text-gray-900">John Doe</p>
            <p className="text-xs text-gray-500">john@example.com</p>
          </div>
        </div>
      </div>
      <div className="px-1 py-1">
        <button
          className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
          onClick={() => {
            // Handle profile navigation
            setIsProfileOpen(false);
          }}
          
        >
          <svg className="h-5 w-5 text-gray-400 group-hover:text-[#AF3535]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <Link to="/profile" className=" text-sm no-underline hover:underline" >
                          Profile
          </Link>
        </button>
      </div>
      <div className="px-1 py-1">
        <button
          className="group flex w-full items-center gap-2 rounded-md px-4 py-2 text-sm text-gray-900 hover:bg-[#AF3535]/10 hover:text-[#AF3535] transition-colors"
          onClick={() => {
            // Handle logout
            setIsProfileOpen(false);
          }}
        >
          <svg className="h-5 w-5 text-gray-400 group-hover:text-[#AF3535]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
          Logout
        </button>
      </div>
    </div>
  )}
</div>
          </div>
        </header>

        <div className="flex flex-1 overflow-hidden">
          {/* Sidebar */}
          <aside className="w-64 p-4 shadow-md overflow-y-auto">
            <div className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
              <FaPlus className="text-red-700 mr-3" size={20} />
              <span>Create your capsule</span>
            </div>
            <hr className="my-2" />
            <Link 
            to="/homepage" 
            className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer"
            >
            <FaHome className="text-red-700 mr-3" size={20} />
            <span>Home</span>
            </Link>
            <div className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
              <FaShareAlt className="text-red-700 mr-3" size={20} />
              <span>Analytics</span>
            </div>
            <div className="flex items-center p-3 rounded-lg hover:bg-gray-100 cursor-pointer">
              <FaStar className="text-red-700 mr-3" size={20} />
              <span>Capsules</span>
            </div>
            <hr className="my-2" />

            <div className="flex justify-between items-center p-3">
              <h4 className="text-lg font-semibold">Friends</h4>
              <Link 
                to="/friends" 
                className="text-sm text-blue-600 hover:text-blue-800 hover:underline"
              >
                See more...
              </Link>
            </div>
           
          </aside>

          {/* Main Profile Content */}
<section className="flex-1 p-8 overflow-y-auto">
  <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md overflow-hidden">
    {/* Profile Header */}
    <div className="p-6 border-b border-gray-200">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <div className="relative">
            <img 
              src={ProfilePictureSample} 
              alt="Profile" 
              className="h-24 w-24 rounded-full object-cover border-2 border-[#AF3535]"
            />
            <button 
  className="absolute bottom-0 right-0 bg-[#AF3535] text-white p-1.5 rounded-full hover:bg-[#AF3535]/90 transition-colors"
  onClick={() => document.getElementById('profilePictureInput').click()}
>
  <svg 
    xmlns="http://www.w3.org/2000/svg" 
    className="h-5 w-5" 
    viewBox="0 0 20 20" 
    fill="currentColor"
  >
    <path 
      fillRule="evenodd" 
      d="M4 5a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V7a2 2 0 00-2-2h-1.586a1 1 0 01-.707-.293l-1.121-1.121A2 2 0 0011.172 3H8.828a2 2 0 00-1.414.586L6.293 4.707A1 1 0 015.586 5H4zm6 9a3 3 0 100-6 3 3 0 000 6z" 
      clipRule="evenodd" 
    />
  </svg>
  <input 
    id="profilePictureInput" 
    type="file" 
    className="hidden" 
    accept="image/*"
    onChange={(e) => {/* Handle image upload */}}
  />
</button>
          </div>
          
          <div>
            <h1 className="text-2xl font-bold">Georgia Santos</h1>
            <p className="text-gray-600">@georgia.santos</p>
          </div>
        </div>
        
        <button 
          className="px-4 py-2 bg-[#AF3535] text-white rounded-md hover:bg-[#AF3535]/90 transition-colors"
          onClick={() => {/* Handle save changes */}}
        >
          Save Changes
        </button>
      </div>
    </div>

    {/* Profile Edit Form */}
    <div className="p-6">
      <div className="space-y-6">
        {/* Biography Section */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Biography</label>
          <textarea
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535]"
            rows="3"
            placeholder="Tell your story..."
            defaultValue="Hi Self! Open this after a year to reminisce wompwomp"
          />
        </div>

        {/* Email Section */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input
            type="email"
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-[#AF3535]"
            defaultValue="georgia@example.com"
          />
        </div>

        {/* Account Actions */}
        <div className="pt-4 border-t border-gray-200">
          <h3 className="text-lg font-medium text-gray-900 mb-3">Account Actions</h3>
          
          <div className="space-y-3">
            <button 
              className="w-full text-left px-4 py-3 bg-gray-100 hover:bg-gray-200 rounded-md transition-colors"
              onClick={() => {/* Handle change password */}}
            >
              <div className="font-medium">Change Password</div>
              <p className="text-sm text-gray-500">Update your account password</p>
            </button>
            
            <button 
              className="w-full text-left px-4 py-3 bg-red-50 hover:bg-red-100 text-red-600 rounded-md transition-colors"
              onClick={() => {/* Handle temporary deactivation */}}
            >
              <div className="font-medium">Temporarily Deactivate Account</div>
              <p className="text-sm text-red-500">Hide your profile and content</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</section>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;