package com.gamenews.models

import java.util.concurrent.atomic.AtomicInteger

/**
 * Model class for the news articles
 */
class Article
private constructor(
    val id: Int,
    var title: String,
    var body: String
) {
    companion object {
        // automatically generate unique ID
        private val idCounter = AtomicInteger()

        /**
         * Create a new article with an id, title, and body
         */
        fun newEntry(title: String, body: String) = Article(idCounter.getAndIncrement(), title, body)
    }
}

// mutable list of articles for storing articles
val articles = mutableListOf(
    Article.newEntry(
        "Vanu has turned the tide",
        "With the release of this app, Vanu grows stronger"
    )
)
