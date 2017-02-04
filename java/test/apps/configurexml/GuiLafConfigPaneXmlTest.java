package apps.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * GuiLafConfigPaneXmlTest.java
 *
 * Description: tests for the GuiLafConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class GuiLafConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("GuiLafConfigPaneXml constructor",new GuiLafConfigPaneXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}

