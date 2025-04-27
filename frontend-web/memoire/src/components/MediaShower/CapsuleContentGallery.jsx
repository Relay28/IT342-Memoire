import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useCapsuleContent } from '../../context/CapsuleWebContextProvider';
import ImageDisplay from './ImageDisplay';
import VideoPlayer from './VideoPlayer';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { Plus, RefreshCw, Play, MoreVertical, Edit, Trash2, X, Image as ImageIcon, UploadCloud } from 'lucide-react';

const CapsuleContentGallery = ({ capsuleId }) => {
  const {
    connectToCapsule,
    disconnectFromCapsule,
    fetchMediaContent,
    deleteContent,
    uploadContent,
    loading,
    error,
    connectionStatus,
    getCapsuleContents
  } = useCapsuleContent();

  const [mediaContents, setMediaContents] = useState([]);
  const [selectedMedia, setSelectedMedia] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const fileInputRef = useRef();
  const lastUpdateTimeRef = useRef(0);
  const checkIntervalRef = useRef(null);
  
  const handleThumbnailClick = (item) => {
    setSelectedMedia(item);
    setShowModal(true);
  };
  // Load media with error handling
  const loadMedia = useCallback(async () => {
    // Prevent loading too frequently
    const now = Date.now();
    if (now - lastUpdateTimeRef.current < 2000) {
      return; // Don't refresh if less than 2 seconds passed
    }
    
    lastUpdateTimeRef.current = now;
    setIsRefreshing(true);
    
    try {
      const list = await fetchMediaContent(capsuleId);
      
      // Update media contents
      setMediaContents(prev => {
        // Check if content has actually changed to prevent needless rerenders
        const prevIds = prev.map(item => item.id).sort().join(',');
        const newIds = list.map(item => item.id).sort().join(',');
        
        if (prevIds === newIds) {
          return prev; // No change, keep previous state
        }
        
        // Content has changed, update state
        if (!selectedMedia && list.length > 0) {
          setSelectedMedia(list[0]);
        } else if (selectedMedia) {
          const stillExists = list.find(item => item.id === selectedMedia.id);
          if (!stillExists && list.length > 0) {
            setSelectedMedia(list[0]);
          } else if (!stillExists) {
            setSelectedMedia(null);
          }
        }
        
        return list;
      });
    } catch (err) {
      console.error("Error loading media:", err);
    } finally {
      setIsRefreshing(false);
    }
  }, [capsuleId, fetchMediaContent, selectedMedia]);

  // Set up initial connection and load
  useEffect(() => {
    // Initial load
    loadMedia();
    
    // Connect to WebSocket
    connectToCapsule(capsuleId);
    
    // Set up a periodic check (every 10s) as backup
    // This ensures we eventually get updates even if WebSocket fails
    checkIntervalRef.current = setInterval(() => {
      loadMedia();
    }, 3000);
    
    return () => {
      disconnectFromCapsule(capsuleId);
      if (checkIntervalRef.current) {
        clearInterval(checkIntervalRef.current);
      }
    };
  }, [capsuleId, connectToCapsule, disconnectFromCapsule, loadMedia]);

  // Handle file upload
  const onFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    
    try {
      await uploadContent(capsuleId, file);
      // Force refresh after upload
      loadMedia();
    } catch (err) {
      console.error("Upload failed:", err);
    }
  };

  // Handle deletion
  const onDelete = async (id) => {
    try {
      await deleteContent(id);
      
      // Update UI immediately
      setMediaContents(prev => {
        const newContents = prev.filter(item => item.id !== id);
        
        // Update selected media if deleted
        if (selectedMedia?.id === id) {
          if (newContents.length > 0) {
            setSelectedMedia(newContents[0]);
          } else {
            setSelectedMedia(null);
          }
        }
        
        return newContents;
      });
      
    } catch (err) {
      console.error("Delete failed:", err);
      // Refresh to ensure we're in sync
      loadMedia();
    }
  };

  // Debug info display - can be removed in production
  const renderDebugInfo = () => (
    <div className="text-xs text-gray-500 mb-2">
      <div>Connection: {connectionStatus}</div>
      <div>Media count: {mediaContents.length}</div>
      <div>Selected: {selectedMedia?.id || 'none'}</div>
      <div>WS content count: {getCapsuleContents(capsuleId)?.length || 0}</div>
    </div>
  );

  if (loading && !isRefreshing && mediaContents.length === 0) {
    return <div>Loading…</div>;
  }
  
  if (error) {
    return <div className="text-[#AF3535]">Error: {error}</div>;
  }

  return (
    <div className="space-y-6 p-4 bg-white rounded-lg shadow-sm dark:bg-gray-800">
      {/* Debug info - remove in production */}
      {renderDebugInfo()}
      
      {/* toolbar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <h2 className="text-xl font-semibold text-gray-800 dark:text-gray-200">Capsule Media</h2>
        <div className="flex gap-2 w-full sm:w-auto">
          <button 
            className="flex items-center gap-2 px-3 py-2 bg-[#AF3535] text-white rounded-md hover:bg-red-700 transition-colors text-sm font-medium"
            onClick={() => fileInputRef.current.click()}>
            <Plus size={16} /> Upload Media
          </button>
          <button 
            className="flex items-center gap-2 px-3 py-2 bg-gray-100 rounded-md hover:bg-gray-200 transition-colors text-sm font-medium dark:bg-gray-700 dark:hover:bg-gray-600 dark:text-gray-200"
            onClick={loadMedia} 
            disabled={isRefreshing}>
            <RefreshCw size={16} className={isRefreshing ? 'animate-spin' : ''} /> Refresh
          </button>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*,video/*"
            onChange={onFile}
            className="hidden"
            multiple
          />
        </div>
      </div>
  
      {/* thumbnails */}
      <div className="space-y-3">
        <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300">Media Gallery</h3>
        <DragDropContext onDragEnd={({ source, destination }) => {
          if (!destination) return;
          const items = Array.from(mediaContents);
          const [m] = items.splice(source.index, 1);
          items.splice(destination.index, 0, m);
          setMediaContents(items);
        }}>
          <Droppable droppableId="thumbs" direction="horizontal">
            {(p) => (
              <div 
                ref={p.innerRef} 
                {...p.droppableProps} 
                className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-3"
              >
                {mediaContents.map((item, i) => (
                  <Draggable key={item.id} draggableId={String(item.id)} index={i}>
                    {(p) => (
                      <div
                        ref={p.innerRef}
                        {...p.draggableProps}
                        {...p.dragHandleProps}
                        onClick={() => {
                          setSelectedMedia(item);
                          setShowModal(true);
                        }}
                        className={`relative h-24 border-2 rounded-lg overflow-hidden cursor-pointer transition-all
                          ${selectedMedia?.id === item.id ? 'border-[#AF3535] ring-2 ring-[#AF3535]/30' : 'border-transparent hover:border-gray-300 dark:hover:border-gray-500'}`}
                      >
                        {item.contentType?.startsWith('image/') ? (
                          <ImageDisplay src={item.url} className="w-full h-full object-cover" />
                        ) : (
                          <div className="relative w-full h-full bg-gray-800 flex items-center justify-center">
                            <VideoPlayer src={item.url} className="w-full h-full object-cover" />
                          </div>
                        )}
                        <div className="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-2">
                          <p className="text-xs text-white truncate">
                            {item.name || 'Untitled'}
                          </p>
                        </div>
                      </div>
                    )}
                  </Draggable>
                ))}
                {p.placeholder}
              </div>
            )}
          </Droppable>
        </DragDropContext>
      </div>
  
      {/* file list */}
      <div className="space-y-3">
        <div className="flex justify-between items-center">
          <h3 className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Media Files ({mediaContents.length})
          </h3>
          {mediaContents.length > 0 && (
            <span className="text-xs text-gray-500 dark:text-gray-400">
              Drag to reorder thumbnails
            </span>
          )}
        </div>
        {mediaContents.length === 0 ? (
          <div className="p-6 text-center border-2 border-dashed rounded-lg text-gray-500 dark:text-gray-400 dark:border-gray-700">
            <UploadCloud size={48} className="mx-auto mb-3" />
            <p>No media files found</p>
            <p className="text-sm mt-1">Click "Upload Media" to add content</p>
          </div>
        ) : (
          <ul className="space-y-2">
            {mediaContents.map((item) => (
              <li 
                key={item.id} 
                className="flex items-center p-3 border rounded-lg hover:bg-gray-50 transition-colors dark:border-gray-700 dark:hover:bg-gray-700/50"
                onClick={() => {
                  setSelectedMedia(item);
                  setShowModal(true);
                }}
              >
                <div className="w-10 h-10 mr-3 overflow-hidden rounded flex-shrink-0">
                  {item.contentType?.startsWith('image/') ? (
                    <ImageDisplay src={item.url} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-gray-800">
                      <Play className="text-white" size={16} />
                    </div>
                  )}
                </div>
                <div className="flex-grow min-w-0">
                  <p className="text-sm font-medium text-gray-800 truncate dark:text-gray-200">
                    {item.name || 'Untitled'}
                  </p>
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    {item.uploadedAt ? new Date(item.uploadedAt).toLocaleString() : 'Unknown date'} • {item.contentType}
                  </p>
                </div>
                <button 
                  className="p-2 text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors dark:hover:bg-red-900/30"
                  onClick={(e) => {
                    e.stopPropagation();
                    onDelete(item.id);
                  }}
                  aria-label="Delete"
                >
                  <Trash2 size={18} />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
  
      {/* Preview Modal */}
      {/* Preview Modal */}
{showModal && selectedMedia && (
  <div className="fixed inset-0 z-50 bg-black bg-opacity-90 flex items-center justify-center p-4">
    {/* Modal Container */}
    <div className="relative w-full max-w-4xl h-full max-h-[90vh] flex flex-col">
      {/* Modal Header */}
      <div className="flex justify-between items-center mb-2">
        <h2 className="text-xl font-bold text-white truncate max-w-[80%]">
          {selectedMedia.name || 'Untitled'}
        </h2>
        <button
          onClick={() => setShowModal(false)}
          className="p-2 text-white hover:text-red-400 transition-colors"
        >
          <X size={24} />
        </button>
      </div>

      {/* Media Display Area */}
      <div className="relative flex-1 bg-black rounded-lg overflow-hidden flex items-center justify-center">
      {selectedMedia ? (
          selectedMedia.contentType?.startsWith('image/') ? (
            <ImageDisplay src={selectedMedia.url} className="w-full h-full object-contain" />
          ) : (
            <VideoPlayer src={selectedMedia.url} className="w-full h-full" />
          )
        ) : (
          <div className="flex items-center justify-center h-full text-gray-500">
            No media selected
          </div>
        )}
      </div>

      {/* Modal Footer */}
      <div className="mt-2 text-center text-gray-300 text-sm">
        {selectedMedia.contentType} •{' '}
        {selectedMedia.size && `${Math.round(selectedMedia.size / 1024)} KB`} •{' '}
        {selectedMedia.uploadedAt && new Date(selectedMedia.uploadedAt).toLocaleString()}
      </div>
    </div>
  </div>
)}
    </div>
  );
};

export default CapsuleContentGallery;