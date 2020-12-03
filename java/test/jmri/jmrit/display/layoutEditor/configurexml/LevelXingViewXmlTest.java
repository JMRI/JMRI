package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LevelXingXmlTest.java
 *
 * Test for the LevelXingXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LevelXingViewXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LevelXingXml constructor",new LevelXingViewXml());
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

