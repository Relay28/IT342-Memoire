import webSocketService from './websocketService';

class CapsuleContentService {
  constructor() {
    this.subscriptions = new Map();
    this.statusHandlers = new Set();
    this.endpoint = "/ws-capsule-content";
    this.connectingPromise = null;
    this.retryCount = 0;
    this.maxRetries = 3;
  }

  createHeaders() {
    const token = this._getAuthToken();
    return {
      Authorization: `Bearer ${token}`
    };
  }

  // Core subscription method with improved error handling and retries
  subscribeToCapsule(capsuleId, onUpdate, onInitialData, username) {
    // Check if already subscribed to this capsule
    if (this.subscriptions.has(capsuleId)) {
      console.log(`Already subscribed to capsule ${capsuleId}, cleaning up old subscription first`);
      this.unsubscribeFromCapsule(capsuleId);
    }
    
    const token = this._getAuthToken();
    if (!token) {
      console.error("No authentication token found");
      this._updateStatus("error: No authentication token");
      return Promise.reject(new Error("No authentication token found"));
    }
    
    console.log(`Connecting to ${this.endpoint} for capsule ${capsuleId}`);
    this._updateStatus('connecting');
    
    // Reset retry count on new subscription
    this.retryCount = 0;
    
    return this._attemptSubscription(capsuleId, onUpdate, onInitialData, username, token);
  }

  async _attemptSubscription(capsuleId, onUpdate, onInitialData, username, token) {
    try {
      // Ensure we have a connection - reuse existing connection promise if in progress
      if (!this.connectingPromise || webSocketService.getConnectionState(this.endpoint) !== 'connecting') {
        this.connectingPromise = webSocketService.connect(token, this.endpoint);
      }
      
      await this.connectingPromise;
      this.connectingPromise = null;
      
      const headers = this.createHeaders();

      // Subscribe to real-time updates
      const updateDestination = `app/topic/capsule-content/updates/${capsuleId}`;
      
      console.log(`Subscribing to ${updateDestination}`);
      const updateSubscription = webSocketService.subscribe(
        updateDestination, 
        (data, message, error) => {
          if (error) {
            console.error(`Error processing update from ${updateDestination}:`, error);
            return;
          }
          console.log("WebSocket received update:", data);
          onUpdate(data);
        }, 
        headers,
        this.endpoint // Specify the endpoint
      );
      
      // Subscribe to initial data
      const initialDestination = `/user/queue/capsule-content/initial`;
      
      console.log(`Subscribing to ${initialDestination}`);
      const initialSubscription = webSocketService.subscribe(
        initialDestination, 
        (data, message, error) => {
          if (error) {
            console.error(`Error processing initial data from ${initialDestination}:`, error);
            return;
          }
          console.log("WebSocket received initial data:", data);
          
          try {
            // Make sure this initial data is for the requested capsule
            if (data && data.capsuleId === capsuleId) {
              onInitialData(data.contents);
            } else {
              console.warn("Received initial data for wrong capsule:", data?.capsuleId);
            }
          } catch (error) {
            console.error("Error processing initial capsule data:", error);
          }
        }, 
        headers,
        this.endpoint // Specify the endpoint
      );
      
      // Store subscriptions for management
      this.subscriptions.set(capsuleId, {
        updateSubscription,
        initialSubscription
      });
      
      // Request initial data with proper error handling
      try {
        console.log(`Sending request for initial data to /app/capsule-content/connect/${capsuleId}`);
        await webSocketService.send(
          `/app/capsule-content/connect/${capsuleId}`, 
          headers, 
          {
            username: username,
            requestTimestamp: new Date().toISOString()
          },
          this.endpoint // Specify the endpoint
        );
        this._updateStatus('connected');
        return true;
      } catch (sendError) {
        console.error("Error requesting initial capsule data:", sendError);
        // Don't fail the subscription just because the initial request failed
        this._updateStatus('connected-with-errors');
        return true;
      }
    } catch (error) {
      console.error(`Subscription attempt ${this.retryCount + 1} failed:`, error);
      
      // Implement retry logic
      if (this.retryCount < this.maxRetries) {
        this.retryCount++;
        console.log(`Retrying subscription (${this.retryCount}/${this.maxRetries})...`);
        this._updateStatus(`retrying: ${this.retryCount}/${this.maxRetries}`);
        
        // Wait before retrying (exponential backoff)
        const delay = Math.min(1000 * Math.pow(2, this.retryCount - 1), 10000);
        await new Promise(resolve => setTimeout(resolve, delay));
        
        return this._attemptSubscription(capsuleId, onUpdate, onInitialData, username, token);
      }
      
      this._updateStatus('error: failed to connect');
      this.connectingPromise = null;
      throw error;
    }
  }

  unsubscribeFromCapsule(capsuleId) {
    const subscriptionInfo = this.subscriptions.get(capsuleId);
    if (subscriptionInfo) {
      console.log(`Unsubscribing from capsule: ${capsuleId}`);
      
      if (subscriptionInfo.updateSubscription) {
        webSocketService.unsubscribe(subscriptionInfo.updateSubscription);
      }
      
      if (subscriptionInfo.initialSubscription) {
        webSocketService.unsubscribe(subscriptionInfo.initialSubscription);
      }
      
      this.subscriptions.delete(capsuleId);
    }
  }

  unsubscribeAll() {
    console.log("Unsubscribing from all capsules");
    for (const capsuleId of this.subscriptions.keys()) {
      this.unsubscribeFromCapsule(capsuleId);
    }
  }

  // Fixed status change handler
  onStatusChange(handler) {
    this.statusHandlers.add(handler);
    
    // Pass current status immediately (if available)
    const currentState = webSocketService.getConnectionState(this.endpoint);
    if (currentState) {
      handler(currentState);
    }
    
    // Set up connection state monitoring
    const connectionCleanup = webSocketService.onConnectionStateChange(state => {
      this._updateStatus(state);
    }, this.endpoint); // Specify the endpoint
    
    // Return cleanup function
    return () => {
      this.statusHandlers.delete(handler);
      connectionCleanup();
    };
  }

  _updateStatus(status) {
    this.statusHandlers.forEach(handler => handler(status));
  }

  _getAuthToken() {
    return localStorage.getItem('authToken') || sessionStorage.getItem('authToken');
  }
}

const capsuleContentService = new CapsuleContentService();
export default capsuleContentService;