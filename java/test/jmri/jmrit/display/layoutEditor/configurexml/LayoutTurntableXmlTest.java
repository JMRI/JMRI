package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

