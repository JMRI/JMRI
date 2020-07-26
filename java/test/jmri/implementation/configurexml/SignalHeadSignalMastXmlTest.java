package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * SignalHeadSignalMastXmlTest.java
 *
 * Test for the SignalHeadSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalHeadSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalHeadSignalMastXml constructor",new SignalHeadSignalMastXml());
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

