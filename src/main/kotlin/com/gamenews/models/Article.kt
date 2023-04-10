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
        /**
         * Create a new article with a new object id, title, and body
         */
        fun newEntry(title: String, body: String) = Article(ObjectId().toString(), title, body)
    }
}
