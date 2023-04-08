package com.gamenews.plugins

import com.gamenews.data.ArticlesDatabase
import com.gamenews.models.Article
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.util.getOrFail
import io.ktor.util.pipeline.PipelineContext

/**
 * This class handles the calls for all routes
 */
class Controller(
    private val db: ArticlesDatabase
) {

    /**
     * Handles calls to show all articles
     */
    suspend fun getAllArticles(context: PipelineContext<Unit, ApplicationCall>) {
        val allArticles = db.getAllArticles()

        if (allArticles.isEmpty()) {
            context.call.respond(
                HttpStatusCode.NotFound,
                "Something went terribly wrong. You have no articles!"
            )
            return
        }

        context.call.respond(
            HttpStatusCode.OK,
            FreeMarkerContent(
                "index.ftl",
                mapOf("articles" to allArticles)
            )
        )
    }

    /**
     * Handles calls for creating a new article
     */
    suspend fun createNewArticle(context: PipelineContext<Unit, ApplicationCall>) {
        context.call.respond(
            HttpStatusCode.OK,
            FreeMarkerContent(
                "new.ftl",
                model = null
            )
        )
    }

    /**
     * Saves a newly created article. If successful, redirects to new article,
     * otherwise we return a NotModified response
     */
    suspend fun saveArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val formParams = context.call.receiveParameters()
        val title = formParams.getOrFail("title")
        val body = formParams.getOrFail("body")

        // Create the article model with the form params, ID is autocreated
        val newArticle = Article.newEntry(title, body)

        // If the article is created successfully, redirect to the article
        if (db.createArticle(newArticle)) {
            context.call.respondRedirect(
                "/articles/${newArticle.id}"
            )
        } else {
            context.call.respond(
                HttpStatusCode.NotModified,
                "Failed to save your article, if this continues, please contact dev"
            )
        }
    }

    /**
     * Shows a single article by ID when called. If successful, redirects to the selected article,
     * else returns NOT FOUND response
     */
    suspend fun getSingleArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)

        article?.let {
            context.call.respond(
                HttpStatusCode.OK,
                FreeMarkerContent(
                    "show.ftl",
                    mapOf("article" to article)
                )
            )
        } ?: context.call.respond(
            HttpStatusCode.NotFound,
            "Sorry, this article no longer exists"
        )
    }

    /**
     * Handles calls for editing of articles. Checks if the articles exists first, in case the article
     * was deleted before the edit button was clicked
     */
    suspend fun editArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)

        if (article == null) {
            context.call.respond(
                HttpStatusCode.NotFound,
                "Sorry, this article no longer exists"
            )
            return
        }

        context.call.respond(
            FreeMarkerContent(
                "edit.ftl",
                mapOf("article" to article)
            )
        )
    }

    /**
     * Handles calls to update an article
     */
    suspend fun updateArticleById(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<String>("id")
        val formParams = context.call.receiveParameters()

        when (formParams.getOrFail("_action")) {
            "update" -> {
                val article = db.getArticleById(id)
                if (article == null) {
                    context.call.respond(
                        HttpStatusCode.BadRequest,
                        "Sorry, this article no longer exists"
                    )
                    return
                }

                // update the title and body of the article, if not null
                article.title = formParams.getOrFail("title")
                article.body = formParams.getOrFail("body")

                if (db.updateArticle(article)) {
                    context.call.respondRedirect(
                        "/article/$id"
                    )
                } else {
                    context.call.respond(
                        HttpStatusCode.NotModified,
                        "Failed to update the article, please try again."
                    )
                }
            }
        }
    }

    /**
     * Handles calls to delete an articles from the db.
     */
    suspend fun deleteArticleById(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<String>("id")
        val article = db.getArticleById(id)
        val formParams = context.call.receiveParameters()

        when (formParams.getOrFail("_action")) {
            "delete" -> {
                if (article == null) {
                    context.call.respond(
                        HttpStatusCode.BadRequest,
                        "Sorry, someone beat you to the punch. We couldn't find that article"
                    )
                    return
                }
                // If the articles was successfully deleted, redirect back to the articles page
                if (db.deleteArticle(article.id)) {
                    context.call.respondRedirect(
                        "/articles"
                    )
                } else {
                    context.call.respond(
                        HttpStatusCode.NotModified,
                        "Deletion of the article was not successful!"
                    )
                }
            }
        }
    }
}
