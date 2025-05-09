// WebSocketService.js
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const CONNECTION_TIMEOUT = 15000; // 15 seconds timeout

class WebSocketService {
  constructor() {
    this.connections = new Map(); // endpoint -> connection info
    this.connectionHandlers = new Map(); // endpoint -> Set of handlers
  }

  connect(authToken, endpoint) {
    // Validate token format to avoid base64 issues
    console.log("Connecting to "+endpoint+" with token "+authToken)
    if (!this._validateToken(authToken)) {
      return Promise.reject(new Error("Invalid authentication token format"));
    }

    // Ensure endpoint is not undefined
    if (!endpoint) {
      return Promise.reject(new Error("Endpoint must be specified"));
    }

    const fullEndpoint = `${API_BASE_URL}${endpoint}`;
    
    // Initialize connection entry if it doesn't exist
    if (!this.connections.has(endpoint)) {
      this.connections.set(endpoint, {
        client: null,
        refCount: 0,
        state: 'disconnected',
        subscriptions: new Map(),
        pendingSubscriptions: new Map(),
        connectPromise: null,
        authToken,
        fullEndpoint,
        connectionTimeout: null // Store timeout reference
      });
      
      // Initialize connection handlers set
      if (!this.connectionHandlers.has(endpoint)) {
        this.connectionHandlers.set(endpoint, new Set());
      }
    }

    const conn = this.connections.get(endpoint);
    conn.refCount++;
    
    console.log(`Connection to ${endpoint} - ref count: ${conn.refCount}`);

    // Already connected with same token
    if (conn.state === 'connected' && conn.authToken === authToken) {
      console.log(`Already connected to ${endpoint} with same token`);
      return Promise.resolve();
    }
    
    // Already connecting with same token
    if (conn.state === 'connecting' && conn.connectPromise && conn.authToken === authToken) {
      console.log(`Connection to ${endpoint} already in progress, reusing promise`);
      return conn.connectPromise;
    }

    // Update token if different
    conn.authToken = authToken;
    
    // Update state
    this._updateConnectionState(endpoint, 'connecting');
    
    conn.connectPromise = new Promise((resolve, reject) => {
      // Clear any existing timeout
      if (conn.connectionTimeout) {
        clearTimeout(conn.connectionTimeout);
      }

      // Set new connection timeout
      conn.connectionTimeout = setTimeout(() => {
        if (conn.state === 'connecting') {
          console.error(`Connection to ${endpoint} timed out after ${CONNECTION_TIMEOUT}ms`);
          this._updateConnectionState(endpoint, 'error');
          reject(new Error(`Connection to ${endpoint} timed out`));
          conn.connectPromise = null;
        }
      }, CONNECTION_TIMEOUT);
      
      // Clean up existing connection
      if (conn.client) {
        console.log(`Disconnecting existing client for ${endpoint}`);
        conn.client.deactivate();
        conn.client = null;
      }
      
      // Store endpoint for reconnection
      conn.endpoint = endpoint;
      
      conn.client = new Client({
        webSocketFactory: () => new WebSocket(
          API_BASE_URL.replace(/^http/, 'ws') + endpoint
        ),
  
      
        reconnectDelay: 5000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        debug: (str) => console.log(`[STOMP DEBUG ${endpoint}]`, str),
        connectHeaders: {
          'Authorization': `Bearer ${authToken}`,
          'accept-version': '1.2,1.1,1.0',
          'heart-beat': '10000,10000',
          'x-endpoint': endpoint
        },
        onConnect: (frame) => {
          // Clear the connection timeout
          if (conn.connectionTimeout) {
            clearTimeout(conn.connectionTimeout);
            conn.connectionTimeout = null;
          }
          
          console.log(`WebSocket connected successfully to ${endpoint}`, frame);
          this._updateConnectionState(endpoint, 'connected');
          
          // Process pending subscriptions
          if (conn.pendingSubscriptions.size > 0) {
            console.log(`Processing ${conn.pendingSubscriptions.size} pending subscriptions for ${endpoint}`);
            conn.pendingSubscriptions.forEach((subInfo, destination) => {
              this.subscribe(destination, subInfo.callback, subInfo.headers, endpoint);
            });
            conn.pendingSubscriptions.clear();
          }
          
          resolve();
        },
        onStompError: (frame) => {
          // Clear the connection timeout
          if (conn.connectionTimeout) {
            clearTimeout(conn.connectionTimeout);
            conn.connectionTimeout = null;
          }
          
          const errorMsg = frame.headers['message'] || 'Unknown STOMP error';
          console.error(`STOMP error for ${endpoint}:`, errorMsg, frame.body);
          this._updateConnectionState(endpoint, 'error');
          reject(new Error(errorMsg));
          conn.connectPromise = null;
        },
        onWebSocketClose: (event) => {
          // Clear the connection timeout
          if (conn.connectionTimeout) {
            clearTimeout(conn.connectionTimeout);
            conn.connectionTimeout = null;
          }
          
          console.warn(`WebSocket closed for ${endpoint}`, event);
          this._updateConnectionState(endpoint, 'disconnected');
          
          // Store the current endpoint for reconnection
          const currentEndpoint = conn.endpoint;
          
          // Don't reset connect promise - handle reconnection manually
          if (conn.refCount > 0 && currentEndpoint) {
            console.log(`WebSocket closed - scheduling manual reconnect for ${currentEndpoint}`);
            setTimeout(() => {
              if (conn.refCount > 0) {
                console.log(`Attempting manual reconnection to ${currentEndpoint}`);
                // Create a fresh connection with the same endpoint and token
                this.connect(conn.authToken, currentEndpoint)
                  .catch(err => console.error(`Manual reconnection to ${currentEndpoint} failed:`, err));
              }
            }, 5000); // 5 second delay before reconnect attempt
          } else {
            conn.connectPromise = null;
          }
        },
        onWebSocketError: (event) => {
          // Clear the connection timeout
          if (conn.connectionTimeout) {
            clearTimeout(conn.connectionTimeout);
            conn.connectionTimeout = null;
          }
          
          console.error(`WebSocket error for ${endpoint}`, event);
          this._updateConnectionState(endpoint, 'error');
          reject(new Error("WebSocket connection error"));
          conn.connectPromise = null;
        }
      });
  
      try {
        conn.client.activate();
      } catch (error) {
        // Clear the connection timeout
        if (conn.connectionTimeout) {
          clearTimeout(conn.connectionTimeout);
          conn.connectionTimeout = null;
        }
        
        console.error(`Error activating STOMP client for ${endpoint}:`, error);
        this._updateConnectionState(endpoint, 'error');
        reject(error);
        conn.connectPromise = null;
      }
    });
    
    return conn.connectPromise;
  }
  
