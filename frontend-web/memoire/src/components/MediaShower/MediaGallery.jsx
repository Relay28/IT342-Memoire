import React, { useState, useEffect } from 'react';
import { useCapsuleContent } from '../../context/CapsuleWebContextProvider';
import ImageDisplay from './ImageDisplay';
import VideoPlayer from './VideoPlayer';

const MediaGallery = ({ capsuleId }) => {
  const {
    connectToCapsule,
    disconnectFromCapsule,
    getCapsuleContents,
    fetchMediaContent,
    getBlobUrl,
  } = useCapsuleContent();

  const [mediaItems, setMediaItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState(null);
  const [selectedBlobUrl, setSelectedBlobUrl] = useState(null);

  // // Connect to capsule and manage content
  // useEffect(() => {
  //   connectToCapsule(capsuleId);
    
  //   return () => {
  //     disconnectFromCapsule(capsuleId);
  //   };
  // }, [capsuleId]);

  // Process contents and fetch blob URLs
  useEffect(() => {
    const contents =  fetchMediaContent(capsuleId);
    if (!contents) return;

    const media = contents
      .filter(item => item.contentType?.match(/^(image|video)\//))
      .sort((a, b) => new Date(b.uploadedAt) - new Date(a.uploadedAt));

    setMediaItems(media);
    
    if (media.length > 0 && !selectedItem) {
      handleSelectMedia(media[0]);
    }
  }, [capsuleId, getCapsuleContents]);

  const handleSelectMedia = async (item) => {
    setSelectedItem(item);
    const url = await getBlobUrl(item.id);
    setSelectedBlobUrl(url);
  };

  if (!mediaItems.length) {
    return <div className="p-4 text-center">No media available</div>;
  }

  return (
    <div className="space-y-4">
      {/* Main Preview */}
      <div className="w-full h-96 bg-black rounded-lg overflow-hidden flex items-center justify-center">
        {selectedItem?.contentType.startsWith('image/') ? (
          <ImageDisplay 
            contentId={selectedItem.id}
            className="max-h-full max-w-full object-contain"
          />
        ) : (
          <VideoPlayer 
            blobUrl={selectedBlobUrl}
            className="h-full w-full"
          />
        )}
      </div>

      {/* Thumbnail Grid */}
      <div className="grid grid-cols-4 gap-2">
        {mediaItems.map(item => (
          <div
            key={item.id}
            className={`relative aspect-square cursor-pointer border-2 rounded-lg overflow-hidden
                      ${selectedItem?.id === item.id ? 'border-blue-500' : 'border-transparent'}`}
            onClick={() => handleSelectMedia(item)}
          >
            {item.contentType.startsWith('image/') ? (
              <ImageDisplay 
                contentId={item.id}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="relative w-full h-full bg-gray-900">
                <VideoPlayer 
                  blobUrl={getBlobUrl(item.id)}
                  className="w-full h-full opacity-70"
                />
                <Play className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 text-white" />
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default MediaGallery;