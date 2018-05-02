package jmri.jmrix.tams.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * TamsSensorManagerXmlTest.java
 *
 * Description: tests for the TamsSensorManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class TamsSensorManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("TamsSensorManagerXml constructor",new TamsSensorManagerXml());
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

