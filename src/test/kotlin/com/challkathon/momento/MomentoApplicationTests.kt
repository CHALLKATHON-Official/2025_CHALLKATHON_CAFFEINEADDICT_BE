package com.challkathon.momento

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = [
        "jwt.secret=testSecretKeyForJwtTokenGenerationThatIsLongEnoughForHmac256Algorithm",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
    ]
)
@ActiveProfiles("test")
class MomentoApplicationTests {

    @Test
    fun contextLoads() {
        // Spring Context가 정상적으로 로드되는지 확인
    }
}