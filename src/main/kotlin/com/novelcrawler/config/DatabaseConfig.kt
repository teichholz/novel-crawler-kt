package com.novelcrawler.config

import arrow.core.raise.Raise
import org.h2.jdbcx.JdbcDataSource
import org.postgresql.ds.PGSimpleDataSource
import javax.sql.DataSource

data class DatabaseConfig(val host: String?, val port: Int?, val user: String?, val password: String?, val database: String?)

data class MissingDatabaseConfiguration(var msg: String) : Throwable(createMsg(msg)) {
    init {
        msg = super.message!!
    }

    companion object {
        fun createMsg(name: String): String = "Missing database configuration for $name. " +
                "If you intend to use the h2 database you do not need to specify any configuration."
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
        val url = cfg.host ?: raise(MissingDatabaseConfiguration("url"))
        val user = cfg.user ?: raise(MissingDatabaseConfiguration("user"))
        val password = cfg.password ?: raise(MissingDatabaseConfiguration("password"))
        val source = PGSimpleDataSource()
        source.setURL(url)
        source.user = user
        source.password = password

        DatabaseConnectionInformation(source, false)
    }
}
