package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

