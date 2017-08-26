package jmri.jmrix.can.cbus.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * CbusTurnoutManagerXmlTest.java
 *
 * Description: tests for the CbusTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class CbusTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("CbusTurnoutManagerXml constructor",new CbusTurnoutManagerXml());
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

