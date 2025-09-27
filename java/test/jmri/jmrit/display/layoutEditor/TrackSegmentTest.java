package jmri.jmrit.display.layoutEditor;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.Operator;

import jmri.JmriException;
import jmri.util.JUnitAppender;

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
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class TrackSegmentTest extends LayoutTrackTest {

    // the amount of variation allowed floating point values in order to be considered equal
    static final double TOLERANCE = 0.000001;

    @Test
    public void testCtor() {
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        // Invalid parameters in TrackSegment constructor call
        TrackSegment ts = new TrackSegment("TS01", null, HitPointType.NONE, (TrackSegment) null, HitPointType.NONE, false, layoutEditor);
        Assert.assertNotNull("TrackSegment TS01 not null", ts);
        JUnitAppender.assertErrorMessage("Invalid object in TrackSegment constructor call - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 1 ('NONE') in TrackSegment constructor - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 2 ('NONE') in TrackSegment constructor - TS01");
    }

    @Test
    public void testReplaceTrackConnection() {
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
        Assert.assertTrue((layoutEditor != null) && (trackSegment != null));

        Assert.assertEquals("trackSegment.toString()", "TrackSegment TS1 c1:{A1 (POS_POINT)}, c2:{A2 (POS_POINT)}", trackSegment.toString());
    }

    @Test
    public void testSetNewConnect() {
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


    protected TrackSegment trackSegment = null;
    //static private TrackSegmentView trackSegmentView = null;


    /**
     * This is called once before all tests
     *
     * @throws Exception when needed
     */
    @BeforeAll
    public static void setUpClass() throws Exception {

        // save the old string comparator
        stringComparator = Operator.getDefaultStringComparator();
        // set default string matching comparator to one that exactly matches and is case sensitive
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

    }

    /**
     * This is called once after all tests
     *
     * @throws Exception when needed
     */
    @AfterAll
    public static void tearDownClass() throws Exception {

        //restore the default string matching comparator
        Operator.setDefaultStringComparator(stringComparator);

    }
    private static Operator.StringComparator stringComparator = null;

    /**
     * This is called before each test
     */
    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        PositionablePoint p1 = new PositionablePoint("A1", PositionablePoint.PointType.ANCHOR, layoutEditor);
        // PositionablePointView p1v = new PositionablePointView(p1, new Point2D.Double(10.0, 20.0), layoutEditor);

        PositionablePoint p2 = new PositionablePoint("A2", PositionablePoint.PointType.ANCHOR, layoutEditor);
        // PositionablePointView p2v = new PositionablePointView(p2, new Point2D.Double(20.0, 33.0), layoutEditor);

        trackSegment = new TrackSegment("TS1", p1, HitPointType.POS_POINT, p2, HitPointType.POS_POINT, true, layoutEditor);
        // trackSegmentView = new TrackSegmentView(trackSegment, layoutEditor);

    }

    @AfterEach
    @Override
    public void tearDown() {
        // release refereces to track segment
        trackSegment = null;
        super.tearDown();
    }
}
