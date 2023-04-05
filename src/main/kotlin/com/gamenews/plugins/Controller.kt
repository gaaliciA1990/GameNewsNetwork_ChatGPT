package com.gamenews.plugins

import com.gamenews.models.Article
import com.gamenews.models.articles
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
class Controller {
    /**
     * Handles calls to show all articles
     */
    suspend fun showAllArticles(context: PipelineContext<Unit, ApplicationCall>) {
        context.call.respond(
            FreeMarkerContent(
                "index.ftl",
                mapOf("articles" to articles)
            )
        )
    }

    /**
     * Handles calls for creating a new article
     */
    suspend fun newArticle(context: PipelineContext<Unit, ApplicationCall>) {
        context.call.respond(
            FreeMarkerContent(
                "new.ftl",
                model = null
            )
        )
    }

    /**
     * Saves a newly created article
     */
    suspend fun saveArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val formParams = context.call.receiveParameters()
        val title = formParams.getOrFail("title")
        val body = formParams.getOrFail("body")

        val newArticle = Article.newEntry(title, body)
        articles.add(newArticle)

        context.call.respondRedirect(
            "/articles/${newArticle.id}"
        )
    }

    /**
     * Showing articles when called
     */
    suspend fun showArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<Int>("id").toInt()

        context.call.respond(
            FreeMarkerContent(
                "show.ftl",
                mapOf("article" to articles.find { it.id == id })
            )
        )
    }

    /**
     * Handles calls for editing of articles
     */
    suspend fun editArticle(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<Int>("id").toInt()

        context.call.respond(
            FreeMarkerContent(
                "edit.ftl",
                mapOf("article" to articles.find { it.id == id })
            )
        )
    }

    /**
     * Handles calls to update and/or delete an article
     */
    suspend fun postArticleById(context: PipelineContext<Unit, ApplicationCall>) {
        val id = context.call.parameters.getOrFail<Int>("id").toInt()
        val formParams = context.call.receiveParameters()

        when (formParams.getOrFail("_action")) {
            "update" -> {
                val index = articles.indexOf(articles.find { it.id == id })
                val title = formParams.getOrFail("title")
                val body = formParams.getOrFail("body")

                articles[index].title = title
                articles[index].body = body

                context.call.respondRedirect(
                    "/article/$id"
                )
            }

            "delete" -> {
                articles.removeIf { it.id == id }

                context.call.respondRedirect(
                    "/articles"
                )
            }
        }
    }
}
