package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}

