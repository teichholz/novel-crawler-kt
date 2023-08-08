package com.novelcrawler.repository

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.repository.entity.Chapters
import com.novelcrawler.repository.entity.Novel
import com.novelcrawler.repository.entity.Novels
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.component.KoinComponent


class NovelRepository {

    suspend fun save(novelDto: NovelDto): Novel {
        return transaction {
            val savedNovel = novelDto.save()

            novelDto.chapters.map {
                it.save(savedNovel)
            }

            savedNovel
        }
    }

    suspend fun mergeOrSave(novelDto: NovelDto): Novel {
        return transaction {
            SchemaUtils.create(Novels, Chapters)

            // TODO naive
            val novel = Novel.find { (Novels.name eq novelDto.name) and (Novels.siteId eq novelDto.site.id) }.firstOrNull() ?:
                return@transaction save(novelDto)

            novel.img = novelDto.img
            novel.detailsHref = novelDto.detailsHref
            val chaptersToProcess = HashSet<ChapterDto>(novelDto.chapters)
            (novel.chapters zip novelDto.chapters).forEach { (chapter, chapterDto) ->
                chapter.name = chapterDto.name
                chapter.contentHref = chapterDto.contentHref
                chapter.releaseDate = chapterDto.releaseDate
                chaptersToProcess.remove(chapterDto)
            }
            chaptersToProcess.forEach {
                it.save(novel)
            }

            novel
        }
    }


    suspend fun <T> transaction(block: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}