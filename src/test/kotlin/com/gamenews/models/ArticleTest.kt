package com.gamenews.models

import com.gamenews.data.ArticlesDatabase
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleTest {
    // Class variables for testing
    var title: String = "Test Article Title"
    var body: String = "This article is the bee knees, balls to the wall"
    lateinit var mockClient: KMongo
    lateinit var mockDB: ArticlesDatabase

    @BeforeTest
    fun beforeEach() {
        mockClient = mockk<KMongo>()
        mockDB = mockk<ArticlesDatabase>()
    }

    @AfterTest
    fun aftereach() {
        clearAllMocks()
        unmockkAll()
    }

    /**
     * Tests that a new articles model is created when newEntry is called
     */
    @Test
    fun test_newEntry_creates_new_article() = runTest {
        // SETUP

        // DO
        val newArticle: Article = Article.newEntry(title, body)

        // ASSERT
        assertEquals(title, newArticle.title)
        assertEquals(body, newArticle.body)
    }
}
