package com.gamenews

import com.gamenews.data.ArticlesDatabase
import com.gamenews.plugins.Controller
import com.gamenews.plugins.configureRouting
import com.gamenews.plugins.configureTemplating
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val controller: Controller = Controller(ArticlesDatabase())

    configureRouting(controller)
    configureTemplating()
}
