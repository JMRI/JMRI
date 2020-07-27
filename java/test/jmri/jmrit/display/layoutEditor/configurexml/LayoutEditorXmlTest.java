package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * LayoutEditorXmlTest.java
 *
 * Test for the LayoutEditorXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutEditorXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("LayoutEditorXml constructor",new LayoutEditorXml());
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

