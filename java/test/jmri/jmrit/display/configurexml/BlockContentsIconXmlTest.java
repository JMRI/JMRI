package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * BlockContentsIconXmlTest.java
 *
 * Test for the BlockContentsIconXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class BlockContentsIconXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("BlockContentsIconXml constructor",new BlockContentsIconXml());
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

