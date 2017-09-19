package jmri.web;

import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
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

      System.setProperty("webdriver.gecko.driver", "/home/Paul/JMRI/geckodriver");
      Before(tags,()->{
         webDriver = new EventFiringWebDriver(new FirefoxDriver());
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
