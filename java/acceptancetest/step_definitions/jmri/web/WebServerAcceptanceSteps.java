package jmri.web;

import cucumber.api.java8.En;
import java.io.File;
import java.util.List;
import jmri.InstanceManager;
import jmri.ConfigureManager;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Cucumber step definitions for Web Server Acceptance tests.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WebServerAcceptanceSteps implements En {

    private EventFiringWebDriver webDriver;

    String[] firefoxtags = {"@webtest", "@firefox"};
    String[] chrometags = {"@webtest", "@chrome"};
    String[] tags = {"@webtest"};
    String[] paneltags = {"@webpanel"};

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

        Given("^panel (.*) is loaded$", (String path) -> {
            InstanceManager.getDefault(ConfigureManager.class)
                .load(new File(path));
        });

        Then("^a page with title (.*) is returned$", (String pageTitle) -> {
            waitLoad();
            Assert.assertEquals("Page Title", pageTitle, webDriver.getTitle());
        });


        After(paneltags, () -> {
           // navigate back home to prevent the webpage from reloading.
           webDriver.get("http://localhost:12080/");
           jmri.util.JUnitUtil.closeAllPanels();
        });
    
        Then("^(.*) has item (.*) with state (.*)$", (String table, String item, String state) -> {
           webDriver.get("http://localhost:12080/");
           waitLoad();
           // navigate to the table.
           (webDriver.findElement(By.linkText("Tables"))).click();
           (webDriver.findElement(By.linkText(table))).click();
           waitLoad();
           // wait for the table to load.
           WebDriverWait wait = new WebDriverWait(webDriver, 10 );
           wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
           WebElement webTable = webDriver.findElement(By.xpath("//div[@id='wrap']//div[@class='container']//table"));

           // find the table body.

           WebElement tableBody = webTable.findElement(By.tagName("tbody"));
           List rows = tableBody.findElements(By.tagName("tr"));
           // we make an assumption that the first column is the systemName and
           // the last column is the state
           int i;
           for(i =0; i< rows.size(); i++){
               List cols = ((WebElement)rows.get(i)).findElements(By.tagName("td"));
               if(cols.size()>0 && ((WebElement)cols.get(0)).getText().equals(item)){
                  Assert.assertEquals("Expected State",state,((WebElement)cols.get(cols.size()-1)).getText());
                  break;
               }
           }
           Assert.assertNotEquals("item found",i,rows.size());
        });

    }

    private void waitLoad(){
            WebDriverWait wait = new WebDriverWait(webDriver, 10);
            wait.until(new ExpectedCondition<Boolean>() {
                // this ExpectedCondition code is derived from code posted by user 
                // Jeff Vincent to
                // https://stackoverflow.com/questions/12858972/how-can-i-ask-the-selenium-webdriver-to-wait-for-few-seconds-in-java
                @Override
                public Boolean apply(WebDriver driver) {
                    String script =
                            "if (typeof window != 'undefined' && window.document) { return window.document.readyState; } else { return 'notready'; }";
                    Boolean result = Boolean.FALSE;
                    if (driver != null) {
                        try {
                            result = ((JavascriptExecutor) driver).executeScript(script).equals("complete");
                        } catch (Exception ex) {
                            // nothing to do, but silence the error
                        }
                    }
                    return result;
                }
            });
    }
}
