package com.gamenews.plugins

import com.gamenews.models.Article
import com.gamenews.models.articles
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.freemarker.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("articles")
        }

        route("articles") {
            /**
             * Show a list of articles.
             */
            get {
                call.respond(
                    FreeMarkerContent(
                        "index.ftl",
                        mapOf("articles" to articles)
                    )
                )
            }
            /**
             * Show a page with fields for creating a new article
             */
            get("new") {
                call.respond(
                    FreeMarkerContent(
                        "new.ftl",
                        model = null
                    )
                )
            }
            /**
             * Save an article
             */
            post {
                val formParams = call.receiveParameters()
                val title = formParams.getOrFail("title")
                val body = formParams.getOrFail("body")

                val newArticle = Article.newEntry(title, body)
                articles.add(newArticle)

                call.respondRedirect("/articles/${newArticle.id}")
            }
            /**
             * Show an article with a specific id
             */
            get("{id}") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("show.ftl", mapOf("article" to articles.find { it.id == id })))
            }
            /**
             * Show a page with fields for editing an article
             */
            get("{id}/edit") {
                val id = call.parameters.getOrFail<Int>("id").toInt()
                call.respond(FreeMarkerContent("edit.ftl", mapOf("article" to articles.find { it.id == id })))
            }
            post("{id}") {
                // Update or delete an article
            }
        }
    }
}
