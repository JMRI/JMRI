package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * @author   Paul Bender  Copyright (C) 2016
 * @author   Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurnoutViewXmlTest {

    @Test
    public void testCtor(){
        Assert.assertNotNull("LayoutTurnoutXml constructor", new LayoutTurnoutViewXml());
    }

    @Test
    public void testFromEnum() {
        LayoutTurnoutViewXml.EnumIO<LayoutTurnout.LinkType> enumMap         = LayoutTurnoutViewXml.linkEnumMap;
        LayoutTurnoutViewXml.EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = LayoutTurnoutViewXml.tTypeEnumMap;
        
        Assert.assertEquals("NO_LINK", enumMap.outputFromEnum(LayoutTurnout.LinkType.NO_LINK));
        Assert.assertEquals("SECOND_3_WAY", enumMap.outputFromEnum(LayoutTurnout.LinkType.SECOND_3_WAY));

        Assert.assertEquals("NONE", tTypeEnumMap.outputFromEnum(LayoutTurnout.TurnoutType.NONE));
        Assert.assertEquals("WYE_TURNOUT", tTypeEnumMap.outputFromEnum(LayoutTurnout.TurnoutType.WYE_TURNOUT));

    }
    
    @Test
    public void testToEnum() {
        LayoutTurnoutViewXml.EnumIO<LayoutTurnout.LinkType> enumMap         = LayoutTurnoutViewXml.linkEnumMap;
        LayoutTurnoutViewXml.EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = LayoutTurnoutViewXml.tTypeEnumMap;
        
        Assert.assertEquals(LayoutTurnout.LinkType.NO_LINK, enumMap.inputFromString("NO_LINK"));
        Assert.assertEquals(LayoutTurnout.LinkType.NO_LINK, enumMap.inputFromString("0"));

        Assert.assertEquals(LayoutTurnout.LinkType.SECOND_3_WAY, enumMap.inputFromString("SECOND_3_WAY"));
        Assert.assertEquals(LayoutTurnout.LinkType.SECOND_3_WAY, enumMap.inputFromString("2"));

        Assert.assertEquals(LayoutTurnout.TurnoutType.NONE, tTypeEnumMap.inputFromString("NONE"));
        Assert.assertEquals(LayoutTurnout.TurnoutType.NONE, tTypeEnumMap.inputFromString("0"));

        Assert.assertEquals(LayoutTurnout.TurnoutType.WYE_TURNOUT, tTypeEnumMap.inputFromString("WYE_TURNOUT"));
        Assert.assertEquals(LayoutTurnout.TurnoutType.WYE_TURNOUT, tTypeEnumMap.inputFromString("3"));
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

