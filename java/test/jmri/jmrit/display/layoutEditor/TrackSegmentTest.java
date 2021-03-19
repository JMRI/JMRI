package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import java.awt.geom.Rectangle2D;
import jmri.JmriException;
import jmri.util.*;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.Operator;

/**
 * Test simple functioning of TrackSegment.
 *
 * Note this uses <code>@BeforeClass</code> and <code>@AfterClass</code>
 * to do static setup.
 *
 * Should not involve geometry or graphics, as the class undertest is pure layout
 * information.  But at least for now, it needs a LayoutEditor, and that requires
 * AWT graphics at run time.
 * <p>
 * Note this uses <code>@BeforeAll</code> and <code>@AfterAll</code> to do
 * static setup.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrackSegmentTest extends LayoutTrackTest {

    // the amount of variation allowed floating point values in order to be considered equal
    static final double tolerance = 0.000001;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        // Invalid parameters in TrackSegment constructor call
        TrackSegment ts = new TrackSegment("TS01", (TrackSegment) null, HitPointType.NONE, (TrackSegment) null, HitPointType.NONE, false, layoutEditor);
        Assert.assertNotNull("TrackSegment TS01 not null", ts);
        JUnitAppender.assertErrorMessage("Invalid object in TrackSegment constructor call - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 1 ('NONE') in TrackSegment constructor - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 2 ('NONE') in TrackSegment constructor - TS01");
    }

    @Test
    public void testReplaceTrackConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        Assert.assertFalse("trackSegment.replaceTrackConnection(null, null, NONE) fail", trackSegment.replaceTrackConnection(null, null, HitPointType.NONE));
        JUnitAppender.assertWarnMessage("TS1.replaceTrackConnection(null, null, NONE); Can't replace null track connection with null");

        LayoutTrack c1 = trackSegment.getConnect1();
        HitPointType t1 = trackSegment.getType1();
        Assert.assertTrue("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.replaceTrackConnection(c1, null, HitPointType.NONE));
        Assert.assertNull("trackSegment.replaceTrackConnection(c1, null, NONE) fail", trackSegment.getConnect1());

        Assert.assertTrue("trackSegment.replaceTrackConnection(null, c1, t1) fail", trackSegment.replaceTrackConnection(null, c1, t1));
        Assert.assertEquals("trackSegment.replaceTrackConnection(null, c1, t1) fail", c1, trackSegment.getConnect1());

        // PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 10.0), layoutEditor);
        PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, layoutEditor);
        Assert.assertTrue("trackSegment.replaceTrackConnection(c1, a3, POS_POINT) fail", trackSegment.replaceTrackConnection(c1, a3, HitPointType.POS_POINT));
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        Assert.assertEquals("trackSegment.toString()", "TrackSegment TS1 c1:{A1 (POS_POINT)}, c2:{A2 (POS_POINT)}", trackSegment.toString());
    }

    @Test
    public void testSetNewConnect() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        trackSegment.setNewConnect1(null, HitPointType.NONE);
        Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect1());
        Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", HitPointType.NONE, trackSegment.getType1());

        trackSegment.setNewConnect2(null, HitPointType.NONE);
        Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", null, trackSegment.getConnect2());
        Assert.assertEquals("trackSegment.setNewConnect1(null, NONE)", HitPointType.NONE, trackSegment.getType2());
    }

    @Test
    public void test_getConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        boolean fail = true;
        try {
            Assert.assertNull("trackSegment.getConnection()", trackSegment.getConnection(HitPointType.NONE));
        } catch (JmriException e) {
            fail = false;
        }
        Assert.assertFalse("trackSegment.getConnection(NONE) threw JmriException", fail);
    }

    @Test
    public void test_getSetLayoutBlock() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());
        trackSegment.setLayoutBlock(null);
        Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());

        LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
        trackSegment.setLayoutBlock(layoutBlock);
        Assert.assertEquals("trackSegment.getLayoutBlock()", layoutBlock, trackSegment.getLayoutBlock());

        trackSegment.setLayoutBlock(null);
        Assert.assertNull("trackSegment.getLayoutBlock()", trackSegment.getLayoutBlock());
    }

    @Test
    public void test_setLayoutBlockByName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        Assert.assertNull("trackSegment.getLayoutBlock() == null (default)", trackSegment.getLayoutBlock());
        trackSegment.setLayoutBlockByName(null);
        Assert.assertNull("trackSegment.getLayoutBlock(null) == null", trackSegment.getLayoutBlock());
        trackSegment.setLayoutBlockByName("");
        Assert.assertNull("trackSegment.getLayoutBlock('') == null", trackSegment.getLayoutBlock());

        trackSegment.setLayoutBlockByName("invalid name");    //note: invalid name
        JUnitAppender.assertErrorMessage("provideLayoutBlock: The block name 'invalid name' does not return a block.");
    }



    //
    // from here down is testing infrastructure
    //


    static private LayoutEditor layoutEditor = null;
    static private TrackSegment trackSegment = null;
    //static private TrackSegmentView trackSegmentView = null;


    /**
     * This is called once before all tests
     *
     * @throws Exception
     */
    @BeforeAll
    public static void setUpClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            // save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
            // set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));
        }
    }

    /**
     * This is called once after all tests
     *
     * @throws Exception
     */
    @AfterAll
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
        }
    }
    private static Operator.StringComparator stringComparator = null;

    /**
     * This is called before each test
     *
     * @throws Exception
     */
    @BeforeEach
    public void setUpEach() throws Exception {
        super.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();

        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();

            PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
            // PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(10.0, 20.0), layoutEditor);

            PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);
            // PositionablePointView p2v = new PositionablePointView(p2, new Point2D.Double(20.0, 33.0), layoutEditor);

            trackSegment = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, true, layoutEditor);
            // trackSegmentView = new TrackSegmentView(trackSegment, layoutEditor);
        }
    }

    /**
     * This is called after each test
     *
     * @throws Exception
     */
    @AfterEach
    public void tearDownEach() throws Exception {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
            // release refereces to layout editor
            layoutEditor = null;
        }

        // release refereces to track segment
        trackSegment = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }
}
