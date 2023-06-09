package com.gamenews.plugins

import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.freemarker.FreeMarker

/**
 * Template settings to tell the app that FreeMarker templates are located in templates dir
 *
 * Output settings convert control chars provided by user to their corresponding HTML entities,
 * which ensure strings are printed correctly with escaping and prevents XSS attacks
 */
fun Application.configureTemplating() {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(
            this::class.java.classLoader,
            "templates"
        )
        outputFormat = HTMLOutputFormat.INSTANCE
    }
}
