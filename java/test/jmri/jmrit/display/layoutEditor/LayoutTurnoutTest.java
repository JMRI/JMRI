package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jmri.JmriException;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test simple functioning of LayoutTurnout
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutTurnoutTest {

    private static LayoutEditor layoutEditor = null;
    LayoutTurnout ltRH = null;
    LayoutTurnout ltLH = null;
    LayoutTurnout ltWY = null;
    LayoutTurnout ltDX = null;
    LayoutTurnout ltRX = null;
    LayoutTurnout ltLX = null;

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutTurnout right hand not null", ltRH);
        Assert.assertNotNull("LayoutTurnout left hand not null", ltLH);
        Assert.assertNotNull("LayoutTurnout wye not null", ltWY);
        Assert.assertNotNull("LayoutTurnout double crossover not null", ltDX);
        Assert.assertNotNull("LayoutTurnout right hand crossover not null", ltRX);
        Assert.assertNotNull("LayoutTurnout left hand crossover not null", ltLX);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        String s = ltRH.toString();
        Assert.assertNotNull("LayoutTurnout right hand toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Right Hand");

        s = ltLH.toString();
        Assert.assertNotNull("LayoutTurnout left hand toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Left Hand");

        s = ltWY.toString();
        Assert.assertNotNull("LayoutTurnout wye toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Wye");

        s = ltDX.toString();
        Assert.assertNotNull("LayoutTurnout double crossover toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Double XOver");

        s = ltRX.toString();
        Assert.assertNotNull("LayoutTurnout right hand crossover toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Right Hand XOver");

        s = ltLX.toString();
        Assert.assertNotNull("LayoutTurnout left hand crossover toString() not null", s);
        Assert.assertEquals(s, "LayoutTurnout Left Hand XOver");
    }

    @Test
    public void testGetVersion() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getVersion(), 1);
        Assert.assertEquals(ltLH.getVersion(), 1);
        Assert.assertEquals(ltWY.getVersion(), 1);
        Assert.assertEquals(ltDX.getVersion(), 1);
        Assert.assertEquals(ltRX.getVersion(), 1);
        Assert.assertEquals(ltLX.getVersion(), 1);
    }

    @Test
    public void testSetVersion() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ltRH.setVersion(2);
        Assert.assertEquals(ltRH.getVersion(), 2);
        ltLH.setVersion(2);
        Assert.assertEquals(ltLH.getVersion(), 2);
        ltWY.setVersion(2);
        Assert.assertEquals(ltWY.getVersion(), 2);
        ltDX.setVersion(2);
        Assert.assertEquals(ltDX.getVersion(), 2);
        ltRX.setVersion(2);
        Assert.assertEquals(ltRX.getVersion(), 2);
        ltLX.setVersion(2);
        Assert.assertEquals(ltLX.getVersion(), 2);
    }

    @Test
    public void testUseBlockSpeed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse(ltRH.useBlockSpeed());
        Assert.assertFalse(ltLH.useBlockSpeed());
        Assert.assertFalse(ltWY.useBlockSpeed());
        Assert.assertFalse(ltDX.useBlockSpeed());
        Assert.assertFalse(ltRX.useBlockSpeed());
        Assert.assertFalse(ltLX.useBlockSpeed());
    }

    @Test
    public void testGetTurnoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getTurnoutName(), "");
        Assert.assertEquals(ltLH.getTurnoutName(), "");
        Assert.assertEquals(ltWY.getTurnoutName(), "");
        Assert.assertEquals(ltDX.getTurnoutName(), "");
        Assert.assertEquals(ltRX.getTurnoutName(), "");
        Assert.assertEquals(ltLX.getTurnoutName(), "");
    }

    @Test
    public void testGetSecondTurnoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getSecondTurnoutName(), "");
        Assert.assertEquals(ltLH.getSecondTurnoutName(), "");
        Assert.assertEquals(ltWY.getSecondTurnoutName(), "");
        Assert.assertEquals(ltDX.getSecondTurnoutName(), "");
        Assert.assertEquals(ltRX.getSecondTurnoutName(), "");
        Assert.assertEquals(ltLX.getSecondTurnoutName(), "");
    }

    @Test
    public void testGetBlockName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getBlockName(), "");
        Assert.assertEquals(ltLH.getBlockName(), "");
        Assert.assertEquals(ltWY.getBlockName(), "");
        Assert.assertEquals(ltDX.getBlockName(), "");
        Assert.assertEquals(ltRX.getBlockName(), "");
        Assert.assertEquals(ltLX.getBlockName(), "");
    }

    @Test
    public void testGetBlockBName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getBlockBName(), "");
        Assert.assertEquals(ltLH.getBlockBName(), "");
        Assert.assertEquals(ltWY.getBlockBName(), "");
        Assert.assertEquals(ltDX.getBlockBName(), "");
        Assert.assertEquals(ltRX.getBlockBName(), "");
        Assert.assertEquals(ltLX.getBlockBName(), "");
    }

    @Test
    public void testGetBlockCName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getBlockCName(), "");
        Assert.assertEquals(ltLH.getBlockCName(), "");
        Assert.assertEquals(ltWY.getBlockCName(), "");
        Assert.assertEquals(ltDX.getBlockCName(), "");
        Assert.assertEquals(ltRX.getBlockCName(), "");
        Assert.assertEquals(ltLX.getBlockCName(), "");
    }

    @Test
    public void testGetBlockDName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getBlockDName(), "");
        Assert.assertEquals(ltLH.getBlockDName(), "");
        Assert.assertEquals(ltWY.getBlockDName(), "");
        Assert.assertEquals(ltDX.getBlockDName(), "");
        Assert.assertEquals(ltRX.getBlockDName(), "");
        Assert.assertEquals(ltLX.getBlockDName(), "");
    }

    @Test
    public void testGetSignalHead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.NONE));
        jmri.util.JUnitAppender.assertWarnMessage("Unhandled point type: 0");

        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTA));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTA2));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTA3));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTB));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTB2));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTC));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTC2));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTD));
        Assert.assertNull(ltRH.getSignalHead(LayoutTurnout.POINTD2));
    }

    @Test
    public void testGetLinkedTurnoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getLinkedTurnoutName(), "");
        Assert.assertEquals(ltLH.getLinkedTurnoutName(), "");
        Assert.assertEquals(ltWY.getLinkedTurnoutName(), "");
        Assert.assertEquals(ltDX.getLinkedTurnoutName(), "");
        Assert.assertEquals(ltRX.getLinkedTurnoutName(), "");
        Assert.assertEquals(ltLX.getLinkedTurnoutName(), "");
    }

    @Test
    public void testSetLinkedTurnoutName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ltRH.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltRH.getLinkedTurnoutName(), "Foo");

        ltLH.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltLH.getLinkedTurnoutName(), "Foo");

        ltWY.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltWY.getLinkedTurnoutName(), "Foo");

        ltDX.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltDX.getLinkedTurnoutName(), "Foo");

        ltRX.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltRX.getLinkedTurnoutName(), "Foo");

        ltLX.setLinkedTurnoutName("Foo");
        Assert.assertEquals(ltLX.getLinkedTurnoutName(), "Foo");
    }

    @Test
    public void testGetLinkType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getLinkType(), LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltLH.getLinkType(), LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltWY.getLinkType(), LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltDX.getLinkType(), LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltRX.getLinkType(), LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltLX.getLinkType(), LayoutTurnout.NO_LINK);
    }

    @Test
    public void testSetLinkType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ltRH.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltRH.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);
        ltLH.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltLH.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);
        ltWY.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltWY.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);
        ltDX.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltDX.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);
        ltRX.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltRX.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);
        ltLX.setLinkType(LayoutTurnout.THROAT_TO_THROAT);
        Assert.assertEquals(ltLX.getLinkType(), LayoutTurnout.THROAT_TO_THROAT);

        ltRH.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltRH.getLinkType(), LayoutTurnout.NO_LINK);
        ltLH.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltLH.getLinkType(), LayoutTurnout.NO_LINK);
        ltWY.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltWY.getLinkType(), LayoutTurnout.NO_LINK);
        ltDX.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltDX.getLinkType(), LayoutTurnout.NO_LINK);
        ltRX.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltRX.getLinkType(), LayoutTurnout.NO_LINK);
        ltLX.setLinkType(LayoutTurnout.NO_LINK);
        Assert.assertEquals(ltLX.getLinkType(), LayoutTurnout.NO_LINK);
    }

    @Test
    public void testGetTurnoutType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getTurnoutType(), LayoutTurnout.RH_TURNOUT);
        Assert.assertEquals(ltLH.getTurnoutType(), LayoutTurnout.LH_TURNOUT);
        Assert.assertEquals(ltWY.getTurnoutType(), LayoutTurnout.WYE_TURNOUT);
        Assert.assertEquals(ltDX.getTurnoutType(), LayoutTurnout.DOUBLE_XOVER);
        Assert.assertEquals(ltRX.getTurnoutType(), LayoutTurnout.RH_XOVER);
        Assert.assertEquals(ltLX.getTurnoutType(), LayoutTurnout.LH_XOVER);
    }

    @Test
    public void testGetConnectA() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getConnectA());
        Assert.assertNull(ltLH.getConnectA());
        Assert.assertNull(ltWY.getConnectA());
        Assert.assertNull(ltDX.getConnectA());
        Assert.assertNull(ltRX.getConnectA());
        Assert.assertNull(ltLX.getConnectA());
    }

    @Test
    public void testGetConnectB() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getConnectB());
        Assert.assertNull(ltLH.getConnectB());
        Assert.assertNull(ltWY.getConnectB());
        Assert.assertNull(ltDX.getConnectB());
        Assert.assertNull(ltRX.getConnectB());
        Assert.assertNull(ltLX.getConnectB());
    }

    @Test
    public void testGetConnectC() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getConnectC());
        Assert.assertNull(ltLH.getConnectC());
        Assert.assertNull(ltWY.getConnectC());
        Assert.assertNull(ltDX.getConnectC());
        Assert.assertNull(ltRX.getConnectC());
        Assert.assertNull(ltLX.getConnectC());
    }

    @Test
    public void testGetConnectD() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getConnectD());
        Assert.assertNull(ltLH.getConnectD());
        Assert.assertNull(ltWY.getConnectD());
        Assert.assertNull(ltDX.getConnectD());
        Assert.assertNull(ltRX.getConnectD());
        Assert.assertNull(ltLX.getConnectD());
    }

    @Test
    public void testGetTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNull(ltRH.getTurnout());
        Assert.assertNull(ltLH.getTurnout());
        Assert.assertNull(ltWY.getTurnout());
        Assert.assertNull(ltDX.getTurnout());
        Assert.assertNull(ltRX.getTurnout());
        Assert.assertNull(ltLX.getTurnout());
    }

    @Test
    public void testGetContinuingSense() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(ltRH.getContinuingSense(), Turnout.CLOSED);
        Assert.assertEquals(ltLH.getContinuingSense(), Turnout.CLOSED);
        Assert.assertEquals(ltWY.getContinuingSense(), Turnout.CLOSED);
        Assert.assertEquals(ltDX.getContinuingSense(), Turnout.CLOSED);
        Assert.assertEquals(ltRX.getContinuingSense(), Turnout.CLOSED);
        Assert.assertEquals(ltLX.getContinuingSense(), Turnout.CLOSED);
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("ltRH.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(150.0, 100.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(132.0, 87.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(168.0, 113.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(162.0, 123.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(132.0, 87.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(150.0, 100.0),
                ltRH.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));

        Assert.assertEquals("ltLH.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(200.0, 175.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(189.0, 149.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(211.0, 201.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(222.0, 195.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(189.0, 149.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(200.0, 175.0),
                ltLH.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));

        Assert.assertEquals("ltWY.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(250.0, 250.0),
                ltWY.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(254.5, 218.5),
                ltWY.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(238.0, 280.0),
                ltWY.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(253.0, 283.0),
                ltWY.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(262.0, 220.0),
                ltWY.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(250.0, 250.0),
                ltWY.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));

        Assert.assertEquals("ltDX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(300.0, 325.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(347.0, 297.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(279.0, 377.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(253.0, 353.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(321.0, 273.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(300.0, 325.0),
                ltDX.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));

        Assert.assertEquals("ltRX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(350.0, 400.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(410.0, 404.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(337.0, 424.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(290.0, 396.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(363.0, 376.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(350.0, 400.0),
                ltRX.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));

        Assert.assertEquals("ltLX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(400.0, 475.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(413.0, 503.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.TURNOUT_A));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(334.0, 476.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.TURNOUT_B));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(387.0, 447.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.TURNOUT_C));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(466.0, 474.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.TURNOUT_D));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(400.0, 475.0),
                ltLX.getCoordsForConnectionType(LayoutTrack.TURNOUT_CENTER));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("ltRH.getBounds() is equal to...",
                new Rectangle2D.Double(132.0, 87.0, 36.0, 36.0),
                ltRH.getBounds());
        Assert.assertEquals("ltLH.getBounds() is equal to...",
                new Rectangle2D.Double(189.0, 149.0, 33.0, 52.0),
                ltLH.getBounds());
        Assert.assertEquals("ltWY.getBounds() is equal to...",
                new Rectangle2D.Double(238.0, 218.5, 16.5, 64.5),
                ltWY.getBounds());
        Assert.assertEquals("ltDX.getBounds() is equal to...",
                new Rectangle2D.Double(253.0, 273.0, 94.0, 104.0),
                ltDX.getBounds());
        Assert.assertEquals("ltRX.getBounds() is equal to...",
                new Rectangle2D.Double(290.0, 376.0, 120.0, 48.0),
                ltRX.getBounds());
        Assert.assertEquals("ltLX.getBounds() is equal to...",
                new Rectangle2D.Double(334.0, 447.0, 132.0, 56.0),
                ltLX.getBounds());
    }

    @Test
    public void testIsDisabledDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("ltRH.isDisabled() is False", ltRH.isDisabled());
        Assert.assertFalse("ltLH.isDisabled() is False", ltLH.isDisabled());
        Assert.assertFalse("ltWY.isDisabled() is False", ltWY.isDisabled());
        Assert.assertFalse("ltDX.isDisabled() is False", ltDX.isDisabled());
        Assert.assertFalse("ltRX.isDisabled() is False", ltRX.isDisabled());
        Assert.assertFalse("ltLX.isDisabled() is False", ltLX.isDisabled());
    }

    @Test
    public void testSetDisabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("ltRH.isDisabledWhenOccupied() is False", ltRH.isDisabledWhenOccupied());
        Assert.assertFalse("ltLH.isDisabledWhenOccupied() is False", ltLH.isDisabledWhenOccupied());
        Assert.assertFalse("ltWY.isDisabledWhenOccupied() is False", ltWY.isDisabledWhenOccupied());
        Assert.assertFalse("ltDX.isDisabledWhenOccupied() is False", ltDX.isDisabledWhenOccupied());
        Assert.assertFalse("ltRX.isDisabledWhenOccupied() is False", ltRX.isDisabledWhenOccupied());
        Assert.assertFalse("ltLX.isDisabledWhenOccupied() is False", ltLX.isDisabledWhenOccupied());
    }

    @Test
    public void testSetDisableWhenOccupied() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            Assert.assertNull("ltRH.getConnection(invalid type) is null",
                    ltRH.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltRH.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            Assert.assertNull("ltLH.getConnection(invalid type) is null",
                    ltLH.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltLH.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            Assert.assertNull("ltWY.getConnection(invalid type) is null",
                    ltWY.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltWY.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            Assert.assertNull("ltDX.getConnection(invalid type) is null",
                    ltDX.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltDX.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            Assert.assertNull("ltRX.getConnection(invalid type) is null",
                    ltRX.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltRX.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            Assert.assertNull("ltLX.getConnection(invalid type) is null",
                    ltLX.getConnection(LayoutEditor.NONE));
            Assert.fail("No exception thrown on ltLX.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");
    }

    @Test
    public void testGetConnectionValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            Assert.assertNull("ltRH.getConnection(valid type) is null",
                    ltRH.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltRH.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltLH.getConnection(valid type) is null",
                    ltLH.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltLH.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltWY.getConnection(valid type) is null",
                    ltWY.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltWY.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltDX.getConnection(valid type) is null",
                    ltDX.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltDX.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltRX.getConnection(valid type) is null",
                    ltRX.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltRX.getConnection(valid type)");
        }

        try {
            Assert.assertNull("ltLX.getConnection(valid type) is null",
                    ltLX.getConnection(LayoutEditor.TURNOUT_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltLX.getConnection(valid type)");
        }
    }

    @Test
    public void testSetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        try {
            // test invalid connection type
            ltRH.setConnection(LayoutEditor.NONE, null, LayoutEditor.NONE);
            Assert.fail("No exception thrown on ltRH.setConnection(invalid connection type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 0");

        try {
            // test invalid object type
            ltRH.setConnection(LayoutEditor.POS_POINT, null, LayoutEditor.POS_POINT);
            Assert.fail("No exception thrown on ltRH.setConnection(invalid object type)");
        } catch (JmriException ex) {
        }
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of connection to layoutturnout - 1");

        try {
            // test valid types
            ltRH.setConnection(LayoutEditor.TURNOUT_A, null, LayoutEditor.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltRH.setConnection(valid types)");
        }
    }

    @Test
    public void testSetConnectsInvalid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        ltRH.setConnectA(null, LayoutEditor.POS_POINT);
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of A connection to layoutturnout - 1");
        ltLH.setConnectB(null, LayoutEditor.POS_POINT);
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of B connection to layoutturnout - 1");
        ltWY.setConnectC(null, LayoutEditor.POS_POINT);
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of C connection to layoutturnout - 1");
        ltDX.setConnectD(null, LayoutEditor.POS_POINT);
        jmri.util.JUnitAppender.assertErrorMessage("unexpected type of D connection to layoutturnout - 1");
    }

    @Test
    public void testSetConnectsValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        ltRH.setConnectA(null, LayoutEditor.NONE);
        ltLH.setConnectB(null, LayoutEditor.NONE);
        ltWY.setConnectC(null, LayoutEditor.NONE);
        ltDX.setConnectD(null, LayoutEditor.NONE);
    }

    @Test
    public void testSetUpDefaultSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        // note: Not really testing anything here,
        // this is just for code coverage.
        ltRH.setUpDefaultSize();
        ltLH.setUpDefaultSize();
        ltWY.setUpDefaultSize();
        ltDX.setUpDefaultSize();
        ltRX.setUpDefaultSize();
        ltLX.setUpDefaultSize();
    }

    @Test
    public void testGetStateDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        Assert.assertTrue("ltRH.getState() is UNKNOWN", ltRH.getState() == Turnout.UNKNOWN);
        Assert.assertTrue("ltLH.getState() is UNKNOWN", ltLH.getState() == Turnout.UNKNOWN);
        Assert.assertTrue("ltWY.getState() is UNKNOWN", ltWY.getState() == Turnout.UNKNOWN);
        Assert.assertTrue("ltDX.getState() is UNKNOWN", ltDX.getState() == Turnout.UNKNOWN);
        Assert.assertTrue("ltRX.getState() is UNKNOWN", ltRX.getState() == Turnout.UNKNOWN);
        Assert.assertTrue("ltLX.getState() is UNKNOWN", ltLX.getState() == Turnout.UNKNOWN);
    }    

    // from here down is testing infrastructure
    @BeforeClass
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
        }
    }

    @AfterClass
    public static void afterClass() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();
            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 75.0);

            ltRH = new LayoutTurnout("Right Hand",
                    LayoutTurnout.RH_TURNOUT, point, 33.0, 1.1, 1.2, layoutEditor);

            point = MathUtil.add(point, delta);
            ltLH = new LayoutTurnout("Left Hand",
                    LayoutTurnout.LH_TURNOUT, point, 66.0, 1.3, 1.4, layoutEditor);

            point = MathUtil.add(point, delta);
            ltWY = new LayoutTurnout("Wye",
                    LayoutTurnout.WYE_TURNOUT, point, 99.0, 1.5, 1.6, layoutEditor);

            point = MathUtil.add(point, delta);
            ltDX = new LayoutTurnout("Double XOver",
                    LayoutTurnout.DOUBLE_XOVER, point, 132.0, 1.7, 1.8, layoutEditor);

            point = MathUtil.add(point, delta);
            ltRX = new LayoutTurnout("Right Hand XOver",
                    LayoutTurnout.RH_XOVER, point, 165.0, 1.9, 2.0, layoutEditor);

            point = MathUtil.add(point, delta);
            ltLX = new LayoutTurnout("Left Hand XOver",
                    LayoutTurnout.LH_XOVER, point, 198.0, 2.1, 2.2, layoutEditor);
        }
    }

    @After
    public void tearDown() throws Exception {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        ltRH = null;
        ltLH = null;
        ltWY = null;
        ltDX = null;
        ltRX = null;
        ltLX = null;
        // reset the instance manager.
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutSlipTest.class);
}
