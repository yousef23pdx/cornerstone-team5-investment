package authentication.users

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import jakarta.inject.Named
import org.springframework.data.jpa.repository.JpaRepository

@Named
interface UsersRepository : JpaRepository<UserEntity, Long> {
    fun findByUsername(username: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
}

@Entity
@Table(name = "users")
data class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    val fullName: String? = null,
    val phoneNumber: String? = null,

    @Column(nullable = false)
    val password: String,

    @Column(nullable = false)
    val role: String = "USER",

    val dateOfBirth: LocalDate? = null,

    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(
        null, "", "", null, null, "", "USER",
        null, LocalDateTime.now(), LocalDateTime.now()
    )
}