package jmri.jmrit.display.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * PositionableLabelXmlTest.java
 *
 * Test for the PositionableLabelXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class PositionableLabelXmlTest {

    @Test
    public void testCtor(){
      Assert.assertNotNull("PositionableLabelXml constructor", new PositionableLabelXml());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @Test
    public void testWindowsFontSpecialCase () {
        var label = new PositionableLabelXml();
        boolean windows = true;
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog", 0, windows));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.plain", 0, windows));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.bold", 1, windows));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.italic", 2, windows));
    }
    
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

