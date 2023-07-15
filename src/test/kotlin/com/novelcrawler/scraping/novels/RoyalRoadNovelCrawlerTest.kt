package com.novelcrawler.scraping.novels

import com.novelcrawler.plugins.Modules
import com.novelcrawler.scraping.model.RoyalRoad
import io.kotest.assertions.print.print
import io.kotest.core.spec.style.FunSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.sequences.shouldNotBeEmpty
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject

class RoyalRoadNovelCrawlerTest : FunSpec(), KoinTest {
    override fun extensions() = listOf(KoinExtension(Modules.crawler))

    val crawler : NovelCrawler by inject(named<RoyalRoad>())


    init {
        test("getNovels") {
            crawler.getNovels().take(1)
        }
    }
}