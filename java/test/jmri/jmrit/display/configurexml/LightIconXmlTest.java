package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LightIconXmlTest.java
 *
 * Description: tests for the LightIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LightIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LightIconXml constructor",new LightIconXml());
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

