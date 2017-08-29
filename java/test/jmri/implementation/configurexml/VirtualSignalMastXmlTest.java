package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * VirtualSignalMastXmlTest.java
 *
 * Description: tests for the VirtualSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class VirtualSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("VirtualSignalMastXml constructor",new VirtualSignalMastXml());
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

