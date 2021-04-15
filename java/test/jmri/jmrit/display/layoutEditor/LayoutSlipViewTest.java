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
 * Test simple functioning of LayoutSlipView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSlipViewTest extends LayoutTurnoutViewTest {

    @Test
    public void testSetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Point2D newCenterPoint = new Point2D.Double(75.0, 150.0);
        lvs.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("lts.getCoordsCenter ", newCenterPoint, lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(60.85786437626905, 135.85786437626905),
                lvs.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(55.20101012677667, 150.0),
                lvs.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(89.14213562373095, 164.14213562373095),
                lvs.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(94.79898987322332, 150.0),
                lvs.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter() is equal to...",
                new Point2D.Double(75.0, 150.0),
                lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft() is equal to...",
                new Point2D.Double(63.92307692307692, 145.3846153846154),
                lvs.getCoordsLeft());

        newCenterPoint = new Point2D.Double(150.0, 75.0);
        lvd.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(154.6153846153846, 63.92307692307692),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

        Assert.assertEquals("ltd.getCoordsCenter ", newCenterPoint, lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(135.85786437626905, 89.14213562373095),
                lvd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(150.0, 94.79898987322332),
                lvd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(164.14213562373095, 60.85786437626905),
                lvd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(150.0, 55.20101012677667),
                lvd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(150.0, 75.0),
                lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft() is equal to...",
                new Point2D.Double(145.3846153846154, 86.07692307692308),
                lvd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsRight() is equal to...",
                new Point2D.Double(154.6153846153846, 63.92307692307692),
                lvd.getCoordsRight());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        lvs.scaleCoords(1.5F, 2.5F);
        Assert.assertEquals("lts.getCoordsCenter ",
                new Point2D.Double(75.0, 250.0),
                lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(54.0, 215.0),
                lvs.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(45.0, 250.0),
                lvs.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(96.0, 285.0),
                lvs.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(105.0, 250.0),
                lvs.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter is equal to...",
                new Point2D.Double(75.0, 250.0),
                lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft is equal to...",
                new Point2D.Double(65.10583976827436, 243.20989003705103),
                lvs.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

        lvd.scaleCoords(2.5F, 1.5F);
        Assert.assertEquals("ltd.getCoordsCenter ",
                new Point2D.Double(250.0, 75.0),
                lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(215.0, 96.0),
                lvd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(250.0, 105.0),
                lvd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(285.0, 54.0),
                lvd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(250.0, 45.0),
                lvd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(250.0, 75.0),
                lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft is equal to...",
                new Point2D.Double(243.20989003705103, 84.89416023172564),
                lvd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(256.790109962949, 65.10583976827436),
                lvd.getCoordsRight());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);
        Assert.assertNotNull("LayoutSlipView single not null", lvs);
        Assert.assertNotNull("LayoutSlipView double not null", lvd);

        lvs.translateCoords(15.5F, 25.5F);
        Assert.assertEquals("lts.getCoordsCenter ",
                new Point2D.Double(65.5, 125.5),
                lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsA() is equal to...",
                new Point2D.Double(51.35786437626905, 111.35786437626905),
                lvs.getCoordsA());

        Assert.assertEquals("lts.getCoordsB() is equal to...",
                new Point2D.Double(45.701010126776666, 125.5),
                lvs.getCoordsB());

        Assert.assertEquals("lts.getCoordsC() is equal to...",
                new Point2D.Double(79.64213562373095, 139.64213562373095),
                lvs.getCoordsC());

        Assert.assertEquals("lts.getCoordsD() is equal to...",
                new Point2D.Double(85.29898987322333, 125.5),
                lvs.getCoordsD());

        Assert.assertEquals("lts.getCoordsCenter is equal to...",
                new Point2D.Double(65.5, 125.5),
                lvs.getCoordsCenter());

        Assert.assertEquals("lts.getCoordsLeft is equal to...",
                new Point2D.Double(54.42307692307692, 120.88461538461539),
                lvs.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

        lvd.translateCoords(25.5F, 15.5F);
        Assert.assertEquals("ltd.getCoordsCenter ",
                new Point2D.Double(125.5, 65.5),
                lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsA() is equal to...",
                new Point2D.Double(111.35786437626905, 79.64213562373095),
                lvd.getCoordsA());

        Assert.assertEquals("ltd.getCoordsB() is equal to...",
                new Point2D.Double(125.5, 85.29898987322332),
                lvd.getCoordsB());

        Assert.assertEquals("ltd.getCoordsC() is equal to...",
                new Point2D.Double(139.64213562373095, 51.35786437626905),
                lvd.getCoordsC());

        Assert.assertEquals("ltd.getCoordsD() is equal to...",
                new Point2D.Double(125.5, 45.70101012677667),
                lvd.getCoordsD());

        Assert.assertEquals("ltd.getCoordsCenter is equal to...",
                new Point2D.Double(125.5, 65.5),
                lvd.getCoordsCenter());

        Assert.assertEquals("ltd.getCoordsLeft is equal to...",
                new Point2D.Double(120.88461538461539, 76.57692307692308),
                lvd.getCoordsLeft());

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(130.1153846153846, 54.42307692307692),
                lvd.getCoordsRight());
    }


    @Test
    public void testFindHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);
        Assert.assertNotNull("LayoutSlipView single not null", lvs);
        Assert.assertNotNull("LayoutSlipView double not null", lvd);

        // First, try miss
        HitPointType hitType = lvs.findHitPointType(MathUtil.zeroPoint2D, true, false);
        Assert.assertTrue("lvs.findHitPointType equals NONE", hitType == HitPointType.NONE);

        // now try hit getCoordsLeft -> SLIP_LEFT
        hitType = lvs.findHitPointType(lvs.getCoordsLeft(), true, false);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_LEFT", hitType == HitPointType.SLIP_LEFT);

        // now try hit getCoordsRight -> SLIP_RIGHT
        hitType = lvs.findHitPointType(lvs.getCoordsRight(), false, false);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_RIGHT", hitType == HitPointType.SLIP_RIGHT);

        // now try hit getCoordsA -> SLIP_A
        hitType = lvs.findHitPointType(lvs.getCoordsA(), false, true);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_A", hitType == HitPointType.SLIP_A);

        // now try hit getCoordsB -> SLIP_B
        hitType = lvs.findHitPointType(lvs.getCoordsB(), false, true);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_B", hitType == HitPointType.SLIP_B);

        // now try hit getCoordsC -> SLIP_C
        hitType = lvs.findHitPointType(lvs.getCoordsC(), false, true);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_C", hitType == HitPointType.SLIP_C);

        // now try hit getCoordsD -> SLIP_D
        hitType = lvs.findHitPointType(lvs.getCoordsD(), false, true);
        Assert.assertTrue("lvs.findHitPointType equals SLIP_D", hitType == HitPointType.SLIP_D);
    }
    

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);

        Assert.assertEquals("lts.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(50.0, 100.0),
                lvs.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("single.getCoordsForConnectionType(NONE); Invalid Connection Type");

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(35.85786437626905, 85.85786437626905),
                lvs.getCoordsForConnectionType(HitPointType.SLIP_A));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(30.20101012677667, 100.0),
                lvs.getCoordsForConnectionType(HitPointType.SLIP_B));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(64.14213562373095, 114.14213562373095),
                lvs.getCoordsForConnectionType(HitPointType.SLIP_C));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(69.79898987322333, 100.0),
                lvs.getCoordsForConnectionType(HitPointType.SLIP_D));

        Assert.assertEquals("lts.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(38.92307692307692, 95.38461538461539),
                lvs.getCoordsForConnectionType(HitPointType.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(100.0, 50.0),
                lvd.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("double.getCoordsForConnectionType(NONE); Invalid Connection Type");

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_A) is equal to...",
                new Point2D.Double(85.85786437626905, 64.14213562373095),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_A));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_B) is equal to...",
                new Point2D.Double(100.0, 69.79898987322332),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_B));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_C) is equal to...",
                new Point2D.Double(114.14213562373095, 35.85786437626905),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_C));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_D) is equal to...",
                new Point2D.Double(100.0, 30.201010126776673),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_D));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_LEFT) is equal to...",
                new Point2D.Double(95.38461538461539, 61.07692307692308),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_LEFT));

        Assert.assertEquals("ltd.getCoordsForConnectionType(SLIP_RIGHT) is equal to...",
                new Point2D.Double(104.61538461538461, 38.92307692307692),
                lvd.getCoordsForConnectionType(HitPointType.SLIP_RIGHT));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutSlip single not null", lts);
        Assert.assertNotNull("LayoutSlip double not null", ltd);


        // +45, -45 in ctors is a 90 degree rotation
        Assert.assertEquals("lvs.getBounds() is equal to...",
                new Rectangle2D.Double(30.20101012677667, 85.85786437626905, 39.59797974644667, 28.284271247461902),
                lvs.getBounds());

        Assert.assertEquals("lvd.getBounds() is equal to...",
                new Rectangle2D.Double(85.85786437626905, 30.201010126776673, 28.284271247461902, 39.59797974644665),
                lvd.getBounds());

    }

    // from here down is testing infrastructure
    private LayoutSingleSlip      lts = null;
    private LayoutSingleSlipView  lvs = null;
    
    private LayoutDoubleSlip      ltd = null;
    private LayoutDoubleSlipView  lvd = null;

    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        
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
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void tearDown() {
        if (lts != null) {
            lts.remove();
            lvs.dispose();
            lts = null;
            lvs = null;
        }
        if (ltd != null) {
            ltd.remove();
            lvd.dispose();
            ltd = null;
            lvd = null;
        }

        super.tearDown();
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipViewTest.class);
}
