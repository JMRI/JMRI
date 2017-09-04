package jmri.jmrix.srcp.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPTurnoutManagerXmlTest.java
 *
 * Description: tests for the SRCPTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SRCPTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SRCPTurnoutManagerXml constructor",new SRCPTurnoutManagerXml());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

