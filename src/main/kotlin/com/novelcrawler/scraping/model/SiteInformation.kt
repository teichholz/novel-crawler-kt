package com.novelcrawler.scraping.model

interface SiteInformation {
    val baseUrl: String
}

interface HasId {
    val id: Int
}

interface FromId<T> {
    fun fromId(index: Int): T
}

interface HasSite {
    val site: Site
}

enum class Site(override val id: Int, val delegate : SiteInformation) :
    SiteInformation by delegate, HasSite, HasId {

    ROYAL_ROAD(0, RoyalRoad()), LIGHT_NOVEL_PUB(1, LightNovelPub());

    override val site: Site
        get() = this

    companion object : FromId<Site> {
        private val map : MutableMap<Int, Site> = mutableMapOf()

        init {
            values().forEach { map[it.id] = it }
        }

        override fun fromId(index: Int): Site {
            return map[index] ?: throw IllegalArgumentException("No site with id $index")
        }
    }
}

class LightNovelPub(override val baseUrl: String = "https://lightnovelpub.com") : SiteInformation


class RoyalRoad(override val baseUrl: String = "https://www.royalroad.com") : SiteInformation