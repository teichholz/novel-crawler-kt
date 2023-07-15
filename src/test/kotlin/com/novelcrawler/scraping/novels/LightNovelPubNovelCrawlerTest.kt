package com.novelcrawler.scraping.novels

import com.novelcrawler.plugins.Modules
import com.novelcrawler.scraping.model.LightNovelPub
import io.kotest.core.spec.style.FunSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.sequences.shouldNotBeEmpty
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.inject

class LightNovelPubNovelCrawlerTest : FunSpec(), KoinTest {
    override fun extensions() = listOf(KoinExtension(Modules.crawler))

    val crawler : NovelCrawler by inject(named<LightNovelPub>())

    init {
        test("getNovels") {
            crawler.getNovels().shouldNotBeEmpty()
        }
    }
}