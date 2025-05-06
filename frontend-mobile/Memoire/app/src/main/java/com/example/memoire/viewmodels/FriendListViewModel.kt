package com.example.memoire.viewmodels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoire.api.FriendshipEntity
import com.example.memoire.api.FriendshipRequest
import com.example.memoire.api.RetrofitClient
import com.example.memoire.models.UserEntity
import kotlinx.coroutines.launch
import java.io.IOException
class FriendListViewModel : ViewModel() {

    private val _friends = MutableLiveData<List<UserEntity>>()
    val friends: LiveData<List<UserEntity>> = _friends

    private val _friendRequests = MutableLiveData<List<FriendshipEntity>>()
    val friendRequests: LiveData<List<FriendshipEntity>> = _friendRequests

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: MutableLiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _requestSent = MutableLiveData(false)
    val requestSent: LiveData<Boolean> = _requestSent

    private val _friendship = MutableLiveData<FriendshipEntity?>()
    val friendship: LiveData<FriendshipEntity?> = _friendship

    private val friendshipService = RetrofitClient.friendInstance

    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = friendshipService.getFriendsList()
                if (response.isSuccessful) {
                    _friends.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load friends: ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchFriendRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = friendshipService.getReceivedFriendRequests()
                if (response.isSuccessful) {
                    _friendRequests.value = response.body() ?: emptyList()
                } else {
                    _errorMessage.value = "Failed to load friend requests: ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptFriendship(friendshipId: Long) {
        viewModelScope.launch {
            try {
                val response = friendshipService.acceptFriendship(friendshipId)
                if (response.isSuccessful) {
                    _errorMessage.value = null // Clear error message immediately
                    fetchFriendRequests() // Refresh friend requests
                    fetchFriends() // Refresh the friends list
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = "Failed to accept request: ${response.message()} - $errorBody"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteFriendship(friendshipId: Long) {
        viewModelScope.launch {
            try {
                val response = friendshipService.deleteFriendship(friendshipId)
                if (response.isSuccessful) {
                    _errorMessage.value = null // Clear error message immediately
                    fetchFriendRequests() // Refresh friend requests
                    fetchFriends() // Refresh the friends list
                } else {
                    _errorMessage.value = "Failed to delete friendship: ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }
    fun sendFriendRequest(friendId: Long) {
        viewModelScope.launch {
            try {
                val request = FriendshipRequest(friendId)
                val response = friendshipService.createFriendship(request)
                if (response.isSuccessful) {
                    _requestSent.value = true
                } else {
                    _errorMessage.value = "Failed to send friend request: ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun cancelFriendRequest(friendId: Long) {
        viewModelScope.launch {
            try {
                val response = friendshipService.cancelRequest(friendId)
                if (response.isSuccessful) {
                    _requestSent.value = false
                } else {
                    _errorMessage.value = "Failed to cancel request: ${response.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun checkFriendshipStatus(friendId: Long, onResult: (Boolean, Boolean, Boolean, Long?) -> Unit) {
        viewModelScope.launch {
            try {
                // Check if they are already friends
                val areFriendsResponse = friendshipService.areFriends(friendId)
                if (areFriendsResponse.isSuccessful && areFriendsResponse.body() == true) {
                    // Find the friendship ID
                    val friendshipResponse = friendshipService.findByUsers(friendId)
                    if (friendshipResponse.isSuccessful) {
                        val friendshipId = friendshipResponse.body()?.id
                        onResult(true, false, false, friendshipId)
                        return@launch
                    }
                }

                // Check if there's a pending request
                val pendingResponse = friendshipService.hasPendingRequest(friendId)
                if (pendingResponse.isSuccessful && pendingResponse.body() == true) {
                    onResult(false, true, false, null)
                    return@launch
                }

                // Check if the user received a request
                val receiverResponse = friendshipService.isReceiver(friendId)
                if (receiverResponse.isSuccessful && receiverResponse.body() == true) {
                    // Find the friendship ID for the received request
                    val friendshipResponse = friendshipService.findByUsers(friendId)
                    if (friendshipResponse.isSuccessful) {
                        val friendshipId = friendshipResponse.body()?.id
                        onResult(false, false, true, friendshipId)
                        return@launch
                    }
                }

                // Not friends, no pending requests
                onResult(false, false, false, null)
            } catch (e: Exception) {
                _errorMessage.value = "Error checking friendship status: ${e.message}"
                onResult(false, false, false, null)
            }
        }
    }

    // New method to find friendship by friend's user ID
    fun findFriendshipById(userId: Long): LiveData<FriendshipEntity?> {
        viewModelScope.launch {
            try {
                val response = friendshipService.findByUsers(userId)
                if (response.isSuccessful) {
                    _friendship.value = response.body()
                } else {
                    _errorMessage.value = "Failed to find friendship: ${response.message()}"
                    _friendship.value = null
                }
            } catch (e: IOException) {
                _errorMessage.value = "Network error: ${e.message}"
                _friendship.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                _friendship.value = null
            }
        }
        return friendship
    }

    fun clearError() {
        _errorMessage.value = null
    }
}