  disconnect(endpoint) {
    const conn = this.connections.get(endpoint);
    if (!conn) return;
    
    conn.refCount--;
    console.log(`Disconnecting from ${endpoint} - ref count: ${conn.refCount}`);
    
    if (conn.refCount <= 0) {
      console.log(`Closing connection to ${endpoint} - no more references`);
      
      // Clear any existing timeout
      if (conn.connectionTimeout) {
        clearTimeout(conn.connectionTimeout);
        conn.connectionTimeout = null;
      }
      
      if (conn.client) {
        conn.client.deactivate();
      }
      this._updateConnectionState(endpoint, 'disconnected');
      conn.connectPromise = null;
      conn.subscriptions.clear();
      conn.pendingSubscriptions.clear();
      this.connections.delete(endpoint);
    }
  }

  subscribe(destination, callback, headers = {}, endpoint) {
    if (!endpoint && this.connections.size === 1) {
      // Default to the only connection if only one exists
      endpoint = Array.from(this.connections.keys())[0];
    }
    
    if (!endpoint) {
      return Promise.reject(new Error("Endpoint must be specified when multiple connections exist"));
    }
    
    const conn = this.connections.get(endpoint);
    if (!conn) {
      return Promise.reject(new Error(`No connection exists for endpoint ${endpoint}`));
    }
    
    if (!conn.client || conn.state !== 'connected') {
      console.warn(`Cannot subscribe to ${destination}, client for ${endpoint} not connected. Queueing subscription.`);
      conn.pendingSubscriptions.set(destination, { callback, headers });
      return { destination, endpoint };
    }
    
    // Add token to headers if not present
    const fullHeaders = { ...headers };
    if (!fullHeaders.Authorization && conn.authToken) {
      fullHeaders.Authorization = `Bearer ${conn.authToken}`;
    }
    
    try {
      const subscription = conn.client.subscribe(destination, (message) => {
        try {
          let data = null;
          if (message.body) {
            data = JSON.parse(message.body);
          }
          callback(data, message);
        } catch (error) {
          console.error(`Error handling message from ${destination} on ${endpoint}:`, error);
          callback(null, message, error);
        }
      }, fullHeaders);
      
      const subscriptionInfo = { 
        destination, 
        subscriptionId: subscription.id,
        callback,
        headers: fullHeaders,
        endpoint
      };
      
      conn.subscriptions.set(destination, subscriptionInfo);
      console.log(`Subscribed to ${destination} with ID ${subscription.id} on ${endpoint}`);
      return subscriptionInfo;
    } catch (error) {
      console.error(`Error subscribing to ${destination} on ${endpoint}:`, error);
      return { destination, error, endpoint };
    }
  }

