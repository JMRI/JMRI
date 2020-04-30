package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutTurnoutXmlTest.java
 *
 * Test for the LayoutTurnoutXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutLHTurnoutXmlTest {

    @Test
    public void testCtor(){
        Assert.assertNotNull("LayoutTurnoutXml constructor",new LayoutLHTurnoutXml());
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

