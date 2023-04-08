package com.gamenews.models

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

/**
 * Model class for the news articles
 */
data class Article(
    @BsonId
    val id: String,
    var title: String,
    var body: String
) {
    companion object {
        // automatically generate unique ID
        private val idCounter = ObjectId().toString()

        /**
         * Create a new article with an id, title, and body
         */
        fun newEntry(title: String, body: String) = Article(idCounter, title, body)
    }
}
