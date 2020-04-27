package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * PositionableLabelXmlTest.java
 *
 * Test for the PositionableLabelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableLabelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableLabelXml constructor",new PositionableLabelXml());
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

