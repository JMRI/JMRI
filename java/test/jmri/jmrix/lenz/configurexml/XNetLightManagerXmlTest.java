package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XNetLightManagerXmlTest.java
 *
 * Test for the XNetLightManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class XNetLightManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("XNetLightManagerXml constructor",new XNetLightManagerXml());
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

