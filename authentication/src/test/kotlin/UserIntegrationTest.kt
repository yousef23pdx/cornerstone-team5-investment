import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserRequestIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun createUser() {
        val user = mapOf(
            "name" to "Ali",
            "age" to 23,
            "username" to "Ali123",
            "password" to "password123"
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val response = restTemplate.postForEntity(
            "/users/v1/register",
            HttpEntity(user, headers),
            String::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }

    @Test
    fun getUsers() {
        val response = restTemplate.getForEntity("/users/v1/list", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
    }
}