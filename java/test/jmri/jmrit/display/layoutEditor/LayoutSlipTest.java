package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of LayoutSlip
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutSlipTest {

    private LayoutEditor layoutEditor = null;
    private LayoutSlip lts = null;
    private LayoutSlip ltd = null;

    @Test
    public void testNew() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor not null", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);
    }

    @Test
    public void testToString() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        String ltsString = lts.toString();
        Assert.assertNotNull("ltsString not null", ltsString);
        Assert.assertEquals(ltsString, "LayoutSlip single");
        Assert.assertEquals(lts.getDisplayName(), "Slip single");

        String ltdString = ltd.toString();
        Assert.assertNotNull("ltdString not null", ltdString);
        Assert.assertEquals(ltdString, "LayoutSlip double");
        Assert.assertEquals(ltd.getDisplayName(), "Slip double");
    }

    @Test
    public void testGetDisplayName() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals(lts.getDisplayName(), "Slip single");
        Assert.assertEquals(ltd.getDisplayName(), "Slip double");
    }

    @Test
    public void testSlipTypeAndState() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("lts.getSlipType() is SINGLE_SLIP", lts.getSlipType() == LayoutTurnout.SINGLE_SLIP);
        Assert.assertTrue("lts.getSlipState() is UNKNOWN", lts.getSlipState() == LayoutTurnout.UNKNOWN);

        Assert.assertTrue("ltd.getSlipType() is DOUBLE_SLIP", ltd.getSlipType() == LayoutTurnout.DOUBLE_SLIP);
        Assert.assertTrue("ltd.getSlipState() is UNKNOWN", ltd.getSlipState() == LayoutTurnout.UNKNOWN);
    }

    @Test
    public void testTurnoutB() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue("lts.getTurnoutBName() is ''", lts.getTurnoutBName() == "");
        Assert.assertNull("lts.getTurnoutB() is null", lts.getTurnoutB());

        Assert.assertTrue("ltd.getTurnoutBName() is ''", ltd.getTurnoutBName() == "");
        Assert.assertNull("ltd.getTurnoutB() is null", ltd.getTurnoutB());
    }

    @Test
    public void testGetConnectionTypes() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        try {
            Assert.assertNull("lts.getConnectionType(SLIP_A) is null", lts.getConnection(LayoutTrack.SLIP_A));
            Assert.assertNull("lts.getConnectionType(SLIP_B) is null", lts.getConnection(LayoutTrack.SLIP_B));
            Assert.assertNull("lts.getConnectionType(SLIP_C) is null", lts.getConnection(LayoutTrack.SLIP_C));
            Assert.assertNull("lts.getConnectionType(SLIP_D) is null", lts.getConnection(LayoutTrack.SLIP_D));

            Assert.assertNull("ltd.getConnectionType(SLIP_A) is null", ltd.getConnection(LayoutTrack.SLIP_A));
            Assert.assertNull("ltd.getConnectionType(SLIP_B) is null", ltd.getConnection(LayoutTrack.SLIP_B));
            Assert.assertNull("ltd.getConnectionType(SLIP_C) is null", ltd.getConnection(LayoutTrack.SLIP_C));
            Assert.assertNull("ltd.getConnectionType(SLIP_D) is null", ltd.getConnection(LayoutTrack.SLIP_D));
        } catch (jmri.JmriException e) {
            Assert.fail("lts.getConnectionType(SLIP_D): Unexpected exception thrown");
        } // OK
    }

    @Test
    public void testGetConnectionTypesFail() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
    public void testActivateTurnout() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    public void testDeactivateTurnout() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    public void testGetCoordsForConnectionType() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("lts.isMainline() false", lts.isMainline());
        Assert.assertFalse("ltd.isMainline() false", ltd.isMainline());
    }

    @Test
    public void testSetCoordsCenter() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        Point2D newCenterPoint = new Point2D.Double(75.0, 150.0);
        lts.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("lts.getCoordsCenter ", newCenterPoint, lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(146.21320343559643, 129.79184719828692),
                lts.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(140.55634918610403, 143.93398282201787),
                lts.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(3.7867965644035877, 170.20815280171308),
                lts.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(9.443650813895971, 156.06601717798213),
                lts.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter() is equal to...",
                new Point2D.Double(75.0, 150.0),
                lts.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft() is equal to...",
                new Point2D.Double(86.7845192296798, 147.7361301880835),
                lts.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(LayoutTrack.SLIP_RIGHT));

        newCenterPoint = new Point2D.Double(150.0, 75.0);
        ltd.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("ltd.getCoordsCenter ", newCenterPoint, ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(129.79184719828692, 174.49747468305833),
                ltd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(143.93398282201787, 180.1543289325507),
                ltd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(170.20815280171308, -24.49747468305833),
                ltd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(156.06601717798213, -30.154328932550698),
                ltd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(150.0, 75.0),
                ltd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft() is equal to...",
                new Point2D.Double(148.4719249274215, 86.90231013595951),
                ltd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsRight() is equal to...",
                new Point2D.Double(151.52807507257856, 63.09768986404051),
                ltd.getCoordsRight());
    }

    @Test
    public void testScaleCoords() {
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
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
        apps.tests.Log4JFixture.setUp();
        // dispose of the single PanelMenu instance
        jmri.jmrit.display.PanelMenu.dispose();
        // reset the instance manager.
        jmri.util.JUnitUtil.resetInstanceManager();
        ///DOMConfigurator.configure("myapp-log4j.xml");
        apps.tests.Log4JFixture.initLogging();

//        Assert.assertFalse(GraphicsEnvironment.isHeadless());
        layoutEditor = new LayoutEditor();
        lts = new LayoutSlip("single", new Point2D.Double(50.0, 100.0), +45.0, layoutEditor, LayoutTurnout.SINGLE_SLIP);
        ltd = new LayoutSlip("double", new Point2D.Double(100.0, 50.0), -45.0, layoutEditor, LayoutTurnout.DOUBLE_SLIP);
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
    //static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutSlipTest.class.getName());
    private final static Logger log = LoggerFactory.getLogger(LayoutSlipTest.class.getName());
}
