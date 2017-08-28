package jmri.jmrix.dcc.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DccTurnoutManagerXmlTest.java
 *
 * Description: tests for the DccTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class DccTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("DccTurnoutManagerXml constructor",new DccTurnoutManagerXml());
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

