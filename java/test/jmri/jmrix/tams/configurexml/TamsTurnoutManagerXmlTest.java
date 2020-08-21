package jmri.jmrix.tams.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * TamsTurnoutManagerXmlTest.java
 *
 * Test for the TamsTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TamsTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TamsTurnoutManagerXml constructor",new TamsTurnoutManagerXml());
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

