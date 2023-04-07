val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

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
    implementation("io.ktor:ktor-server-core-jvm:_")
    implementation("io.ktor:ktor-server-auth-jvm:_")
    implementation("io.ktor:ktor-server-netty-jvm:_")
    implementation("ch.qos.logback:logback-classic:_")
    implementation("io.ktor:ktor-server-freemarker:$ktor_version")
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