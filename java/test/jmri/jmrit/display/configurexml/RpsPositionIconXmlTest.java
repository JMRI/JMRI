package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * RpsPositionIconXmlTest.java
 *
 * Test for the RpsPositionIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class RpsPositionIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("RpsPositionIconXml constructor",new RpsPositionIconXml());
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

