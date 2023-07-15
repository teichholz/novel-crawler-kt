package com.novelcrawler.scraping.chapters

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.scraping.selenium.SeleniumDriver
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


context(SeleniumDriver)
class DetailsRoyalRoad : ChapterCrawler {
    override fun chapters(novel: NovelDto): Sequence<ChapterDto> {
        driver.get(novel.detailsHref)

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
}