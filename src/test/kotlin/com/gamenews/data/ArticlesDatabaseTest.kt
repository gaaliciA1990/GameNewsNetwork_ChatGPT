package com.gamenews.data

import com.gamenews.models.Article
import com.mongodb.client.result.InsertOneResult
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
import kotlin.test.assertTrue

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
        val result = mockk<InsertOneResult>()

        coEvery { mockCollection.insertOne(article) } returns result
        coEvery { result.wasAcknowledged() } returns true

        // DO
        val added = articlesDB.createArticle(article)

        // ASSERT
        assertTrue(added)
    }
}
// SET UP

// DO

// ASSERT
