package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XBeeLightManagerXmlTest.java
 *
 * Test for the XBeeLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XBeeLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XBeeLightManagerXml constructor",new XBeeLightManagerXml());
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

