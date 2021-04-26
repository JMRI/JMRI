package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * DccSignalHeadXmlTest.java
 *
 * Test for the DccSignalHeadXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccSignalHeadXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccSignalHeadXml constructor",new DccSignalHeadXml());
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

