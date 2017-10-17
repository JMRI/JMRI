package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LnTurnoutManagerXmlTest.java
 *
 * Description: tests for the LnTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnTurnoutManagerXml constructor",new LnTurnoutManagerXml());
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

