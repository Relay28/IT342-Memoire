import React, { useState, useRef } from 'react';
import ImageDisplay from './ImageDisplay';
import VideoPlayer from './VideoPlayer';

const MediaCarousel = ({ mediaItems, fallbackImage }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [transitionDirection, setTransitionDirection] = useState(null);
  const [fullscreenItem, setFullscreenItem] = useState(null);
  const carouselRef = useRef(null);
  
  const currentItem = mediaItems[currentIndex];

  const nextSlide = () => {
    setTransitionDirection('right');
    setCurrentIndex((prevIndex) => 
      prevIndex === mediaItems.length - 1 ? 0 : prevIndex + 1
    );
  };

  const prevSlide = () => {
    setTransitionDirection('left');
    setCurrentIndex((prevIndex) => 
      prevIndex === 0 ? mediaItems.length - 1 : prevIndex - 1
    );
  };

  const goToSlide = (index) => {
    setTransitionDirection(index > currentIndex ? 'right' : 'left');
    setCurrentIndex(index);
  };

  const toggleFullscreen = (media) => {
    setFullscreenItem(fullscreenItem ? null : { media });
  };

  const closeFullscreen = () => {
    setFullscreenItem(null);
  };

  const isImage = (media) => {
    return media.contentType?.startsWith('image/') || 
           media.url.match(/\.(jpeg|jpg|gif|png|webp)$/i) !== null;
  };

  return (
    <div className="relative w-full aspect-[3/2] overflow-hidden rounded-2xl shadow-lg" ref={carouselRef}>
      {/* Media display with transition effects */}
      <div className="relative h-full w-full bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-800 dark:to-gray-900">
        <div 
          className={`h-full w-full absolute top-0 left-0 ${
            transitionDirection === 'right' 
              ? 'animate-slide-in-right' 
              : transitionDirection === 'left' 
                ? 'animate-slide-in-left' 
                : ''
          }`}
          key={currentIndex}
        >
          <div 
            className="h-full w-full relative overflow-hidden cursor-pointer group"
            onClick={() => toggleFullscreen(currentItem)}
          >
            {isImage(currentItem) ? (
              <ImageDisplay 
                src={currentItem.url}
                alt={`Media content ${currentIndex + 1}`}
                className="h-full w-full object-cover transition-all duration-500 group-hover:scale-105"
                fallback={fallbackImage}
              />
            ) : (
              <VideoPlayer 
                src={currentItem.url}
                className="h-full w-full object-cover"
                controls
              />
            )}
            
            {/* Overlay gradient */}
            <div className="absolute inset-0 bg-gradient-to-t from-black/30 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
            
            {/* Media counter */}
            {mediaItems.length > 1 && (
              <div className="absolute bottom-4 left-4 bg-black/50 text-white text-xs px-2 py-1 rounded-full backdrop-blur-sm">
                {currentIndex + 1} / {mediaItems.length}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Fullscreen overlay */}
      {fullscreenItem && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/90 backdrop-blur-sm">
          <button
            onClick={closeFullscreen}
            className="absolute top-6 right-6 text-white hover:text-gray-300 z-50 transition-colors"
            aria-label="Close fullscreen"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
              <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <div className="relative w-full h-full flex items-center justify-center">
            {isImage(fullscreenItem.media) ? (
              <ImageDisplay
                src={fullscreenItem.media.url}
                alt="Fullscreen media"
                className="max-w-full max-h-full object-contain"
                fallback={fallbackImage}
              />
            ) : (
              <VideoPlayer
                src={fullscreenItem.media.url}
                className="max-w-full max-h-full"
                controls
                autoPlay
              />
            )}
          </div>
          
          {/* Navigation in fullscreen mode */}
          {mediaItems.length > 1 && (
            <>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  const newIndex = currentIndex > 0 ? currentIndex - 1 : mediaItems.length - 1;
                  setCurrentIndex(newIndex);
                  setFullscreenItem({ media: mediaItems[newIndex] });
                }}
                className="absolute left-6 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-3 rounded-full hover:bg-black/70 z-50 transition-all hover:scale-110"
                aria-label="Previous media"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  const newIndex = currentIndex < mediaItems.length - 1 ? currentIndex + 1 : 0;
                  setCurrentIndex(newIndex);
                  setFullscreenItem({ media: mediaItems[newIndex] });
                }}
                className="absolute right-6 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-3 rounded-full hover:bg-black/70 z-50 transition-all hover:scale-110"
                aria-label="Next media"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
              </button>
            </>
          )}
        </div>
      )}

      {/* Navigation arrows - only show if there are multiple items */}
      {mediaItems.length > 1 && (
        <>
          <button
            onClick={prevSlide}
            className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-white/90 dark:bg-gray-800/90 text-gray-800 dark:text-white p-2 rounded-full shadow-md hover:bg-white dark:hover:bg-gray-700 transition-all duration-300 hover:scale-110 z-10"
            aria-label="Previous media"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
          </button>
          <button
            onClick={nextSlide}
            className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-white/90 dark:bg-gray-800/90 text-gray-800 dark:text-white p-2 rounded-full shadow-md hover:bg-white dark:hover:bg-gray-700 transition-all duration-300 hover:scale-110 z-10"
            aria-label="Next media"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
          </button>
        </>
      )}

      {/* Indicators - pill style */}
      {mediaItems.length > 1 && (
        <div className="absolute bottom-4 left-1/2 transform -translate-x-1/2 flex justify-center space-x-2 z-10">
          {mediaItems.map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`h-1.5 rounded-full transition-all duration-300 ${
                index === currentIndex 
                  ? 'bg-white dark:bg-gray-300 w-6' 
                  : 'bg-white/50 dark:bg-gray-500/50 w-2 hover:w-3'
              }`}
              aria-label={`Go to media ${index + 1}`}
            />
          ))}
        </div>
      )}

      {/* Animation styles */}
      <style jsx>{`
        @keyframes slideInRight {
          from {
            transform: translateX(100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
        
        @keyframes slideInLeft {
          from {
            transform: translateX(-100%);
            opacity: 0;
          }
          to {
            transform: translateX(0);
            opacity: 1;
          }
        }
        
        .animate-slide-in-right {
          animation: slideInRight 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
        }
        
        .animate-slide-in-left {
          animation: slideInLeft 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards;
        }
      `}</style>
    </div>
  );
};

export default MediaCarousel;