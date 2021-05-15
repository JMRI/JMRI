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
        } catch (IllegalArgumentException e) {
            JUnitAppender.assertErrorMessage("will throw single.getConnection(NONE); Invalid Connection Type");
        }
        try {
            // this should throw up (NONE is not a valid connection type)
            Assert.assertNull("ltd.getConnectionType(NONE) is null", ltd.getConnection(HitPointType.NONE));
            Assert.fail("ltd.getConnectionType(NONE): No exception thrown");
        } catch (IllegalArgumentException e) {
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
    public void testIsMainline() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertFalse("lts.isMainline() false", lts.isMainline());
        Assert.assertFalse("ltd.isMainline() false", ltd.isMainline());
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
        } catch (IllegalArgumentException ex) {
        }
        JUnitAppender.assertErrorMessage("will throw single.getConnection(NONE); Invalid Connection Type");

        try {
            // test Invalid Connection Type
            Assert.assertNull("ltd.getConnection(invalid type) is null",
                    ltd.getConnection(HitPointType.NONE));
            Assert.fail("No exception thrown on ltd.getConnection(invalid type)");
        } catch (IllegalArgumentException ex) {
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
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on lts.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("ltd.getConnection(valid type) is null",
                    ltd.getConnection(HitPointType.SLIP_B));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on ltd.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("lts.getConnection(valid type) is null",
                    lts.getConnection(HitPointType.SLIP_C));
        } catch (IllegalArgumentException ex) {
            Assert.fail("Exception thrown on lts.getConnection(valid type)");
        }
        try {
            // test valid connection type (null value)
            Assert.assertNull("ltd.getConnection(valid type) is null",
                    ltd.getConnection(HitPointType.SLIP_D));
        } catch (IllegalArgumentException ex) {
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


    // from here down is testing infrastructure
    private static LayoutEditor layoutEditor = null;
    private LayoutSingleSlip lts = null;
    private LayoutDoubleSlip ltd = null;
    private LayoutSingleSlipView lvs = null;
    private LayoutDoubleSlipView lvd = null;

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
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            lts = new LayoutSingleSlip("single", layoutEditor);
            lvs = new LayoutSingleSlipView(lts, new Point2D.Double(50.0, 100.0), +45.0, layoutEditor);
            layoutEditor.addLayoutTrack(lts, lvs);

            ltd = new LayoutDoubleSlip("double", layoutEditor);
            lvd = new LayoutDoubleSlipView(ltd, new Point2D.Double(100.0, 50.0), -45.0, layoutEditor);
            layoutEditor.addLayoutTrack(ltd, lvd);
        }
    }

    @AfterEach
    public void tearDown() {
        if (lts != null) {
            lts.remove();
            lts = null;
        }
        if (lvs != null) {
            lvs.dispose();
            lvs = null;
        }
        if (ltd != null) {
            ltd.remove();
            ltd = null;
        }
        if (lvd != null) {
            lvd.dispose();
            lvd = null;
        }
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipTest.class);
}
