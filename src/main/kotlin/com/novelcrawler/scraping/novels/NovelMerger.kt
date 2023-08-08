package com.novelcrawler.scraping.novels

import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.repository.NovelRepository
import com.novelcrawler.scraping.selenium.SeleniumDriver

class NovelMerger {
    private val log by LoggerDelegate()

    private val processed = mutableMapOf<NovelCrawler, Int>()


    context(SeleniumDriver)
    suspend fun merge(crawler: NovelCrawler) {
        var alreadyProcessed = processed.get(crawler) ?: 0
        val repository = NovelRepository()

        try {
            log.info("Start merge process from ${crawler.javaClass.name}. Already processed ${alreadyProcessed} chapters")
            crawler.getNovels(alreadyProcessed).forEach {
                repository.mergeOrSave(it)
                log.info("Merged novel ${it.name}")
                ++alreadyProcessed
            }
        } catch (e: Exception) {
            processed[crawler] = alreadyProcessed
            log.info("Error while merging novels. Already processed ${alreadyProcessed} chapters from ${crawler.javaClass.name}")
            throw e
        }
    }

    fun reset(crawler: NovelCrawler) = processed.remove(crawler)
}