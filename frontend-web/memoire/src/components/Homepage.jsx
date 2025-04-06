import React from 'react';
import '../css/homepage.css';
import mmrlogo from '../assets/mmrlogo.png';
import bgmemoire from '../assets/bgmemoire.jpg';
import ProfilePictureSample from '../assets/ProfilePictureSample.png';
import { FaSearch, FaMoon, FaBell, FaPlus, FaHome, FaStar, FaShareAlt } from 'react-icons/fa';

const Homepage = () => {
  return (
    <div className="homepage-wrapper">
      <div className="homepage-container">
        {/* Header */}
        <header className="homepage-header">
          <button className="logo-btn">
            <img src={mmrlogo} alt="Mémoire Logo" className="logo-img-homepage" />
            <div className="homepage-logo">MÉMOIRE</div>
          </button>

          <div className="search-container">
            <FaSearch color="#b22222" />
            <input type="text" placeholder="Search here..." />
          </div>

          <div className="header-icons">
            <button className="icon-btn">
              <FaMoon size={32} />
            </button>
            <button className="icon-btn">
              <FaBell size={32} />
            </button>
            <button className="profile-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" />
            </button>
          </div>
        </header>

        <div className="main-content">
          {/* Sidebar */}
          <aside className="sidebar">
            <div className="menu-item"><FaPlus color="#b22222" size={24} /> Create your capsule</div>
            <div className="hr-css"><hr /></div>
            <div className="menu-item"><FaHome color="#b22222" size={24} /> Home</div>
            <div className="menu-item"><FaShareAlt color="#b22222" size={24} /> Analytics</div>
            <div className="menu-item"><FaStar color="#b22222" size={24} /> Capsules</div>
            <div className="hr-css"><hr /></div>

            <h4 className="friends-header">Friends</h4>

            <button className="friend-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" /> Jean Dow
            </button>
            <button className="friend-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" /> Lee JungShibal
            </button>
            <button className="friend-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" /> Aniyo Moragu
            </button>
            <button className="friend-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" /> Eomma ImongMama
            </button>
            <button className="friend-btn">
              <img src={ProfilePictureSample} alt="user" className="profile-pic" /> Jinja Lee
            </button>
          </aside>

          {/* Main Capsule Content */}
          <section className="content">
            <div className="capsule-card">
              {/* Ellipsis Button and Dropdown */}
              <div className="ellipsis-container">
                <button className="ellipsis-btn" onClick={() => document.getElementById('dropdown').classList.toggle('show')}>
                  ⋯
                </button>
                <div id="dropdown" className="dropdown-content">
                  <button>Report</button>
                </div>
              </div>

              <div className="capsule-header">
                <img src={ProfilePictureSample} alt="user" className="profile-pic" />
                <div className="user-info">
                  <strong>Georgia Santos</strong>
                  <p>Opened on February 24, 2025</p>
                </div>
              </div>

              <div className="capsule-info">
                <p>Its been a year...</p>
                <hr />
                <div className="capsule-name">
                  Memories of 2024
                  <div className="capsule-created-on">Created on February 24, 2025</div>
                </div>
                <p>Hi Self! Open this after a year to reminisce wompwomp</p>
              </div>

              <div className="photos">
                <img src={bgmemoire} alt="memory" />
                <img src={bgmemoire} alt="memory" />
                <img src={bgmemoire} alt="memory" />
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Homepage;
