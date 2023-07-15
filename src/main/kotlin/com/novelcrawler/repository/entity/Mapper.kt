package com.novelcrawler.repository.entity

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.scraping.model.Site
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction


fun Novel.fullyRealizeToDto() = transaction { toDto() }

context(Transaction)
fun Novel.toDto() = NovelDto(
    chapters = chapters.map { it.toDto() },
    name = name,
    img = img,
    detailsHref = detailsHref,
    site = Site.fromId(siteId)
)

fun Chapter.fullyRealizeToDto() = transaction { toDto() }

fun Chapter.toDto() = ChapterDto(
    name = name,
    contentHref = contentHref,
    releaseDate = releaseDate
)