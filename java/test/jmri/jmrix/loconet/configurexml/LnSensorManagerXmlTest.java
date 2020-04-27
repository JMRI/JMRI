package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LnSensorManagerXmlTest.java
 *
 * Test for the LnSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnSensorManagerXml constructor",new LnSensorManagerXml());
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

