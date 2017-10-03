package jmri.jmrit.roster.configurexml;

import jmri.util.JUnitUtil;
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
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

