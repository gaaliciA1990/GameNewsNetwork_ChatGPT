package com.gamenews

import com.gamenews.data.ArticlesDatabase
import com.gamenews.models.Article
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
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
        mockDB = mockk()
        controller = Controller(mockDB)
    }

    @AfterTest
    fun aftereach() {
        clearAllMocks()
        unmockkAll()
    }

    /**
     * Integration test to confirm we load the articles page and
     * all information is displayed from template
     */
    @Test
    fun testRoot() = testApplication {
        // SETUP
        environment {
            config = ApplicationConfig("application-test.conf")
        }
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

    @Test
    fun `new article page is displayed when going to create a new article`() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        // DO
        val response = client.get("/articles/new")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Create new article"))
    }

    @Test
    fun `save new articles creates an article and responds with HTTPS Found status`() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Test Title"
        val body = "I am a body, feed me"

        // Mock db to create article
        coEvery { mockDB.createArticle(any()) } returns true

        // DO
        val response = client.post("/articles") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("body", body)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `save new articles fails to save and responds with HTTPS Not Modified status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Does it matter?"
        val body = "I should fail!"

        // Mock db to create article
        coEvery { mockDB.createArticle(any()) } returns false

        // DO
        val response = client.post("/articles") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("body", body)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Failed to save your article"))
    }

    @Test
    fun `single article is displayed when using an article id and responds with HTTPS OK status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Amazing Test Article"
        val body = "This test is generating wonders!"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle

        // DO
        val response = client.get("/articles/${testArticle.id}")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(title))
    }

    @Test
    fun `single article is not displayed when using an id and responds with HTTPS Not Found status `() =
        testApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }

            val testArticle = Article.newEntry("null", "null")

            // Mock db to get an article but return null value
            coEvery { mockDB.getArticleById(testArticle.id) } returns null

            // DO
            val response = client.get("/articles/${testArticle.id}")

            // ASSERT
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
        }

    @Test
    fun `display article to edit shows the edit page`() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "Edit Me Article"
        val body = "I'm so lonely!"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle

        // DO
        val response = client.get("/articles/${testArticle.id}/edit")

        // ASSERT
        assertTrue(response.bodyAsText().contains("Edit article"))
    }

    @Test
    fun `display article fails to shows the edit page and responds with HTTPS Not Found status`() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val testArticle = Article.newEntry("null", "null")

        // Mock db to get an article but return null value
        coEvery { mockDB.getArticleById(testArticle.id) } returns null

        // DO
        val response = client.get("/articles/${testArticle.id}/edit")

        // ASSERT
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
    }

    @Test
    fun `updating an article updates the article in the db and returns HTTPS Found status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Edit Me Article"
        val body = "I'm so lonely!"
        val newTitle = "I'm a new article"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        // Mock update to return true for success
        coEvery { mockDB.updateArticle(testArticle) } returns true

        // DO
        val response = client.post("/articles/${testArticle.id}") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", newTitle)
                        append("body", body)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `updating an article fails to update in the db and returns HTTPS Not Modified status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val newTitle = "I'm a new article"

        val testArticle = Article.newEntry("null", "null")

        // Mock db to get an article
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        // Mock update to return false for failure
        coEvery { mockDB.updateArticle(testArticle) } returns false

        // DO
        val response = client.post("/articles/${testArticle.id}") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", newTitle)
                        append("body", "null")
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Failed to update the article, please try again."))
    }

    @Test
    fun `updating an article fails due to article not found and returns HTTPS Bad Request status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "I don't exist"
        val body = "I'm not real"
        val newTitle = "I will fail"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns null

        // DO
        val response = client.post("/articles/${testArticle.id}") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", newTitle)
                        append("body", body)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
    }

    @Test
    fun `deleting an article succeeds and returns HTTPS Found status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Deleted Article"
        val body = "What did I do to deserve this?!"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockDB.deleteArticle(testArticle.id) } returns true

        // DO
        val response = client.delete("/articles/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `deleting an article fails to delete and returns HTTPS Not Modified status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "I can't be deleted"
        val body = "Evil laughs, muahahahahaha!"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockDB.deleteArticle(testArticle.id) } returns false

        // DO
        val response = client.delete("/articles/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Deletion of the article was not successful!"))
    }

    @Test
    fun `deleting an article fails due to article not found and returns HTTPS Bad Request status `() = testApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "I don't exist"
        val body = "I'm not real"

        val testArticle = Article.newEntry(title, body)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns null

        // DO
        val response = client.delete("/articles/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            response.bodyAsText().contains("Sorry, someone beat you to the punch. We couldn't find that article")
        )
    }
}
