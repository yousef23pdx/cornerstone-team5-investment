data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null // ISO format: "YYYY-MM-DD"
)

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val phoneNumber: String?,
    val role: String
)

data class UpdateProfileRequest(
    val email: String,
    val phoneNumber: String?,
    val dateOfBirth: String?
)