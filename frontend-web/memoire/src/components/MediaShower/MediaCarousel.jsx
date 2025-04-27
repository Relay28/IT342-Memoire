import React, { useState, useRef } from 'react';
import ImageDisplay from './ImageDisplay';
import VideoPlayer from './VideoPlayer';

const MediaCarousel = ({ mediaItems, fallbackImage }) => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [transitionDirection, setTransitionDirection] = useState(null);
  const [fullscreenItem, setFullscreenItem] = useState(null);
  const carouselRef = useRef(null);
  
  // Calculate how many groups of 3 we can show
  const groupCount = Math.ceil(mediaItems.length / 3);
  
  // Get the current group of 3 media items
  const currentGroup = mediaItems.slice(currentIndex * 3, currentIndex * 3 + 3);
  
  // Fill the remaining slots if we don't have 3 items in the last group
  while (currentGroup.length < 3 && mediaItems.length > 3) {
    currentGroup.push(mediaItems[currentGroup.length]);
  }

  const nextSlide = () => {
    setTransitionDirection('right');
    setCurrentIndex((prevIndex) => 
      prevIndex === groupCount - 1 ? 0 : prevIndex + 1
    );
  };

  const prevSlide = () => {
    setTransitionDirection('left');
    setCurrentIndex((prevIndex) => 
      prevIndex === 0 ? groupCount - 1 : prevIndex - 1
    );
  };

  const goToSlide = (index) => {
    setTransitionDirection(index > currentIndex ? 'right' : 'left');
    setCurrentIndex(index);
  };

  const toggleFullscreen = (media, index) => {
    setFullscreenItem(fullscreenItem ? null : { media, index });
  };

  const closeFullscreen = () => {
    setFullscreenItem(null);
  };

  const isImage = (media) => {
    return media.contentType?.startsWith('image/') || 
           media.url.match(/\.(jpeg|jpg|gif|png|webp)$/i) !== null;
  };

  return (
    <div className="relative w-full aspect-[3/1] overflow-hidden rounded-lg" ref={carouselRef}>
      {/* Media display with transition effects */}
      <div className="relative h-full w-full bg-gray-200 dark:bg-gray-700">
        <div 
          className={`grid grid-cols-3 gap-2 h-full w-full absolute top-0 left-0 ${
            transitionDirection === 'right' 
              ? 'animate-slide-in-right' 
              : transitionDirection === 'left' 
                ? 'animate-slide-in-left' 
                : ''
          }`}
          key={currentIndex} // Force re-render for animation
        >
          {currentGroup.map((media, index) => {
            const itemIsImage = isImage(media);
            
            return (
              <div 
                key={media.id || index} 
                className="h-full w-full relative overflow-hidden cursor-pointer"
                onClick={() => toggleFullscreen(media, index)}
              >
                {itemIsImage ? (
                  <ImageDisplay 
                    src={media.url}
                    alt={`Media content ${index + 1}`}
                    className="h-full w-full object-cover transition-transform duration-300 hover:scale-105"
                    fallback={fallbackImage}
                  />
                ) : (
                  <VideoPlayer 
                    src={media.url}
                    className="h-full w-full object-cover"
                    controls
                  />
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Fullscreen overlay */}
      {fullscreenItem && (
        <div className="fixed inset-0 bg-black bg-opacity-90 z-50 flex items-center justify-center p-4">
          <button
            onClick={closeFullscreen}
            className="absolute top-4 right-4 text-white text-2xl z-50 hover:text-gray-300"
            aria-label="Close fullscreen"
          >
            ✕
          </button>
          
          <div className="relative w-full h-full flex items-center justify-center">
            {isImage(fullscreenItem.media) ? (
              <ImageDisplay
                src={fullscreenItem.media.url}
                alt={`Fullscreen media ${fullscreenItem.index + 1}`}
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
                  const newIndex = fullscreenItem.index > 0 ? fullscreenItem.index - 1 : mediaItems.length - 1;
                  setFullscreenItem({ media: mediaItems[newIndex], index: newIndex });
                }}
                className="absolute left-4 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-4 rounded-full hover:bg-black/70 z-50"
                aria-label="Previous media"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </button>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  const newIndex = fullscreenItem.index < mediaItems.length - 1 ? fullscreenItem.index + 1 : 0;
                  setFullscreenItem({ media: mediaItems[newIndex], index: newIndex });
                }}
                className="absolute right-4 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-4 rounded-full hover:bg-black/70 z-50"
                aria-label="Next media"
              >
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
              </button>
            </>
          )}
        </div>
      )}

      {/* Navigation arrows - only show if there are more than 3 items */}
      {mediaItems.length > 3 && (
        <>
          <button
            onClick={prevSlide}
            className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-2 rounded-full hover:bg-black/70 transition-all duration-300 hover:scale-110 z-10"
            aria-label="Previous media"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
          </button>
          <button
            onClick={nextSlide}
            className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-black/50 text-white p-2 rounded-full hover:bg-black/70 transition-all duration-300 hover:scale-110 z-10"
            aria-label="Next media"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
            </svg>
          </button>
        </>
      )}

      {/* Indicators */}
      {groupCount > 1 && (
        <div className="absolute bottom-2 left-0 right-0 flex justify-center space-x-2 z-10">
          {Array.from({ length: groupCount }).map((_, index) => (
            <button
              key={index}
              onClick={() => goToSlide(index)}
              className={`h-2 w-2 rounded-full transition-all duration-300 ${
                index === currentIndex 
                  ? 'bg-white w-6' 
                  : 'bg-white/50 hover:bg-white/70'
              }`}
              aria-label={`Go to media group ${index + 1}`}
            />
          ))}
        </div>
      )}

      {/* Add these styles to your global CSS or CSS-in-JS */}
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
          animation: slideInRight 0.5s ease-in-out forwards;
        }
        
        .animate-slide-in-left {
          animation: slideInLeft 0.5s ease-in-out forwards;
        }
      `}</style>
    </div>
  );
};

export default MediaCarousel;