package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XNetTurnoutManagerXmlTest.java
 *
 * Test for the XNetTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetTurnoutManagerXml constructor",new XNetTurnoutManagerXml());
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

