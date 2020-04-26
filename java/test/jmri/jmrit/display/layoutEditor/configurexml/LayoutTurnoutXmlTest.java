package jmri.jmrit.display.layoutEditor.configurexml;

import javax.annotation.Nonnull;

import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutTurnoutXmlTest.java
 *
 * Description: tests for the LayoutTurnoutXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class LayoutTurnoutXmlTest {

    @Test
    public void testCtor(){
        Assert.assertNotNull("LayoutTurnoutXml constructor",new LayoutTurnoutXml());
    }

    @Test
    public void testFromEnum() {
        LayoutTurnoutXml.EnumIO<LayoutTurnout.LinkType> enumMap = LayoutTurnoutXml.linkEnumMap;

        Assert.assertEquals("0", enumMap.outputFromEnum(LayoutTurnout.LinkType.NO_LINK));
        Assert.assertEquals("2", enumMap.outputFromEnum(LayoutTurnout.LinkType.SECOND_3_WAY));
    }
    
    @Test
    public void testToEnum() {
        LayoutTurnoutXml.EnumIO<LayoutTurnout.LinkType> enumMap = LayoutTurnoutXml.linkEnumMap;
        
        Assert.assertEquals(LayoutTurnout.LinkType.NO_LINK, enumMap.inputFromString("0"));
        Assert.assertEquals(LayoutTurnout.LinkType.SECOND_3_WAY, enumMap.inputFromString("2"));

        // Assert.assertEquals(null, enumMap.inputFromString("21"));
        // Assert.assertEquals(null, enumMap.inputFromString("A"));
        // Assert.assertEquals(null, enumMap.inputFromString(""));
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

