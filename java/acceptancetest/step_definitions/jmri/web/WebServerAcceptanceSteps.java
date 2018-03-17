package jmri.web;

import cucumber.api.java8.En;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.awt.GraphicsEnvironment;

/**
 * Cucumber step definitions for Web Server Acceptance tests.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WebServerAcceptanceSteps implements En {
     
   private EventFiringWebDriver webDriver;
   
   String[] firefoxtags = {"@webtest","@firefox"};
   String[] chrometags = {"@webtest","@chrome"};
   String[] tags = {"@webtest"};
   
   public WebServerAcceptanceSteps(jmri.InstanceManager instance) {

      Given("^I am using firefox$", () -> {
         webDriver = jmri.util.web.BrowserFactory.getBrowser("Firefox");
      });

      Given("^I am using chrome$", () -> {
         webDriver = jmri.util.web.BrowserFactory.getBrowser("Chrome");
      });

      When("^I ask for the url (.*)$", (String url) -> {
         webDriver.get(url);
      });

      Then("^a page with title (.*) is returned$", (String pageTitle) -> {
        WebDriverWait wait = new WebDriverWait(webDriver,10);
        wait.until(new ExpectedCondition<Boolean>() {
            // this ExpectedCondition code is derived from code posted by user 
            // Jeff Vincent to
            // https://stackoverflow.com/questions/12858972/how-can-i-ask-the-selenium-webdriver-to-wait-for-few-seconds-in-java
            @Override
            public Boolean apply(WebDriver driver) {
               String script = "if (typeof window != 'undefined' && window.document) { return window.document.readyState; } else { return 'notready'; }";
               Boolean result;
               try {
                  result = ((JavascriptExecutor) driver).executeScript(script).equals("complete");
               } catch (Exception ex) {
                  result = Boolean.FALSE;
               }
             return result;
           }
        });
        Assert.assertEquals("Page Title",pageTitle,webDriver.getTitle());
      });
   }
}
