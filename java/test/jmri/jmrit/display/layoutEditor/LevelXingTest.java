package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.implementation.VirtualSignalHead;
import jmri.implementation.VirtualSignalMast;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
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

    @Rule   // 10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule   // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2);

    private static LayoutEditor layoutEditor = null;
    private static LevelXing levelXing = null;

    private static LayoutBlock layoutBlock1 = null;
    private static LayoutBlock layoutBlock2 = null;

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

        Assert.assertNotNull("layoutBlock1 is null", layoutBlock1);
        Assert.assertNotNull("layoutBlock2 is null", layoutBlock2);

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
        JUnitAppender.assertWarnMessage("Unhandled loc: 0");
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
        JUnitAppender.assertWarnMessage("Unhandled loc: 0");
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
        JUnitAppender.assertWarnMessage("Unhandled loc: 0");
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

        //these should all return null
        try {
            Assert.assertNull("levelXing.getConnection(POINTA)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_A));
            Assert.assertNull("levelXing.getConnection(POINTB)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_B));
            Assert.assertNull("levelXing.getConnection(POINTC)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_C));
            Assert.assertNull("levelXing.getConnection(POINTD)",
                    levelXing.getConnection(LevelXing.LEVEL_XING_D));

            //this should output a warning and throw a JmriException
            levelXing.getConnection(LayoutTrack.NONE);
            Assert.fail("levelXing.getConnection didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("Invalid Point Type 0");
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
            JUnitAppender.assertWarnMessage("Invalid Connection Type 0");
        }

        try {   //unexpected type of connection to LevelXing
            levelXing.setConnection(LevelXing.LEVEL_XING_A, null, LevelXing.POS_POINT);
            Assert.fail("levelXing.setConnection(LEVEL_XING_A, null, POS_POINT) didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("unexpected type of connection to LevelXing - 1");
        }

        try {   //these should all be good
            levelXing.setConnection(LevelXing.LEVEL_XING_A, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_B, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_C, null, LayoutTrack.NONE);
            levelXing.setConnection(LevelXing.LEVEL_XING_D, null, LayoutTrack.NONE);
        } catch (JmriException ex) {
            Assert.fail("levelXing.setConnection(LEVEL_XING_[ABCD], null, POS_POINT) didn't throw exception");
        }
    }

    @Test
    public void testSetConnectionABCD() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);

        //unexpected type of connection
        levelXing.setConnectA(null, LayoutTrack.TRACK);
        JUnitAppender.assertErrorMessage("unexpected type of A connection to levelXing - 10");
        levelXing.setConnectB(null, LayoutTrack.TRACK);
        JUnitAppender.assertErrorMessage("unexpected type of B connection to levelXing - 10");
        levelXing.setConnectC(null, LayoutTrack.TRACK);
        JUnitAppender.assertErrorMessage("unexpected type of C connection to levelXing - 10");
        levelXing.setConnectD(null, LayoutTrack.TRACK);
        JUnitAppender.assertErrorMessage("unexpected type of D connection to levelXing - 10");

        TrackSegment ts = new TrackSegment("TS1", null, LayoutTrack.NONE, null, LayoutTrack.NONE, false, false, false, layoutEditor);

        //unexpected type of connection
        levelXing.setConnectA(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("unexpected type of A connection to levelXing - 0");
        levelXing.setConnectB(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("unexpected type of B connection to levelXing - 0");
        levelXing.setConnectC(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("unexpected type of C connection to levelXing - 0");
        levelXing.setConnectD(ts, LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("unexpected type of D connection to levelXing - 0");

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
        JUnitAppender.assertErrorMessage("Invalid connection type 0");

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
        Assert.assertNotNull("layoutBlock1 is null", layoutBlock1);
        Assert.assertNotNull("layoutBlock2 is null", layoutBlock2);

        levelXing.setLayoutBlockAC(layoutBlock1);
        levelXing.setLayoutBlockBD(layoutBlock2);

        levelXing.setLayoutBlockAC(null);
        levelXing.setLayoutBlockBD(null);
    }

    @Test
    public void testCanRemove() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("levelXing is null", levelXing);
        Assert.assertNotNull("layoutBlock1 is null", layoutBlock1);
        Assert.assertNotNull("layoutBlock2 is null", layoutBlock2);

//        Thread misc1 = jmri.util.swing.JemmyUtil.createModalDialogOperatorThread(
//                Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonOK"));  // NOI18N
        levelXing.canRemove();
//        JUnitUtil.waitFor(() -> {
//            return !(misc1.isAlive());
//        }, "misc1 finished");

        levelXing.setLayoutBlockAC(layoutBlock1);
        levelXing.setLayoutBlockBD(layoutBlock2);

        levelXing.canRemove();

        levelXing.setLayoutBlockAC(null);
        levelXing.setLayoutBlockBD(null);
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

    //
    // from here down is testing infrastructure
    //
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            // set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

            layoutEditor = new LayoutEditor("LevelXing Tests Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);

            //create a layout block
            layoutBlock1 = new LayoutBlock("ILB1", "Test Block 1");
            layoutBlock2 = new LayoutBlock("ILB2", "Test Block 2");

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
