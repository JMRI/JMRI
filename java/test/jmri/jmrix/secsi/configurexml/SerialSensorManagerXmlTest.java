package jmri.jmrix.secsi.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SerialSensorManagerXmlTest.java
 *
 * Test for the SerialSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class SerialSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("SerialSensorManagerXml constructor",new SerialSensorManagerXml());
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

