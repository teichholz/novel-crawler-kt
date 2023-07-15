package com.novelcrawler.scraping.sync

import com.novelcrawler.config.Config
import com.novelcrawler.logger.LoggerDelegate
import com.novelcrawler.scraping.model.RoyalRoad
import com.novelcrawler.scraping.novels.NovelCrawler
import com.novelcrawler.scraping.novels.NovelMerger
import io.ktor.server.application.*
import kjob.core.Job
import kjob.core.KJob
import kjob.core.KronJob
import kjob.core.dsl.JobContextWithProps
import kjob.core.dsl.JobRegisterContext
import kjob.core.dsl.KJobFunctions
import kjob.core.job.JobExecutionType
import kjob.core.kjob
import kjob.jdbi.JdbiKJob
import kjob.kron.Kron
import kjob.kron.KronModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named

object Sync : Job("Sync")
object SyncKron : KronJob("Sync Kron", "* * */1 ? * * *")

object NovelSyncJob : KoinComponent {
    private val log by LoggerDelegate()
    // val crawlers : List<NovelCrawler> = getKoin().getAll<NovelCrawler>()
    val crawler: NovelCrawler by inject(named<RoyalRoad>())
    val config: Config by inject()

    context(Application)
    suspend fun start() {
        val kjob = kjob(JdbiKJob) {
            connectionString = config.database!!.host
            extension(KronModule)
        }.start()
        val merger = NovelMerger()

        kjob.register(Sync) {
            executionType = JobExecutionType.NON_BLOCKING
            maxRetries = 5
            execute {
                merger.merge(crawler)
            }.onError {
            }.onComplete {
                merger.reset(crawler)
                log.info("Job ${jobName} completed in ${time()}")
            }
        }
        kjob.schedule(Sync)

/*
        kjob(Kron).kron(SyncKron) {
            executionType = JobExecutionType.NON_BLOCKING
            maxRetries = 5
            execute {
                merger.merge(crawler)
            }.onError {
            }.onComplete {
                merger.reset(crawler)
                log.info("Job ${jobName} completed in ${time()}")
            }
        }
*/

        environment.monitor.subscribe(ApplicationStopping) {
            kjob.shutdown()
        }
    }
}