  unsubscribe(subscriptionInfo) {
    if (!subscriptionInfo) return;
    
    const { destination, subscriptionId, endpoint } = subscriptionInfo;
    
    // Find the right connection
    let conn;
    if (endpoint) {
      conn = this.connections.get(endpoint);
    } else if (this.connections.size === 1) {
      conn = this.connections.values().next().value;
    }
    
    if (!conn) {
      console.error(`Cannot unsubscribe: no connection found for ${endpoint || 'default'}`);
      return;
    }
    
    // Check if this was a pending subscription
    if (conn.pendingSubscriptions.has(destination)) {
      console.log(`Removing pending subscription to ${destination} from ${endpoint}`);
      conn.pendingSubscriptions.delete(destination);
      return;
    }
    
    if (!conn.client || conn.state !== 'connected') {
      console.log(`Cannot unsubscribe from ${destination}, client for ${endpoint} not connected`);
      return;
    }

    try {
      if (subscriptionId && conn.client.connected) {
        console.log(`Unsubscribing from ${destination} with ID ${subscriptionId} on ${endpoint}`);
        conn.client.unsubscribe(subscriptionId);
      }
      conn.subscriptions.delete(destination);
    } catch (error) {
      console.error(`Error unsubscribing from ${destination} on ${endpoint}:`, error);
    }
  }

  send(destination, headers = {}, body = {}, endpoint) {
    if (!endpoint && this.connections.size === 1) {
      // Default to only connection if only one exists
      endpoint = Array.from(this.connections.keys())[0];
    }
    
    if (!endpoint) {
      return Promise.reject(new Error("Endpoint must be specified when multiple connections exist"));
    }
    
    const conn = this.connections.get(endpoint);
    if (!conn || !conn.client || conn.state !== 'connected') {
      return Promise.reject(new Error(`Not connected to ${endpoint}`));
    }
    
    // Add token to headers if not present
    const fullHeaders = { ...headers };
    if (!fullHeaders.Authorization && conn.authToken) {
      fullHeaders.Authorization = `Bearer ${conn.authToken}`;
    }
    
    return new Promise((resolve, reject) => {
      try {
        const bodyStr = typeof body === 'string' ? body : JSON.stringify(body);
        conn.client.publish({
          destination,
          headers: fullHeaders,
          body: bodyStr
        });
        console.log(`Message sent to ${destination} on ${endpoint}`);
        resolve();
      } catch (error) {
        console.error(`Error sending message to ${destination} on ${endpoint}:`, error);
        reject(error);
      }
    });
  }

  onConnectionStateChange(handler, endpoint) {
    if (!endpoint && this.connections.size === 1) {
      // Default to only connection if only one exists
      endpoint = Array.from(this.connections.keys())[0];
    }
    
    if (!endpoint) {
      console.warn("Cannot register connection handler without specifying endpoint");
      return () => {};
    }
    
    // Initialize handlers set if needed
    if (!this.connectionHandlers.has(endpoint)) {
      this.connectionHandlers.set(endpoint, new Set());
    }
    
    const handlers = this.connectionHandlers.get(endpoint);
    handlers.add(handler);
    
    // Get current state if available
    const conn = this.connections.get(endpoint);
    if (conn) {
      handler(conn.state);
    } else {
      handler('disconnected');
    }
    
    // Return cleanup function
    return () => {
      const handlers = this.connectionHandlers.get(endpoint);
      if (handlers) {
        handlers.delete(handler);
      }
    };
  }

  _updateConnectionState(endpoint, state) {
    const conn = this.connections.get(endpoint);
    if (conn) {
      conn.state = state;
    }
    
    const handlers = this.connectionHandlers.get(endpoint);
    if (handlers) {
      handlers.forEach(handler => handler(state));
    }
  }

  // Helper method to validate JWT token format
  _validateToken(token) {
    if (!token) return false;
    
    // Simple JWT format validation - should have 3 dot-separated segments
    const segments = token.split('.');
    if (segments.length !== 3) {
      console.error("Invalid JWT token format: doesn't have 3 segments");
      return false;
    }
    
    // Additional check for common issues like extra spaces
    if (token.includes(' ') && !token.startsWith('Bearer ')) {
      console.error("Invalid JWT token format: contains spaces");
      return false;
    }
    
    return true;
  }

  // Helper to get connection state
  getConnectionState(endpoint) {
    if (!endpoint) {
      console.warn("getConnectionState called without endpoint parameter");
      return 'disconnected';
    }
    
    const conn = this.connections.get(endpoint);
    return conn ? conn.state : 'disconnected';
  }
}

const webSocketService = new WebSocketService();
export default webSocketService;