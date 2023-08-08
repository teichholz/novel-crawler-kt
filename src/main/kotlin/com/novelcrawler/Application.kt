package com.novelcrawler

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.ResourceScope
import arrow.fx.coroutines.resourceScope
import com.novelcrawler.config.Config
import com.novelcrawler.config.MissingDatabaseConfiguration
import com.novelcrawler.plugins.configureHTTP
import com.novelcrawler.plugins.configureMonitoring
import com.novelcrawler.plugins.configureSerialization
import com.novelcrawler.repository.setupDataBase
import com.novelcrawler.scraping.selenium.SeleniumDriver
import com.novelcrawler.scraping.selenium.SeleniumDriverImpl
import com.novelcrawler.scraping.selenium.toSeleniumDriver
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URL

fun main() = SuspendApp {
    either {
        resourceScope {
            val env = System.getenv("NOVEL_CRAWLER_ENV") ?: "dev"
            val config = loadConfig(env).bind()
            either { setupDataBase(config) }.bind()
            val driver = driver(config)

            server(Netty, port = 8080, module = Application::module)

            println("connect")
            val sd = RemoteWebDriver(
                URL("http://teichserver:31669"),
                FirefoxOptions(),
            )
            sd.get("http://www.google.com")
            println("connected")

            sd.quit()
            println("closed")

//            with(driver) {
//                with(Site.ROYAL_ROAD) {
//                    val royalRoadNovelCrawler = RoyalRoadNovelCrawler()
//                    with(royalRoadNovelCrawler.lift()) {
//                        print("Starting to scrape novels from $name")
//                        NovelSyncJob.start()
//                    }
//                }
//            }


            awaitCancellation()
        }
    }.onLeft {
        when (it) {
            is Throwable -> it.printStackTrace()
            is MissingDatabaseConfiguration -> println(it)
            is String -> println(it)
        }
    }
}

fun Application.module() {
    configureHTTP()
    configureSerialization()
    configureMonitoring()
}

fun loadConfig(env: String): Either<Throwable, Config> =
    Either.catch {
        ConfigLoaderBuilder.default()
            .addResourceSource("/config/$env.yml")
            .build()
            .loadConfigOrThrow<Config>()
    }

suspend fun ResourceScope.driver(config: Config): SeleniumDriver =
    install({ aquire(config) }) { driver, _ -> driver.close() }

private fun aquire(config: Config): SeleniumDriver {
    return when (config.env) {
        "dev" -> {
            SeleniumDriverImpl()
        }

        "k8s" -> {
            //SeleniumDriverK8s()
            RemoteWebDriver(
                URL("http://teichserver:31669"),
                FirefoxOptions(), true
            ).toSeleniumDriver()
        }

        else -> {
            throw Exception("Unknown environment ${config.env}")
        }
    }
}
