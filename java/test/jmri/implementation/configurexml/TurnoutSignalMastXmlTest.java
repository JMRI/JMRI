package jmri.implementation.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * TurnoutSignalMastXmlTest.java
 *
 * Test for the TurnoutSignalMastXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TurnoutSignalMastXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TurnoutSignalMastXml constructor",new TurnoutSignalMastXml());
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

