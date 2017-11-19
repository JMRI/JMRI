package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the XBeeTurnoutManagerXml class
 *
 * @author Paul Bender  Copyright (C) 2016
 */
public class XBeeTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeTurnoutManagerXml constructor",new XBeeTurnoutManagerXml());
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

