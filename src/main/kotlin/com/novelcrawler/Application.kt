package com.novelcrawler

import com.novelcrawler.plugins.configureHTTP
import com.novelcrawler.plugins.configureKoin
import com.novelcrawler.plugins.configureMonitoring
import com.novelcrawler.plugins.configureSerialization
import com.novelcrawler.scraping.sync.NovelSyncJob
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch


fun main() {
    embeddedServer(Netty, port = 8080, host = "127.0.0.1", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureMonitoring()
    configureKoin()
    launch { NovelSyncJob.start() }
    //configureDatabases()
    //configureSecurity()
}
