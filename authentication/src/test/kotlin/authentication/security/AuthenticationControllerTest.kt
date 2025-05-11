
package authentication.security

import authentication.jwt.JwtService
import authentication.users.UserEntity
import authentication.users.UsersService
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.security.Principal
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var authenticationManager: AuthenticationManager

    @MockBean
    lateinit var userDetailsService: UserDetailsService

    @MockBean
    lateinit var jwtService: JwtService

    @MockBean
    lateinit var usersService: UsersService

    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper = ObjectMapper()
    }

    @Test
    fun `login returns JWT on valid credentials`() {
        val request = AuthenticationRequest("testuser", "password")
        val auth = mock(Authentication::class.java)

        `when`(authenticationManager.authenticate(any())).thenReturn(auth)
        `when`(auth.isAuthenticated).thenReturn(true)
        `when`(jwtService.generateToken(request.username)).thenReturn("mocked-jwt-token")

        mockMvc.perform(
            post("/auth/v1/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
    }

    @Test
    fun `check-token returns userId if token is valid`() {
        val user = UserEntity(
            id = 123L,
            username = "testuser",
            email = "test@example.com",
            password = "pass",
            role = "USER"
        )

        `when`(usersService.findByUsername("testuser")).thenReturn(user)

        mockMvc.perform(
            post("/auth/v1/check-token")
                .with(csrf())
                .with(user("testuser").roles("USER"))
        ).andExpect(status().isOk)
         .andExpect(jsonPath("$.userId").value(123))
    }
}
