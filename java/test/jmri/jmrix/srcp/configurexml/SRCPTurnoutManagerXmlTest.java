package jmri.jmrix.srcp.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SRCPTurnoutManagerXmlTest.java
 *
 * Test for the SRCPTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SRCPTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SRCPTurnoutManagerXml constructor",new SRCPTurnoutManagerXml());
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

