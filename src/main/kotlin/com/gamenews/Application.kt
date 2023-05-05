package com.gamenews

import com.gamenews.data.AdminRepository
import com.gamenews.data.ArticlesRepository
import com.gamenews.models.Admin
import com.gamenews.models.Article
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.server.application.Application
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase("GNNDatabase")
    val articles = database.getCollection<Article>()
    val admins = database.getCollection<Admin>()

    val articlesRepo = ArticlesRepository(articles)
    val adminRepo = AdminRepository(admins)
    val controller = Controller(articlesRepo, adminRepo)

    configureRouting(controller)
    configureTemplating()
}
