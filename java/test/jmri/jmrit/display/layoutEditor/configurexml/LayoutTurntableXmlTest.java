package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutTurntableXmlTest.java
 *
 * Test for the LayoutTurntableXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutTurntableXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutTurntableXml constructor",new LayoutTurntableXml());
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

