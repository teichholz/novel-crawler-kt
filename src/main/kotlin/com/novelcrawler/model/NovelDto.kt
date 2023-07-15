package com.novelcrawler.model

import com.novelcrawler.scraping.model.Site

data class NovelDto(var chapters: List<ChapterDto> = mutableListOf(), val name: String, val img: String, val detailsHref: String, val site: Site = Site.ROYAL_ROAD)
