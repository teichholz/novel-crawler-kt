package com.novelcrawler.repository.entity

import org.jetbrains.exposed.dao.id.IntIdTable

object Novels : IntIdTable() {
    // val chapters
    val name = varchar("name", 255)
    val img = varchar("img", 255)
    val detailsHref = varchar("details_href", 255)
    val siteId = integer("site_id")
}