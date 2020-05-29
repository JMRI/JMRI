package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import java.awt.geom.Rectangle2D;
import jmri.JmriException;
import jmri.util.*;
import org.junit.*;
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
    public void testConstructionLinesRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        trackSegment.showConstructionLine = 0;
        Assert.assertTrue("From 0", trackSegment.isShowConstructionLines());
        Assert.assertTrue("From 0", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        Assert.assertFalse("HIDECONALL", trackSegment.isShowConstructionLines());
        Assert.assertTrue("HIDECONALL", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        Assert.assertFalse("HIDECON", trackSegment.isShowConstructionLines());
        Assert.assertTrue("HIDECON", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        Assert.assertTrue("SHOWCON", trackSegment.isShowConstructionLines());
        Assert.assertFalse("SHOWCON", trackSegment.hideConstructionLines());

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        Assert.assertFalse("all", trackSegment.isShowConstructionLines());
        Assert.assertFalse("all", trackSegment.hideConstructionLines());

    }

    @Test
    public void hideConstructionLinesOfInt() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = 0;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, 0);

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL);

        // ----
        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.SHOWCON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECON);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.HIDECON);

        trackSegment.showConstructionLine = TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL;
        trackSegment.hideConstructionLines(TrackSegment.HIDECONALL);
        Assert.assertEquals(trackSegment.showConstructionLine, TrackSegment.SHOWCON | TrackSegment.HIDECON | TrackSegment.HIDECONALL);
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


    /*
     *  Arrow Decorations
     */
    @Test
    public void testDefaultGetSetArrowStyle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertEquals("trackSegment.getArrowStyle() == 0 (default).", 0, trackSegment.getArrowStyle());
        trackSegment.setArrowStyle(-1);
        Assert.assertNotEquals("trackSegment.setArrowStyle(-1) not allowed.", -1, trackSegment.getArrowStyle());
        trackSegment.setArrowStyle(5);
        Assert.assertEquals("trackSegment.getArrowStyle() == 5 (after set).", 5, trackSegment.getArrowStyle());
    }

    @Test
    public void testDefaultIsSetArrowEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("trackSegment.isArrowEndStart() == false (after set).", trackSegment.isArrowEndStart());
        trackSegment.setArrowEndStart(true);
        Assert.assertTrue("trackSegment.isArrowEndStart() == true (default).", trackSegment.isArrowEndStart());
    }

    @Test
    public void testDefaultIsSetArrowEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertFalse("trackSegment.isArrowEndStop() == false (after set).", trackSegment.isArrowEndStop());
        trackSegment.setArrowEndStop(true);
        Assert.assertTrue("trackSegment.isArrowEndStop() == true (default).", trackSegment.isArrowEndStop());
    }

    @Test
    public void testDefaultIsSetArrowDirIn() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertFalse("trackSegment.isArrowDirIn() == false (after set).", trackSegment.isArrowDirIn());
        trackSegment.setArrowDirIn(true);
        Assert.assertTrue("trackSegment.isArrowDirIn() == true (default).", trackSegment.isArrowDirIn());
    }

    @Test
    public void testDefaultIsSetArrowDirOut() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isArrowDirOut() == true (default).", trackSegment.isArrowDirOut());
            trackSegment.setArrowDirOut(true);
            Assert.assertTrue("trackSegment.isArrowDirOut() == false (after set).", trackSegment.isArrowDirOut());
        }
    }

    @Test
    public void testDefaultGetSetArrowLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getArrowLineWidth() == 4 (default).", 4, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setArrowLineWidth(-1) not allowed.", -1, trackSegment.getArrowLineWidth());
            trackSegment.setArrowLineWidth(5);
            Assert.assertEquals("trackSegment.getArrowLineWidth() == 5 (after set).", 5, trackSegment.getArrowLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetArrowLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getArrowLength() == 4 (default).", 4, trackSegment.getArrowLength());
            trackSegment.setArrowLength(-1);
            Assert.assertNotEquals("trackSegment.setArrowLength(-1) not allowed.", -1, trackSegment.getArrowLength());
            trackSegment.setArrowLength(5);
            Assert.assertEquals("trackSegment.getArrowLength() == 5 (after set).", 5, trackSegment.getArrowLength());
        }
    }


    /*
        Bumper Decorations
     */
    @Test
    public void testDefaultIsSetBumperEndStart() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperEndStart() == true (default).", trackSegment.isBumperEndStart());
            trackSegment.setBumperEndStart(true);
            Assert.assertTrue("trackSegment.isBumperEndStart() == false (after set).", trackSegment.isBumperEndStart());
        }
    }

    @Test
    public void testDefaultIsSetBumperEndStop() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperEndStop() == true (default).", trackSegment.isBumperEndStop());
            trackSegment.setBumperEndStop(true);
            Assert.assertTrue("trackSegment.isBumperEndStop() == false (after set).", trackSegment.isBumperEndStop());
        }
    }

    @Test
    public void testDefaultGetSetBumperLineWidth() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBumperLineWidth() == 8 (default).", 8, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(-1);
            Assert.assertNotEquals("trackSegment.setBumperLineWidth(-1) not allowed.", -1, trackSegment.getBumperLineWidth());
            trackSegment.setBumperLineWidth(5);
            Assert.assertEquals("trackSegment.getBumperLineWidth() == 5 (after set).", 5, trackSegment.getBumperLineWidth());
        }
    }

    @Test
    public void testDefaultGetSetBumperLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertEquals("trackSegment.getBumperLength() == 10 (default).", 10, trackSegment.getBumperLength());
            trackSegment.setBumperLength(-1);
            Assert.assertNotEquals("trackSegment.setBumperLength(-1) not allowed.", -1, trackSegment.getBumperLength());
            trackSegment.setBumperLength(8);
            Assert.assertEquals("trackSegment.getBumperLength() == 8 (after set).", 8, trackSegment.getBumperLength());
        }
    }

    @Test
    public void testDefaultIsSetBumperFlipped() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        if ((layoutEditor != null) && (trackSegment != null)) {
            Assert.assertFalse("trackSegment.isBumperFlipped() == true (default).", trackSegment.isBumperFlipped());
            trackSegment.setBumperFlipped(true);
            Assert.assertTrue("trackSegment.isBumperFlipped() == false (after set).", trackSegment.isBumperFlipped());
        }
    }



    static private LayoutEditor layoutEditor = null;
    static private TrackSegment trackSegment = null;
    //static private TrackSegmentView trackSegmentView = null;

    //
    // from here down is testing infrastructure
    //
    /**
     * This is called once before all tests
     *
     * @throws Exception
     */
    @BeforeClass
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
    @AfterClass
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
    @Before
    public void setUpEach() throws Exception {
        super.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.resetInstanceManager();
        
        if (!GraphicsEnvironment.isHeadless()) {
            layoutEditor = new LayoutEditor();

            PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
            // PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(10.0, 20.0), layoutEditor);
            // layoutEditor.addLayoutTrack(p1, p1v);
            
            PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);
            // PositionablePointView p2v = new PositionablePointView(p2, new Point2D.Double(20.0, 33.0), layoutEditor);
            // layoutEditor.addLayoutTrack(p2, p2v);

            trackSegment = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, true, layoutEditor);
            // trackSegmentView = new TrackSegmentView(trackSegment, layoutEditor);
            // layoutEditor.addLayoutTrack(trackSegment, trackSegmentView);
        }
    }

    /**
     * This is called after each test
     *
     * @throws Exception
     */
    @After
    public void tearDownEach() throws Exception {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
            // release refereces to layout editor
            layoutEditor = null;
        }
        
        // release refereces to track segment
        trackSegment = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
}
