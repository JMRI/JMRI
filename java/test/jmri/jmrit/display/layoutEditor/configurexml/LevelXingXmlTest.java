package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
      Assert.assertNotNull("LevelXingXml constructor",new LevelXingXml());
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

