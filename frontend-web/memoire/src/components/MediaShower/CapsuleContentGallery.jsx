import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useCapsuleContent } from '../../context/CapsuleWebContextProvider';
import ImageDisplay from './ImageDisplay';
import VideoPlayer from './VideoPlayer';
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd';
import { Plus, RefreshCw, Play, MoreVertical, Edit, Trash2, X } from 'lucide-react';

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
  const [isRefreshing, setIsRefreshing] = useState(false);
  const fileInputRef = useRef();
  const lastUpdateTimeRef = useRef(0);
  const checkIntervalRef = useRef(null);
  
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
    }, 10000);
    
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
    return <div className="text-red-500">Error: {error}</div>;
  }

  return (
    <div className="space-y-6">
      {/* Debug info - remove in production */}
      {renderDebugInfo()}
      
      {/* toolbar */}
      <div className="flex justify-between">
        <h2>Capsule Media</h2>
        <div className="flex gap-2">
          <button 
            className="flex items-center gap-1 px-2 py-1 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => fileInputRef.current.click()}>
            <Plus size={16} /> Upload
          </button>
          <button 
            className="flex items-center gap-1 px-2 py-1 bg-gray-200 rounded hover:bg-gray-300"
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
          />
        </div>
      </div>

      {/* preview */}
      <div className="w-full h-64 bg-gray-100 rounded overflow-hidden">
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

      {/* thumbnails */}
      <DragDropContext onDragEnd={({ source, destination }) => {
        if (!destination) return;
        const items = Array.from(mediaContents);
        const [m] = items.splice(source.index, 1);
        items.splice(destination.index, 0, m);
        setMediaContents(items);
      }}>
        <Droppable droppableId="thumbs" direction="horizontal">
          {(p) => (
            <div ref={p.innerRef} {...p.droppableProps} className="grid grid-cols-4 gap-2">
              {mediaContents.map((item, i) => (
                <Draggable key={item.id} draggableId={String(item.id)} index={i}>
                  {(p) => (
                    <div
                      ref={p.innerRef}
                      {...p.draggableProps}
                      {...p.dragHandleProps}
                      onClick={() => setSelectedMedia(item)}
                      className={`h-24 border rounded overflow-hidden cursor-pointer
                        ${selectedMedia?.id === item.id ? 'ring-2 ring-blue-500' : ''}`}
                    >
                      {item.contentType?.startsWith('image/') ? (
                        <ImageDisplay src={item.url} className="w-full h-full object-cover" />
                      ) : (
                        <div className="relative w-full h-full bg-black flex items-center justify-center">
                          <Play className="text-white" />
                        </div>
                      )}
                    </div>
                  )}
                </Draggable>
              ))}
              {p.placeholder}
            </div>
          )}
        </Droppable>
      </DragDropContext>

      {/* file list */}
      <div>
        <h3>Files ({mediaContents.length})</h3>
        {mediaContents.length === 0 ? (
          <div className="p-4 text-center text-gray-500">No media files found</div>
        ) : (
          <ul className="space-y-2">
            {mediaContents.map((item) => (
              <li key={item.id} className="flex items-center p-2 border rounded">
                <div className="w-12 h-12 mr-3 overflow-hidden rounded">
                  {item.contentType?.startsWith('image/') ? (
                    <ImageDisplay src={item.url} className="w-full h-full object-cover" />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center bg-gray-800">
                      <Play className="text-white" />
                    </div>
                  )}
                </div>
                <div className="flex-grow">
                  <div>{item.filePath ? item.filePath.split('/').pop() : 'Unnamed file'}</div>
                  <div className="text-sm text-gray-500">
                    {item.uploadedAt ? new Date(item.uploadedAt).toLocaleString() : 'Unknown date'}
                  </div>
                </div>
                <button 
                  className="p-1 text-red-500 hover:bg-red-50 rounded"
                  onClick={() => onDelete(item.id)}>
                  <Trash2 size={18} />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default CapsuleContentGallery;