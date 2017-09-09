package jmri.web;

import cucumber.api.java.en.*;
import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

/**
 * Cucumber step defintions for Home Servlet Acceptance tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class HomeServletAcceptanceSteps implements En {
     
   private EventFiringWebDriver webDriver;
      
   public HomeServletAcceptanceSteps() {

      webDriver = new EventFiringWebDriver(new FirefoxDriver());

      When("^I ask for the /index\\.html$", () -> {
         webDriver.get("http://localhost:12080");
      });

      Then("^the home page is returned$", () -> {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
      });
   }
}
