package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetTurnoutManagerXmlTest.java
 *
 * Description: tests for the XNetTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetTurnoutManagerXml constructor",new XNetTurnoutManagerXml());
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

