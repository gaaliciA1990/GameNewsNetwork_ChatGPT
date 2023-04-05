package com.gamenews.models

import com.gamenews.models.Article
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ArticleTest {
    // Class variables for testing
    var title: String = "Test Article Title"
    var body: String = "This article is the bee knees, balls to the wall"

    @Test
    fun test_newEntry_adds_new_article(){
        // SETUP

        // DO
        val newArticle: Article = Article.newEntry(title, body)
        articles.add(newArticle)

        // ASSERT
        assertEquals(title, newArticle.title)
        assertEquals(body, newArticle.body)
        assertEquals(2, articles.size)
    }
}