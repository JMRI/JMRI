package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutTurnoutXmlTest.java
 *
 * Description: tests for the LayoutTurnoutXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutTurnoutXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutTurnoutXml constructor",new LayoutTurnoutXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false);
        apps.tests.Log4JFixture.tearDown();
    }
}

