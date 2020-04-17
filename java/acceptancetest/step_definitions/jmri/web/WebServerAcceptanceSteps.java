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


        Then("^(.*) is visible$", (String table) -> {
                    webDriver.get("http://localhost:12080/");
                    waitLoad();
                    // navigate to the table.
                    (webDriver.findElement(By.linkText("Tables"))).click();
                    (webDriver.findElement(By.linkText(table))).click();
                    waitLoad();
                    // wait for the table to load.
                    WebDriverWait wait = new WebDriverWait(webDriver, 10);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
                });

        Then("item (.*) with entry (.*) has state (.*)$", (String item, String column, String state) -> {
                    WebElement element = findTableCellItemNameAndCollumn("name", column, item);
                    assertThat(element.getText()).isEqualTo(state);
                });

        When("^(.*) for (.*) in (.*) is clicked$",
                (String column, String item, String table) -> {
                    WebDriverWait wait = new WebDriverWait(webDriver, 10 );
                    //set xpath paths to item of interest
                    String tablePath = "//table[@id='jmri-data']";
                    String cellPath = tablePath + "//tr[@data-name='" + item + "']//td[@class='" + column + "']";

                    //wait until the requested cell is visible (twice, since row is immed. repainted from json update)
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cellPath)));
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(cellPath)));
                    //must be only one cell that matches
                    assertThat((Integer) webDriver.findElements(By.xpath(cellPath)).size()).isEqualTo(1);
                    //click on the target cell
                    webDriver.findElement(By.xpath(cellPath)).click();
                });

        After(paneltags, () -> {
            // navigate back home to prevent the webpage from reloading.
            webDriver.get("http://localhost:12080/");
            jmri.util.JUnitUtil.closeAllPanels();
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

    private WebElement findTableCellItemNameAndCollumn(String rowName, String columnName,String itemName) {
        WebElement webTable = webDriver.findElement(By.xpath("//div[@id='wrap']//div[@class='container']//table"));
        // find the columns containing the nameName and the column label in the table header.
        WebElement tableHeader = webTable.findElement(By.tagName("thead"));
        List<WebElement> headerRows = tableHeader.findElements(By.tagName("tr"));
        List<WebElement> header = headerRows.get(0).findElements(By.tagName("th"));
        int rowNameCol = 0;
        int desiredCol = 3;
        for (int i = 0; i < header.size(); i++) {
            if (header.get(i).getText().equals(rowName)) {
                rowNameCol= i;
            }
            if (header.get(i).getText().equals(columnName)) {
                desiredCol = i;
            }
        }

        // find the table body.

        WebElement tableBody = webTable.findElement(By.tagName("tbody"));
        List<WebElement> rows = tableBody.findElements(By.tagName("tr"));


        for (int i = 0; i < rows.size(); i++) {
            List<WebElement> cols = rows.get(i).findElements(By.tagName("td"));
            if (cols.size() > 0 && cols.get(rowNameCol).getText().equals(itemName)) {
                return cols.get(desiredCol);
            }
        }
        throw new ItemNotFoundException("Unable to find row with " + rowName + " = " + itemName);
    }

    private final class ItemNotFoundException extends RuntimeException {
        public ItemNotFoundException(String reason){
            super(reason);
        }
    }
}
