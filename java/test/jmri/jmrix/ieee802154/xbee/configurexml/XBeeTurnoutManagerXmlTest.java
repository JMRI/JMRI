package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XBeeTurnoutManagerXmlTest.java
 *
 * Test for the XBeeTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XBeeTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeTurnoutManagerXml constructor",new XBeeTurnoutManagerXml());
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

