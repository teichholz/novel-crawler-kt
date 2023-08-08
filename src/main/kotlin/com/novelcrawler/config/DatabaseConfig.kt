package com.novelcrawler.config

import arrow.core.raise.Raise
import arrow.core.raise.ensureNotNull
import arrow.core.raise.zipOrAccumulate
import org.h2.jdbcx.JdbcDataSource
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

data class DatabaseConfig(val host: String?, val port: Int?, val user: String?, val password: String?, val database: String?)

data class MissingDatabaseConfiguration(var msg: String) {
    fun createMsg(): String = "Missing database configuration for the following fields: ${msg}"

    companion object {
        fun combine(that: MissingDatabaseConfiguration, other: MissingDatabaseConfiguration): MissingDatabaseConfiguration {
            return MissingDatabaseConfiguration("${that.msg}, ${other.msg}")
        }
    }
}

data class DatabaseConnectionInformation(val dataSource: DataSource, val isEmbedded: Boolean) {
    val isNotEmbedded
        get() = !isEmbedded
}

context(Raise<MissingDatabaseConfiguration>)
fun connect(cfg: DatabaseConfig?): DatabaseConnectionInformation {
    val required = setOf(cfg?.host, cfg?.port, cfg?.user, cfg?.password)
    return if (cfg == null || required.all { it == null }) {
        val source = JdbcDataSource()
        source.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
        source.user = "root"
        source.password = ""

        DatabaseConnectionInformation(source, true)
    } else {
        zipOrAccumulate(
            MissingDatabaseConfiguration::combine,
            { lift(cfg::host, "Host") },
            { lift(cfg::user, "User") },
            { lift(cfg::password, "Password") },
        ) { url, user, password ->
            val source = PGSimpleDataSource()
            source.setURL(url)
            source.user = user
            source.password = password

            DatabaseConnectionInformation(source, false)
        }
    }
}

fun Raise<MissingDatabaseConfiguration>.lift(block: () -> String?, err: String): String =
    ensureNotNull(block()) { raise(MissingDatabaseConfiguration(err)) }
