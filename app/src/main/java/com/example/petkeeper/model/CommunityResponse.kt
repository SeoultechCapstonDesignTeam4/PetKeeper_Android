package com.example.petkeeper.model

data class CommunityResponse(
    val data: List<Post>
)

data class Post(
    val POST_ID: Int,
    val USER_ID: Int,
    val POST_CONTENT: String,
    val POST_IMAGE: String?,
    val POST_DATE: String,
    val POST_TIME: String?,
    val USER: UserInfo,
    val p_post_likes: List<Like>,
    val p_post_comments: List<Comment>,
    var LikesCount: Int = 0,
    var CommentsCount: Int = 0,
    var IsLikedByCurrentUser: Boolean = false
)

data class PostList(
    val POST_ID: Int,
    val POSTED_USER_ID: Int,
    val POSTED_USER_PET_IMAGE: String,
    val POSTED_USER_EMAIL: String,
    val POST_CONTENT: String,
    val POST_IMAGE: String,
    val LIKESCOUNT: Int,
    val CommentsCount: Int,
    val POST_DATE: String,
    val POST_TIME: String,
    val IsLikedByCurrentUser: Boolean
)

data class UserInfo(
    val USER_IMAGE: String,
    val USER_EMAIL: String,
    val USER_NAME: String? = null,
    val p_pets: List<USER_Pet>
)

data class USER_Pet(
    val PET_ID: Int,
    val PET_NAME: String,
    val PET_IMAGE: String
)

data class Like(
    val LIKE_ID: Int,
    val USER_ID: Int,
    val USER: User
)

data class Comment(
    val COMMENT_ID: Int,
    val USER_ID: Int,
    val COMMENT_TEXT: String,
    val COMMENT_DATE: String?,
    val COMMENT_TIME: String?,
    val USER: User
)
