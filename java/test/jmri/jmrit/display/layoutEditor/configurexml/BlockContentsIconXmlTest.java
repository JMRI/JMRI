package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * BlockContentsIconXmlTest.java
 *
 * Description: tests for the BlockContentsIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class BlockContentsIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("BlockContentsIconXml constructor",new BlockContentsIconXml());
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

