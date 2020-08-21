package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SignalHeadIconXmlTest.java
 *
 * Test for the SignalHeadIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SignalHeadIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SignalHeadIconXml constructor",new SignalHeadIconXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSignalHeadManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

