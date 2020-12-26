package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * DCCppTurnoutManagerXmlTest.java
 *
 * Test for the DCCppTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DCCppTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DCCppTurnoutManagerXml constructor",new DCCppTurnoutManagerXml());
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

