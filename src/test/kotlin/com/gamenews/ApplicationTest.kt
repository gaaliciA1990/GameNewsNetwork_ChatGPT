@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.gamenews

import com.gamenews.data.ArticlesRepository
import com.gamenews.models.Article
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import java.time.LocalDateTime
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    lateinit var mockDB: ArticlesRepository
    lateinit var controller: Controller
    private val publishDate: LocalDateTime = LocalDateTime.parse("2023-04-16T16:41:00")

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

    private fun configuredTestApplication(
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        environment {
            config = ApplicationConfig("application-test.conf")
        }
        block()
    }

    /**
     * Integration test to confirm we load the articles page and
     * all information is displayed from template
     */
    @Test
    fun testRoot() = configuredTestApplication {
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val articleCount = 3

        // mock the articles in the mock DB
        coEvery { mockDB.getArticlesCount() } returns articleCount
        coEvery { mockDB.getSetOfArticles(any(), any()) } returns listOf(mockk(relaxed = true), mockk(relaxed = true))

        // DO
        val response = client.get("/")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("GNN"))
    }

    @Test
    fun `new article page is displayed when going to create a new article`() = configuredTestApplication {
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
    fun `save new articles creates an article and responds with HTTPS Found status`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Test Title"
        val body = "I am a body, feed me"
        val date = "2023-04-16 16:41:00"

        // Mock db to create article
        coEvery { mockDB.createArticle(any()) } returns true

        // DO
        val response = client.post("/articles") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("body", body)
                        append("publish_date", date)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `save new articles fails to save and responds with HTTPS Not Modified status `() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "Does it matter?"
        val body = "I should fail!"
        val date = "2023-04-16 16:41:00"

        // Mock db to create article
        coEvery { mockDB.createArticle(any()) } returns false

        // DO
        val response = client.post("/articles") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("title", title)
                        append("body", body)
                        append("publish_date", date)
                    }
                )
            )
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Failed to save your article"))
    }

    @Test
    fun `single article is displayed when using an article id and responds with HTTPS OK status `() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val title = "Amazing Test Article"
            val body = "This test is generating wonders!"

            val testArticle = Article.newEntry(title, body, publishDate)

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
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article but return null value
            coEvery { mockDB.getArticleById(testArticle.id) } returns null

            // DO
            val response = client.get("/articles/${testArticle.id}")

            // ASSERT
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
        }

    @Test
    fun `display article to edit shows the edit page`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "Edit Me Article"
        val body = "I'm so lonely!"

        val testArticle = Article.newEntry(title, body, publishDate)

        // Mock db to get an article
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle

        // DO
        val response = client.get("/articles/${testArticle.id}/edit")

        // ASSERT
        assertTrue(response.bodyAsText().contains("Edit article"))
    }

    @Test
    fun `display article fails to shows the edit page and responds with HTTPS Not Found status`() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article but return null value
            coEvery { mockDB.getArticleById(testArticle.id) } returns null

            // DO
            val response = client.get("/articles/${testArticle.id}/edit")

            // ASSERT
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
        }

    @Test
    fun `updating an article updates the article in the db and returns HTTPS Found status `() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val title = "Edit Me Article"
            val body = "I'm so lonely!"
            val newTitle = "I'm a new article"

            val testArticle = Article.newEntry(title, body, publishDate)

            // Mock db to get an article
            coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
            // Mock update to return true for success
            coEvery { mockDB.updateArticle(testArticle) } returns true

            // DO
            val response = client.post("/articles/update/${testArticle.id}") {
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
    fun `updating an article fails to update in the db and returns HTTPS Not Modified status `() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val newTitle = "I'm a new article"

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article
            coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
            // Mock update to return false for failure
            coEvery { mockDB.updateArticle(testArticle) } returns false

            // DO
            val response = client.post("/articles/update/${testArticle.id}") {
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
    fun `updating an article fails due to article not found and returns HTTPS Bad Request status `() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val title = "I don't exist"
            val body = "I'm not real"
            val newTitle = "I will fail"

            val testArticle = Article.newEntry(title, body, publishDate)

            // Mock db to get an article but doesn't find it
            coEvery { mockDB.getArticleById(testArticle.id) } returns null

            // DO
            val response = client.post("/articles/update/${testArticle.id}") {
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
    fun `deleting an article succeeds and returns HTTPS Found status `() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Deleted Article"
        val body = "What did I do to deserve this?!"

        val testArticle = Article.newEntry(title, body, publishDate)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockDB.deleteArticle(testArticle.id) } returns true

        // DO
        val response = client.post("/articles/delete/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `deleting an article fails to delete and returns HTTPS Not Modified status `() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "I can't be deleted"
        val body = "Evil laughs, muahahahahaha!"

        val testArticle = Article.newEntry(title, body, publishDate)

        // Mock db to get an article but doesn't find it
        coEvery { mockDB.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockDB.deleteArticle(testArticle.id) } returns false

        // DO
        val response = client.post("/articles/delete/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Deletion of the article was not successful!"))
    }

    @Test
    fun `deleting an article fails due to article not found and returns HTTPS Bad Request status `() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val title = "I don't exist"
            val body = "I'm not real"

            val testArticle = Article.newEntry(title, body, publishDate)

            // Mock db to get an article but doesn't find it
            coEvery { mockDB.getArticleById(testArticle.id) } returns null

            // DO
            val response = client.post("/articles/delete/${testArticle.id}") {
                setBody(MultiPartFormDataContent(formData { }))
            }

            // ASSERT
            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertTrue(
                response.bodyAsText().contains("Sorry, someone beat you to the punch. We couldn't find that article")
            )
        }
}
