// import React, {useState, useRef, useEffect, useContext} from 'react';
// import bgmemoire from '../assets/bgmemoire.jpg';
// import ProfilePictureSample from '../assets/ProfilePictureSample.png';
// import { PersonalInfoContext } from './AuthProvider'; // Adjust the path as needed
// import Header from '../components/Header'; // Import the Header component
// import Sidebar from '../components/Sidebar'; // Import the Sidebar component

// const FriendsPage = () => {
//   const reportRef = useRef(null);
//   const { personalInfo } = useContext(PersonalInfoContext);
//   const userData = personalInfo || {
//       username: "",
//       email: "",
//       bio: "",
//       profilePicture: ProfilePictureSample
//     };
  

//   return (
//     <div className="min-h-screen bg-gray-100">
//       <div className="flex flex-col h-screen">
//         {/* Header */}
//         <Header userData={userData} />
        
//         <div className="flex flex-1 h-screen overflow-hidden">
//             <Sidebar />
//           {/* Main Capsule Content */}
//           <section className="flex-1 p-8 overflow-y-auto">
//   <div className="max-w-2xl mx-auto">
//     {/* Header with Friends title and Search bar */}
//     <div className="flex justify-between items-center mb-6">
//       <h1 className="text-2xl font-bold">Friends</h1>
//       <div className="relative w-64">
//         <input
//           type="text"
//           placeholder="Search friends..."
//           className="w-full py-2 px-4 pr-10 rounded-full border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#AF3535] focus:border-transparent"
//         />
//         <svg
//           xmlns="http://www.w3.org/2000/svg"
//           className="h-5 w-5 absolute right-3 top-2.5 text-gray-400"
//           viewBox="0 0 20 20"
//           fill="currentColor"
//         >
//           <path
//             fillRule="evenodd"
//             d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z"
//             clipRule="evenodd"
//           />
//         </svg>
//       </div>
//     </div>

//     {/* Friends List with profile pictures */}
//     <div className="space-y-4">
//       <div className="bg-white rounded-lg shadow-md p-6">
//         <div className="flex items-center">
//           <img 
//             src="https://randomuser.me/api/portraits/women/44.jpg" 
//             alt="Jean Dow"
//             className="h-12 w-12 rounded-full object-cover mr-4"
//           />
//           <div>
//             <strong className="block text-lg">Jean Dow</strong>
//             <p className="text-sm text-gray-500 mt-1">5 capsules created</p>
//           </div>
//         </div>
//       </div>

//       <div className="bg-white rounded-lg shadow-md p-6">
//         <div className="flex items-center">
//           <img 
//             src="https://randomuser.me/api/portraits/women/68.jpg" 
//             alt="Jean Dow"
//             className="h-12 w-12 rounded-full object-cover mr-4"
//           />
//           <div>
//             <strong className="block text-lg">Jean Dow</strong>
//             <p className="text-sm text-gray-500 mt-1">5 capsules created</p>
//           </div>
//         </div>
//       </div>

//       <div className="bg-white rounded-lg shadow-md p-6">
//         <div className="flex items-center">
//           <img 
//             src="https://randomuser.me/api/portraits/women/32.jpg" 
//             alt="Jean Dow"
//             className="h-12 w-12 rounded-full object-cover mr-4"
//           />
//           <div>
//             <strong className="block text-lg">Jean Dow</strong>
//             <p className="text-sm text-gray-500 mt-1">5 capsules created</p>
//           </div>
//         </div>
//       </div>
//     </div>
//   </div>
// </section>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default FriendsPage;