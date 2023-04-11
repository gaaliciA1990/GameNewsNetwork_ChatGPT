package com.gamenews

import com.gamenews.data.ArticlesDatabase
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    lateinit var mockDB: ArticlesDatabase
    lateinit var controller: Controller

    @BeforeTest
    fun beforeEach() {
        controller = Controller(mockDB)
    }

    @AfterTest
    fun aftereach() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun testRoot() = testApplication {
        // SETUP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        // mock the articles in the mock DB
        coEvery { mockDB.getAllArticles() } returns listOf(mockk(relaxed = true), mockk(relaxed = true))

        // DO
        val response = client.get("/")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Game News Network"))
    }
}
