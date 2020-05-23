package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SignalMastIconXmlTest.java
 *
 * Test for the SignalMastIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalMastIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalMastIconXml constructor",new SignalMastIconXml());
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

