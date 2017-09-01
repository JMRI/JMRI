package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SignalMastIconXmlTest.java
 *
 * Description: tests for the SignalMastIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalMastIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalMastIconXml constructor",new SignalMastIconXml());
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

