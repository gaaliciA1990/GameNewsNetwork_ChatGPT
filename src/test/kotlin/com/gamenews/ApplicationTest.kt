@file:Suppress("MaximumLineLength", "MaxLineLength")

package com.gamenews

import com.gamenews.data.AdminRepository
import com.gamenews.data.ArticlesRepository
import com.gamenews.exceptions.UnauthorizedAccessException
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
import kotlin.test.*

class ApplicationTest {
    lateinit var mockArticleRepo: ArticlesRepository
    lateinit var mockAdminRepo: AdminRepository
    lateinit var controller: Controller
    private val publishDate: LocalDateTime = LocalDateTime.parse("2023-04-16T16:41:00")

    @BeforeTest
    fun beforeEach() {
        mockArticleRepo = mockk()
        mockAdminRepo = mockk()
        controller = Controller(mockArticleRepo, mockAdminRepo)
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns false
    }

    @AfterTest
    fun afterEach() {
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
    fun `all articles are loaded and displayed with create new article visible as admin`() = configuredTestApplication {
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val articleCount = 3

        // mock the articles in the mock DB
        coEvery { mockArticleRepo.getArticlesCount() } returns articleCount
        coEvery { mockArticleRepo.getSetOfArticles(any(), any()) } returns listOf(
            mockk(relaxed = true),
            mockk(relaxed = true)
        )
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

        // DO
        val response = client.get("/")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("GNN"))
        assertTrue(response.bodyAsText().contains("Create article"))
    }

    @Test
    fun `all articles are loaded and displayed with create new article hidden as non admin`() = configuredTestApplication {
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val articleCount = 3

        // mock the articles in the mock DB
        coEvery { mockArticleRepo.getArticlesCount() } returns articleCount
        coEvery { mockArticleRepo.getSetOfArticles(any(), any()) } returns listOf(
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        // DO
        val response = client.get("/")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("GNN"))
        assertFalse(response.bodyAsText().contains("Create article"))
    }

    @Test
    fun `new article page inaccessible as non admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns false

        // DO

        // ASSERT
        assertFailsWith<UnauthorizedAccessException> {
            client.get("/articles/new")
        }
    }

    @Test
    fun `new article page is displayed as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

        // DO
        val response = client.get("/articles/new")

        // ASSERT
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Create new article"))
    }

    @Test
    fun `save new articles creates an article and responds with HTTPS Found status as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Test Title"
        val body = "I am a body, feed me"
        val date = "2023-04-16 16:41:00"

        // Mock db to create article
        coEvery { mockArticleRepo.createArticle(any()) } returns true
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
    fun `save new articles fails to save and responds with HTTPS Not Modified status as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "Does it matter?"
        val body = "I should fail!"
        val date = "2023-04-16 16:41:00"

        // Mock db to create article
        coEvery { mockArticleRepo.createArticle(any()) } returns false
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
    fun `single article is displayed when using an article id and responds with HTTPS OK status as admin`() =
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
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

            // DO
            val response = client.get("/articles/${testArticle.id}")

            // ASSERT
            assertEquals(HttpStatusCode.OK, response.status)
            assertTrue(response.bodyAsText().contains(title))
        }

    @Test
    fun `single article is not displayed when using an id and responds with HTTPS Not Found status as admin`() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article but return null value
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns null
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

            // DO
            val response = client.get("/articles/${testArticle.id}")

            // ASSERT
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
        }

    @Test
    fun `display article to edit fails as non admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "I'm Fake"
        val body = "Make me real!"

        val testArticle = Article.newEntry(title, body, publishDate)

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns false

        // Mock db to get an article
        coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle

        // ASSERT
        assertFailsWith<UnauthorizedAccessException> {
            client.get("/articles/${testArticle.id}/edit")
        }
    }

    @Test
    fun `display article to edit loads the edit page as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }

        val title = "Edit Me Article"
        val body = "I'm so lonely!"

        val testArticle = Article.newEntry(title, body, publishDate)

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

        // Mock db to get an article
        coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle

        // DO
        val response = client.get("/articles/${testArticle.id}/edit")

        // ASSERT
        assertTrue(response.bodyAsText().contains("Edit article"))
    }

    @Test
    fun `display article fails to shows the edit page and responds with HTTPS Not Found status as admin`() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article but return null value
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns null
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

            // DO
            val response = client.get("/articles/${testArticle.id}/edit")

            // ASSERT
            assertEquals(HttpStatusCode.NotFound, response.status)
            assertTrue(response.bodyAsText().contains("Sorry, this article no longer exists"))
        }

    @Test
    fun `updating an article fails as non admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Edit Me Article"
        val body = "I'm so lonely!"

        val testArticle = Article.newEntry(title, body, publishDate)

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns false

        // ASSERT
        assertFailsWith<UnauthorizedAccessException> {
            client.post("/articles/update/${testArticle.id}")
        }
    }

    @Test
    fun `updating an article updates the article in the db and returns HTTPS Found status as admin`() =
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
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle
            // Mock update to return true for success
            coEvery { mockArticleRepo.updateArticle(testArticle) } returns true
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
    fun `updating an article fails to update in the db and returns HTTPS Not Modified status as admin`() =
        configuredTestApplication {
            // SET UP
            application {
                configureRouting(controller)
                configureTemplating()
            }
            val newTitle = "I'm a new article"

            val testArticle = Article.newEntry("null", "null", publishDate)

            // Mock db to get an article
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle
            // Mock update to return false for failure
            coEvery { mockArticleRepo.updateArticle(testArticle) } returns false
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
    fun `updating an article fails due to article not found and returns HTTPS Bad Request status as admin`() =
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
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns null
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
    fun `deleting an article fails as non admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Deleted Article"
        val body = "What did I do to deserve this?!"

        val testArticle = Article.newEntry(title, body, publishDate)

        coEvery { mockAdminRepo.getAdminByIp(any()) } returns false

        // ASSERT
        assertFailsWith<UnauthorizedAccessException> {
            client.post("/articles/delete/${testArticle.id}")
        }
    }

    @Test
    fun `deleting an article succeeds and returns HTTPS Found status as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "Deleted Article"
        val body = "What did I do to deserve this?!"

        val testArticle = Article.newEntry(title, body, publishDate)

        // Mock db to get an article but doesn't find it
        coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockArticleRepo.deleteArticle(testArticle.id) } returns true
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

        // DO
        val response = client.post("/articles/delete/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.Found, response.status)
    }

    @Test
    fun `deleting an article fails to delete and returns HTTPS Not Modified status as admin`() = configuredTestApplication {
        // SET UP
        application {
            configureRouting(controller)
            configureTemplating()
        }
        val title = "I can't be deleted"
        val body = "Evil laughs, muahahahahaha!"

        val testArticle = Article.newEntry(title, body, publishDate)

        // Mock db to get an article but doesn't find it
        coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns testArticle
        coEvery { mockArticleRepo.deleteArticle(testArticle.id) } returns false
        coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

        // DO
        val response = client.post("/articles/delete/${testArticle.id}") {
            setBody(MultiPartFormDataContent(formData { }))
        }

        // ASSERT
        assertEquals(HttpStatusCode.NotModified, response.status)
        assertTrue(response.bodyAsText().contains("Deletion of the article was not successful!"))
    }

    @Test
    fun `deleting an article fails due to article not found and returns HTTPS Bad Request status as admin`() =
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
            coEvery { mockArticleRepo.getArticleById(testArticle.id) } returns null
            coEvery { mockAdminRepo.getAdminByIp(any()) } returns true

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
