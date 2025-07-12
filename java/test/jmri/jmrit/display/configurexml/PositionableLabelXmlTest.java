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
        var loadAndStorePreferences = jmri.InstanceManager.getDefault(jmri.configurexml.LoadAndStorePreferences.class);
        loadAndStorePreferences.setExcludeFontExtensions(true);
        
        var label = new PositionableLabelXml();
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog", 0));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.plain", 0));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.bold", 1));
        Assert.assertEquals("Dialog", label.simplifyFontname("Dialog.italic", 2));
    }
    
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}

