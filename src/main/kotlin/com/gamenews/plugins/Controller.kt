package com.gamenews.plugins

import com.gamenews.data.AdminRepository
import com.gamenews.data.ArticlesRepository
import com.gamenews.models.Article
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.freemarker.FreeMarkerContent
import io.ktor.server.plugins.origin
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.util.getOrFail
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor

/**
 * This class handles the calls for all routes
 */
class Controller(
    private val articleRepo: ArticlesRepository,
    private val adminRepo: AdminRepository
) {
    companion object {
        const val PAGESIZE = 3
    }

    /**
     * Handles calls to show a page of articles on the home screen based
     * on page size (number of articles) and page number
     */
    suspend fun displayArticlePages(call: ApplicationCall) {
        // Get the page number from the query param or default to 1
        val pageNumber = call.parameters["page"]?.toIntOrNull() ?: 1
        // get all the articles we have
        val allArticles = articleRepo.getArticlesCount()
        // get the set of articles we want per page
        val articlesPerPage = articleRepo.getSetOfArticles(pageNumber, PAGESIZE)
        // Set our page count based on total articles we have
        val pageCount = floor((allArticles + PAGESIZE - 1) / PAGESIZE.toDouble()).toInt()
        // verify the IP address, if admin = true, else false
        val isAdmin = verifyAdmin(call)

        call.respond(
            HttpStatusCode.OK,
            FreeMarkerContent(
                "index.ftl",
                mapOf(
                    "articles" to articlesPerPage,
                    "pageSize" to PAGESIZE,
                    "pageNumber" to pageNumber,
                    "articleCount" to allArticles,
                    "pageCount" to pageCount,
                    "admin" to isAdmin,
                )
            )
        )
    }

    /**
     * Handles calls for creating new article and redirects to the page to
     * being the new article creation.
     */
    suspend fun displayNewArticlePage(call: ApplicationCall) {
        // verify the IP address
        requireAdminOrFail(call)

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
        // verify the IP address
        requireAdminOrFail(call)

        val formParams = call.receiveParameters()
        val title = formParams.getOrFail("title").trim()
        val body = formParams.getOrFail("body").trim()
        val date = formParams.getOrFail("publish_date").trim()

        var datePattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        var publishDate = LocalDateTime.parse(date, datePattern)

        // Create the article model with the form params, ID is autocreated
        val newArticle = Article.newEntry(title, body, publishDate)

        // If the article is created successfully, redirect to the article
        if (articleRepo.createArticle(newArticle)) {
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
        val article = articleRepo.getArticleById(id)
        // verify the IP address, if admin = true, else false
        val isAdmin = verifyAdmin(call)

        article?.let {
            call.respond(
                HttpStatusCode.OK,
                FreeMarkerContent(
                    "show.ftl",
                    mapOf(
                        "article" to article,
                        "admin" to isAdmin,
                    )
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
        val article = articleRepo.getArticleById(id)

        // verify the IP address
        requireAdminOrFail(call)

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
                mapOf(
                    "article" to article,
                )
            )
        )
    }

    /**
     * Handles calls to update an article
     */
    suspend fun updateArticleById(call: ApplicationCall) {
        // verify the IP address
        requireAdminOrFail(call)

        val id = call.parameters.getOrFail<String>("id")
        val formParams = call.receiveParameters()

        val article = articleRepo.getArticleById(id)

        // Make sure the article still exists in case it's deleted before edit clicked
        if (article == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                "Sorry, this article no longer exists"
            )
            return
        }

        // update the title and body of the article, if not null
        article.title = formParams.getOrFail("title").trim()
        article.body = formParams.getOrFail("body").trim()

        if (articleRepo.updateArticle(article)) {
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
        // verify the IP address
        requireAdminOrFail(call)

        val id = call.parameters.getOrFail<String>("id")
        val article = articleRepo.getArticleById(id)

        // Make sure the article still exists in case it's deleted before delete clicked
        if (article == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                "Sorry, someone beat you to the punch. We couldn't find that article"
            )
            return
        }

        // If the articles was successfully deleted, redirect back to the articles page
        if (articleRepo.deleteArticle(article.id)) {
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

    /**
     * Helper method for verify the IP belongs to an admin
     */
    private suspend fun verifyAdmin(call: ApplicationCall): Boolean {
        val userIP = call.request.origin.remoteHost

        // Check if the IP address matches our admin IPs
        if (adminRepo.getAdminByIp(userIP)) {
            return true
        }
        // if IP not in our table, return false
        return false
    }

    /**
     * Helper function for checking if user is an Admin to access the feature/page
     */
    private suspend fun requireAdminOrFail(call: ApplicationCall) {
        val isAdmin = verifyAdmin(call)

        // If the IP isn't an admin, they don't get to see the page
        if (!isAdmin) {
            call.respond(
                HttpStatusCode.NotFound,
                "Hmmm, that page doesn't appear to exist."
            )
            throw RuntimeException("Unauthorized Access")
        }
    }
}
