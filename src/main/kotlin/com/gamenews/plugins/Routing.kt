package com.gamenews.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting(
    controller: Controller
) {
    routing {
        get("/") {
            call.respondRedirect("articles")
        }

        route("articles") {
            /**
             * Show a list of articles.
             */
            get {
                controller.displayAllArticles(this)
            }

            /**
             * Show a page with fields for creating a new article
             */
            get("new") {
                controller.displayNewArticlePage(this)
            }

            /**
             * Save an article
             */
            post {
                controller.saveNewArticle(this)
            }

            /**
             * Show an article with a specific id
             */
            get("{id}") {
                controller.displaySingleArticle(this)
            }

            /**
             * Show a page with fields for editing an article
             */
            get("{id}/edit") {
                controller.displayEditArticle(this)
            }

            /**
             * Update an article
             */
            post("{id}") {
                controller.updateArticleById(this)
            }

            /**
             * Delete an article
             */

            post("{id}") {
                controller.deleteArticleById(this)
            }
        }
    }
}
