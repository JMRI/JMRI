package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of LayoutSlip
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutSlipTest {

    LayoutEditor layoutEditor = null;
    LayoutSlip lts = null;
    LayoutSlip ltd = null;

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        String ltsString = lts.toString();
        Assert.assertNotNull("ltsString not null", ltsString);
        Assert.assertEquals(ltsString, "LayoutSlip single");

        String ltdString = ltd.toString();
        Assert.assertNotNull("ltdString not null", ltdString);
        Assert.assertEquals(ltdString, "LayoutSlip double");
    }

    @Test
    public void testGetDisplayName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertEquals(lts.getDisplayName(), "Slip single");
        Assert.assertEquals(ltd.getDisplayName(), "Slip double");
    }

    @Test
    public void testSlipTypeAndState() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("lts.getSlipType() is SINGLE_SLIP", lts.getSlipType() == LayoutTurnout.SINGLE_SLIP);
        Assert.assertTrue("lts.getSlipState() is UNKNOWN", lts.getSlipState() == LayoutTurnout.UNKNOWN);

        Assert.assertTrue("ltd.getSlipType() is DOUBLE_SLIP", ltd.getSlipType() == LayoutTurnout.DOUBLE_SLIP);
        Assert.assertTrue("ltd.getSlipState() is UNKNOWN", ltd.getSlipState() == LayoutTurnout.UNKNOWN);
    }

    @Test
    public void testTurnoutB() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("lts.getTurnoutBName() is ''", lts.getTurnoutBName() == "");
        Assert.assertNull("lts.getTurnoutB() is null", lts.getTurnoutB());

        Assert.assertTrue("ltd.getTurnoutBName() is ''", ltd.getTurnoutBName() == "");
        Assert.assertNull("ltd.getTurnoutB() is null", ltd.getTurnoutB());
    }

    @Test
    public void testGetConnectionTypes() throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNull("lts.getConnectionType(SLIP_A) is null", lts.getConnection(LayoutTrack.SLIP_A));
        Assert.assertNull("lts.getConnectionType(SLIP_B) is null", lts.getConnection(LayoutTrack.SLIP_B));
        Assert.assertNull("lts.getConnectionType(SLIP_C) is null", lts.getConnection(LayoutTrack.SLIP_C));
        Assert.assertNull("lts.getConnectionType(SLIP_D) is null", lts.getConnection(LayoutTrack.SLIP_D));

        Assert.assertNull("ltd.getConnectionType(SLIP_A) is null", ltd.getConnection(LayoutTrack.SLIP_A));
        Assert.assertNull("ltd.getConnectionType(SLIP_B) is null", ltd.getConnection(LayoutTrack.SLIP_B));
        Assert.assertNull("ltd.getConnectionType(SLIP_C) is null", ltd.getConnection(LayoutTrack.SLIP_C));
        Assert.assertNull("ltd.getConnectionType(SLIP_D) is null", ltd.getConnection(LayoutTrack.SLIP_D));
    }

    @Test
    public void testGetConnectionTypesFail() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // this should throw up (SLIP_CENTER is not a valid connection type)
            Assert.assertNull("lts.getConnectionType(SLIP_CENTER) is null", lts.getConnection(LayoutTrack.SLIP_CENTER));
            Assert.fail("lts.getConnectionType(SLIP_CENTER): No exception thrown");
        } catch (jmri.JmriException e) {
            jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 20");
        }
        try {
            // this should throw up (SLIP_CENTER is not a valid connection type)
            Assert.assertNull("ltd.getConnectionType(SLIP_CENTER) is null", ltd.getConnection(LayoutTrack.SLIP_CENTER));
            Assert.fail("ltd.getConnectionType(SLIP_CENTER): No exception thrown");
        } catch (jmri.JmriException e) {
            jmri.util.JUnitAppender.assertErrorMessage("Invalid Connection Type 20");
        } // OK
    }

    @Test
    public void testSlipState() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("Single slip state unknown", lts.getSlipState() == LayoutTurnout.UNKNOWN);
        lts.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);
        lts.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_BD", lts.getSlipState() == LayoutTurnout.STATE_BD);
        lts.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_AD", lts.getSlipState() == LayoutTurnout.STATE_AD);
        lts.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);
        lts.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_BD", lts.getSlipState() == LayoutTurnout.STATE_BD);
        lts.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);

        Assert.assertTrue("Double slip state unknown", ltd.getSlipState() == LayoutTurnout.UNKNOWN);
        ltd.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_BC", ltd.getSlipState() == LayoutTurnout.STATE_BC);
        ltd.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AD", ltd.getSlipState() == LayoutTurnout.STATE_AD);
        ltd.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AD", ltd.getSlipState() == LayoutTurnout.STATE_AD);
        ltd.toggleState(LayoutTrack.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_BD", ltd.getSlipState() == LayoutTurnout.STATE_BD);
        ltd.toggleState(LayoutTrack.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_BC", ltd.getSlipState() == LayoutTurnout.STATE_BC);
    }

    @Test
    @Ignore("No Test yet")
    public void testActivateTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    @Ignore("No Test yet")
    public void testDeactivateTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertEquals("lts.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(50.0, 100.0),
                lts.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(35.85786437626905, 85.85786437626905),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_A));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(30.20101012677667, 100.0),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_B));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(64.14213562373095, 114.14213562373095),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_C));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(69.79898987322333, 100.0),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_D));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_CENTER) is equal to...",
                new Point2D.Double(50.0, 100.0),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_CENTER));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(38.92307692307692, 95.38461538461539),
                lts.getCoordsForConnectionType(LayoutTrack.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(100.0, 50.0),
                ltd.getCoordsForConnectionType(LayoutTrack.NONE));
        jmri.util.JUnitAppender.assertErrorMessage("Invalid connection type 0");

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(85.85786437626905, 64.14213562373095),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_A));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(100.0, 69.79898987322332),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_B));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(114.14213562373095, 35.85786437626905),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_C));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(100.0, 30.201010126776673),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_D));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_CENTER) is equal to...",
                new Point2D.Double(100.0, 50.0),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_CENTER));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(95.38461538461539, 61.07692307692308),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertEquals("lts.getBounds() is equal to...",
                new Rectangle2D.Double(30.20101012677667, 85.85786437626905, 39.59797974644667, 28.284271247461902),
                lts.getBounds());
        Rectangle2D b = ltd.getBounds();
        Assert.assertEquals("ltd.getBounds() is equal to...",
                new Rectangle2D.Double(30.20101012677667, 85.85786437626905, 39.59797974644667, 28.284271247461902),
                lts.getBounds());

    }

    @Test
    public void testIsMainline() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("lts.isMainline() false", lts.isMainline());
        Assert.assertFalse("ltd.isMainline() false", ltd.isMainline());
    }

    @Test
    public void testSetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Point2D newCenterPoint = new Point2D.Double(75.0, 150.0);
        lts.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("lts.getCoordsCenter ", newCenterPoint, lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(60.85786437626905, 135.85786437626905),
                lts.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(55.20101012677667, 150.0),
                lts.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(89.14213562373095, 164.14213562373095),
                lts.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(94.79898987322332, 150.0),
                lts.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter() is equal to...",
                new Point2D.Double(75.0, 150.0),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft() is equal to...",
                new Point2D.Double(63.92307692307692, 145.3846153846154),
                lts.getCoordsLeft());

        newCenterPoint = new Point2D.Double(150.0, 75.0);
        ltd.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(154.6153846153846, 63.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));

        Assert.assertEquals("ltd.getCoordsCenter ", newCenterPoint, ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(135.85786437626905, 89.14213562373095),
                ltd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(150.0, 94.79898987322332),
                ltd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(164.14213562373095, 60.85786437626905),
                ltd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(150.0, 55.20101012677667),
                ltd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(150.0, 75.0),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft() is equal to...",
                new Point2D.Double(145.3846153846154, 86.07692307692308),
                ltd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsRight() is equal to...",
                new Point2D.Double(154.6153846153846, 63.92307692307692),
                ltd.getCoordsRight());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        lts.scaleCoords(1.5F, 2.5F);
        Assert.assertEquals("lts.getCoordsCenter ",
                new Point2D.Double(75.0, 250.0),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(54.0, 215.0),
                lts.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(45.0, 250.0),
                lts.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(96.0, 285.0),
                lts.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(105.0, 250.0),
                lts.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter is equal to...",
                new Point2D.Double(75.0, 250.0),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft is equal to...",
                new Point2D.Double(65.10583976827436, 243.20989003705103),
                lts.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));

        ltd.scaleCoords(2.5F, 1.5F);
        Assert.assertEquals("ltd.getCoordsCenter ",
                new Point2D.Double(250.0, 75.0),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(215.0, 96.0),
                ltd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(250.0, 105.0),
                ltd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(285.0, 54.0),
                ltd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(250.0, 45.0),
                ltd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(250.0, 75.0),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft is equal to...",
                new Point2D.Double(243.20989003705103, 84.89416023172564),
                ltd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(256.790109962949, 65.10583976827436),
                ltd.getCoordsRight());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        lts.translateCoords(15.5F, 25.5F);
        Assert.assertEquals("lts.getCoordsCenter ",
                new Point2D.Double(65.5, 125.5),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(51.35786437626905, 111.35786437626905),
                lts.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(45.701010126776666, 125.5),
                lts.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(79.64213562373095, 139.64213562373095),
                lts.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(85.29898987322333, 125.5),
                lts.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter is equal to...",
                new Point2D.Double(65.5, 125.5),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft is equal to...",
                new Point2D.Double(54.42307692307692, 120.88461538461539),
                lts.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));

        ltd.translateCoords(25.5F, 15.5F);
        Assert.assertEquals("ltd.getCoordsCenter ",
                new Point2D.Double(125.5, 65.5),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(111.35786437626905, 79.64213562373095),
                ltd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(125.5, 85.29898987322332),
                ltd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(139.64213562373095, 51.35786437626905),
                ltd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(125.5, 45.70101012677667),
                ltd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(125.5, 65.5),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft is equal to...",
                new Point2D.Double(120.88461538461539, 76.57692307692308),
                ltd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(130.1153846153846, 54.42307692307692),
                ltd.getCoordsRight());
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           layoutEditor = new LayoutEditor();
           lts = new LayoutSlip("single", new Point2D.Double(50.0, 100.0), +45.0, layoutEditor, LayoutTurnout.SINGLE_SLIP);
           ltd = new LayoutSlip("double", new Point2D.Double(100.0, 50.0), -45.0, layoutEditor, LayoutTurnout.DOUBLE_SLIP);
        }
    }

    @After
    public void tearDown() throws Exception {
        // do this to dispose of the sensor, signal and icon frames
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        lts = null;
        ltd = null;

        // reset the instance manager.
        JUnitUtil.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(LayoutSlipTest.class.getName());
}
