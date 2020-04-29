package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutBlockManagerXmlTest.java
 *
 * Test for the LayoutBlockManagerXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutBlockManagerXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutBlockManagerXml constructor",new LayoutBlockManagerXml());
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

