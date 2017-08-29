package jmri.web;

import cucumber.api.java.en.*;
import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;

/**
 * Cucumber step defintions for Home Servlet Acceptance tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class HomeServletAcceptanceSteps implements En {
      
   public HomeServletAcceptanceSteps() {

      When("^I ask for the /index\\.html$", () -> {
         // Write code here that turns the phrase above into concrete actions
         throw new PendingException();
      });

      Then("^the home page is returned$", () -> {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
      });
   }
}
