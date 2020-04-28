package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * LayoutTurnoutXmlTest.java
 *
 * Test for the LayoutTurnoutXml class
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
        LayoutTurnoutXml.EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = LayoutTurnoutXml.tTypeEnumMap;
        
        Assert.assertEquals("NO_LINK", enumMap.outputFromEnum(LayoutTurnout.LinkType.NO_LINK));
        Assert.assertEquals("SECOND_3_WAY", enumMap.outputFromEnum(LayoutTurnout.LinkType.SECOND_3_WAY));

        Assert.assertEquals("NONE", tTypeEnumMap.outputFromEnum(LayoutTurnout.TurnoutType.NONE));
        Assert.assertEquals("WYE_TURNOUT", tTypeEnumMap.outputFromEnum(LayoutTurnout.TurnoutType.WYE_TURNOUT));

    }
    
    @Test
    public void testToEnum() {
        LayoutTurnoutXml.EnumIO<LayoutTurnout.LinkType> enumMap = LayoutTurnoutXml.linkEnumMap;
        LayoutTurnoutXml.EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = LayoutTurnoutXml.tTypeEnumMap;
        
        Assert.assertEquals(LayoutTurnout.LinkType.NO_LINK, enumMap.inputFromString("NO_LINK"));
        Assert.assertEquals(LayoutTurnout.LinkType.NO_LINK, enumMap.inputFromString("0"));

        Assert.assertEquals(LayoutTurnout.LinkType.SECOND_3_WAY, enumMap.inputFromString("SECOND_3_WAY"));
        Assert.assertEquals(LayoutTurnout.LinkType.SECOND_3_WAY, enumMap.inputFromString("2"));

        Assert.assertEquals(LayoutTurnout.TurnoutType.NONE, tTypeEnumMap.inputFromString("NONE"));
        Assert.assertEquals(LayoutTurnout.TurnoutType.NONE, tTypeEnumMap.inputFromString("0"));

        Assert.assertEquals(LayoutTurnout.TurnoutType.WYE_TURNOUT, tTypeEnumMap.inputFromString("WYE_TURNOUT"));
        Assert.assertEquals(LayoutTurnout.TurnoutType.WYE_TURNOUT, tTypeEnumMap.inputFromString("3"));

        // Assert.assertEquals(null, enumMap.inputFromString("21"));
        // Assert.assertEquals(null, enumMap.inputFromString("A"));
        // Assert.assertEquals(null, enumMap.inputFromString(""));
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

