package com.gamenews.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val controller: Controller = Controller()

    routing {
        get("/") {
            call.respondRedirect("articles")
        }

        route("articles") {
            /**
             * Show a list of articles.
             */
            get {
                controller.showAllArticles(this)
            }

            /**
             * Show a page with fields for creating a new article
             */
            get("new") {
                controller.newArticle(this)
            }

            /**
             * Save an article
             */
            post {
                controller.saveArticle(this)
            }

            /**
             * Show an article with a specific id
             */
            get("{id}") {
                controller.showArticle(this)
            }

            /**
             * Show a page with fields for editing an article
             */
            get("{id}/edit") {
                controller.editArticle(this)
            }

            /**
             * Update or delete an article
             */
            post("{id}") {
                controller.postArticleById(this)
            }
        }
    }
}
