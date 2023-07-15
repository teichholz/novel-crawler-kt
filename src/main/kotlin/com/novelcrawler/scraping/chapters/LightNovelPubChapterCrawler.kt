package com.novelcrawler.scraping.chapters

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.scraping.selenium.SeleniumDriver
import org.openqa.selenium.By
import java.time.LocalDateTime

context(SeleniumDriver)
class LightNovelPubChapterCrawler : ChapterCrawler {
    override fun chapters(novel: NovelDto): Sequence<ChapterDto> {
        val allPages = allPages(novel.detailsHref)
        return allPages.flatMap {
            driver.get(it);
            driver.findElements(By.cssSelector("ul.chapter-list > li")).map {
                val contentUrl = it.findElement(By.cssSelector("a")).getAttribute("href")
                val name = it.findElement(By.cssSelector("a")).text
                val releaseDate = it.findElement(By.cssSelector("a time")).getAttribute("datetime")
                ChapterDto(name, contentUrl, LocalDateTime.parse(releaseDate))
            }
        }.asSequence()
    }

    fun allPages(detailsHref: String): List<String> {
        val pages = mutableListOf<String>()
        var currentPage = 1

        do {
            setPage(detailsHref, currentPage++)
            pages.add(driver.currentUrl)
        } while (currentPageHasNextPage())

        return pages
    }

    private fun setPage(detailsHref: String, page: Int) {
        driver.get("$detailsHref/chapters?page=$page")
    }

    private fun currentPageHasNextPage(): Boolean {
        return kotlin.runCatching {
            driver.findElement(By.cssSelector("ul.pagination li.active + li"))
        }.isSuccess
    }
}