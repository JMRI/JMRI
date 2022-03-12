package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SignalMastIconXmlTest.java
 *
 * Test for the SignalMastIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalMastIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalMastIconXml constructor",new SignalMastIconXml());
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

