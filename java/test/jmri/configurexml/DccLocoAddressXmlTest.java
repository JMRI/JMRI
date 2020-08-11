package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DccLocoAddressXmlTest.java
 *
 * Test for the DccLocoAddressXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccLocoAddressXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccLocoAddressXml constructor",new DccLocoAddressXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

