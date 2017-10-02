package jmri.web;

import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import jmri.web.server.WebServer;

/**
 * Cucumber step defintions for Home Servlet Acceptance tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class HomeServletAcceptanceSteps implements En {
     
   private EventFiringWebDriver webDriver;
   
   String[] tags = {"@webtest"};
   
   public HomeServletAcceptanceSteps() {

      Before(tags,()->{
         Class<? extends WebDriver> driverClass = FirefoxDriver.class;
         WebDriverManager.getInstance(driverClass).setup();
         webDriver = driverClass.newInstance();
      });

      When("^I ask for the /index\\.html$", () -> {
         webDriver.get("http://localhost:12080");
      });

      Then("^the home page is returned$", () -> {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
      });

      After(tags,()->{
         webDriver.close();
      });

   }
}
