package com.novelcrawler.scraping.novels

import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.scraping.model.HasSite
import com.novelcrawler.scraping.selenium.SeleniumDriver
import org.openqa.selenium.By
import org.openqa.selenium.By.ByCssSelector
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


context(HasSite, SeleniumDriver)
class RoyalRoadNovelCrawler : NovelCrawler {
    val logger by LoggerDelegate()

    val NOVELS_PER_PAGE = 20
    val FIRST_PAGE_INDEX = 1

    fun page(page: Int) = "${site.baseUrl}/fictions/active-popular?page=$page"

    override fun getNovels(alreadyProcessed: Int): Sequence<NovelDto> {
        val startPage = alreadyProcessed / NOVELS_PER_PAGE + FIRST_PAGE_INDEX

        return (startPage..2).asSequence()
            .flatMap { getNovels(page(it)) }
            .map {  novel ->
                novel.copy(chapters = chapters(novel)).also {
                    logger.info("Parsed ${it.chapters.size} chapters for ${it.name}")
                }
            }
    }

    fun getNovels(pageUrl: String): List<NovelDto> {
        logger.info("Navigating to page $pageUrl")
        driver.get(pageUrl)

        return driver.findElements(ByCssSelector(".fiction-list-item")).stream()
                .map {
                    val name = it.findElement(ByCssSelector(".fiction-title a")).text
                    val img = it.findElement(ByCssSelector("a img")).getAttribute("src")
                    val detailsHref = it.findElement(ByCssSelector(".fiction-title a")).getAttribute("href")
                    NovelDto(name = name, img = img, detailsHref = detailsHref, site = site).also { logger.info("Parsed novel {}", it) }
                }.toList()
    }


    fun chapters(novel: NovelDto): List<ChapterDto> {
        driver.get(novel.detailsHref)
        val chapters = mutableListOf<ChapterDto>()

        do {
            val elements = driver.findElements(ByCssSelector(".chapter-row"))
            elements.map {
                    val contentUrl = it.findElement(ByCssSelector("td a")).getAttribute("href")
                    val name = it.findElement(ByCssSelector("td a")).text
                    val releaseDate = it.findElement(ByCssSelector("td.text-right a time")).getAttribute("datetime")
                        .let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
                    ChapterDto(name, contentUrl, releaseDate)
                }.toList().let { chapters.addAll(it) }
        } while (tryClickNextPage().isSuccess)

        return chapters
    }

    private fun tryClickNextPage(): Result<Unit> {
        return kotlin.runCatching {
            driver.findElement(By.cssSelector("ul.pagination-small li.page-active + li")).click()
        }
    }
}