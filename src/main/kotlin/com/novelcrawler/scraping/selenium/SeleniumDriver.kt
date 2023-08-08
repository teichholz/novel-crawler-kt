package com.novelcrawler.scraping.selenium

import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.File
import java.net.URL
import kotlin.time.Duration
import kotlin.time.toJavaDuration

interface SeleniumDriver : AutoCloseable {
    val driver: RemoteWebDriver

    fun waitingDriver(duration: Duration): WebDriverWait = WebDriverWait(driver, duration.toJavaDuration())

    fun <T> restoreCurrentPage(runnable: () -> T): T {
        val url = driver.currentUrl
        return runnable().also { driver.get(url) }
    }

    override fun close() {
        closeSession()
    }

    fun closeSession() {
        driver.quit()
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
class SeleniumDriverK8s : SeleniumDriver {

    override val driver = RemoteWebDriver(
        URL("http://firefox-grid.default:4444"),
        FirefoxOptions()
    )
}

fun WebDriver.toSeleniumDriver(): SeleniumDriver = object : SeleniumDriver {
    override val driver: RemoteWebDriver = this@toSeleniumDriver as RemoteWebDriver
}