package com.novelcrawler.scraping.novels

import com.novelcrawler.model.NovelDto


interface NovelCrawler {
    fun getNovels(): Sequence<NovelDto> = getNovels(0)

    fun getNovels(alreadyProcessed: Int): Sequence<NovelDto>
 }