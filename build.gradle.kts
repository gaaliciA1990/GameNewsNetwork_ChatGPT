plugins {
    kotlin("jvm")
    id("io.ktor.plugin")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.ben-manes.versions")
    jacoco
}

group = "com.gamenews"
version = "0.0.1"

kotlin {
    jvmToolchain(17)
}

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
    implementation(KotlinX.coroutines.core)
    implementation("ch.qos.logback:logback-classic:_")
    implementation("org.litote.kmongo:kmongo:_")
    implementation("org.litote.kmongo:kmongo-coroutine:_")

    testImplementation(Kotlin.test.junit)
    testImplementation("io.ktor:ktor-server-tests-jvm:_")
    testImplementation(KotlinX.coroutines.test)
    testImplementation(Testing.mockK)
    testImplementation("org.testng:testng:_")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:_")
}

detekt {
    autoCorrect = true
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}