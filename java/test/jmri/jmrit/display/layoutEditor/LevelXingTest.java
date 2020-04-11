package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.CheckForNull;
import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.Operator;

/**
 * Test simple functioning of LevelXing
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LevelXingTest {

    @Rule   //10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule   //allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2);

    private static Operator.StringComparator stringComparator = null;

    private static LayoutEditor layoutEditor = null;
    private static LevelXing levelXing = null;

    private static LayoutBlock layoutBlockAC = null;
    private static LayoutBlock layoutBlockBD = null;

    private static PositionablePoint a1 = null;
    private static PositionablePoint a2 = null;
    private static PositionablePoint a3 = null;
    private static PositionablePoint a4 = null;
    private static TrackSegment ts1 = null;
    private static TrackSegment ts2 = null;
    private static TrackSegment ts3 = null;
    private static TrackSegment ts4 = null;

    private static SignalHead sh1 = null;
    private static SignalHead sh2 = null;
    private static SignalHead sh3 = null;
    private static SignalHead sh4 = null;

    private static SignalMast sm1 = null;
    private static SignalMast sm2 = null;
    private static SignalMast sm3 = null;
    private static SignalMast sm4 = null;

    private static Sensor s1 = null;
    private static Sensor s2 = null;
    private static Sensor s3 = null;
    private static Sensor s4 = null;

    private static String myLevelXingName = "MyLevelXing";
    private static Point2D levelXingPoint = new Point2D.Double(50.0, 50.0);

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        Assert.assertNotNull("sh1 is null", sh1);
        Assert.assertNotNull("sh2 is null", sh2);
        Assert.assertNotNull("sh3 is null", sh3);
        Assert.assertNotNull("sh4 is null", sh4);

        Assert.assertNotNull("sm1 is null", sm1);
        Assert.assertNotNull("sm2 is null", sm2);
        Assert.assertNotNull("sm3 is null", sm3);
        Assert.assertNotNull("sm4 is null", sm4);

        Assert.assertNotNull("s1 is null", s1);
        Assert.assertNotNull("s2 is null", s2);
        Assert.assertNotNull("s3 is null", s3);
        Assert.assertNotNull("s4 is null", s4);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        Assert.assertEquals("levelXing.toString()", "LevelXing " + myLevelXingName, levelXing.toString());
    }

    @Test
    public void testGetSignalHeads() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        //these should all return null
        Assert.assertNull("levelXing.getSignalHead(POINTA)",
                levelXing.getSignalHead(LevelXing.POINTA));
        Assert.assertNull("levelXing.getSignalHead(POINTB)",
                levelXing.getSignalHead(LevelXing.POINTB));
        Assert.assertNull("levelXing.getSignalHead(POINTC)",
                levelXing.getSignalHead(LevelXing.POINTC));
        Assert.assertNull("levelXing.getSignalHead(POINTD)",
                levelXing.getSignalHead(LevelXing.POINTD));

        //this should output a warning
        levelXing.getSignalHead(LayoutTrack.NONE);
        JUnitAppender.assertWarnMessage("MyLevelXing.getSignalHead(0); Unhandled loc");
    }

    @Test
    public void testGetSignalMasts() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        //these should all return null
        Assert.assertNull("levelXing.getSignalMast(POINTA)",
                levelXing.getSignalMast(LevelXing.POINTA));
        Assert.assertNull("levelXing.getSignalMast(POINTB)",
                levelXing.getSignalMast(LevelXing.POINTB));
        Assert.assertNull("levelXing.getSignalMast(POINTC)",
                levelXing.getSignalMast(LevelXing.POINTC));
        Assert.assertNull("levelXing.getSignalMast(POINTD)",
                levelXing.getSignalMast(LevelXing.POINTD));

        //this should output a warning
        levelXing.getSignalMast(LayoutTrack.NONE);
        JUnitAppender.assertWarnMessage("MyLevelXing.getSignalMast(0); Unhandled loc");
    }

    @Test
    public void testGetSensors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        //these should all return null
        Assert.assertNull("levelXing.getSensor(POINTA)",
                levelXing.getSensor(LevelXing.POINTA));
        Assert.assertNull("levelXing.getSensor(POINTB)",
                levelXing.getSensor(LevelXing.POINTB));
        Assert.assertNull("levelXing.getSensor(POINTC)",
                levelXing.getSensor(LevelXing.POINTC));
        Assert.assertNull("levelXing.getSensor(POINTD)",
                levelXing.getSensor(LevelXing.POINTD));

        //this should output a warning
        levelXing.getSensor(LayoutTrack.NONE);
        JUnitAppender.assertWarnMessage("MyLevelXing.getSensor(0); Unhandled loc");
    }

    @Test
    public void testGetSetSignalName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        testNullSignalNames();
        setSignalNames();
        clearSignalNames();
    }

    @Test
    public void testGetSetSignalMasts() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        testNullSignalMasts();
        setSignalMasts();
        clearSignalMasts();
    }

    @Test
    public void testGetSetSensors() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        testNullSensors();
        setSensors();
        clearSensors();
    }

    @Test
    public void testRemoveBeanReference() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        levelXing.removeBeanReference(null);

        //do signal heads
        testNullSignalNames();
        setSignalNames();
        levelXing.removeBeanReference(sh1);
        levelXing.removeBeanReference(sh2);
        levelXing.removeBeanReference(sh3);
        levelXing.removeBeanReference(sh4);
        testNullSignalNames();

        //do signal masts
        testNullSignalMasts();
        setSignalMasts();
        levelXing.removeBeanReference(sm1);
        levelXing.removeBeanReference(sm2);
        levelXing.removeBeanReference(sm3);
        levelXing.removeBeanReference(sm4);
        testNullSignalMasts();

        //do sensors
        testNullSensors();
        setSensors();
        levelXing.removeBeanReference(s1);
        levelXing.removeBeanReference(s2);
        levelXing.removeBeanReference(s3);
        levelXing.removeBeanReference(s4);
        testNullSensors();
    }

    @Test
    public void testGetConnections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        try {   //these should all return null and NOT throw an exception
            Assert.assertNull("levelXing.getConnection(POINTA)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_A));
            Assert.assertNull("levelXing.getConnection(POINTB)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_B));
            Assert.assertNull("levelXing.getConnection(POINTC)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_C));
            Assert.assertNull("levelXing.getConnection(POINTD)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_D));
        } catch (JmriException ex) {
            Assert.fail("levelXing.getConnection threw exception: " + ex);
        }

        try {   //this should output a warning and throw a JmriException
            levelXing.getConnection(LayoutTrack.NONE);
            Assert.fail("levelXing.getConnection didn't throw an exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("MyLevelXing.getConnection(0); invalid connection type");
        }
    }

    @Test
    public void testSetConnections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        try {   //invalid connection type
            levelXing.setConnection(LayoutTrack.NONE, null, LayoutTrack.NONE);
            Assert.fail("levelXing.setConnection(NONE, null, NONE) didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("MyLevelXing.setConnection(0, null, 0); invalid connection type");
        }

        try {   //unexpected type of connection to LevelXing
            levelXing.setConnection(LevelXing.LEVEL_XING_A, null, LevelXing.POS_POINT);
            Assert.fail("levelXing.setConnection(LEVEL_XING_A, null, POS_POINT) didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertErrorMessage("MyLevelXing.setConnection(6, null, 1); unexpected type");
        }

        try {   //these should all be good
            levelXing.setConnection(LevelXing.LEVEL_XING_A, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_B, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_C, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_D, null, LayoutTrack.NONE);
        } catch (JmriException ex) {
            Assert.fail("levelXing.setConnection(LEVEL_XING_[ABCD], null, NONE) threw exception");
        }
    }

    @Test
    public void testSetConnectionABCD() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        TrackSegment ts = new TrackSegment("TS1", null, LayoutTrack.NONE, null, LayoutTrack.NONE, false, false, false, layoutEditor);

        //unexpected type of connection
        levelXing.setConnectA(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("MyLevelXing.setConnectA(TS1, 0); unexpected type");
        levelXing.setConnectB(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("MyLevelXing.setConnectB(TS1, 0); unexpected type");
        levelXing.setConnectC(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("MyLevelXing.setConnectC(TS1, 0); unexpected type");
        levelXing.setConnectD(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("MyLevelXing.setConnectD(TS1, 0); unexpected type");

        //everything should be good here (no error output)
        levelXing.setConnectA(ts, LayoutTrack.TRACK);
        levelXing.setConnectB(ts, LayoutTrack.TRACK);
        levelXing.setConnectC(ts, LayoutTrack.TRACK);
        levelXing.setConnectD(ts, LayoutTrack.TRACK);
        levelXing.setConnectA(null, LayoutTrack.NONE);
        levelXing.setConnectB(null, LayoutTrack.NONE);
        levelXing.setConnectC(null, LayoutTrack.NONE);
        levelXing.setConnectD(null, LayoutTrack.NONE);
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        //invalid connection type
        levelXing.getCoordsForConnectionType(LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("MyLevelXing.getCoordsForConnectionType(0); Invalid connection type");

        //valid connection types (no error output)
        Assert.assertEquals("levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_CENTER)",
                levelXingPoint, levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_CENTER));
        Assert.assertEquals("levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_A)",
                new Point2D.Double(30, 50),
                levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_A));
        Assert.assertEquals("levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_B)",
                new Point2D.Double(36, 64),
                levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_B));
        Assert.assertEquals("levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_C)",
                new Point2D.Double(70, 50),
                levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_C));
        Assert.assertEquals("levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_D)",
                new Point2D.Double(64, 36),
                levelXing.getCoordsForConnectionType(LayoutTrack.LEVEL_XING_D));
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        Assert.assertEquals("levelXing.getBounds()",
                new Rectangle2D.Double(30, 36, 40, 28),
                levelXing.getBounds());
    }

    @Test
    public void testSetLayoutBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        //these should both be null
        Assert.assertNull("levelXing.getLayoutBlockAC() is null", levelXing.getLayoutBlockAC());
        Assert.assertNull("levelXing.getLayoutBlockBD() is null", levelXing.getLayoutBlockBD());

        levelXing.setAllLayoutBlocks(layoutBlockAC);
        Assert.assertEquals("levelXing.getLayoutBlockAC() == layoutBlockAC", layoutBlockAC, levelXing.getLayoutBlockAC());
        Assert.assertEquals("levelXing.getLayoutBlockBD() == layoutBlockAC", layoutBlockAC, levelXing.getLayoutBlockBD());

        levelXing.setLayoutBlockAC(null);
        Assert.assertNull("levelXing.getLayoutBlockAC() is null", levelXing.getLayoutBlockAC());
        Assert.assertNull("levelXing.getLayoutBlockBD() is null", levelXing.getLayoutBlockBD());

        levelXing.setLayoutBlockBD(null);
        Assert.assertNull("levelXing.getLayoutBlockAC() is null", levelXing.getLayoutBlockAC());
        Assert.assertNull("levelXing.getLayoutBlockBD() is null", levelXing.getLayoutBlockBD());

        levelXing.setLayoutBlockAC(layoutBlockAC);
        Assert.assertEquals("levelXing.getLayoutBlockAC() == layoutBlockAC", layoutBlockAC, levelXing.getLayoutBlockAC());
        Assert.assertEquals("levelXing.getLayoutBlockBD() == layoutBlockAC", layoutBlockAC, levelXing.getLayoutBlockBD());
        levelXing.setLayoutBlockBD(layoutBlockBD);
        Assert.assertEquals("levelXing.getLayoutBlockAC() == layoutBlockAC", layoutBlockAC, levelXing.getLayoutBlockAC());
        Assert.assertEquals("levelXing.getLayoutBlockBD() == layoutBlockBD", layoutBlockBD, levelXing.getLayoutBlockBD());

        levelXing.setLayoutBlockAC(null);
        Assert.assertNull("levelXing.getLayoutBlockAC() is null", levelXing.getLayoutBlockAC());
        Assert.assertEquals("levelXing.getLayoutBlockBD() == layoutBlockBD", layoutBlockBD, levelXing.getLayoutBlockBD());
        levelXing.setLayoutBlockBD(null);
        Assert.assertNull("levelXing.getLayoutBlockAC() is null", levelXing.getLayoutBlockAC());
        Assert.assertNull("levelXing.getLayoutBlockBD() is null", levelXing.getLayoutBlockBD());
    }

    @Test
    public void testCanRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        //Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
        //Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  //NOI18N
        levelXing.canRemove();
        //JUnitUtil.waitFor(() -> {
        //return !(misc1.isAlive());
        //}, "misc1 finished");

        levelXing.setLayoutBlockAC(layoutBlockAC);
        levelXing.setLayoutBlockBD(layoutBlockBD);

        levelXing.canRemove();

        levelXing.setLayoutBlockAC(null);
        levelXing.setLayoutBlockBD(null);
    }

    @Test
    public void testIsMainlines() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        setupTracks();

        Assert.assertTrue("levelXing.isMainline() is not true", levelXing.isMainline());
        Assert.assertTrue("levelXing.isMainlineAC() is not true", levelXing.isMainlineAC());
        Assert.assertTrue("levelXing.isMainlineBD() is not true", levelXing.isMainlineBD());

        ts1.setMainline(true);
        Assert.assertTrue("levelXing.isMainline() is not true", levelXing.isMainline());
        Assert.assertTrue("levelXing.isMainlineAC() is not true", levelXing.isMainlineAC());
        Assert.assertTrue("levelXing.isMainlineBD() is not true", levelXing.isMainlineBD());
        ts1.setMainline(false);

        ts2.setMainline(true);
        Assert.assertTrue("levelXing.isMainline() is not true", levelXing.isMainline());
        Assert.assertTrue("levelXing.isMainlineAC() is not true", levelXing.isMainlineAC());
        Assert.assertTrue("levelXing.isMainlineBD() is not true", levelXing.isMainlineBD());
        ts2.setMainline(false);

        ts3.setMainline(true);
        Assert.assertTrue("levelXing.isMainline() is not true", levelXing.isMainline());
        Assert.assertTrue("levelXing.isMainlineAC() is not true", levelXing.isMainlineAC());
        Assert.assertTrue("levelXing.isMainlineBD() is not true", levelXing.isMainlineBD());
        ts3.setMainline(false);

        ts4.setMainline(true);
        Assert.assertTrue("levelXing.isMainline() is not true", levelXing.isMainline());
        Assert.assertFalse("levelXing.isMainlineAC() is not true", levelXing.isMainlineAC());
        Assert.assertTrue("levelXing.isMainlineBD() is not true", levelXing.isMainlineBD());
        ts4.setMainline(false);
    }

    @Test
    public void testSetCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        Point2D newCenter = new Point2D.Double(200, 100);
        levelXing.setCoordsCenter(newCenter);
        Assert.assertEquals("levelXing.getCoordsCenter()",
                newCenter, levelXing.getCoordsCenter());

        Point2D newA = new Point2D.Double(150, 100);
        levelXing.setCoordsA(newA);
        Assert.assertEquals("levelXing.getCoordsA()",
                newA, levelXing.getCoordsA());

        Point2D newB = new Point2D.Double(170, 130);
        levelXing.setCoordsB(newB);
        Assert.assertEquals("levelXing.getCoordsB()",
                newB, levelXing.getCoordsB());

        Point2D newC = new Point2D.Double(240, 100);
        levelXing.setCoordsC(newC);
        Assert.assertEquals("levelXing.getCoordsC()",
                newC, levelXing.getCoordsC());

        Point2D newD = new Point2D.Double(230, 70);
        levelXing.setCoordsD(newD);
        Assert.assertEquals("levelXing.getCoordsD()",
                newD, levelXing.getCoordsD());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        levelXing.scaleCoords(3, 4);

        Assert.assertEquals("levelXing.getCoordsCenter()",
                new Point2D.Double(150, 200), levelXing.getCoordsCenter());
        Assert.assertEquals("levelXing.getCoordsA()",
                new Point2D.Double(90, 200), levelXing.getCoordsA());
        Assert.assertEquals("levelXing.getCoordsB()",
                new Point2D.Double(108, 256), levelXing.getCoordsB());
        Assert.assertEquals("levelXing.getCoordsC()",
                new Point2D.Double(210, 200), levelXing.getCoordsC());
        Assert.assertEquals("levelXing.getCoordsD()",
                new Point2D.Double(192, 144), levelXing.getCoordsD());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        levelXing.translateCoords(50, 20);
        Assert.assertEquals("levelXing.getCoordsCenter()",
                new Point2D.Double(100, 70), levelXing.getCoordsCenter());
        Assert.assertEquals("levelXing.getCoordsA()",
                new Point2D.Double(80, 70), levelXing.getCoordsA());
        Assert.assertEquals("levelXing.getCoordsB()",
                new Point2D.Double(86, 84), levelXing.getCoordsB());
        Assert.assertEquals("levelXing.getCoordsC()",
                new Point2D.Double(120, 70), levelXing.getCoordsC());
        Assert.assertEquals("levelXing.getCoordsD()",
                new Point2D.Double(114, 56), levelXing.getCoordsD());
    }

    @Test
    public void testGetBlockBoundaries() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        //no blocks... no errors
        String[] blockBoundaries = levelXing.getBlockBoundaries();
        Assert.assertNull("levelXing.getBlockBoundaries()[0] is null", blockBoundaries[0]);
        Assert.assertNull("levelXing.getBlockBoundaries()[1] is null", blockBoundaries[1]);
        Assert.assertNull("levelXing.getBlockBoundaries()[2] is null", blockBoundaries[2]);
        Assert.assertNull("levelXing.getBlockBoundaries()[3] is null", blockBoundaries[3]);

        //setup tracks
        setupTracks();
        //setup blocks
        ts1.setLayoutBlock(layoutBlockBD);
        ts2.setLayoutBlock(layoutBlockAC);
        ts3.setLayoutBlock(layoutBlockBD);
        ts4.setLayoutBlock(layoutBlockAC);
        levelXing.setLayoutBlockAC(layoutBlockAC);
        levelXing.setLayoutBlockBD(layoutBlockBD);

        //blocks and tracks...
        blockBoundaries = levelXing.getBlockBoundaries();
        Assert.assertEquals("levelXing.getBlockBoundaries()[0]", "Test Block BD - Test Block AC", blockBoundaries[0]);
        Assert.assertEquals("levelXing.getBlockBoundaries()[1]", "Test Block AC - Test Block BD", blockBoundaries[1]);
        Assert.assertEquals("levelXing.getBlockBoundaries()[2]", "Test Block BD - Test Block AC", blockBoundaries[2]);
        Assert.assertEquals("levelXing.getBlockBoundaries()[3]", "Test Block AC - Test Block BD", blockBoundaries[3]);
    }

    @Test
    public void testRemoveIsActive() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        Assert.assertTrue("levelXing.isActive()", levelXing.isActive());
        levelXing.remove(); //this will clear the active flag
        Assert.assertFalse("levelXing.isActive()", levelXing.isActive());
    }

    @Test
    public void testAddRemoveSignalMastLogic() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        levelXing.addSignalMastLogic(sm1);
        levelXing.addSignalMastLogic(sm1);  //note:duplicate to test already added code
        levelXing.addSignalMastLogic(sm2);
        levelXing.addSignalMastLogic(sm3);
        levelXing.addSignalMastLogic(sm4);

        levelXing.removeSignalMastLogic(sm1);
        levelXing.removeSignalMastLogic(sm1);  //note:duplicate to test already removed code
        levelXing.removeSignalMastLogic(sm2);
        levelXing.removeSignalMastLogic(sm3);
        levelXing.removeSignalMastLogic(sm4);
    }

    @Test
    public void testCheckForFreeConnections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        List l = levelXing.checkForFreeConnections();
        Assert.assertEquals("Number of free connections", 4, l.size());
        Assert.assertEquals("connections[0]", LayoutTrack.LEVEL_XING_A, l.get(0));
        Assert.assertEquals("connections[1]", LayoutTrack.LEVEL_XING_B, l.get(1));
        Assert.assertEquals("connections[2]", LayoutTrack.LEVEL_XING_C, l.get(2));
        Assert.assertEquals("connections[3]", LayoutTrack.LEVEL_XING_D, l.get(3));
    }

    @Test
    public void testCheckForUnAssignedBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        Assert.assertFalse("levelXing.checkForUnAssignedBlocks()", levelXing.checkForUnAssignedBlocks());
        levelXing.setLayoutBlockAC(layoutBlockAC);
        Assert.assertTrue("levelXing.checkForUnAssignedBlocks()", levelXing.checkForUnAssignedBlocks());
        levelXing.setLayoutBlockAC(null);
        levelXing.setLayoutBlockBD(layoutBlockBD);
        Assert.assertFalse("levelXing.checkForUnAssignedBlocks()", levelXing.checkForUnAssignedBlocks());
        levelXing.setLayoutBlockAC(layoutBlockAC);
        Assert.assertTrue("levelXing.checkForUnAssignedBlocks()", levelXing.checkForUnAssignedBlocks());
    }

    @Test
    public void testCheckForNonContiguousBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlockAC is null", layoutBlockAC);
        Assert.assertNotNull("layoutBlockBD is null", layoutBlockBD);

        setupTracks();

        //setup blocks
        levelXing.setLayoutBlockAC(layoutBlockAC);
        levelXing.setLayoutBlockBD(layoutBlockBD);

        ts1.setLayoutBlock(layoutBlockBD);
        ts2.setLayoutBlock(layoutBlockAC);
        ts3.setLayoutBlock(layoutBlockBD);
        ts4.setLayoutBlock(layoutBlockAC);

        HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps = new HashMap<>();
        levelXing.checkForNonContiguousBlocks(blockNamesToTrackNameSetMaps);
        Assert.assertEquals("number of noncontiguous blocks", 2, blockNamesToTrackNameSetMaps.size());

        Assert.assertNull("map['BOGUS'] not null", blockNamesToTrackNameSetMaps.get("BOGUS"));

        //layoutBlockAC
        List<Set<String>> trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlockAC.getUserName());
        Assert.assertNotNull("map['Test Block 1']", trackNameSets);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        Set<String> trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 1, trackNameSet.size());

        Iterator<String> it = trackNameSet.iterator();
        Assert.assertEquals("levelXing name", levelXing.getName(), it.next());

        //layoutBlockBD
        trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlockBD.getUserName());
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 1, trackNameSet.size());

        it = trackNameSet.iterator();
        Assert.assertEquals("levelXing name", levelXing.getName(), it.next());
    }

    //
    //private methods
    //
    private void setSignalNames() {
        levelXing.setSignalAName(sh1.getSystemName());
        levelXing.setSignalBName(sh2.getSystemName());
        levelXing.setSignalCName(sh3.getSystemName());
        levelXing.setSignalDName(sh4.getSystemName());

        Assert.assertEquals("levelXing.getSignalAName()",
                sh1.getSystemName(), levelXing.getSignalAName());
        Assert.assertEquals("levelXing.getSignalBName()",
                sh2.getSystemName(), levelXing.getSignalBName());
        Assert.assertEquals("levelXing.getSignalCName()",
                sh3.getSystemName(), levelXing.getSignalCName());
        Assert.assertEquals("levelXing.getSignalDName()",
                sh4.getSystemName(), levelXing.getSignalDName());
    }

    private void clearSignalNames() {

        levelXing.setSignalAName(null);
        levelXing.setSignalBName(null);
        levelXing.setSignalCName(null);
        levelXing.setSignalDName(null);

        //these should all return an empty string (again)
        testNullSignalNames();
    }

    private void testNullSignalNames() {
        //these should all return an empty string
        Assert.assertEquals("levelXing.getSignalAName()",
                "", levelXing.getSignalAName());
        Assert.assertEquals("levelXing.getSignalBName()",
                "", levelXing.getSignalBName());
        Assert.assertEquals("levelXing.getSignalCName()",
                "", levelXing.getSignalCName());
        Assert.assertEquals("levelXing.getSignalDName()",
                "", levelXing.getSignalDName());
    }

    private void setSignalMasts() {
        levelXing.setSignalAMast(sm1.getSystemName());
        levelXing.setSignalBMast(sm2.getSystemName());
        levelXing.setSignalCMast(sm3.getSystemName());
        levelXing.setSignalDMast(sm4.getSystemName());

        Assert.assertEquals("levelXing.getSignalAMastName()",
                sm1.getSystemName(), levelXing.getSignalAMastName());
        Assert.assertEquals("levelXing.getSignalBMastName()",
                sm2.getSystemName(), levelXing.getSignalBMastName());
        Assert.assertEquals("levelXing.getSignalCMastName()",
                sm3.getSystemName(), levelXing.getSignalCMastName());
        Assert.assertEquals("levelXing.getSignalDMastName()",
                sm4.getSystemName(), levelXing.getSignalDMastName());
    }

    private void clearSignalMasts() {

        levelXing.setSignalAMast(null);
        levelXing.setSignalBMast(null);
        levelXing.setSignalCMast(null);
        levelXing.setSignalDMast(null);

        //these should all return an empty string (again)
        testNullSignalMasts();
    }

    private void testNullSignalMasts() {
        //these should all return an empty string
        Assert.assertEquals("levelXing.getSignalAMastName()",
                "", levelXing.getSignalAMastName());
        Assert.assertEquals("levelXing.getSignalBMastName()",
                "", levelXing.getSignalBMastName());
        Assert.assertEquals("levelXing.getSignalCMastName()",
                "", levelXing.getSignalCMastName());
        Assert.assertEquals("levelXing.getSignalDMastName()",
                "", levelXing.getSignalDMastName());
    }

    private void setSensors() {
        levelXing.setSensorAName(s1.getSystemName());
        levelXing.setSensorBName(s2.getSystemName());
        levelXing.setSensorCName(s3.getSystemName());
        levelXing.setSensorDName(s4.getSystemName());

        Assert.assertEquals("levelXing.getSignalAMastName()",
                s1.getSystemName(), levelXing.getSensorAName());
        Assert.assertEquals("levelXing.getSignalBMastName()",
                s2.getSystemName(), levelXing.getSensorBName());
        Assert.assertEquals("levelXing.getSignalCMastName()",
                s3.getSystemName(), levelXing.getSensorCName());
        Assert.assertEquals("levelXing.getSignalDMastName()",
                s4.getSystemName(), levelXing.getSensorDName());
    }

    private void clearSensors() {

        levelXing.setSensorAName(null);
        levelXing.setSensorBName(null);
        levelXing.setSensorCName(null);
        levelXing.setSensorDName(null);

        //these should all return an empty string (again)
        testNullSensors();
    }

    private void testNullSensors() {
        //these should all return an empty string
        Assert.assertEquals("levelXing.getSensorAName()",
                "", levelXing.getSensorAName());
        Assert.assertEquals("levelXing.getSensorBName()",
                "", levelXing.getSensorBName());
        Assert.assertEquals("levelXing.getSensorCName()",
                "", levelXing.getSensorCName());
        Assert.assertEquals("levelXing.getSensorDName()",
                "", levelXing.getSensorDName());
    }

    private void setupTracks() {
        List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
        Assert.assertNotNull("layoutTracks is null", layoutTracks);

        //add 1st anchor
        a1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, new Point2D.Double(10, 50), layoutEditor);
        Assert.assertNotNull("a1 is null", a1);
        layoutTracks.add(a1);

        //connect the levelXing leg A to 1st anchor
        int tsIdx = 1;
        ts1 = addNewTrackSegment(levelXing, LayoutTrack.LEVEL_XING_A, a1, LayoutTrack.POS_POINT, tsIdx++);

        //add 2nd anchor
        a2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, new Point2D.Double(20, 80), layoutEditor);
        Assert.assertNotNull("a2 is null", a2);
        layoutTracks.add(a2);

        //connect the levelXing leg B to 2st anchor
        ts2 = addNewTrackSegment(levelXing, LayoutTrack.LEVEL_XING_B, a2, LayoutTrack.POS_POINT, tsIdx++);

        //add 3rd anchor
        a3 = new PositionablePoint("A3", PositionablePoint.ANCHOR, new Point2D.Double(90, 50), layoutEditor);
        Assert.assertNotNull("a3 is null", a3);
        layoutTracks.add(a3);

        //connect the levelXing leg B to 2st anchor
        ts3 = addNewTrackSegment(levelXing, LayoutTrack.LEVEL_XING_C, a3, LayoutTrack.POS_POINT, tsIdx++);

        //add 4th anchor
        a4 = new PositionablePoint("A4", PositionablePoint.ANCHOR, new Point2D.Double(80, 20), layoutEditor);
        Assert.assertNotNull("a4 is null", a4);
        layoutTracks.add(a4);

        //connect the levelXing leg B to 2st anchor
        ts4 = addNewTrackSegment(levelXing, LayoutTrack.LEVEL_XING_D, a4, LayoutTrack.POS_POINT, tsIdx++);

        //wait for layout editor to finish setup and drawing
        new QueueTool().waitEmpty();
    }

    private static TrackSegment addNewTrackSegment(
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2,
            int idx) {
        TrackSegment result = null;
        if ((c1 != null) && (c2 != null)) {
            //create new track segment
            String name = layoutEditor.getFinder().uniqueName("T", idx);
            result = new TrackSegment(name, c1, t1, c2, t2,
                    false, true, layoutEditor);
            Assert.assertNotNull("new TrackSegment is null", result);
            layoutEditor.getLayoutTracks().add(result);
            //link to connected objects
            layoutEditor.setLink(c1, t1, result, LayoutTrack.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutTrack.TRACK);
        }
        return result;
    }

    //
    //from here down is testing infrastructure
    //
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            //save the old string comparator
            stringComparator = Operator.getDefaultStringComparator();
            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

            layoutEditor = new LayoutEditor("LevelXing Tests Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);

            //create a layout block
            layoutBlockAC = new LayoutBlock("ILB1", "Test Block AC");
            layoutBlockBD = new LayoutBlock("ILB2", "Test Block BD");

            //create signal heads
            sh1 = new VirtualSignalHead("VH1", "signal head 1");
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh1);

            sh2 = new VirtualSignalHead("VH2", "signal head 2");
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh2);

            sh3 = new VirtualSignalHead("VH3", "signal head 3");
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh3);

            sh4 = new VirtualSignalHead("VH4", "signal head 4");
            InstanceManager.getDefault(jmri.SignalHeadManager.class).register(sh4);

            //create signal masts
            sm1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight(VM1)", "signal mast 1");
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm1);

            sm2 = new VirtualSignalMast("IF$vsm:basic:one-searchlight(VM2)", "signal mast 2");
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm2);

            sm3 = new VirtualSignalMast("IF$vsm:basic:one-searchlight(VM3)", "signal mast 3");
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm3);

            sm4 = new VirtualSignalMast("IF$vsm:basic:one-searchlight(VM4)", "signal mast 4");
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(sm4);

            //create sensors
            s1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
            s1.setUserName("sensor 1");

            s2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
            s2.setUserName("sensor 2");

            s3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
            s3.setUserName("sensor 3");

            s4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
            s4.setUserName("sensor 4");

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            new QueueTool().waitEmpty();
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;

            //restore the default string matching comparator
            Operator.setDefaultStringComparator(stringComparator);
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            levelXing = new LevelXing(myLevelXingName, levelXingPoint, layoutEditor);
        }
    }
}
