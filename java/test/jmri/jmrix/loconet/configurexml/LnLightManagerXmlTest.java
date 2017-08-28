package jmri.jmrix.loconet.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LnLightManagerXmlTest.java
 *
 * Description: tests for the LnLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LnLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LnLightManagerXml constructor",new LnLightManagerXml());
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

