package com.novelcrawler.scraping.chapters

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto


interface ChapterCrawler {
    fun chapters(novel: NovelDto): Sequence<ChapterDto>
}