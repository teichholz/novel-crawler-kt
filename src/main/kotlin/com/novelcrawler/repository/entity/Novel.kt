package com.novelcrawler.repository.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Novel(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Novel>(Novels)

    var name by Novels.name
    var img by Novels.img
    var detailsHref by Novels.detailsHref
    var siteId by Novels.siteId
    val chapters by Chapter referrersOn Chapters.novel

}