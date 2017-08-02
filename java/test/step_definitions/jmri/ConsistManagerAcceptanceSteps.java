package jmri;

import cucumber.api.java.en.*;
import cucumber.api.java8.En;
import cucumber.api.PendingException;
import org.junit.Assert;

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
   }
}
