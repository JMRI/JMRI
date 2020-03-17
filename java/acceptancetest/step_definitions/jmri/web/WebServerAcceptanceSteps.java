package jmri.web;

import cucumber.api.java8.En;
import java.io.File;
import java.util.List;
import jmri.InstanceManager;
import jmri.ConfigureManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Sets.newLinkedHashSet;

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
            assertThat(webDriver.getTitle()).isEqualTo(pageTitle);
        });

        Then("^either (.*) or (.*) is returned as the title$", (String pageTitle, String formatedPageTitle) -> {
            waitLoad();
            assertThat(webDriver.getTitle()).isIn(newLinkedHashSet(pageTitle, formatedPageTitle));
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


           // find the columns containing the name and the state in the table header.
           WebElement tableHeader = webTable.findElement(By.tagName("thead"));
           List<WebElement> headerRows= tableHeader.findElements(By.tagName("tr"));
           List<WebElement> header = headerRows.get(0).findElements(By.tagName("th"));
           int nameCol=0;
           int stateCol=3;
           for(int i =0;i<header.size();i++){
               if(header.get(i).getText().equals("name")){
                   nameCol =i;
               }
               if(header.get(i).getText().equals("state")){
                   stateCol = i;
               }
           }

            // find the table body.

            WebElement tableBody = webTable.findElement(By.tagName("tbody"));
            List<WebElement> rows = tableBody.findElements(By.tagName("tr"));

           int i;
           for(i =0; i< rows.size(); i++){
               List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
               if(cols.size()>0 && cols.get(nameCol).getText().equals(item)){
                  assertThat(cols.get(stateCol).getText()).isEqualTo(state);
                  break;
               }
           }
           assertThat(rows.size()).isNotEqualTo(i).withFailMessage("item not found");
        });

        //Find the specified cell in the table, check value, then click on it and
        //  verify new value is as expected. Some columns are not supposed to change. 
        Then("^table (.*) has row (.*) column (.*) with text (.*) after click (.*)$",
                (String table, String row, String column, String text, String after) -> {

                    //navigate to home page and wait for it to load
                    webDriver.get("http://localhost:12080/");
                    waitLoad();
                    // navigate to the page for the requested table.
                    (webDriver.findElement(By.linkText("Tables"))).click();
                    (webDriver.findElement(By.linkText(table))).click();
                    // wait for the page to load. note that table is loaded via ajax, so it may still be loading
                    waitLoad();
                    WebDriverWait wait = new WebDriverWait(webDriver, 10);

                    //set xpath paths to items of interest
                    String tablePath = "//table[@id='jmri-data']";
                    String cellPath = tablePath + "//tr[@data-name='" + row + "']//td[@class='" + column + "']";
                    String cellAfterPath = cellPath + "[text()='" + after + "']";

                    //wait until the requested cell is visible (twice, since row is immed. repainted from json update)
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cellPath)));
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cellPath)));
                    //must be only one cell that matches
                    assertThat((Integer) webDriver.findElements(By.xpath(cellPath)).size()).isEqualTo(1);
                    //cell text must match expected value
                    assertThat(webDriver.findElement(By.xpath(cellPath)).getText()).isEqualTo(text);
                    //click on the target cell
                    webDriver.findElement(By.xpath(cellPath)).click();
                    //wait for cell to be updated with the "after" value
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cellAfterPath)));
                    //check that "after" value is correct
                    assertThat(webDriver.findElement(By.xpath(cellAfterPath)).getText()).isEqualTo(after);
                });
    }

    private void waitLoad() {
        WebDriverWait wait = new WebDriverWait(webDriver, 10);
        wait.until((ExpectedCondition<Boolean>) (WebDriver driver) -> {
            // this ExpectedCondition code is derived from code posted by user
            // Jeff Vincent to
            // https://stackoverflow.com/questions/12858972/how-can-i-ask-the-selenium-webdriver-to-wait-for-few-seconds-in-java
            String script
                    = "if (typeof window != 'undefined' && window.document) { return window.document.readyState; } else { return 'notready'; }";
            Boolean result = Boolean.FALSE;
            if (driver != null) {
                try {
                    result = ((JavascriptExecutor) driver).executeScript(script).equals("complete");
                } catch (Exception ex) {
                    // nothing to do, but silence the error
                }
            }
            return result;
        });
    }
}
