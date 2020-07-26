package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * VirtualSignalMastXmlTest.java
 *
 * Test for the VirtualSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class VirtualSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("VirtualSignalMastXml constructor",new VirtualSignalMastXml());
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

