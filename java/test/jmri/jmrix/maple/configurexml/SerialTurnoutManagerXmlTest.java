package jmri.jmrix.maple.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialTurnoutManagerXmlTest.java
 *
 * Test for the SerialTurnoutManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialTurnoutManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialTurnoutManagerXml constructor",new SerialTurnoutManagerXml());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

