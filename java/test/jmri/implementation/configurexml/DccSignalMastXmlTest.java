package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DccSignalMastXmlTest.java
 *
 * Test for the DccSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccSignalMastXml constructor",new DccSignalMastXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

