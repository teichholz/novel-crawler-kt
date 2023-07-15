package com.novelcrawler.repository.entity

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Chapter(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Chapter>(Chapters)

    var novel by Novel referencedOn Chapters.novel
    var name by Chapters.name
    var contentHref by Chapters.contentHref
    var releaseDate by Chapters.releaseDate
}