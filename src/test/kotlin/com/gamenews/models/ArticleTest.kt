package com.gamenews.models

import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class ArticleTest {
    // Class variables for testing
    var title: String = "Test Article Title"
    var body: String = "This article is the bee knees, balls to the wall"
    var date: LocalDateTime = LocalDateTime.parse("2023-04-14T12:41:00")

    /**
     * Tests that a new articles model is created when newEntry is called
     */
    @Test
    fun test_newEntry_creates_new_article() {
        // SETUP

        // DO
        val newArticle: Article = Article.newEntry(title, body, date)

        // ASSERT
        assertEquals(title, newArticle.title)
        assertEquals(body, newArticle.body)
        assertEquals(date, newArticle.publishDate)
    }
}
