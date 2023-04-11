package com.gamenews

import com.gamenews.data.ArticlesDatabase
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val client = KMongo.createClient().coroutine
    val database = client.getDatabase("ArticlesDatabase")

    val articlesDB = ArticlesDatabase(database)
    val controller: Controller = Controller(articlesDB)

    configureRouting(controller)
    configureTemplating()
}
