package org.example.app

import org.assertj.core.api.Assertions.assertThat
import org.example.core.domain.GarageConfig
import org.example.core.domain.Sector
import org.example.core.domain.Spot
import org.example.core.port.GarageConfigProvider
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("h2")
class GaragemIntegrationTest {

    @Autowired lateinit var rest: TestRestTemplate

    @TestConfiguration
    class FakeProviderConfig {
        @Bean
        @Primary
        fun fakeProvider(): GarageConfigProvider = object : GarageConfigProvider {
            override fun fetchConfig(): GarageConfig = GarageConfig(
                sectors = listOf(Sector("A", basePrice = 10.0, maxCapacity = 2)),
                spots = listOf(
                    Spot(id = 1, sector = "A", lat = -23.0, lng = -46.0),
                    Spot(id = 2, sector = "A", lat = -23.1, lng = -46.1),
                ),
            )
        }
    }

    private fun postJson(path: String, body: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val resp = rest.exchange(path, HttpMethod.POST, HttpEntity(body, headers), Void::class.java)
        assertThat(resp.statusCode.is2xxSuccessful).isTrue()
    }

    private fun getJsonForMap(path: String, body: String): Map<*, *> {
        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val resp = rest.exchange(path, HttpMethod.GET, HttpEntity(body, headers), Map::class.java)
        assertThat(resp.statusCode.is2xxSuccessful).isTrue()
        return resp.body!!
    }

    @Test
    fun `entry parked and exit flow then revenue`() {
        postJson(
            "/webhook",
            """{"license_plate":"ABC1234","entry_time":"2025-01-01T12:00:00Z","event_type":"ENTRY"}"""
        )
        postJson(
            "/webhook",
            """{"license_plate":"ABC1234","lat":-23.0,"lng":-46.0,"event_type":"PARKED"}"""
        )
        postJson(
            "/webhook",
            """{"license_plate":"ABC1234","exit_time":"2025-01-01T14:00:00Z","event_type":"EXIT"}"""
        )

        // 120 min > 30 min (carência) → ceil(120/60) = 2 horas * R$10 * 0.90 (0% lotação) = 18.00
        val resp = getJsonForMap(
            "/revenue",
            """{"date":"2025-01-01","sector":"A"}"""
        )
        assertThat(resp["amount"] as Number).isEqualTo(18.0)
        assertThat(resp["currency"]).isEqualTo("BRL")
    }
}
