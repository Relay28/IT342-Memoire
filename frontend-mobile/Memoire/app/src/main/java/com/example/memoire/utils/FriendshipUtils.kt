package com.example.memoire.utils

import com.example.memoire.api.FriendshipEntity

object FriendshipUtils {
    fun getRequesterId(sessionManager: SessionManager, friendship: FriendshipEntity): Long {
        val loggedInUserId = sessionManager.getUserSession()["userId"] as? Long ?: return -1

        return if (loggedInUserId == friendship.friendId) {
            friendship.userId
        } else if (loggedInUserId == friendship.userId) {
            friendship.friendId
        } else {
            -1 // Return -1 if no match is found
        }
    }
}