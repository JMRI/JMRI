package jmri.jmrix.pi.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the RaspberryPiTurnoutManagerXml class.
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RaspberryPiTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RaspberryPiTurnoutManagerXml constructor", new RaspberryPiTurnoutManagerXml());
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

