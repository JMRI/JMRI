package jmri.web;

import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.awt.GraphicsEnvironment;
import jmri.web.server.WebServer;

/**
 * Cucumber step defintions for Web Server Acceptance tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class WebServerAcceptanceSteps implements En {
     
   private EventFiringWebDriver webDriver;
   
   String[] firefoxtags = {"@webtest","@firefox"};
   String[] chrometags = {"@webtest","@chrome"};
   String[] tags = {"@webtest"};
   
   public WebServerAcceptanceSteps() {

      Given("^I am using firefox$", () -> {
         if(GraphicsEnvironment.isHeadless()) {
            FirefoxBinary firefoxBinary = new FirefoxBinary();
            firefoxBinary.addCommandLineOptions("--headless");
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            firefoxOptions.setBinary(firefoxBinary);
            webDriver = new EventFiringWebDriver(new FirefoxDriver(firefoxOptions));
         } else {
            webDriver = new EventFiringWebDriver(new FirefoxDriver());
         }
      });

      Given("^I am using chrome$", () -> {
         if(GraphicsEnvironment.isHeadless()) {
             ChromeOptions chromeOptions = new ChromeOptions();
             chromeOptions.addArguments("--headless");
             webDriver = new EventFiringWebDriver(new ChromeDriver(chromeOptions));
         } else {
             webDriver = new EventFiringWebDriver(new ChromeDriver());
         }
      });

      When("^I ask for the url (.*)$", (String url) -> {
         webDriver.get(url);
      });

      Then("^a page with title (.*) is returned$", (String pageTitle) -> {
        Assert.assertEquals("Page Title",pageTitle,webDriver.getTitle());
      });

      After(tags,()->{
         webDriver.close();
      });

   }
}
