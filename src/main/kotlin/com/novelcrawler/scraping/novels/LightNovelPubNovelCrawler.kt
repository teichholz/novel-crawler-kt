package com.novelcrawler.scraping.novels

import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.scraping.model.HasSite
import com.novelcrawler.scraping.selenium.SeleniumDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

context(HasSite, SeleniumDriver)
class LightNovelPubNovelCrawler : NovelCrawler {
    val logger by LoggerDelegate()

    override fun getNovels(state: Int): Sequence<NovelDto> {
        logger.info("Getting novels from ${site.baseUrl}")
        driver.get(site.baseUrl)
        logger.info("Clicked on Ranking")
        driver.findElement(By.linkText("Ranking")).click()
        return driver.findElements(By.className("novel-item")).asSequence()
                .map {
                    val name = it.findElement(By.cssSelector("div.item-body h2.title a")).text
                    val img = it.findElement(By.cssSelector(".cover img")).getAttribute("src")
                    val detailsHref = it.findElement(By.cssSelector("div.item-body h2.title a")).getAttribute("href")
                    NovelDto(name = name, img = img, detailsHref = detailsHref, site = site).also { logger.debug("{}", it) }
                }
    }

    fun chapters(detailsHref: String): Sequence<ChapterDto> {
        driver.get(detailsHref)

        return generateSequence {
            val chapters = driver.findElements(By.ByCssSelector(".chapter-row")).map {
                val contentUrl = it.findElement(By.ByCssSelector("td a")).getAttribute("href")
                val name = it.findElement(By.ByCssSelector("td a")).text
                val releaseDate = it.findElement(By.ByCssSelector("td.text-right a time")).getAttribute("datetime")
                    .let { LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME) }
                ChapterDto(name, contentUrl, releaseDate)
            }.toList()

            nextPageLinkElement().onSuccess { it.click() }.map { chapters }.getOrNull()
        }.flatMap { it.asSequence() }
    }

    fun nextPageLinkElement(): Result<WebElement> {
        val page = "ul.pagination-small li.page-active"
        return kotlin.runCatching {
            driver.findElement(By.ByCssSelector("$page + li"))
        }
    }

    private fun setPage(detailsHref: String, page: Int) {
        driver.get("$detailsHref/chapters?page=$page")
    }

    private fun nextPageElement(): Boolean {
        return kotlin.runCatching {
            driver.findElement(By.cssSelector("ul.pagination-small li.page-active + li"))
        }.isSuccess
    }
}