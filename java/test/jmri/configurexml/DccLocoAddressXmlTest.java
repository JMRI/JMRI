package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

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
      Assertions.assertNotNull(new DccLocoAddressXml(), "DccLocoAddressXml constructor");
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

