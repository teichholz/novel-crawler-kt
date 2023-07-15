package com.novelcrawler.repository

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.repository.entity.Chapter
import com.novelcrawler.repository.entity.Novel

fun NovelDto.save(): Novel {
    val dto = this
    return Novel.new {
        name = dto.name
        img = dto.img
        detailsHref = dto.detailsHref
        siteId = dto.site.id
    }
}

fun ChapterDto.save(savedNovel: Novel): Chapter {
    val dto = this
    return Chapter.new {
        name = dto.name
        contentHref = dto.contentHref
        releaseDate = dto.releaseDate
        novel = savedNovel
    }
}
