package com.gamenews.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.delete
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
                controller.displayArticlePages(this.call)
            }

            /**
             * Show a page with fields for creating a new article
             */
            get("new") {
                controller.displayNewArticlePage(this.call)
            }

            /**
             * Save an article
             */
            post {
                controller.saveNewArticle(this.call)
            }

            /**
             * Show an article with a specific id
             */
            get("{id}") {
                controller.displaySingleArticle(this.call)
            }

            /**
             * Show a page with fields for editing an article
             */
            get("{id}/edit") {
                controller.displayEditArticle(this.call)
            }

            /**
             * Update an article
             */
            post("{id}") {
                controller.updateArticleById(this.call)
            }

            /**
             * Delete an article
             */

            delete("{id}") {
                controller.deleteArticleById(this.call)
            }
        }
    }
}
