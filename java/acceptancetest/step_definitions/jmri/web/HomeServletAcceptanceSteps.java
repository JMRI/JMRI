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

      Before(tags,()->{
         webDriver = new EventFiringWebDriver(new FirefoxDriver());
      });

      When("^I ask for the /index\\.html$", () -> {
         webDriver.get("http://localhost:12080");
      });

      Then("^the home page is returned$", () -> {
        Assert.assertEquals("Page Title","My JMRI Railroad",webDriver.getTitle());
      });

      After(tags,()->{
         webDriver.close();
      });

   }
}
