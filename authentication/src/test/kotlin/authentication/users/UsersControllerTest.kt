package authentication.users

import RegisterRequest
import UpdateProfileRequest
import UserResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDate
import java.util.*

class UsersControllerTest {

    private val usersRepository: UsersRepository = mock(UsersRepository::class.java)
    private val passwordEncoder: PasswordEncoder = mock(PasswordEncoder::class.java)
    private val service = UsersService(usersRepository, passwordEncoder)

    @Test
    fun `registerUser with valid input returns UserResponse`() {
        val request = RegisterRequest("user1", "test@example.com", "Password123", "User One", "12345678", "2000-01-01")
        val encodedPass = "encodedPass"

        `when`(usersRepository.findByUsername("user1")).thenReturn(null)
        `when`(usersRepository.findByEmail("test@example.com")).thenReturn(null)
        `when`(passwordEncoder.encode("Password123")).thenReturn(encodedPass)

        val savedUser = UserEntity(
            id = 1L,
            username = "user1",
            email = "test@example.com",
            password = encodedPass,
            fullName = "User One",
            phoneNumber = "12345678",
            dateOfBirth = LocalDate.parse("2000-01-01")
        )
        `when`(usersRepository.save(any(UserEntity::class.java))).thenReturn(savedUser)

        val result = service.registerUser(request)

        assertEquals("user1", result.username)
        assertEquals("test@example.com", result.email)
    }

    @Test
    fun `registerUser with existing username throws exception`() {
        val request = RegisterRequest("user1", "test@example.com", "Password123")
        `when`(usersRepository.findByUsername("user1")).thenReturn(UserEntity(username = "user1", email = "a@b.com", password = "pass"))

        val ex = assertThrows<TransferFundsException> {
            service.registerUser(request)
        }
        assertEquals("Username already exists", ex.message)
    }

    @Test
    fun `updateMyProfile with valid input updates fields`() {
        val existing = UserEntity(
            id = 1L,
            username = "user1",
            email = "test@example.com",
            password = "hashedpass",
            fullName = "User One",
            phoneNumber = "12345678",
            role = "USER"
        )

        val request = UpdateProfileRequest(
            email = "updated@example.com",
            phoneNumber = "99999999",
            dateOfBirth = "1995-01-01"
        )

        `when`(usersRepository.findById(1L)).thenReturn(Optional.of(existing))
        `when`(usersRepository.findByEmail("updated@example.com")).thenReturn(null)
        `when`(usersRepository.save(any(UserEntity::class.java))).thenAnswer { it.arguments[0] }

        val result = service.updateMyProfile(1L, request)

        assertEquals("updated@example.com", result.email)
        assertEquals("99999999", result.phoneNumber)
    }

    @Test
    fun `findUserById with invalid id throws exception`() {
        `when`(usersRepository.findById(99L)).thenReturn(Optional.empty())

        val ex = assertThrows<NoSuchElementException> {
            service.findUserById(99L)
        }

        assertEquals("User with id 99 not found", ex.message)
    }

    @Test
    fun `updateUserRole changes role to ADMIN`() {
        val user = UserEntity(
            id = 1L,
            username = "user1",
            email = "email@example.com",
            password = "pass",
            role = "USER"
        )

        `when`(usersRepository.findById(1L)).thenReturn(Optional.of(user))
        `when`(usersRepository.save(any(UserEntity::class.java))).thenAnswer { it.arguments[0] }

        val updated = service.updateUserRole(1L, "ADMIN")

        assertEquals("ADMIN", updated.role)
    }

    @Test
    fun `deleteUser with missing ID throws exception`() {
        `when`(usersRepository.existsById(42L)).thenReturn(false)

        val ex = assertThrows<NoSuchElementException> {
            service.deleteUser(42L)
        }

        assertEquals("User with id 42 not found", ex.message)
    }

    @Test
    fun `listUsers returns converted responses`() {
        val users = listOf(
            UserEntity(id = 1L, username = "u1", email = "e1", password = "p1"),
            UserEntity(id = 2L, username = "u2", email = "e2", password = "p2")
        )

        `when`(usersRepository.findAll()).thenReturn(users)

        val result = service.listUsers()

        assertEquals(2, result.size)
        assertEquals("u1", result[0].username)
    }
}