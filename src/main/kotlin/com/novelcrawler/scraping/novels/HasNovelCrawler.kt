package com.novelcrawler.scraping.novels

interface HasNovelCrawler {
    val crawler: NovelCrawler
}

fun NovelCrawler.lift(): HasNovelCrawler = object : HasNovelCrawler {
    override val crawler: NovelCrawler = this@lift
}