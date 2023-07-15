package com.novelcrawler.scraping.novels

import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.repository.NovelRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NovelMerger : KoinComponent {
    private val log by LoggerDelegate()

    private val repository: NovelRepository by inject()
    private val processed = mutableMapOf<NovelCrawler, Int>()

    suspend fun merge(crawler: NovelCrawler) {
        var alreadyProcessed = processed.get(crawler) ?: 0
        try {
            crawler.getNovels(alreadyProcessed).forEach {
                repository.mergeOrSave(it)
                log.info("Merged novel ${it.name}")
                ++alreadyProcessed
            }
        } catch (e: Exception) {
            processed[crawler] = alreadyProcessed
            throw e
        }
    }

    fun reset(crawler: NovelCrawler) = processed.remove(crawler)
}