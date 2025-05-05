package authentication.users

import RegisterRequest
import UpdateProfileRequest
import UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/users")
@Tag(name = "AUTHENTICATION", description = "Handles user registration and profile management")
class UsersController(
    private val usersService: UsersService
) {

    @PostMapping("/v1/register")
    @Operation(
        summary = "Register a new user",
        description = "Registers a new user using username, email, and password",
        responses = [
            ApiResponse(responseCode = "200", description = "User successfully registered"),
            ApiResponse(responseCode = "400", description = "Validation failed or user already exists")
        ]
    )
    fun registerUser(@RequestBody request: RegisterRequest): ResponseEntity<Any> {
        return try {
            val newUser = usersService.registerUser(request)
            ResponseEntity.ok(newUser)
        } catch (e: TransferFundsException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }

    @GetMapping("/v1/list")
    @Operation(
        summary = "List all users",
        description = "Returns a list of all registered users (admin only)",
        responses = [
            ApiResponse(responseCode = "200", description = "Users listed successfully")
        ]
    )
    fun users(): ResponseEntity<List<UserResponse>> {
        val users = usersService.listUsers()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/v1/me")
    @Operation(
        summary = "Get current user's profile",
        responses = [ApiResponse(responseCode = "200", description = "User profile retrieved")]
    )
    fun getProfile(principal: Principal): ResponseEntity<UserResponse> {
        val username = principal.name
        val user = usersService.findByUsername(username)
        return ResponseEntity.ok(
            UserResponse(
                id = user.id!!,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                phoneNumber = user.phoneNumber,
                role = user.role
            )
        )
    }

    @PutMapping("/v1/me")
    @Operation(
        summary = "Update current user's profile",
        responses = [
            ApiResponse(responseCode = "200", description = "Profile updated"),
            ApiResponse(responseCode = "400", description = "Validation error")
        ]
    )
    fun updateProfile(
        principal: Principal,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<Any> {
        return try {
            val username = principal.name
            val user = usersService.findByUsername(username)
            val updated = usersService.updateMyProfile(user.id!!, request)
            ResponseEntity.ok(updated)
        } catch (e: TransferFundsException) {
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        }
    }
}