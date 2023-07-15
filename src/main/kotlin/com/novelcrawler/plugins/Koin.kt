package com.novelcrawler.plugins

import com.novelcrawler.config.Config
import com.novelcrawler.repository.DatabaseConfiguration
import com.novelcrawler.repository.NovelRepository
import com.novelcrawler.scraping.model.LightNovelPub
import com.novelcrawler.scraping.model.RoyalRoad
import com.novelcrawler.scraping.model.Site
import com.novelcrawler.scraping.novels.LightNovelPubNovelCrawler
import com.novelcrawler.scraping.novels.NovelCrawler
import com.novelcrawler.scraping.novels.RoyalRoadNovelCrawler
import com.novelcrawler.scraping.selenium.SeleniumDriverImpl
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import io.ktor.server.application.*
import org.koin.core.module.dsl.createdAtStart
import org.koin.core.module.dsl.withOptions
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(Modules.db, Modules.repository, Modules.crawler)
    }
}

class Modules {
    companion object {
        val config = module {
            single {
                ConfigLoaderBuilder.default()
                    .addResourceSource("/config/dev.yml")
                    .build()
                    .loadConfigOrThrow<Config>()
            } withOptions { createdAtStart() }
        }

        val db = module {
            includes(config)
            single {
                DatabaseConfiguration()
            } withOptions {
                createdAtStart()
            }
        }

        val repository by lazy {
            module {
                single {
                    NovelRepository()
                }
            }
        }

        val crawler = module {
                val crawlers = mutableListOf<NovelCrawler>()

                with(SeleniumDriverImpl()) {
                    single<NovelCrawler>(named<RoyalRoad>()) {
                        with (Site.ROYAL_ROAD) {
                            RoyalRoadNovelCrawler().also { crawlers.add(it) }
                        }
                    }
                    single<NovelCrawler>(named<LightNovelPub>()) {
                        with (Site.LIGHT_NOVEL_PUB) {
                            LightNovelPubNovelCrawler().also { crawlers.add(it) }
                        }
                    }
                }
            }

    }
}