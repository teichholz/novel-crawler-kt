package com.novelcrawler.model

import java.time.LocalDateTime

data class ChapterDto(val name: String, val contentHref: String, val releaseDate: LocalDateTime)
