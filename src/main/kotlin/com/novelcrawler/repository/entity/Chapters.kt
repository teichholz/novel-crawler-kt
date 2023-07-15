package com.novelcrawler.repository.entity

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object Chapters : IntIdTable() {
    val name = varchar("name", 255)
    val contentHref = varchar("content_href", 255)
    val releaseDate = datetime("release_date")
    val novel = reference("novel", Novels)
}