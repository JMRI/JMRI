package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import jmri.*;
import jmri.util.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutTurnoutView
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LayoutTurnoutViewTest extends LayoutTrackViewTest {

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertEquals("ltRH.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(150.0, 100.0),
                ltRHv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Right Hand.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(132.0, 87.0),
                ltRHv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(168.0, 113.0),
                ltRHv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(162.0, 123.0),
                ltRHv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(132.0, 87.0),
                ltRHv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltRH.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(150.0, 100.0),
                ltRHv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));

        Assert.assertEquals("ltLH.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(200.0, 175.0),
                ltLHv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Left Hand.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(189.0, 149.0),
                ltLHv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(211.0, 201.0),
                ltLHv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(222.0, 195.0),
                ltLHv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(189.0, 149.0),
                ltLHv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltLH.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(200.0, 175.0),
                ltLHv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));

        Assert.assertEquals("ltWY.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(250.0, 250.0),
                ltWYv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Wye.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(254.5, 218.5),
                ltWYv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(238.0, 280.0),
                ltWYv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(253.0, 283.0),
                ltWYv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(262.0, 220.0),
                ltWYv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltWY.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(250.0, 250.0),
                ltWYv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));

        Assert.assertEquals("ltDX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(300.0, 325.0),
                ltDXv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Double XOver.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(347.0, 297.0),
                ltDXv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(279.0, 377.0),
                ltDXv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(253.0, 353.0),
                ltDXv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(321.0, 273.0),
                ltDXv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltDX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(300.0, 325.0),
                ltDXv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));

        Assert.assertEquals("ltRX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(350.0, 400.0),
                ltRXv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Right Hand XOver.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(410.0, 404.0),
                ltRXv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(337.0, 424.0),
                ltRXv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(290.0, 396.0),
                ltRXv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(363.0, 376.0),
                ltRXv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltRX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(350.0, 400.0),
                ltRXv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));

        Assert.assertEquals("ltLX.getCoordsForConnectionType(NONE) is equal to...",
                new Point2D.Double(400.0, 475.0),
                ltLXv.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("Left Hand XOver.getCoordsForConnectionType(NONE); Invalid Connection Type");
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_A) is equal to...",
                new Point2D.Double(413.0, 503.0),
                ltLXv.getCoordsForConnectionType(HitPointType.TURNOUT_A));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_B) is equal to...",
                new Point2D.Double(334.0, 476.0),
                ltLXv.getCoordsForConnectionType(HitPointType.TURNOUT_B));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_C) is equal to...",
                new Point2D.Double(387.0, 447.0),
                ltLXv.getCoordsForConnectionType(HitPointType.TURNOUT_C));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_D) is equal to...",
                new Point2D.Double(466.0, 474.0),
                ltLXv.getCoordsForConnectionType(HitPointType.TURNOUT_D));
        Assert.assertEquals("ltLX.getCoordsForConnectionType(TURNOUT_CENTER) is equal to...",
                new Point2D.Double(400.0, 475.0),
                ltLXv.getCoordsForConnectionType(HitPointType.TURNOUT_CENTER));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

            // the commented out tests were from the older code,
            // which relied on execution order of the side-effects
            // from the testSetUpDefaultSize test.
        Assert.assertEquals("ltRH.getBounds() is equal to...",
                // new Rectangle2D.Double(121.0, 80.0, 58.0, 55.0),
                new Rectangle2D.Double(132.0, 87.0, 36.0, 36.0),
                ltRHv.getBounds());

        Assert.assertEquals("ltLH.getBounds() is equal to...",
                // new Rectangle2D.Double(184.0, 135.0, 50.0, 80.0),
                new Rectangle2D.Double(189.0, 149.0, 33.0, 52.0),
                ltLHv.getBounds());

        Assert.assertEquals("ltWY.getBounds() is equal to...",
                // new Rectangle2D.Double(232.0, 201.0, 25.0, 100.0),
                new Rectangle2D.Double(238.0, 218.5, 16.5, 64.5),
                ltWYv.getBounds());

        Assert.assertEquals("ltDX.getBounds() is equal to...",
                // new Rectangle2D.Double(199.0, 213.0, 202.0, 224.0),
                new Rectangle2D.Double(253.0, 273.0, 94.0, 104.0),
                ltDXv.getBounds());

        Assert.assertEquals("ltRX.getBounds() is equal to...",
                // new Rectangle2D.Double(223.0, 345.0, 254.0, 110.0),
                new Rectangle2D.Double(290.0, 376.0, 120.0, 48.0),
                ltRXv.getBounds());

        Assert.assertEquals("ltLX.getBounds() is equal to...",
                // new Rectangle2D.Double(259.0, 413.0, 282.0, 124.0),
                new Rectangle2D.Double(334.0, 447.0, 132.0, 56.0),
                ltLXv.getBounds());
    }

    @Test
    public void testSetUpDefaultSize() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);

        // note: Not really testing anything here,
        // this is just for code coverage. But note that it
        // modifies the current layout manager, which must
        // be refreshed before the next test.
        ltRHv.setUpDefaultSize();
        ltLHv.setUpDefaultSize();
        ltWYv.setUpDefaultSize();
        ltDXv.setUpDefaultSize();
        ltRXv.setUpDefaultSize();
        ltLXv.setUpDefaultSize();
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


    // from here down is testing infrastructure
    @BeforeAll
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            JUnitUtil.resetInstanceManager();
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalSensorManager();
            JUnitUtil.initInternalSignalHeadManager();
        }
    }

    @AfterAll
    public static void afterClass() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @BeforeEach
    @javax.annotation.OverridingMethodsMustInvokeSuper
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {

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
    }

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

}
