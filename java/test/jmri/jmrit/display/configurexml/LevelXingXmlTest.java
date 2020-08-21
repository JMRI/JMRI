package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.layoutEditor.configurexml.LevelXingXml;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * LevelXingXmlTest.java
 *
 * Test for the LevelXingXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LevelXingXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LevelXingXml constructor", new LevelXingXml());
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
