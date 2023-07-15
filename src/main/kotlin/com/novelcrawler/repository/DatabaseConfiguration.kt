package com.novelcrawler.repository

import arrow.core.raise.recover
import com.novelcrawler.config.Config
import com.novelcrawler.config.DatabaseConnectionInformation
import com.novelcrawler.config.connect
import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.repository.entity.Chapters
import com.novelcrawler.repository.entity.Novels
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DatabaseConfiguration : KoinComponent {
    val logger by LoggerDelegate()
    val config: Config by inject()

    val schemaName = "crawler"

    init {
        val databaseConnectionInformation = recover({ connect(config.database) }, { throw it })

        with(databaseConnectionInformation) {
            maybeRunMigrations()
            setUpOrm()
        }
    }

    context(DatabaseConnectionInformation)
    fun maybeRunMigrations() {
        if (isNotEmbedded) {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .defaultSchema(schemaName)
                .load()

            flyway.migrate()
        }
    }

    context(DatabaseConnectionInformation)
    fun setUpOrm() {
        org.jetbrains.exposed.sql.Database.connect(datasource = dataSource)
        val schema = Schema(schemaName)

        if (isEmbedded) {
            transaction {
                SchemaUtils.createSchema(schema)
                SchemaUtils.setSchema(schema)
                SchemaUtils.create(Novels, Chapters)
            }
        } else {
            transaction {
                SchemaUtils.setSchema(schema)
                val stmts = SchemaUtils.statementsRequiredToActualizeScheme(Novels, Chapters)
                if (stmts.isNotEmpty()) {
                    logger.info("Database schema is not up to date with entities. The following statements will need to be executed\n: ${stmts.joinToString("\n")}")
                }
            }
        }
    }
}