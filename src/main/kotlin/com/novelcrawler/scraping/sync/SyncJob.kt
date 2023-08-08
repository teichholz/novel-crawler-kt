package com.novelcrawler.scraping.sync

import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.scraping.novels.HasNovelCrawler
import com.novelcrawler.scraping.novels.NovelMerger
import com.novelcrawler.scraping.selenium.SeleniumDriver


object NovelSyncJob {
    private val log by LoggerDelegate()

    context(SeleniumDriver, HasNovelCrawler)
    suspend fun start() {
        val merger = NovelMerger()

        log.info("Start sync process")
        merger.merge(crawler)

        merger.reset(crawler)
    }

}
