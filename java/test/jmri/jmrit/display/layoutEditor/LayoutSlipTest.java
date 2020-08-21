package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import jmri.JmriException;
import jmri.util.*;
import jmri.util.junit.annotations.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutSlip
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutSlipTest {

    private static LayoutEditor layoutEditor = null;
    private LayoutSlip lts = null;
    private LayoutSlip ltd = null;

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        String ltsString = lts.toString();
        Assert.assertNotNull("ltsString not null", ltsString);
        Assert.assertEquals("LayoutSlip single (Unknown)", ltsString);

        String ltdString = ltd.toString();
        Assert.assertNotNull("ltdString not null", ltdString);
        Assert.assertEquals("LayoutSlip double (Unknown)", ltdString);
    }

    @Test
    public void testGetDisplayName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertEquals("Slip single", lts.getDisplayName());
        Assert.assertEquals("Slip double", ltd.getDisplayName());
    }

    @Test
    public void testGetSlipType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertTrue("lts.getSlipType() is SINGLE_SLIP", lts.getSlipType() == LayoutTurnout.TurnoutType.SINGLE_SLIP);
        Assert.assertTrue("ltd.getSlipType() is DOUBLE_SLIP", ltd.getSlipType() == LayoutTurnout.TurnoutType.DOUBLE_SLIP);
    }

    @Test
    public void testGetSlipState() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertTrue("lts.getSlipState() is UNKNOWN", lts.getSlipState() == LayoutTurnout.UNKNOWN);
        Assert.assertTrue("ltd.getSlipState() is UNKNOWN", ltd.getSlipState() == LayoutTurnout.UNKNOWN);
    }

    @Test
    public void testTurnoutB() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertTrue("lts.getTurnoutBName() is ''", lts.getTurnoutBName() == "");
        Assert.assertNull("lts.getTurnoutB() is null", lts.getTurnoutB());

        Assert.assertTrue("ltd.getTurnoutBName() is ''", ltd.getTurnoutBName() == "");
        Assert.assertNull("ltd.getTurnoutB() is null", ltd.getTurnoutB());
    }

    @Test
    public void testGetConnectionTypes() throws jmri.JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertNull("lts.getConnectionType(SLIP_A) is null", lts.getConnection(HitPointType.SLIP_A));
        Assert.assertNull("lts.getConnectionType(SLIP_B) is null", lts.getConnection(HitPointType.SLIP_B));
        Assert.assertNull("lts.getConnectionType(SLIP_C) is null", lts.getConnection(HitPointType.SLIP_C));
        Assert.assertNull("lts.getConnectionType(SLIP_D) is null", lts.getConnection(HitPointType.SLIP_D));

        Assert.assertNull("ltd.getConnectionType(SLIP_A) is null", ltd.getConnection(HitPointType.SLIP_A));
        Assert.assertNull("ltd.getConnectionType(SLIP_B) is null", ltd.getConnection(HitPointType.SLIP_B));
        Assert.assertNull("ltd.getConnectionType(SLIP_C) is null", ltd.getConnection(HitPointType.SLIP_C));
        Assert.assertNull("ltd.getConnectionType(SLIP_D) is null", ltd.getConnection(HitPointType.SLIP_D));
    }

    @Test
    public void testGetConnectionTypesFail() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        try {
            // this should throw up (NONE is not a valid connection type)
            Assert.assertNull("lts.getConnectionType(NONE) is null", lts.getConnection(HitPointType.NONE));
            Assert.fail("lts.getConnectionType(NONE): No exception thrown");
        } catch (jmri.JmriException e) {
            JUnitAppender.assertErrorMessage("will throw single.getConnection(NONE); Invalid Connection Type");
        }
        try {
            // this should throw up (NONE is not a valid connection type)
            Assert.assertNull("ltd.getConnectionType(NONE) is null", ltd.getConnection(HitPointType.NONE));
            Assert.fail("ltd.getConnectionType(NONE): No exception thrown");
        } catch (jmri.JmriException e) {
            JUnitAppender.assertErrorMessage("will throw double.getConnection(NONE); Invalid Connection Type");
        } // OK
    }

    @Test
    public void testSlipState() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertTrue("Single slip state unknown", lts.getSlipState() == LayoutTurnout.UNKNOWN);
        lts.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);
        lts.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_BD", lts.getSlipState() == LayoutTurnout.STATE_BD);
        lts.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_AD", lts.getSlipState() == LayoutTurnout.STATE_AD);
        lts.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);
        lts.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Single slip state STATE_BD", lts.getSlipState() == LayoutTurnout.STATE_BD);
        lts.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Single slip state STATE_AC", lts.getSlipState() == LayoutTurnout.STATE_AC);

        Assert.assertTrue("Double slip state unknown", ltd.getSlipState() == LayoutTurnout.UNKNOWN);
        ltd.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_BC", ltd.getSlipState() == LayoutTurnout.STATE_BC);
        ltd.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AD", ltd.getSlipState() == LayoutTurnout.STATE_AD);
        ltd.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AC", ltd.getSlipState() == LayoutTurnout.STATE_AC);
        ltd.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_AD", ltd.getSlipState() == LayoutTurnout.STATE_AD);
        ltd.toggleState(HitPointType.SLIP_LEFT);
        Assert.assertTrue("Double slip state STATE_BD", ltd.getSlipState() == LayoutTurnout.STATE_BD);
        ltd.toggleState(HitPointType.SLIP_RIGHT);
        Assert.assertTrue("Double slip state STATE_BC", ltd.getSlipState() == LayoutTurnout.STATE_BC);
    }

    @Test
    @Disabled("No Test yet")
    @ToDo("finish initialization of test and write code to test activation of turnouts")
    public void testActivateTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    @Disabled("No Test yet")
    @ToDo("finish initialization of test and write code to test deactivation of turnouts")
    public void testDeactivateTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        // nothing to do here until we've assigned physical turnouts
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertEquals("lts.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(50.0, 100.0),
                lts.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("single.getCoordsForConnectionType(NONE); Invalid Connection Type");

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(35.85786437626905, 85.85786437626905),
                lts.getCoordsForConnectionType(HitPointType.SLIP_A));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(30.20101012677667, 100.0),
                lts.getCoordsForConnectionType(HitPointType.SLIP_B));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(64.14213562373095, 114.14213562373095),
                lts.getCoordsForConnectionType(HitPointType.SLIP_C));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(69.79898987322333, 100.0),
                lts.getCoordsForConnectionType(HitPointType.SLIP_D));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(38.92307692307692, 95.38461538461539),
                lts.getCoordsForConnectionType(HitPointType.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(100.0, 50.0),
                ltd.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("double.getCoordsForConnectionType(NONE); Invalid Connection Type");

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(85.85786437626905, 64.14213562373095),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_A));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(100.0, 69.79898987322332),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_B));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(114.14213562373095, 35.85786437626905),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_C));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(100.0, 30.201010126776673),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_D));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(95.38461538461539, 61.07692307692308),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                ltd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertEquals("lts.getBounds() is equal to...",
                new Rectangle2D.Double(30.20101012677667, 85.85786437626905, 39.59797974644667, 28.284271247461902),
                lts.getBounds());
        ltd.getBounds();
        Assert.assertEquals("ltd.getBounds() is equal to...",
                new Rectangle2D.Double(30.20101012677667, 85.85786437626905, 39.59797974644667, 28.284271247461902),
                lts.getBounds());

    }

    @Test
    public void testIsMainline() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertFalse("lts.isMainline() false", lts.isMainline());
        Assert.assertFalse("ltd.isMainline() false", ltd.isMainline());
    }

    @Test
    public void testSetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

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
                ltd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

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
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

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
                ltd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

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
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

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
                ltd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

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

    @Test
    public void testGetConnectionInvalid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        try {
            // test Invalid Connection Type
            Assert.assertNull("lts.getConnection(invalid type) is null",
                    lts.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on lts.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw single.getConnection(NONE); Invalid Connection Type");

        try {
            // test Invalid Connection Type
            Assert.assertNull("ltd.getConnection(invalid type) is null",
                    ltd.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltd.getConnection(invalid type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw double.getConnection(NONE); Invalid Connection Type");
    }

    @Test
    public void testGetConnectionValid() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        try {
            // test valid connection type (null value)
            Assert.assertNull("lts.getConnection(valid type) is null",
                    lts.getConnection(HitPointType.SLIP_A));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on lts.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("ltd.getConnection(valid type) is null",
                    ltd.getConnection(HitPointType.SLIP_B));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltd.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("lts.getConnection(valid type) is null",
                    lts.getConnection(HitPointType.SLIP_C));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on lts.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("ltd.getConnection(valid type) is null",
                    ltd.getConnection(HitPointType.SLIP_D));
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltd.getConnection(valid type)");
        }
    }

    @Test
    public void testSetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        try {
            // test Invalid Connection Type
            lts.setConnection(HitPointType.NONE, null, HitPointType.NONE);
            Assert.fail("No exception thrown on lts.setConnection(Invalid Connection Type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw single.setConnection(NONE, null, NONE); Invalid Connection Type");

        try {
            // test Invalid Connection Type
            ltd.setConnection(HitPointType.NONE, null, HitPointType.NONE);
            Assert.fail("No exception thrown on ltd.setConnection(Invalid Connection Type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw double.setConnection(NONE, null, NONE); Invalid Connection Type");

        try {
            // test invalid object type
            lts.setConnection(HitPointType.SLIP_A, null, HitPointType.POS_POINT);
            Assert.fail("No exception thrown on lts.setConnection(invalid object type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw single.setConnection(SLIP_A, null, POS_POINT); Invalid type");
        try {
            // test invalid object type
            ltd.setConnection(HitPointType.SLIP_B, null, HitPointType.POS_POINT);
            Assert.fail("No exception thrown on ltd.setConnection(invalid object type)");
        } catch (JmriException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw double.setConnection(SLIP_B, null, POS_POINT); Invalid type");

        try {
            // test valid types
            lts.setConnection(HitPointType.SLIP_C, null, HitPointType.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on lts.setConnection(valid types)");
        }
        try {
            // test valid types
            ltd.setConnection(HitPointType.SLIP_D, null, HitPointType.NONE);
        } catch (JmriException ex) {
            Assert.fail("Exception thrown on ltd.setConnection(valid types)");
        }
    }

    @Test
    public void testFindHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        // First, try miss
        HitPointType hitType = lts.findHitPointType(MathUtil.zeroPoint2D, true, false);
        Assert.assertTrue("lts.findHitPointType equals NONE", hitType == HitPointType.NONE);

        // now try hit getCoordsLeft -> SLIP_LEFT
        hitType = lts.findHitPointType(lts.getCoordsLeft(), true, false);
        Assert.assertTrue("lts.findHitPointType equals SLIP_LEFT", hitType == HitPointType.SLIP_LEFT);

        // now try hit getCoordsRight -> SLIP_RIGHT
        hitType = lts.findHitPointType(lts.getCoordsRight(), false, false);
        Assert.assertTrue("lts.findHitPointType equals SLIP_RIGHT", hitType == HitPointType.SLIP_RIGHT);

        // now try hit getCoordsA -> SLIP_A
        hitType = lts.findHitPointType(lts.getCoordsA(), false, true);
        Assert.assertTrue("lts.findHitPointType equals SLIP_A", hitType == HitPointType.SLIP_A);

        // now try hit getCoordsB -> SLIP_B
        hitType = lts.findHitPointType(lts.getCoordsB(), false, true);
        Assert.assertTrue("lts.findHitPointType equals SLIP_B", hitType == HitPointType.SLIP_B);

        // now try hit getCoordsC -> SLIP_C
        hitType = lts.findHitPointType(lts.getCoordsC(), false, true);
        Assert.assertTrue("lts.findHitPointType equals SLIP_C", hitType == HitPointType.SLIP_C);

        // now try hit getCoordsD -> SLIP_D
        hitType = lts.findHitPointType(lts.getCoordsD(), false, true);
        Assert.assertTrue("lts.findHitPointType equals SLIP_D", hitType == HitPointType.SLIP_D);
    }

    // from here down is testing infrastructure
    @BeforeAll
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            layoutEditor = new LayoutEditor();
        }
    }

    @AfterAll
    public static void afterClass() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            lts = new LayoutSingleSlip("single", new Point2D.Double(50.0, 100.0), +45.0, layoutEditor);
            ltd = new LayoutDoubleSlip("double", new Point2D.Double(100.0, 50.0), -45.0, layoutEditor);
        }
    }

    @AfterEach
    public void tearDown() {
        if (lts != null) {
            lts.remove();
            lts.dispose();
            lts = null;
        }
        if (ltd != null) {
            ltd.remove();
            ltd.dispose();
            ltd = null;
        }
    }
    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipTest.class);
}
