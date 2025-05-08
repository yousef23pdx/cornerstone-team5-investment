package authentication.users

import RegisterRequest
import UpdateProfileRequest
import UserResponse
import jakarta.inject.Named
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

const val USERNAME_MIN_LENGTH = 4
const val USERNAME_MAX_LENGTH = 30
const val PASSWORD_MIN_LENGTH = 9
const val PASSWORD_MAX_LENGTH = 30

@Named
@Service
class UsersService(
    private val usersRepository: UsersRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun registerUser(request: RegisterRequest): UserResponse {
        // Username validation
        if (request.username.isBlank() || request.username.length !in USERNAME_MIN_LENGTH..USERNAME_MAX_LENGTH) {
            throw TransferFundsException("Username must be between $USERNAME_MIN_LENGTH and $USERNAME_MAX_LENGTH characters")
        }

        // Password validation
        if (request.password.isBlank() || request.password.length !in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH) {
            throw TransferFundsException("Password must be between $PASSWORD_MIN_LENGTH and $PASSWORD_MAX_LENGTH characters")
        }

        // Email validation
        if (request.email.isBlank() || !request.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            throw TransferFundsException("Email must be a valid format")
        }

        // Phone number validation (optional)
        if (!request.phoneNumber.isNullOrBlank() &&
            !request.phoneNumber.matches(Regex("^\\+?[0-9]{8,15}$"))
        ) {
            throw TransferFundsException("Phone number must be 8–15 digits, optionally starting with +")
        }

        // Date of birth validation (optional)
        val parsedDob = if (!request.dateOfBirth.isNullOrBlank()) {
            try {
                val dob = LocalDate.parse(request.dateOfBirth)
                val today = LocalDate.now()
                val minDob = today.minusYears(120)

                if (dob.isAfter(today)) {
                    throw TransferFundsException("Date of birth cannot be in the future")
                }
                if (dob.isBefore(minDob)) {
                    throw TransferFundsException("Date of birth cannot be more than 120 years ago")
                }

                dob
            } catch (ex: Exception) {
                throw TransferFundsException("Date of birth must be in YYYY-MM-DD format")
            }
        } else null

        // Uniqueness checks
        if (usersRepository.findByUsername(request.username) != null) {
            throw TransferFundsException("Username already exists")
        }

        if (usersRepository.findByEmail(request.email) != null) {
            throw TransferFundsException("Email already in use")
        }

        // Create and save user
        val user = UserEntity(
            username = request.username,
            email = request.email,
            password = passwordEncoder.encode(request.password),
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            dateOfBirth = parsedDob
        )

        val saved = usersRepository.save(user)

        return UserResponse(
            id = saved.id!!,
            username = saved.username,
            email = saved.email,
            fullName = saved.fullName,
            phoneNumber = saved.phoneNumber,
            role = saved.role
        )
    }

    fun updateMyProfile(currentUserId: Long, request: UpdateProfileRequest): UserResponse {
        val user = findUserById(currentUserId)

        // Email uniqueness check
        if (request.email != user.email &&
            usersRepository.findByEmail(request.email) != null
        ) {
            throw TransferFundsException("Email already in use")
        }

        // Validate new email format
        if (!request.email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            throw TransferFundsException("Invalid email format")
        }

        // Validate phone (optional)
        if (!request.phoneNumber.isNullOrBlank() &&
            !request.phoneNumber.matches(Regex("^\\+?[0-9]{8,15}$"))
        ) {
            throw TransferFundsException("Phone number must be valid")
        }

        // Validate date of birth (optional)
        val parsedDob = if (!request.dateOfBirth.isNullOrBlank()) {
            try {
                val dob = LocalDate.parse(request.dateOfBirth)
                val today = LocalDate.now()
                val minDob = today.minusYears(120)

                if (dob.isAfter(today)) {
                    throw TransferFundsException("Date of birth cannot be in the future")
                }
                if (dob.isBefore(minDob)) {
                    throw TransferFundsException("Date of birth cannot be more than 120 years ago")
                }

                dob
            } catch (ex: Exception) {
                throw TransferFundsException("Date of birth must be in YYYY-MM-DD format")
            }
        } else user.dateOfBirth


        val updatedUser = user.copy(
            email = request.email,
            phoneNumber = request.phoneNumber,
            dateOfBirth = parsedDob,
            updatedAt = LocalDateTime.now()
        )

        val saved = usersRepository.save(updatedUser)

        return UserResponse(
            id = saved.id!!,
            username = saved.username,
            email = saved.email,
            fullName = saved.fullName,
            phoneNumber = saved.phoneNumber,
            role = saved.role
        )
    }

    fun findByUsername(username: String): UserEntity {
        return usersRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found for username: $username")
    }

    fun listAllUsers(): List<UserEntity> {
        return usersRepository.findAll()
    }

    fun findUserById(id: Long): UserEntity {
        return usersRepository.findById(id)
            .orElseThrow { NoSuchElementException("User with id $id not found") }
    }

    fun updateUserRole(id: Long, newRole: String): UserEntity {
        val user = findUserById(id)
        val updated = user.copy(role = newRole.uppercase())
        return usersRepository.save(updated)
    }

    fun deleteUser(id: Long) {
        if (!usersRepository.existsById(id)) {
            throw NoSuchElementException("User with id $id not found")
        }
        usersRepository.deleteById(id)
    }

    fun listUsers(): List<UserResponse> {
        return usersRepository.findAll().map {
            UserResponse(
                id = it.id ?: 0,
                username = it.username,
                email = it.email,
                fullName = it.fullName,
                phoneNumber = it.phoneNumber,
                role = it.role
            )
        }
    }
}