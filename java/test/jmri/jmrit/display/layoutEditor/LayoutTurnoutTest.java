package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of LayoutTurnout
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutTurnoutTest {

    LayoutEditor layoutEditor = null;
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
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("ltRH.getBounds() is equal to...",
                new Rectangle2D.Double(132.0, 87.0, 36.0, 36.0),
                ltRH.getBounds());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           layoutEditor = new LayoutEditor();
           ltRH = new LayoutTurnout("Right Hand",
                LayoutTurnout.RH_TURNOUT, new Point2D.Double(150.0, 100.0),
                33.0, 1.1, 1.2, layoutEditor);

           ltLH = new LayoutTurnout("Left Hand",
                LayoutTurnout.LH_TURNOUT, new Point2D.Double(200.0, 100.0),
                66.0, 1.3, 1.4, layoutEditor);

           ltWY = new LayoutTurnout("Wye",
                LayoutTurnout.WYE_TURNOUT, new Point2D.Double(250.0, 100.0),
                99.0, 1.5, 1.6, layoutEditor);

           ltDX = new LayoutTurnout("Double XOver",
                LayoutTurnout.DOUBLE_XOVER, new Point2D.Double(300.0, 100.0),
                132.0, 1.7, 1.8, layoutEditor);

           ltRX = new LayoutTurnout("Right Hand XOver",
                LayoutTurnout.RH_XOVER, new Point2D.Double(350.0, 100.0),
                165.0, 1.9, 2.0, layoutEditor);

           ltLX = new LayoutTurnout("Left Hand XOver",
                LayoutTurnout.LH_XOVER, new Point2D.Double(400.0, 100.0),
                198.0, 2.1, 2.2, layoutEditor);
       }
    }

    @After
    public void tearDown() throws Exception {
        if(layoutEditor!=null) {
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
    private final static Logger log = LoggerFactory.getLogger(LayoutSlipTest.class.getName());
}
