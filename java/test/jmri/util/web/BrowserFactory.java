package jmri.util.web;

import java.util.Map;
import java.util.HashMap;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;

/**
 * Provide browsers for use in web tests. Adapted from:
 * http://toolsqa.com/selenium-webdriver/factory-design-principle-in-frameworks/
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
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxBinary.addCommandLineOptions("--headless");
                    firefoxOptions.setBinary(firefoxBinary);
                    firefoxOptions.setLogLevel(org.openqa.selenium.firefox.FirefoxDriverLogLevel.ERROR);
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
                    if (GraphicsEnvironment.isHeadless()) {
                        chromeOptions.addArguments("--headless");
                    } else {
                        chromeOptions.addArguments("--log-level=3");
                    }
                    driver = new EventFiringWebDriver(new ChromeDriver(chromeOptions));
                    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
                    drivers.put("Chrome", driver);
                }
                break;
            default:
                jmri.util.Log4JUtil.warnOnce(log, "Unexpected browserName = {}", browserName);
                break;
        }
        return driver;
    }

    /**
     * close all currently open web browsers.
     */
    public static void CloseAllDriver() {
        for (String key : drivers.keySet()) {
            drivers.get(key).close();
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BrowserFactory.class);
}
