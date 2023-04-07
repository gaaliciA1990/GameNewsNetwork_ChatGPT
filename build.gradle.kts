plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
    jacoco
}

group = "com.gamenews"
version = "0.0.1"

application {
    mainClass.set("com.gamenews.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(Ktor.server.core)
    implementation(Ktor.server.auth)
    implementation(Ktor.server.netty)
    implementation(Ktor.server.freeMarker)
    implementation("ch.qos.logback:logback-classic:_")

    testImplementation("io.ktor:ktor-server-tests-jvm:_")
    testImplementation(Kotlin.test.junit)

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}

detekt {
    autoCorrect = true
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}