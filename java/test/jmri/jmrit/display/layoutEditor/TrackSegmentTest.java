package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    // static final double TOLERANCE = 0.000001;

    @Test
    public void testCtor() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        // Invalid parameters in TrackSegment constructor call
        TrackSegment ts = new TrackSegment("TS01", null, HitPointType.NONE, (TrackSegment) null, HitPointType.NONE, false, layoutEditor);
        assertNotNull( ts, "TrackSegment TS01 not null");
        JUnitAppender.assertErrorMessage("Invalid object in TrackSegment constructor call - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 1 ('NONE') in TrackSegment constructor - TS01");
        JUnitAppender.assertErrorMessage("Invalid connect type 2 ('NONE') in TrackSegment constructor - TS01");
    }

    @Test
    public void testReplaceTrackConnection() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        assertFalse( trackSegment.replaceTrackConnection(null, null, HitPointType.NONE),
                "trackSegment.replaceTrackConnection(null, null, NONE) fail");
        JUnitAppender.assertWarnMessage("TS1.replaceTrackConnection(null, null, NONE); Can't replace null track connection with null");

        LayoutTrack c1 = trackSegment.getConnect1();
        HitPointType t1 = trackSegment.getType1();
        assertTrue( trackSegment.replaceTrackConnection(c1, null, HitPointType.NONE),
                "trackSegment.replaceTrackConnection(c1, null, NONE) fail");
        assertNull( trackSegment.getConnect1(),
                "trackSegment.replaceTrackConnection(c1, null, NONE) fail");

        assertTrue( trackSegment.replaceTrackConnection(null, c1, t1),
                "trackSegment.replaceTrackConnection(null, c1, t1) fail");
        assertEquals( c1, trackSegment.getConnect1(),
                "trackSegment.replaceTrackConnection(null, c1, t1) fail");

        // PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, new Point2D.Double(10.0, 10.0), layoutEditor);
        PositionablePoint a3 = new PositionablePoint("A3", PositionablePoint.PointType.ANCHOR, layoutEditor);
        assertTrue( trackSegment.replaceTrackConnection(c1, a3, HitPointType.POS_POINT),
                "trackSegment.replaceTrackConnection(c1, a3, POS_POINT) fail");
    }

    @Test
    public void testToString() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        assertEquals( "TrackSegment TS1 c1:{A1 (POS_POINT)}, c2:{A2 (POS_POINT)}",
                trackSegment.toString(), "trackSegment.toString()");
    }

    @Test
    public void testSetNewConnect() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        trackSegment.setNewConnect1(null, HitPointType.NONE);
        assertNull(  trackSegment.getConnect1(), "trackSegment.setNewConnect1(null, NONE)");
        assertEquals( HitPointType.NONE, trackSegment.getType1(), "trackSegment.setNewConnect1(null, NONE)");

        trackSegment.setNewConnect2(null, HitPointType.NONE);
        assertNull( trackSegment.getConnect2(), "trackSegment.setNewConnect1(null, NONE)");
        assertEquals( HitPointType.NONE, trackSegment.getType2(), "trackSegment.setNewConnect1(null, NONE)");
    }

    @Test
    public void test_getConnection() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        JmriException ex = assertThrows( JmriException.class, () -> {
            var t = trackSegment.getConnection(HitPointType.NONE);
            fail("Should have thrown, created " + t);
        }, "trackSegment.getConnection(NONE) threw JmriException");
        assertNotNull( ex);
    }

    @Test
    public void test_getSetLayoutBlock() {
        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock()");
        trackSegment.setLayoutBlock(null);
        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock()");

        LayoutBlock layoutBlock = new LayoutBlock("ILB999", "Test Block");
        trackSegment.setLayoutBlock(layoutBlock);
        assertEquals( layoutBlock, trackSegment.getLayoutBlock(),
                "trackSegment.getLayoutBlock()");

        trackSegment.setLayoutBlock(null);
        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock()");
    }

    @Test
    public void test_setLayoutBlockByName() {

        assertNotNull(layoutEditor);
        assertNotNull(trackSegment);

        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock() == null (default)");
        trackSegment.setLayoutBlockByName(null);
        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock(null) == null");
        trackSegment.setLayoutBlockByName("");
        assertNull( trackSegment.getLayoutBlock(), "trackSegment.getLayoutBlock('') == null");

        trackSegment.setLayoutBlockByName("invalid name");    //note: invalid name
        JUnitAppender.assertErrorMessage("provideLayoutBlock: The block name 'invalid name' does not return a block.");
    }



    //
    // from here down is testing infrastructure
    //


    protected TrackSegment trackSegment = null;
    //static private TrackSegmentView trackSegmentView = null;


    /**
     * This is called once before all tests.
     */
    @BeforeAll
    public static void setUpClass() {

        // save the old string comparator
        stringComparator = Operator.getDefaultStringComparator();
        // set default string matching comparator to one that exactly matches and is case sensitive
        Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

    }

    /**
     * This is called once after all tests.
     */
    @AfterAll
    public static void tearDownClass() {

        //restore the default string matching comparator
        Operator.setDefaultStringComparator(stringComparator);

    }
    private static Operator.StringComparator stringComparator = null;

    /**
     * This is called before each test.
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
