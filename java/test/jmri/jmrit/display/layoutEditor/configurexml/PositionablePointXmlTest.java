package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionablePointXmlTest.java
 *
 * Test for the PositionablePointXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionablePointXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionablePointXml constructor",new PositionablePointXml());
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

