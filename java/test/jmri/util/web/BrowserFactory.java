package jmri.util.web;

import java.util.Map;
import java.util.HashMap;
import org.openqa.selenium.WebDriver;
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
 * Provide browsers for use in web tests.
 * Adapted from: http://toolsqa.com/selenium-webdriver/factory-design-principle-in-frameworks/
 *
 * @author	Paul Bender Copyright 2018
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
              if(GraphicsEnvironment.isHeadless()) {
                FirefoxBinary firefoxBinary = new FirefoxBinary();
                firefoxBinary.addCommandLineOptions("--headless");
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setBinary(firefoxBinary);
                driver = new EventFiringWebDriver(new FirefoxDriver(firefoxOptions));
                } else {
                   driver = new EventFiringWebDriver(new FirefoxDriver());
                }
                driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
				drivers.put("Firefox", driver);
			}
			break;
		case "Chrome":
			driver = drivers.get("Chrome");
			if (driver == null) {
               WebDriverManager.getInstance(ChromeDriver.class).setup();
               if(GraphicsEnvironment.isHeadless()) {
                  ChromeOptions chromeOptions = new ChromeOptions();
                  chromeOptions.addArguments("--headless");
                  driver = new EventFiringWebDriver(new ChromeDriver(chromeOptions));
              } else {
                 driver = new EventFiringWebDriver(new ChromeDriver());
              }
              driver.manage().timeouts().implicitlyWait(10,TimeUnit.SECONDS);
		      drivers.put("Chrome", driver);
			}
			break;
		}
		return driver;
	}

    /**
     * close all currently open beb browsers.
     */
    public static void CloseAllDriver() {
		for (String key : drivers.keySet()) {
			drivers.get(key).close();
		}
    }

}
