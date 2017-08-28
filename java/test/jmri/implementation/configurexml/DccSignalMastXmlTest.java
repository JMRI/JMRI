package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DccSignalMastXmlTest.java
 *
 * Description: tests for the DccSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccSignalMastXml constructor",new DccSignalMastXml());
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

