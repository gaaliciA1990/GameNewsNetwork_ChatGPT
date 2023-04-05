package com.gamenews.plugins

import com.gamenews.models.articles
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.freemarker.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondRedirect("articles")
        }

        route("articles") {
            /**
             * Get a list of articles.
             */
            get {
                // Show a list of articles
                call.respond(
                    FreeMarkerContent(
                        "index.ftl",
                        mapOf("articles" to articles)
                    )
                )
            }
            get("new") {
                // Show a page with fields for creating a new article
            }
            post {
                // Save an article
            }
            get("{id}") {
                // Show an article with a specific id
            }
            get("{id}/edit") {
                // Show a page with fields for editing an article
            }
            post("{id}") {
                // Update or delete an article
            }
        }
    }
}
