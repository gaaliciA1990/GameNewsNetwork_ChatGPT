package com.gamenews.data

import com.gamenews.models.Article
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

/**
 * Class for our database queries
 */
class ArticlesDatabase {
    private val client = KMongo.createClient().coroutine
    private val database = client.getDatabase("ArticlesDatabase")

    private val articles = database.getCollection<Article>()

    /**
     * Query all articles in the db
     */
    suspend fun getAllArticles(): List<Article> {
        return articles.find().toList()
    }

    /**
     * Query to create a new article
     */
    suspend fun createArticle(article: Article): Boolean {
        return articles.insertOne(article).wasAcknowledged()
    }

    /**
     * Query for an article by ID
     */
    suspend fun getArticleById(id: String): Article? {
        return articles.findOneById(id)
    }

    /**
     * Query to update an existing article, if it exists. False returned if
     * article not found with the ID
     */
    suspend fun updateArticle(article: Article): Boolean {
        val exists = articles.findOneById(article.id) != null

        return if (exists) {
            articles.updateOneById(article.id, article).wasAcknowledged()
        } else {
            return false
        }
    }

    /**
     * Delete article from the db if the ID is found.
     */
    suspend fun deleteArticle(id: String): Boolean {
        val exists = articles.findOneById(id) != null

        return if (exists) {
            articles.deleteOneById(id).wasAcknowledged()
        } else {
            return false
        }
    }
}
