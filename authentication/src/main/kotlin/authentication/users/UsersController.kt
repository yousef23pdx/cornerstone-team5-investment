package authentication.users

import RegisterRequest
import UpdateProfileRequest
import UserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
@Tag(name = "AUTHENTICATION", description = "Handles user registration and listing")
class UsersController(
    private val usersService: UsersService
) {

    @PostMapping("/v1/register")
    @Operation(
        summary = "Register a new user",
        description = "Registers a new user using username and password",
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
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "List all users (Admin only)",
        description = "Returns a list of all registered users (ADMIN role required)",
        responses = [
            ApiResponse(responseCode = "200", description = "Users listed successfully"),
            ApiResponse(responseCode = "403", description = "Access denied")
        ]
    )
    fun listUsers(): ResponseEntity<List<UserResponse>> {
        val users = usersService.listUsers()
        return ResponseEntity.ok(users)
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun listAllUsersForAdmin(): ResponseEntity<List<UserEntity>> {
        return ResponseEntity.ok(usersService.listAllUsers())
    }

    @PutMapping("/admin/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserRole(@PathVariable id: Long, @RequestParam role: String): ResponseEntity<UserEntity> {
        val updated = usersService.updateUserRole(id, role)
        return ResponseEntity.ok(updated)
    }

    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<Void> {
        usersService.deleteUser(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/me")
    fun getMyProfile(@RequestAttribute("userId") userId: Long): ResponseEntity<UserResponse> {
        val user = usersService.findUserById(userId)
        return ResponseEntity.ok(
            UserResponse(
                id = user.id ?: 0,
                username = user.username,
                email = user.email,
                fullName = user.fullName,
                phoneNumber = user.phoneNumber,
                role = user.role
            )
        )
    }

    @PutMapping("/me")
    fun updateMyProfile(
        @RequestAttribute("userId") userId: Long,
        @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<UserResponse> {
        val updated = usersService.updateMyProfile(userId, request)
        return ResponseEntity.ok(updated)
    }
}