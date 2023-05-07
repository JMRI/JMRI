package jmri.jmrit.display.layoutEditor;

import java.awt.geom.*;

import jmri.*;
import jmri.util.*;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of LayoutTurnout.
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutTurnoutTest extends LayoutTrackTest {

    @Test
    public void testEnums() {
        // Check that enum ordinal values used for I/O don't
        // change.  Want to remove this once I/O is done
        // via enum names instead of numbers.

        Assert.assertEquals(0, LayoutTurnout.TurnoutType.NONE.ordinal());
        Assert.assertEquals(1, LayoutTurnout.TurnoutType.RH_TURNOUT.ordinal());
        Assert.assertEquals(2, LayoutTurnout.TurnoutType.LH_TURNOUT.ordinal());
        Assert.assertEquals(3, LayoutTurnout.TurnoutType.WYE_TURNOUT.ordinal());
        Assert.assertEquals(4, LayoutTurnout.TurnoutType.DOUBLE_XOVER.ordinal());
        Assert.assertEquals(5, LayoutTurnout.TurnoutType.RH_XOVER.ordinal());
        Assert.assertEquals(6, LayoutTurnout.TurnoutType.LH_XOVER.ordinal());
        Assert.assertEquals(7, LayoutTurnout.TurnoutType.SINGLE_SLIP.ordinal());
        Assert.assertEquals(8, LayoutTurnout.TurnoutType.DOUBLE_SLIP.ordinal());

        Assert.assertEquals(0, LayoutTurnout.LinkType.NO_LINK.ordinal());
        Assert.assertEquals(1, LayoutTurnout.LinkType.FIRST_3_WAY.ordinal());
        Assert.assertEquals(2, LayoutTurnout.LinkType.SECOND_3_WAY.ordinal());
        Assert.assertEquals(3, LayoutTurnout.LinkType.THROAT_TO_THROAT.ordinal());
    }

    @Test
    public void testNew() {

        Assert.assertNotNull("LayoutTurnout right hand not null", ltRH);
        Assert.assertNotNull("LayoutTurnout left hand not null", ltLH);
        Assert.assertNotNull("LayoutTurnout wye not null", ltWY);
        Assert.assertNotNull("LayoutTurnout double crossover not null", ltDX);
        Assert.assertNotNull("LayoutTurnout right hand crossover not null", ltRX);
        Assert.assertNotNull("LayoutTurnout left hand crossover not null", ltLX);
    }

    @Test
    public void testToString() {
        String s = ltRH.toString();
        Assert.assertNotNull("LayoutTurnout right hand toString() not null", s);
        Assert.assertEquals("LayoutTurnout Right Hand", s);

        s = ltLH.toString();
        Assert.assertNotNull("LayoutTurnout left hand toString() not null", s);
        Assert.assertEquals("LayoutTurnout Left Hand", s);

        s = ltWY.toString();
        Assert.assertNotNull("LayoutTurnout wye toString() not null", s);
        Assert.assertEquals("LayoutTurnout Wye", s);

        s = ltDX.toString();
        Assert.assertNotNull("LayoutTurnout double crossover toString() not null", s);
        Assert.assertEquals("LayoutTurnout Double XOver", s);

        s = ltRX.toString();
        Assert.assertNotNull("LayoutTurnout right hand crossover toString() not null", s);
        Assert.assertEquals("LayoutTurnout Right Hand XOver", s);

        s = ltLX.toString();
        Assert.assertNotNull("LayoutTurnout left hand crossover toString() not null", s);
        Assert.assertEquals("LayoutTurnout Left Hand XOver", s);
    }

    @Test
    public void testGetVersion() {
        assertEquals(1, ltRH.getVersion());
        assertEquals(1, ltLH.getVersion());
        assertEquals(1, ltWY.getVersion());
        assertEquals(1, ltDX.getVersion());
        assertEquals(1, ltRX.getVersion());
        assertEquals(1, ltLX.getVersion());
    }

    @Test
    public void testSetVersion() {
        ltRH.setVersion(2);
        assertEquals(2, ltRH.getVersion());
        ltLH.setVersion(2);
        assertEquals(2, ltLH.getVersion());
        ltWY.setVersion(2);
        assertEquals(2, ltWY.getVersion());
        ltDX.setVersion(2);
        assertEquals(2, ltDX.getVersion());
        ltRX.setVersion(2);
        assertEquals(2, ltRX.getVersion());
        ltLX.setVersion(2);
        assertEquals(2, ltLX.getVersion());
    }

    @Test
    public void testUseBlockSpeed() {
        Assert.assertFalse(ltRH.useBlockSpeed());
        Assert.assertFalse(ltLH.useBlockSpeed());
        Assert.assertFalse(ltWY.useBlockSpeed());
        Assert.assertFalse(ltDX.useBlockSpeed());
        Assert.assertFalse(ltRX.useBlockSpeed());
        Assert.assertFalse(ltLX.useBlockSpeed());
    }

    @Test
    public void testGetTurnoutName() {
        assertEquals("", ltRH.getTurnoutName());
        assertEquals("", ltLH.getTurnoutName());
        assertEquals("", ltWY.getTurnoutName());
        assertEquals("", ltDX.getTurnoutName());
        assertEquals("", ltRX.getTurnoutName());
        assertEquals("", ltLX.getTurnoutName());
    }

    @Test
    public void testGetSecondTurnoutName() {
        assertEquals("", ltRH.getSecondTurnoutName());
        assertEquals("", ltLH.getSecondTurnoutName());
        assertEquals("", ltWY.getSecondTurnoutName());
        assertEquals("", ltDX.getSecondTurnoutName());
        assertEquals("", ltRX.getSecondTurnoutName());
        assertEquals("", ltLX.getSecondTurnoutName());
    }

    @Test
    public void testGetBlockName() {
        assertEquals("", ltRH.getBlockName());
        assertEquals("", ltLH.getBlockName());
        assertEquals("", ltWY.getBlockName());
        assertEquals("", ltDX.getBlockName());
        assertEquals("", ltRX.getBlockName());
        assertEquals("", ltLX.getBlockName());
    }

    @Test
    public void testGetBlockBName() {
        assertEquals("", ltRH.getBlockBName());
        assertEquals("", ltLH.getBlockBName());
        assertEquals("", ltWY.getBlockBName());
        assertEquals("", ltDX.getBlockBName());
        assertEquals("", ltRX.getBlockBName());
        assertEquals("", ltLX.getBlockBName());
    }

    @Test
    public void testGetBlockCName() {
        assertEquals("", ltRH.getBlockCName());
        assertEquals("", ltLH.getBlockCName());
        assertEquals("", ltWY.getBlockCName());
        assertEquals("", ltDX.getBlockCName());
        assertEquals("", ltRX.getBlockCName());
        assertEquals("", ltLX.getBlockCName());
    }

    @Test
    public void testGetBlockDName() {
        assertEquals("", ltRH.getBlockDName());
        assertEquals("", ltLH.getBlockDName());
        assertEquals("", ltWY.getBlockDName());
        assertEquals("", ltDX.getBlockDName());
        assertEquals("", ltRX.getBlockDName());
        assertEquals("", ltLX.getBlockDName());
    }

    @Test
    public void testGetSignalHead() {
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.NONE));
        JUnitAppender.assertWarnMessage("Right Hand.getSignalHead(NONE); Unhandled point type");

        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTA1));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTA2));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTA3));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTB1));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTB2));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTC1));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTC2));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTD1));
        assertNull(ltRH.getSignalHead(LayoutTurnout.Geometry.POINTD2));
    }

    @Test
    public void testGetLinkedTurnoutName() {
        assertEquals("", ltRH.getLinkedTurnoutName());
        assertEquals("", ltLH.getLinkedTurnoutName());
        assertEquals("", ltWY.getLinkedTurnoutName());
        assertEquals("", ltDX.getLinkedTurnoutName());
        assertEquals("", ltRX.getLinkedTurnoutName());
        assertEquals("", ltLX.getLinkedTurnoutName());
    }

    @Test
    public void testSetLinkedTurnoutName() {
        ltRH.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltRH.getLinkedTurnoutName());

        ltLH.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltLH.getLinkedTurnoutName());

        ltWY.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltWY.getLinkedTurnoutName());

        ltDX.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltDX.getLinkedTurnoutName());

        ltRX.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltRX.getLinkedTurnoutName());

        ltLX.setLinkedTurnoutName("Foo");
        assertEquals("Foo", ltLX.getLinkedTurnoutName());
    }

    @Test
    public void testGetLinkType() {
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltRH.getLinkType());
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltLH.getLinkType());
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltWY.getLinkType());
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltDX.getLinkType());
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltRX.getLinkType());
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltLX.getLinkType());
    }

    @Test
    public void testSetLinkType() {
        ltRH.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltRH.getLinkType());
        ltLH.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltLH.getLinkType());
        ltWY.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltWY.getLinkType());
        ltDX.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltDX.getLinkType());
        ltRX.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltRX.getLinkType());
        ltLX.setLinkType(LayoutTurnout.LinkType.THROAT_TO_THROAT);
        assertEquals(LayoutTurnout.LinkType.THROAT_TO_THROAT, ltLX.getLinkType());

        ltRH.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltRH.getLinkType());
        ltLH.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltLH.getLinkType());
        ltWY.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltWY.getLinkType());
        ltDX.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltDX.getLinkType());
        ltRX.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltRX.getLinkType());
        ltLX.setLinkType(LayoutTurnout.LinkType.NO_LINK);
        assertEquals(LayoutTurnout.LinkType.NO_LINK, ltLX.getLinkType());
    }

    @Test
    public void testGetTurnoutType() {
        assertEquals(LayoutTurnout.TurnoutType.RH_TURNOUT, ltRH.getTurnoutType());
        assertEquals(LayoutTurnout.TurnoutType.LH_TURNOUT, ltLH.getTurnoutType() );
        assertEquals(LayoutTurnout.TurnoutType.WYE_TURNOUT, ltWY.getTurnoutType());
        assertEquals(LayoutTurnout.TurnoutType.DOUBLE_XOVER, ltDX.getTurnoutType());
        assertEquals(LayoutTurnout.TurnoutType.RH_XOVER, ltRX.getTurnoutType());
        assertEquals(LayoutTurnout.TurnoutType.LH_XOVER, ltLX.getTurnoutType());
    }

    @Test
    public void testGetConnectA() {
        Assert.assertNull(ltRH.getConnectA());
        Assert.assertNull(ltLH.getConnectA());
        Assert.assertNull(ltWY.getConnectA());
        Assert.assertNull(ltDX.getConnectA());
        Assert.assertNull(ltRX.getConnectA());
        Assert.assertNull(ltLX.getConnectA());
    }

    @Test
    public void testGetConnectB() {
        Assert.assertNull(ltRH.getConnectB());
        Assert.assertNull(ltLH.getConnectB());
        Assert.assertNull(ltWY.getConnectB());
        Assert.assertNull(ltDX.getConnectB());
        Assert.assertNull(ltRX.getConnectB());
        Assert.assertNull(ltLX.getConnectB());
    }

    @Test
    public void testGetConnectC() {
        Assert.assertNull(ltRH.getConnectC());
        Assert.assertNull(ltLH.getConnectC());
        Assert.assertNull(ltWY.getConnectC());
        Assert.assertNull(ltDX.getConnectC());
        Assert.assertNull(ltRX.getConnectC());
        Assert.assertNull(ltLX.getConnectC());
    }

    @Test
    public void testGetConnectD() {
        Assert.assertNull(ltRH.getConnectD());
        Assert.assertNull(ltLH.getConnectD());
        Assert.assertNull(ltWY.getConnectD());
        Assert.assertNull(ltDX.getConnectD());
        Assert.assertNull(ltRX.getConnectD());
        Assert.assertNull(ltLX.getConnectD());
    }

    @Test
    public void testGetTurnout() {
        Assert.assertNull(ltRH.getTurnout());
        Assert.assertNull(ltLH.getTurnout());
        Assert.assertNull(ltWY.getTurnout());
        Assert.assertNull(ltDX.getTurnout());
        Assert.assertNull(ltRX.getTurnout());
        Assert.assertNull(ltLX.getTurnout());
    }

    @Test
    public void testGetContinuingSense() {
        assertEquals(Turnout.CLOSED, ltRH.getContinuingSense());
        assertEquals(Turnout.CLOSED, ltLH.getContinuingSense());
        assertEquals(Turnout.CLOSED, ltWY.getContinuingSense());
        assertEquals(Turnout.CLOSED, ltDX.getContinuingSense());
        assertEquals(Turnout.CLOSED, ltRX.getContinuingSense());
        assertEquals(Turnout.CLOSED, ltLX.getContinuingSense());
    }

    @Test
    public void testIsDisabledDefault() {
        Assert.assertFalse("ltRH.isDisabled() is False", ltRH.isDisabled());
        Assert.assertFalse("ltLH.isDisabled() is False", ltLH.isDisabled());
        Assert.assertFalse("ltWY.isDisabled() is False", ltWY.isDisabled());
        Assert.assertFalse("ltDX.isDisabled() is False", ltDX.isDisabled());
        Assert.assertFalse("ltRX.isDisabled() is False", ltRX.isDisabled());
        Assert.assertFalse("ltLX.isDisabled() is False", ltLX.isDisabled());
    }

    @Test
    public void testSetDisabled() {
        ltRH.setDisabled(true);
        Assert.assertTrue("ltRH.isDisabled() is True", ltRH.isDisabled());
        ltLH.setDisabled(true);
        Assert.assertTrue("ltLH.isDisabled() is True", ltLH.isDisabled());
        ltWY.setDisabled(true);
        Assert.assertTrue("ltWY.isDisabled() is True", ltWY.isDisabled());
        ltDX.setDisabled(true);
        Assert.assertTrue("ltDX.isDisabled() is True", ltDX.isDisabled());
        ltRX.setDisabled(true);
        Assert.assertTrue("ltRX.isDisabled() is True", ltRX.isDisabled());
        ltLX.setDisabled(true);
        Assert.assertTrue("ltLX.isDisabled() is True", ltLX.isDisabled());
    }

    @Test
    public void testIsDisabledWhenOccupied() {
        Assert.assertFalse("ltRH.isDisabledWhenOccupied() is False", ltRH.isDisabledWhenOccupied());
        Assert.assertFalse("ltLH.isDisabledWhenOccupied() is False", ltLH.isDisabledWhenOccupied());
        Assert.assertFalse("ltWY.isDisabledWhenOccupied() is False", ltWY.isDisabledWhenOccupied());
        Assert.assertFalse("ltDX.isDisabledWhenOccupied() is False", ltDX.isDisabledWhenOccupied());
        Assert.assertFalse("ltRX.isDisabledWhenOccupied() is False", ltRX.isDisabledWhenOccupied());
        Assert.assertFalse("ltLX.isDisabledWhenOccupied() is False", ltLX.isDisabledWhenOccupied());
    }

    @Test
    public void testSetDisableWhenOccupied() {
        ltRH.setDisableWhenOccupied(true);
        Assert.assertTrue("ltRH.isDisabledWhenOccupied() is True", ltRH.isDisabledWhenOccupied());
        ltLH.setDisableWhenOccupied(true);
        Assert.assertTrue("ltLH.isDisabledWhenOccupied() is True", ltLH.isDisabledWhenOccupied());
        ltWY.setDisableWhenOccupied(true);
        Assert.assertTrue("ltWY.isDisabledWhenOccupied() is True", ltWY.isDisabledWhenOccupied());
        ltDX.setDisableWhenOccupied(true);
        Assert.assertTrue("ltDX.isDisabledWhenOccupied() is True", ltDX.isDisabledWhenOccupied());
        ltRX.setDisableWhenOccupied(true);
        Assert.assertTrue("ltRX.isDisabledWhenOccupied() is True", ltRX.isDisabledWhenOccupied());
        ltLX.setDisableWhenOccupied(true);
        Assert.assertTrue("ltLX.isDisabledWhenOccupied() is True", ltLX.isDisabledWhenOccupied());
    }

    @Test
    public void testGetConnectionInvalid() {
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            Assert.assertNull("ltRH.getConnection(invalid type) is null",
                    ltRH.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltRH.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Right Hand.getConnection(NONE); Invalid Connection Type");

        try {
            Assert.assertNull("ltLH.getConnection(invalid type) is null",
                    ltLH.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltLH.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Left Hand.getConnection(NONE); Invalid Connection Type");

        try {
            Assert.assertNull("ltWY.getConnection(invalid type) is null",
                    ltWY.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltWY.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Wye.getConnection(NONE); Invalid Connection Type");

        try {
            Assert.assertNull("ltDX.getConnection(invalid type) is null",
                    ltDX.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltDX.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Double XOver.getConnection(NONE); Invalid Connection Type");

        try {
            Assert.assertNull("ltRX.getConnection(invalid type) is null",
                    ltRX.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltRX.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Right Hand XOver.getConnection(NONE); Invalid Connection Type");

        try {
            Assert.assertNull("ltLX.getConnection(invalid type) is null",
                    ltLX.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltLX.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Left Hand XOver.getConnection(NONE); Invalid Connection Type");
    }

    @Test
    public void testGetConnectionValid() {
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            Assert.assertNull("ltRH.getConnection(valid type) is null",
                    ltRH.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltRH.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltLH.getConnection(valid type) is null",
                    ltLH.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltLH.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltWY.getConnection(valid type) is null",
                    ltWY.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltWY.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltDX.getConnection(valid type) is null",
                    ltDX.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltDX.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltRX.getConnection(valid type) is null",
                    ltRX.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltRX.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltLX.getConnection(valid type) is null",
                    ltLX.getConnection(HitPointType.TURNOUT_A));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltLX.getConnection(valid type)");
        }
    }

    @Test
    public void testSetConnection() {
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            // test Invalid Connection Type
            ltRH.setConnection(HitPointType.NONE, null, HitPointType.NONE);
            Assert.fail("No exception thrown on ltRH.setConnection(Invalid Connection Type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Right Hand.setConnection(NONE, null, NONE); Invalid Connection Type");

        try {
            // test unexpected type
            ltRH.setConnection(HitPointType.POS_POINT, null, HitPointType.POS_POINT);
            Assert.fail("No exception thrown on ltRH.setConnection(unexpected type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw Right Hand.setConnection(POS_POINT, null, POS_POINT); unexpected type");

        try {
            // test valid types
            ltRH.setConnection(HitPointType.TURNOUT_A, null, HitPointType.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltRH.setConnection(valid types)");
        }
    }

    @Test
    public void testSetConnectsInvalid() {
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        ltRH.setConnectA(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Right Hand.setConnectA(null, POS_POINT); unexpected type");
        ltRH.setConnectB(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Right Hand.setConnectB(null, POS_POINT); unexpected type");
        ltRH.setConnectC(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Right Hand.setConnectC(null, POS_POINT); unexpected type");
        ltRH.setConnectD(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Right Hand.setConnectD(null, POS_POINT); unexpected type");

        ltLH.setConnectA(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Left Hand.setConnectA(null, POS_POINT); unexpected type");
        ltLH.setConnectB(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Left Hand.setConnectB(null, POS_POINT); unexpected type");
        ltLH.setConnectC(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Left Hand.setConnectC(null, POS_POINT); unexpected type");
        ltLH.setConnectD(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Left Hand.setConnectD(null, POS_POINT); unexpected type");

        ltWY.setConnectA(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Wye.setConnectA(null, POS_POINT); unexpected type");
        ltWY.setConnectB(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Wye.setConnectB(null, POS_POINT); unexpected type");
        ltWY.setConnectC(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Wye.setConnectC(null, POS_POINT); unexpected type");
        ltWY.setConnectD(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Wye.setConnectD(null, POS_POINT); unexpected type");

        ltDX.setConnectA(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Double XOver.setConnectA(null, POS_POINT); unexpected type");
        ltDX.setConnectB(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Double XOver.setConnectB(null, POS_POINT); unexpected type");
        ltDX.setConnectC(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Double XOver.setConnectC(null, POS_POINT); unexpected type");
        ltDX.setConnectD(null, HitPointType.POS_POINT);
        JUnitAppender.assertErrorMessage("Double XOver.setConnectD(null, POS_POINT); unexpected type");
    }

    @Test
    public void testSetConnectsValid() {
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        ltRH.setConnectA(null, HitPointType.NONE);
        ltLH.setConnectB(null, HitPointType.NONE);
        ltWY.setConnectC(null, HitPointType.NONE);
        ltDX.setConnectD(null, HitPointType.NONE);
    }

    @Test
    public void testGetStateDefault() {
        assertNotNull( layoutEditor, "LayoutEditor exists");

        assertEquals(Turnout.UNKNOWN, ltRH.getState(), "ltRH.getState() is UNKNOWN");
        assertEquals(Turnout.UNKNOWN, ltLH.getState(), "ltLH.getState() is UNKNOWN");
        assertEquals(Turnout.UNKNOWN, ltWY.getState(), "ltWY.getState() is UNKNOWN");
        assertEquals(Turnout.UNKNOWN, ltDX.getState(), "ltDX.getState() is UNKNOWN");
        assertEquals(Turnout.UNKNOWN, ltRX.getState(), "ltRX.getState() is UNKNOWN");
        assertEquals(Turnout.UNKNOWN, ltLX.getState(), "ltLX.getState() is UNKNOWN");
    }

    @Test
    public void testSupportingTurnoutTwoSensor() throws JmriException {

        Turnout tOne = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        Turnout tTwo = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT2");
        Assert.assertNotNull("exists", tOne);
        Assert.assertNotNull("exists", tTwo);

        Sensor t1Closed = InstanceManager.getDefault(SensorManager.class).provideSensor("IST1Closed");
        Sensor t1Thrown = InstanceManager.getDefault(SensorManager.class).provideSensor("IST1Thrown");
        Sensor t2Closed = InstanceManager.getDefault(SensorManager.class).provideSensor("IST2Closed");
        Sensor t2Thrown = InstanceManager.getDefault(SensorManager.class).provideSensor("IST2Thrown");

        tOne.provideFirstFeedbackSensor("IST1Thrown");
        tOne.provideSecondFeedbackSensor("IST1Closed");
        tOne.setFeedbackMode(Turnout.TWOSENSOR);

        tTwo.provideFirstFeedbackSensor("IST2Thrown");
        tTwo.provideSecondFeedbackSensor("IST2Closed");
        tTwo.setFeedbackMode(Turnout.TWOSENSOR);

        int start1Listeners = tOne.getPropertyChangeListeners().length;
        int start2Listeners = tTwo.getPropertyChangeListeners().length;

        // not a test of using a String to set the Turnout
        ltRX.setTurnout("IT1");
        ltRX.setSecondTurnout("IT2");

        Assert.assertEquals("t1 +1 listeners ", start1Listeners + 1, tOne.getPropertyChangeListeners().length);
        Assert.assertEquals("t2 +1 listeners", start2Listeners + 1, tTwo.getPropertyChangeListeners().length);

        // not a test on the actual bean name, just that one is retrievable
        Assert.assertEquals("tOne name fetchable", tOne.getDisplayName(), ltRX.getTurnoutName());
        Assert.assertEquals("tTwo name fetchable", tTwo.getDisplayName(), ltRX.getSecondTurnoutName());

        assertEquals(Turnout.UNKNOWN, ltRX.getState(), "0 sensor states known getState UNKNOWN");

        t1Closed.setKnownState(Sensor.ACTIVE);
        assertEquals(Turnout.INCONSISTENT, ltRX.getState(), "only 1 sensor known INCONSISTENT");

        t1Thrown.setKnownState(Sensor.INACTIVE);
        assertEquals(Turnout.INCONSISTENT, ltRX.getState(), "only 2 sensor known INCONSISTENT");
        assertEquals(Turnout.CLOSED, tOne.getState(), "main turnout known ");

        t2Closed.setKnownState(Sensor.ACTIVE);
        assertEquals(Turnout.INCONSISTENT, ltRX.getState(), "only 3 sensor known INCONSISTENT");

        t2Thrown.setKnownState(Sensor.INACTIVE);
        Assert.assertEquals("t1 CLOSED", Turnout.CLOSED, tOne.getState());
        Assert.assertEquals("t2 CLOSED", Turnout.CLOSED, tTwo.getState());
        Assert.assertEquals("both turnouts CLOSED", Turnout.CLOSED, ltRX.getState());

        t2Closed.setKnownState(Sensor.INACTIVE);
        assertEquals(Turnout.INCONSISTENT, ltRX.getState(), "t2 leg status INCONSISTENT");
        t2Thrown.setKnownState(Sensor.ACTIVE);
        Assert.assertEquals("t2 THROWN", Turnout.THROWN, tTwo.getState());
        assertEquals(Turnout.INCONSISTENT, ltRX.getState(), "t2 THROWN t1 CLOSED INCONSISTENT");

        // remove turnouts and check num listeners
        ltRX.setSecondTurnout("");
        Assert.assertEquals("t1 +1 listeners ", start1Listeners + 1, tOne.getPropertyChangeListeners().length);
        Assert.assertEquals("t2 start listeners", start2Listeners, tTwo.getPropertyChangeListeners().length);

        ltRX.setTurnout("");
        Assert.assertEquals("t1 start listeners ", start1Listeners, tOne.getPropertyChangeListeners().length);

        tOne.dispose();
        tTwo.dispose();

        t1Closed.dispose();
        t1Thrown.dispose();

        t2Closed.dispose();
        t2Thrown.dispose();
    }

    @Test
    public void testSupportingTurnoutLogic() throws JmriException {

        Turnout stOne = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITS1");
        Turnout stTwo = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITS2");

        // not a test of using a String to set the Turnout
        ltRX.setTurnout("ITS1");
        ltRX.setSecondTurnout("ITS2");

        Assert.assertFalse(ltRX.isSecondTurnoutInverted());

        ltRX.setSecondTurnoutInverted(true);
        Assert.assertTrue(ltRX.isSecondTurnoutInverted());

        // Here we're testing the commanded state logic that joins the Turnouts when operated,
        // the actual LayoutTurnout status is dependent on the feedback status.
        Assert.assertEquals("t2 inverted CLOSED when t1 THROWN", Turnout.UNKNOWN, stTwo.getCommandedState());

        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        Assert.assertEquals("ltRX UNKNOWN", Turnout.UNKNOWN, ltRX.getState());
        stOne.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("t2 inverted CLOSED when t1 THROWN", Turnout.CLOSED, stTwo.getCommandedState());
        Assert.assertEquals("ltRX THROWN", Turnout.THROWN, ltRX.getState());

        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("t2 inverted THROWN when t1 CLOSED", Turnout.THROWN, stTwo.getCommandedState());
        Assert.assertEquals("ltRX CLOSED", Turnout.CLOSED, ltRX.getState());

        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("t1 inverted CLOSED when t2 THROWN", Turnout.CLOSED, stOne.getCommandedState());
        Assert.assertEquals("ltRX CLOSED", Turnout.CLOSED, ltRX.getState());

        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("t1 inverted THROWN when t2 CLOSED", Turnout.THROWN, stOne.getCommandedState());
        Assert.assertEquals("ltRX THROWN", Turnout.THROWN, ltRX.getState());

        ltRX.setSecondTurnoutInverted(false);
        Assert.assertFalse(ltRX.isSecondTurnoutInverted());

        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("t2 THROWN when t1 THROWN", Turnout.THROWN, stTwo.getCommandedState());

        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("t2 CLOSED when t1 CLOSED", Turnout.CLOSED, stTwo.getCommandedState());

        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.THROWN);
        Assert.assertEquals("t1 THROWN when t2 THROWN", Turnout.THROWN, stOne.getCommandedState());

        stTwo.setCommandedState(Turnout.UNKNOWN);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.CLOSED);
        Assert.assertEquals("t1 CLOSED when t2 CLOSED", Turnout.CLOSED, stOne.getCommandedState());

        stOne.dispose();
        stTwo.dispose();
    }

    @Test
    public void testThrowWhenOccupiedOneTurnout() throws JmriException {

        Assert.assertEquals("ltRH starts unknown state", Turnout.UNKNOWN, ltRH.getState());
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("no change null Turnout", Turnout.UNKNOWN, ltRH.getState());

        ltRH.setDisabled(true);
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("no change null Turnout and disabled", Turnout.UNKNOWN, ltRH.getState());

        Turnout otOne = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITS1");
        ltRH.setTurnout("ITS1");
        otOne.setCommandedState(Turnout.UNKNOWN);
        Assert.assertEquals("turnout set ", Turnout.UNKNOWN, otOne.getState());
        Assert.assertEquals("turnout set ", Turnout.UNKNOWN, ltRH.getState());

        ltRH.setDisabled(true);
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("turnout still UNKNOWN after set CLOSE disabled", Turnout.UNKNOWN, ltRH.getState());
        ltRH.setDisabled(false);

        LayoutBlock layoutBlock = new LayoutBlock("ILB1", "Test Block");
        Sensor occSensor = InstanceManager.getDefault(SensorManager.class).provideSensor("ISOccupancy1");
        occSensor.setKnownState(Sensor.ACTIVE);
        layoutBlock.setOccupancySensorName("ISOccupancy1");
        assertEquals(LayoutBlock.OCCUPIED, layoutBlock.getOccupancy(), "Occupied when sensor active");

        ltRHv.setLayoutBlock(layoutBlock);

        // occupied, occ. active disabled
        ltRH.setDisableWhenOccupied(false);
        occSensor.setKnownState(Sensor.ACTIVE);
        otOne.setCommandedState(Turnout.UNKNOWN);
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("ltRH CLOSED when occupied, occ when active not disabled", Turnout.CLOSED, otOne.getCommandedState());
        ltRH.setState(Turnout.THROWN);
        Assert.assertEquals("ltRH THROWN when occupied, occ when active not disabled", Turnout.THROWN, otOne.getCommandedState());

        // occupied, occ. active enabled
        ltRH.setDisableWhenOccupied(true);
        occSensor.setKnownState(Sensor.ACTIVE);
        otOne.setCommandedState(Turnout.UNKNOWN);
        ltRH.setState(Turnout.THROWN);
        Assert.assertEquals("ltRH unchanged when occupied, occ when active enabled", Turnout.UNKNOWN, ltRH.getState());
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("ltRH unchanged when occupied, occ when active enabled", Turnout.UNKNOWN, ltRH.getState());

        // not occupied, occ. active disabled
        ltRH.setDisableWhenOccupied(false);
        occSensor.setKnownState(Sensor.INACTIVE);
        otOne.setCommandedState(Turnout.UNKNOWN);
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("ltRH CLOSED when occupied, occ when active not disabled", Turnout.CLOSED, otOne.getCommandedState());
        ltRH.setState(Turnout.THROWN);
        Assert.assertEquals("ltRH THROWN when occupied, occ when active not disabled", Turnout.THROWN, otOne.getCommandedState());

        // not occupied, occ. active enabled
        ltRH.setDisableWhenOccupied(true);
        occSensor.setKnownState(Sensor.INACTIVE);
        otOne.setCommandedState(Turnout.UNKNOWN);
        ltRH.setState(Turnout.CLOSED);
        Assert.assertEquals("ltRH CLOSED when occupied, occ when active not disabled", Turnout.CLOSED, otOne.getCommandedState());
        ltRH.setState(Turnout.THROWN);
        Assert.assertEquals("ltRH THROWN when occupied, occ when active not disabled", Turnout.THROWN, otOne.getCommandedState());

        layoutBlock.dispose();
        otOne.dispose();
    }

    @Test
    public void testSecondaryTurnoutStateWhenSet() throws JmriException {

        // not testing occupancy as that was covered in previous test
        Turnout stOne = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITS1");
        Turnout stTwo = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITS2");

        // not a test of using a String to set the Turnout
        ltRX.setTurnout("ITS1");
        ltRX.setSecondTurnout("ITS2");

        ltRX.setSecondTurnoutInverted(false);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        ltRX.setState(Turnout.THROWN);
        Assert.assertEquals("StOne THROWN", Turnout.THROWN, stOne.getCommandedState());
        Assert.assertEquals("StTwo THROWN", Turnout.THROWN, stTwo.getCommandedState());

        ltRX.setSecondTurnoutInverted(false);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        ltRX.setState(Turnout.THROWN);
        Assert.assertEquals("StOne THROWN", Turnout.THROWN, stOne.getCommandedState());
        Assert.assertEquals("StTwo THROWN", Turnout.THROWN, stTwo.getCommandedState());

        ltRX.setState(Turnout.CLOSED);
        Assert.assertEquals("StOne CLOSED", Turnout.CLOSED, stOne.getCommandedState());
        Assert.assertEquals("StTwo CLOSED", Turnout.CLOSED, stTwo.getCommandedState());

        ltRX.setSecondTurnoutInverted(true);
        stOne.setCommandedState(Turnout.UNKNOWN);
        stTwo.setCommandedState(Turnout.UNKNOWN);
        ltRX.setState(Turnout.THROWN);
        Assert.assertEquals("StOne THROWN", Turnout.THROWN, stOne.getCommandedState());
        Assert.assertEquals("StTwo CLOSED", Turnout.CLOSED, stTwo.getCommandedState());

        ltRX.setState(Turnout.CLOSED);
        Assert.assertEquals("StOne CLOSED", Turnout.CLOSED, stOne.getCommandedState());
        Assert.assertEquals("StTwo THROWN", Turnout.THROWN, stTwo.getCommandedState());
    }

    @Test
    public void testToggle() throws JmriException {

        // null Turnout
        Assert.assertEquals("starts UNKNOWN", Turnout.UNKNOWN, ltRH.getState());
        ltRH.toggleTurnout();
        Assert.assertEquals("still UNKNOWN no Turnout to toggle", Turnout.UNKNOWN, ltRH.getState());

        Turnout ptOne = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("ITP1");
        ltRH.setTurnout("ITP1");

        ptOne.setCommandedState(Turnout.UNKNOWN);
        ltRH.toggleTurnout();
        Assert.assertEquals("UNKNOWN to CLOSED when toggled", Turnout.CLOSED, ptOne.getCommandedState());

        ltRH.toggleTurnout();
        Assert.assertEquals("CLOSED to THROWN when toggled", Turnout.THROWN, ptOne.getCommandedState());

        ltRH.toggleTurnout();
        Assert.assertEquals("THROWN to CLOSED when toggled", Turnout.CLOSED, ptOne.getCommandedState());
    }

    private LayoutRHTurnout ltRH = null;
    private LayoutRHTurnoutView ltRHv = null;

    private LayoutLHTurnout ltLH = null;
    private LayoutLHTurnoutView ltLHv = null;

    private LayoutWye ltWY = null;
    private LayoutWyeView ltWYv = null;

    private LayoutDoubleXOver ltDX = null;
    private LayoutDoubleXOverView ltDXv = null;

    private LayoutRHXOver ltRX = null;
    private LayoutRHXOverView ltRXv = null;

    private LayoutLHXOver ltLX = null;
    private LayoutLHXOverView ltLXv = null;

    @Override
    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();

        Point2D point = new Point2D.Double(150.0, 100.0);
        Point2D delta = new Point2D.Double(50.0, 75.0);

        ltRH = new LayoutRHTurnout("Right Hand", layoutEditor); // point, 33.0, 1.1, 1.2,
        ltRHv = new LayoutRHTurnoutView(ltRH, point, 33.0, 1.1, 1.2, layoutEditor);
        layoutEditor.addLayoutTrack(ltRH, ltRHv);

        point = MathUtil.add(point, delta);
        ltLH = new LayoutLHTurnout("Left Hand", layoutEditor); // point, 66.0, 1.3, 1.4,
        ltLHv = new LayoutLHTurnoutView(ltLH, point, 66.0, 1.3, 1.4, layoutEditor);
        layoutEditor.addLayoutTrack(ltLH, ltLHv);

        point = MathUtil.add(point, delta);
        ltWY = new LayoutWye("Wye", layoutEditor); // point, 99.0, 1.5, 1.6,
        ltWYv = new LayoutWyeView(ltWY, point, 99.0, 1.5, 1.6, layoutEditor);
        layoutEditor.addLayoutTrack(ltWY, ltWYv);

        point = MathUtil.add(point, delta);
        ltDX = new LayoutDoubleXOver("Double XOver", layoutEditor); // point, 132.0, 1.7, 1.8,
        ltDXv = new LayoutDoubleXOverView(ltDX, point, 132.0, 1.7, 1.8, layoutEditor);
        layoutEditor.addLayoutTrack(ltDX, ltDXv);

        point = MathUtil.add(point, delta);
        ltRX = new LayoutRHXOver("Right Hand XOver", layoutEditor); // point, 165.0, 1.9, 2.0,
        ltRXv = new LayoutRHXOverView(ltRX, point, 165.0, 1.9, 2.0, layoutEditor);
        layoutEditor.addLayoutTrack(ltRX, ltRXv);

        point = MathUtil.add(point, delta);
        ltLX = new LayoutLHXOver("Left Hand XOver", layoutEditor); // point, 198.0, 2.1, 2.2,
        ltLXv = new LayoutLHXOverView(ltLX, point, 198.0, 2.1, 2.2, layoutEditor);
        layoutEditor.addLayoutTrack(ltLX, ltLXv);

    }

    @Override
    @AfterEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        if (ltRH != null) {
            ltRH.remove();
            ltRH = null;
        }
        if (ltRHv != null) {
            ltRHv.remove();
            ltRHv.dispose();
            ltRHv = null;
        }

        if (ltLH != null) {
            ltLH.remove();
            ltLH = null;
        }
        if (ltLHv != null) {
            ltLHv.remove();
            ltLHv.dispose();
            ltLHv = null;
        }

        if (ltWY != null) {
            ltWY.remove();
            ltWY = null;
        }
        if (ltWYv != null) {
            ltWYv.remove();
            ltWYv.dispose();
            ltWYv = null;
        }

        if (ltDX != null) {
            ltDX.remove();
            ltDX = null;
        }
        if (ltDXv != null) {
            ltDXv.remove();
            ltDXv.dispose();
            ltDXv = null;
        }

        if (ltRX != null) {
            ltRX.remove();
            ltRX = null;
        }
        if (ltRXv != null) {
            ltRXv.remove();
            ltRXv.dispose();
            ltRXv = null;
        }

        if (ltLX != null) {
            ltLX.remove();
            ltLX = null;
        }
        if (ltLXv != null) {
            ltLXv.remove();
            ltLXv.dispose();
            ltLXv = null;
        }

        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutTest.class);
}
