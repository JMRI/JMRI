package apps.startup.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * StartupActionsPreferencesPanelXmlTest.java
 *
 * Description: tests for the StartupActionsPreferencesPanelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class StartupActionsPreferencesPanelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("StartupActionsPreferencesPanelXml constructor",new StartupActionsPreferencesPanelXml());
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

