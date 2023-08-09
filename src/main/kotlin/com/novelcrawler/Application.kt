package com.novelcrawler

import arrow.continuations.SuspendApp
import arrow.continuations.ktor.server
import arrow.core.Either
import arrow.core.raise.either
import arrow.fx.coroutines.Resource
import arrow.fx.coroutines.resource
import arrow.fx.coroutines.resourceScope
import arrow.resilience.Schedule
import com.novelcrawler.config.Config
import com.novelcrawler.config.MissingDatabaseConfiguration
import com.novelcrawler.logger.getLogger
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
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URL

val log = getLogger("Application")

fun main() = SuspendApp {
    either {
        resourceScope {
            val env = System.getenv("NOVEL_CRAWLER_ENV") ?: "dev"
            val config = loadConfig(env).bind()
            either { setupDataBase(config) }.bind()

            server(Netty, port = 8080, module = Application::module)

            //val test = install({ log.info("Aquiring something") }) {_, _ -> delay(10000); log.info("Released something") }

           val driver = install({ aquire(config).also { log.info("Aquired selenium driver") } }) { driver, exitCase ->
                log.info("Before releasing. Exit case: $exitCase")
                Either.catch {
                    driver.driver.get("http://www.google.com")
                }.also { log.info("$it") }

                Schedule.recurs<Unit>(10).repeat {
                    Either.catch {
                        driver.driver.quit()
                    }.onLeft { log.error("Failed to quit driver", it) }
                        .onRight { log.info("Successfully quit driver") }
                }
            }

            //driver(config).use { awaitCancellation() }



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

fun driver(config: Config): Resource<SeleniumDriver> =
    resource {
        install({ aquire(config).also { log.info("Aquired selenium driver") } }) { driver, _ ->
            driver.close(); log.info(
            "Released selenium driver"
        )
        }
    }

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
