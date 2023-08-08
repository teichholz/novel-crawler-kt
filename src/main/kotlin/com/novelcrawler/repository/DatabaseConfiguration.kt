package com.novelcrawler.repository

import arrow.core.raise.Raise
import arrow.core.raise.ensure
import com.novelcrawler.config.Config
import com.novelcrawler.config.DatabaseConnectionInformation
import com.novelcrawler.config.MissingDatabaseConfiguration
import com.novelcrawler.config.connect
import com.novelcrawler.repository.entity.Chapters
import com.novelcrawler.repository.entity.Novels
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Schema
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction


val schemaName = "crawler"

context(Raise<MissingDatabaseConfiguration>, Raise<String>)
fun setupDataBase(config: Config) {
    val databaseConnectionInformation = connect(config.database)

    with(databaseConnectionInformation) {
        maybeRunMigrations()
        setUpOrm()
    }
}

context(DatabaseConnectionInformation)
private fun maybeRunMigrations() {
    if (isNotEmbedded) {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .defaultSchema(schemaName)
            .load()

        flyway.migrate()
    }
}

context(DatabaseConnectionInformation, Raise<String>)
private fun setUpOrm() {
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
            ensure(stmts.isEmpty()) {
                "Database schema is not up to date with entities. The following statements will need to be executed\n: ${stmts.joinToString("\n")}"
            }
        }
    }
}