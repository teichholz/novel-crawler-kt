package com.novelcrawler.repository

import com.novelcrawler.model.ChapterDto
import com.novelcrawler.model.NovelDto
import com.novelcrawler.plugins.Modules
import com.novelcrawler.repository.entity.Chapters
import com.novelcrawler.repository.entity.Novel
import com.novelcrawler.repository.entity.Novels
import com.novelcrawler.repository.entity.fullyRealizeToDto
import com.novelcrawler.repository.entity.toDto
import io.kotest.core.spec.style.FunSpec
import io.kotest.koin.KoinExtension
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.test.KoinTest
import org.koin.test.inject
import java.time.LocalDateTime

class NovelRepositoryTest : FunSpec(), KoinTest {

    override fun extensions() = listOf(KoinExtension(Modules.repository))

    val repository : NovelRepository by inject()

    init {
        beforeEach {
            transaction {
                Chapters.deleteAll()
                Novels.deleteAll()
            }
        }

        xtest("save novel without chapters") {
            val chapter = ChapterDto(name = "Chapter", contentHref = "href", releaseDate = LocalDateTime.now())
            val novel = NovelDto(chapters = listOf(chapter), name = "Novel", img = "img", detailsHref = "href")

            val saved = repository.save(novel)

            transaction {
                saved.toDto().shouldBe(novel)
            }
        }

        xtest("Merge works for novels") {
            val novelDto = NovelDto(name = "Novel", img = "img", detailsHref = "href")
            val changedNovelDto = NovelDto(name = "Novel", img = "new img", detailsHref = "new href")

            val saved = repository.mergeOrSave(novelDto)
            val changedSaved = repository.mergeOrSave(changedNovelDto)

            val actual = transaction { Novel.findById(saved.id)!! }

            saved.id shouldBe changedSaved.id
            actual.fullyRealizeToDto() shouldBe changedNovelDto
        }

        xtest("Merge works for novels with chapters") {
            val chapterDto = ChapterDto(name = "Chapter 1", contentHref = "href", releaseDate = LocalDateTime.now())
            val novelDto = NovelDto(chapters = listOf(chapterDto), name = "Novel", img = "img", detailsHref = "href")

            val changedChapterDto = ChapterDto(name = "Changed Chapter 1", contentHref = "changed href", releaseDate = LocalDateTime.now())
            val newChapterDto = ChapterDto(name = "Chapter 2", contentHref = "href", releaseDate = LocalDateTime.now())
            val changedNovelDto = NovelDto(chapters = listOf(changedChapterDto, newChapterDto), name = "Novel", img = "img", detailsHref = "href")

            val saved = repository.mergeOrSave(novelDto)
            val changedSaved = repository.mergeOrSave(changedNovelDto)

            saved.id shouldBe changedSaved.id
            saved.fullyRealizeToDto() shouldBe changedNovelDto
        }
    }
}
