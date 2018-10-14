package jmri.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DccLocoAddressXmlTest.java
 *
 * Description: tests for the DccLocoAddressXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccLocoAddressXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccLocoAddressXml constructor",new DccLocoAddressXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

