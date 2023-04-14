package com.gamenews.plugins

import com.gamenews.data.ArticlesDatabase
import com.gamenews.models.Article
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.util.getOrFail

/**
 * This class handles the calls for all routes
 */
class Controller(
    private val db: ArticlesDatabase
) {

    /**
     * Handles calls to show all articles
     */
    suspend fun displayAllArticles(call: ApplicationCall) {
        val allArticles = db.getAllArticles()

        call.respond(
            HttpStatusCode.OK,
            FreeMarkerContent(
                "index.ftl",
                mapOf("articles" to allArticles)
            )
        )
    }

    /**
     * Handles calls for creating new article and redirects to the page to
     * being the new article creation.
     */
    suspend fun displayNewArticlePage(call: ApplicationCall) {
        call.respond(
            HttpStatusCode.OK,
            FreeMarkerContent(
                "new.ftl",
                model = null
            )
        )
    }

    /**
     * Saves a newly created article in the page redirect from createNewArticle. If successful, redirects to new
     * article, otherwise we return a NotModified response
     */
    suspend fun saveNewArticle(call: ApplicationCall) {
        val formParams = call.receiveParameters()
        val title = formParams.getOrFail("title")
        val body = formParams.getOrFail("body")

        // Create the article model with the form params, ID is autocreated
        val newArticle = Article.newEntry(title, body)

        // If the article is created successfully, redirect to the article
        if (db.createArticle(newArticle)) {
            call.respondRedirect(
                "/articles/${newArticle.id}"
            )
        } else {
            call.respond(
                HttpStatusCode.NotModified,
                "Failed to save your article, if this continues, please contact dev"
            )
        }
    }

    /**
     * Shows a single article by ID when called. If successful, redirects to the selected article,
     * else returns NOT FOUND response
     */
    suspend fun displaySingleArticle(call: ApplicationCall) {
        val id = call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)

        article?.let {
            call.respond(
                HttpStatusCode.OK,
                FreeMarkerContent(
                    "show.ftl",
                    mapOf("article" to article)
                )
            )
        } ?: call.respond(
            HttpStatusCode.NotFound,
            "Sorry, this article no longer exists"
        )
    }

    /**
     * Handles calls for editing of an article. Checks if the article exists first, in case the article
     * was deleted before the edit button was clicked
     */
    suspend fun displayEditArticle(call: ApplicationCall) {
        val id = call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)

        if (article == null) {
            call.respond(
                HttpStatusCode.NotFound,
                "Sorry, this article no longer exists"
            )
            return
        }

        call.respond(
            FreeMarkerContent(
                "edit.ftl",
                mapOf("article" to article)
            )
        )
    }

    /**
     * Handles calls to update an article
     */
    suspend fun updateArticleById(call: ApplicationCall) {
        val id = call.parameters.getOrFail<String>("id")
        val formParams = call.receiveParameters()

        val article = db.getArticleById(id)

        // Make sure the article still exists in case it's deleted before edit clicked
        if (article == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                "Sorry, this article no longer exists"
            )
            return
        }

        // update the title and body of the article, if not null
        article.title = formParams.getOrFail("title")
        article.body = formParams.getOrFail("body")

        if (db.updateArticle(article)) {
            call.respondRedirect(
                "/articles/$id"
            )
        } else {
            call.respond(
                HttpStatusCode.NotModified,
                "Failed to update the article, please try again."
            )
        }
    }

    /**
     * Handles calls to delete an article from the db.
     */
    suspend fun deleteArticleById(call: ApplicationCall) {
        val id = call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)

        // Make sure the article still exists in case it's deleted before delete clicked
        if (article == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                "Sorry, someone beat you to the punch. We couldn't find that article"
            )
            return
        }
        // If the articles was successfully deleted, redirect back to the articles page
        if (db.deleteArticle(article.id)) {
            call.respondRedirect(
                "/articles"
            )
        } else {
            call.respond(
                HttpStatusCode.NotModified,
                "Deletion of the article was not successful!"
            )
        }
    }
}
