package jmri.jmrix.grapevine.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SerialSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSensorManagerXml constructor", new SerialSensorManagerXml());
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

