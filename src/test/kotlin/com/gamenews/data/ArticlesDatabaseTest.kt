package com.gamenews.data

import com.gamenews.models.Article
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for the ArticlesDB class
 */
class ArticlesDatabaseTest {
    lateinit var mockCollection: CoroutineCollection<Article>
    lateinit var articlesDB: ArticlesDatabase
    lateinit var publisher: CoroutineFindPublisher<Article>

    @BeforeTest
    fun beforeEach() {
        mockCollection = mockk()
        articlesDB = ArticlesDatabase(mockCollection)
        publisher = mockk()
    }

    @AfterTest
    fun aftereach() {
        clearAllMocks()
        unmockkAll()
    }

    @Test
    fun `getAllArticles returns a list of articles in the db we added`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")
        val article2: Article = Article.newEntry("Test 2", "Test Body 2")

        coEvery { mockCollection.find() } returns publisher
        coEvery { publisher.toList() } returns listOf(article1, article2)

        // DO
        val listOfArticles = articlesDB.getAllArticles()

        // ASSERT
        assertFalse(listOfArticles.isEmpty())
        assertEquals(2, listOfArticles.size)
        assertEquals("Test 1", listOfArticles[0].title)
    }

    @Test
    fun `getAllArticles returns an empty list of articles`() = runTest {
        // SET UP
        coEvery { mockCollection.find() } returns publisher
        coEvery { publisher.toList() } returns emptyList()

        // DO
        val listOfArticles = articlesDB.getAllArticles()

        // ASSERT
        assertTrue(listOfArticles.isEmpty())
    }

    @Test
    fun `createArticle adds the new article to the db with the correct acknowledge`() = runTest {
        // SET UP
        val article: Article = Article.newEntry("New Article", "New test body")
        val ackResult = mockk<InsertOneResult>()

        coEvery { mockCollection.insertOne(article, any()) } returns ackResult
        coEvery { ackResult.wasAcknowledged() } returns true

        // DO
        val added = articlesDB.createArticle(article)

        // ASSERT
        assertTrue(added)
    }

    @Test
    fun `getArticleById returns the article we query for`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")

        coEvery { mockCollection.findOneById(article1.id) } returns article1

        // DO
        val returnedArticle = articlesDB.getArticleById(article1.id)

        // ASSERT
        assertNotNull(returnedArticle)
        assertEquals(article1, returnedArticle)
        assertEquals(article1.title, returnedArticle.title)
    }

    @Test
    fun `updateArticle changes the title of the article in the db`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")
        val updatedArticle = article1.copy(title = "New Test 1")
        val ackResult = mockk<UpdateResult>()

        // make sure our article is returned
        coEvery { mockCollection.findOneById(article1.id) } returns article1

        coEvery { mockCollection.updateOneById(article1.id, updatedArticle, any()) } returns ackResult
        coEvery { ackResult.wasAcknowledged() } returns true

        // DO
        val updated = articlesDB.updateArticle(updatedArticle)

        // ASSERT
        assertTrue(updated)
    }

    @Test
    fun `updateArticle returns false when the article is not in the db`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")
        val updatedArticle = article1.copy(title = "New Test 1")
        val ackResult = mockk<UpdateResult>()

        // make sure our article isn't found
        coEvery { mockCollection.findOneById(article1.id) } returns null

        // DO
        val updated = articlesDB.updateArticle(updatedArticle)

        // ASSERT
        assertFalse(updated)
    }

    @Test
    fun `deleteArticle deletes the article in the db by ID`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")
        val ackResult = mockk<DeleteResult>()

        // make sure our article is returned
        coEvery { mockCollection.findOneById(article1.id) } returns article1

        coEvery { mockCollection.deleteOneById(article1.id) } returns ackResult
        coEvery { ackResult.wasAcknowledged() } returns true

        // DO
        val deleted = articlesDB.deleteArticle(article1.id)

        // ASSERT
        assertTrue(deleted)
    }

    @Test
    fun `deleteArticle returns false when the article is not in the db`() = runTest {
        // SET UP
        val article1: Article = Article.newEntry("Test 1", "Test Body")
        val ackResult = mockk<DeleteResult>()

        // make sure our article isn't found
        coEvery { mockCollection.findOneById(article1.id) } returns null

        // DO
        val updated = articlesDB.deleteArticle(article1.id)

        // ASSERT
        assertFalse(updated)
    }
}
