package jmri.util.web;

import java.util.Map;
import java.util.HashMap;

import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Provide browsers for use in web tests. Adapted from: http://toolsqa.com/selenium-webdriver/factory-design-principle-in-frameworks/
 *
 * @author Paul Bender Copyright 2018
 */
public class BrowserFactory {

    private static Map<String, EventFiringWebDriver> drivers = new HashMap<String, EventFiringWebDriver>();

    /**
     * Factory method for getting browsers
     *
     * @param browserName name of the web browser to open.
     * @return the web driver for that browser.
     */
    public static EventFiringWebDriver getBrowser(String browserName) {
        EventFiringWebDriver driver = null;

        switch (browserName) {
            case "Firefox":
                driver = drivers.get("Firefox");
                if (driver == null) {
                    WebDriverManager.getInstance(FirefoxDriver.class).setup();
                    FirefoxBinary firefoxBinary = new FirefoxBinary();
                    firefoxBinary.addCommandLineOptions("--headless");
                    FirefoxOptions firefoxOptions = new FirefoxOptions()
                            .setBinary(firefoxBinary)
                            .setLogLevel(FirefoxDriverLogLevel.FATAL);
                    // the following causes javascript console messages to be output to standard out.
                    //firefoxOptions.addPreference("devtools.console.stdout.content", true);

                    driver = new EventFiringWebDriver(new FirefoxDriver(firefoxOptions));
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    drivers.put("Firefox", driver);
                }
                break;
            case "Chrome":
                driver = drivers.get("Chrome");
                if (driver == null) {
                    WebDriverManager.getInstance(ChromeDriver.class).setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--headless", "--log-level=3", "--disable-extensions");
                    
                    LoggingPreferences logPrefs = new LoggingPreferences();
                    logPrefs.enable(LogType.BROWSER, Level.SEVERE);
                    chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, logPrefs);

                    driver = new EventFiringWebDriver(new ChromeDriver(chromeOptions));
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    drivers.put("Chrome", driver);
                }
                break;
            default:
                jmri.util.LoggingUtil.warnOnce(log, "Unexpected browserName = {}", browserName);
                break;
        }
        return driver;
    }

    /**
     * close all currently open web browsers.
     */
    public static void CloseAllDriver() {
        drivers.keySet().forEach(o -> drivers.get(o).close());
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BrowserFactory.class);

}
