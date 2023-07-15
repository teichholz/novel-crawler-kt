package com.novelcrawler.scraping.selenium

import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

interface SeleniumDriver {
    val driver: RemoteWebDriver

    fun waitingDriver(duration: Duration): WebDriverWait = WebDriverWait(driver, duration.toJavaDuration())

    fun <T> restoreCurrentPage(runnable: () -> T): T {
        val url = driver.currentUrl
        return runnable().also { driver.get(url) }
    }
}

class SeleniumDriverImpl : SeleniumDriver {
    private val PROXY_HOST = "213.136.89.121"
    private val PROXY_PORT = 80

    private val PROFILE_PATH = "/Users/t.eichholz/Library/Application Support/Firefox/Profiles/selenium.default-release"
    val PROFILE = FirefoxProfile(File(PROFILE_PATH)).apply {
        setPreference("network.proxy.type", 1)
        setPreference("network.proxy.http", PROXY_HOST)
        setPreference("network.proxy.http_port", PROXY_PORT)
        setPreference("dom.webdriver.enabled", false)
        setPreference("useAutomationExtension", false)
        updateUserPrefs(File("$PROFILE_PATH/prefs.js"))
    }


    override val driver = FirefoxDriver(
        FirefoxOptions()
            //.addArguments("-headless")
            // .setProfile(PROFILE)
    )
}
