package jmri;

import cucumber.api.java.en.*;
import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;

/**
 * Cucumber step defintions for Consist Manager Acceptance tests.
 *
 * @author  Paul Bender Copyright (C) 2017
 */
public class ConsistManagerAcceptanceSteps implements En {
      
   private jmri.ConsistManager cm = null; 

   public ConsistManagerAcceptanceSteps() {

      Given("^the InstanceManager is started$", () -> {
          jmri.util.JUnitUtil.resetInstanceManager();
      });
 
      When("^I ask for the Consist Manager$", () -> {
          cm = jmri.InstanceManager.getNullableDefault(ConsistManager.class);
      });

      Then("^the consist manager is null$", () -> {
          // Write code here that turns the phrase above into concrete actions
          Assert.assertNull(cm);
      });

      Given("^A Command Station Instance$", () -> {
          jmri.util.JUnitUtil.initDebugCommandStation();
      });

      Then("^the consist manager is not null$", () -> {
          Assert.assertNotNull(cm);
      });

      Then("^the consist manager is an Nmra Consist Manager$", () -> {
          Assert.assertTrue(cm instanceof jmri.implementation.NmraConsistManager);
      });

      Given("^An Operations Mode Programmer Instance$", () -> {
          jmri.util.JUnitUtil.initDebugProgrammerManager();
      });

      Then("^the consist manager is an Dcc Consist Manager$", () -> {
          Assert.assertTrue(cm instanceof jmri.implementation.DccConsistManager);
      });
   }
}
