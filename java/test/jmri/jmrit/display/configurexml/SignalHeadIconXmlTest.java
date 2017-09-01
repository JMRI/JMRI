package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SignalHeadIconXmlTest.java
 *
 * Description: tests for the SignalHeadIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalHeadIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalHeadIconXml constructor",new SignalHeadIconXml());
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

