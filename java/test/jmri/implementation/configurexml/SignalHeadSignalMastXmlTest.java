package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SignalHeadSignalMastXmlTest.java
 *
 * Description: tests for the SignalHeadSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalHeadSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalHeadSignalMastXml constructor",new SignalHeadSignalMastXml());
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

