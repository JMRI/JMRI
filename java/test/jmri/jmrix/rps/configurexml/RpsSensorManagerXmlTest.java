package jmri.jmrix.rps.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsSensorManagerXmlTest.java
 *
 * Description: tests for the RpsSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsSensorManagerXml constructor",new RpsSensorManagerXml());
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

