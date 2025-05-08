import com.example.memoire.models.TimeCapsuleDTO
import com.example.memoire.models.UserEntity
import java.util.Date

// CommentRequest.kt
data class CommentRequest(
    val text: String
)


// CommentEntity.kt
data class CommentEntity(
    val id: Long,
    val text: String,
    val timeCapsule: TimeCapsuleDTO,
    val userId: Long,
    val username: String,
    val userProfileImage: String?,
    val reactions: List<CommentReactionEntity> = emptyList(),
    val createdAt: Date,
    var reactionCount: Int,
    val updatedAt: String?
)

// ReactionRequest.kt
data class ReactionRequest(
    val type: String
)

// CommentReactionEntity.kt
data class CommentReactionEntity(
    val id: Long,
    val type: String,
    val user: UserEntity,
    val comment: CommentReference? = null, // Optional on frontend to avoid circular reference
    val reactedAt: String
)

// CommentReference.kt (to avoid circular reference)
data class CommentReference(
    val id: Long
)