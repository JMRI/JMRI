package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

