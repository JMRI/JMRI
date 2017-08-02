package jmri.jmrit.roster.configurexml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RosterConfigPaneXmlTest.java
 *
 * Description: tests for the RosterConfigPaneXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RosterConfigPaneXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RosterConfigPaneXml constructor",new RosterConfigPaneXml());
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

