package authentication.security

import authentication.users.UsersRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomerUserDetailsService(
    private val usersRepository: UsersRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = usersRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found")

        return User.withUsername(user.username)
            .username(user.username)
            .password(user.password)
            .build()
    }
}