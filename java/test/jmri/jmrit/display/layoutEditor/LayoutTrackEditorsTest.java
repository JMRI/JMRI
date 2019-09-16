package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JTextField;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
// import jmri.JmriException;
// import jmri.Sensor;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackEditorsTest {

    private static LayoutEditor layoutEditor = null;

    private LayoutTurnout dxo = null;
    private LayoutSlip slip = null;
    private LevelXing xing = null;
    private TrackSegment segment = null;

    private JFrameOperator jfo = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        Assert.assertNotNull("exists",t);
        e.dispose();
    }

    @Test
    public void testHasNxSensorPairsNull(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        Assert.assertFalse("null block NxSensorPairs",t.hasNxSensorPairs(null));
        e.dispose();
    }

    @Test
    public void testHasNxSensorPairsDisconnectedBlock(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor(); // create layout editor
        LayoutTrackEditors t = new LayoutTrackEditors(e);
        LayoutBlock b = new LayoutBlock("test", "test");
        Assert.assertFalse("disconnected block NxSensorPairs",t.hasNxSensorPairs(b));
        e.dispose();
    }

    @Test
    public void testEditTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();
        createBlocks();

        // Edit the double crossover
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(dxo);

        // Select main turnout
        jfo = new JFrameOperator("Edit Turnout");
        JComboBoxOperator tcbo = new JComboBoxOperator(jfo, 0);
        Assert.assertNotNull(tcbo);
        tcbo.selectItem(1);

        // Enable second turnout and select it
        new JCheckBoxOperator(jfo, 0).doClick();
        JComboBoxOperator tcbo2 = new JComboBoxOperator(jfo, 1);
        Assert.assertNotNull(tcbo2);
        tcbo2.selectItem(2);

        // Enable Invert and Hide
        new JCheckBoxOperator(jfo, 1).doClick();
        new JCheckBoxOperator(jfo, 2).doClick();

        // Select each block box, the first two get predefined blocks
        JComboBoxOperator blk_cbo_A = new JComboBoxOperator(jfo, 2);
        Assert.assertNotNull(blk_cbo_A);
        blk_cbo_A.selectItem(1);

        JComboBoxOperator blk_cbo_B = new JComboBoxOperator(jfo, 3);
        Assert.assertNotNull(blk_cbo_B);
        blk_cbo_B.selectItem(1);

        JComboBoxOperator blk_cbo_C = new JComboBoxOperator(jfo, 4);
        Assert.assertNotNull(blk_cbo_C);
        JTextField c = ((JTextField) blk_cbo_C.getEditor().getEditorComponent());
        c.setText("Blk 3");

        JComboBoxOperator blk_cbo_D = new JComboBoxOperator(jfo, 5);
        Assert.assertNotNull(blk_cbo_D);
        JTextField d = ((JTextField) blk_cbo_D.getEditor().getEditorComponent());
        d.setText("Blk 3");

        new JButtonOperator(jfo, "Done").doClick();
    }

    @Test
    public void testEditTurnoutCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double crossover
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(dxo);
        jfo = new JFrameOperator("Edit Turnout");

        new JButtonOperator(jfo, "Cancel").doClick();
    }

    @Test
    public void testEditSlipDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createTurnouts();
        createBlocks();

        // Edit the double slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(slip);

        // Select turnout A
        jfo = new JFrameOperator("Edit Slip");
        JComboBoxOperator tcbA = new JComboBoxOperator(jfo, 0);
        Assert.assertNotNull(tcbA);
        tcbA.selectItem(1);

        // Select turnout B
        JComboBoxOperator tcbB = new JComboBoxOperator(jfo, 1);
        Assert.assertNotNull(tcbB);
        tcbB.selectItem(2);

        // Select a block
        JComboBoxOperator blk_cbo = new JComboBoxOperator(jfo, 10);
        Assert.assertNotNull(blk_cbo);
        blk_cbo.selectItem(1);

        // Enable Hide
        new JCheckBoxOperator(jfo, 0).doClick();

        // Trigger Test button
        new JButtonOperator(jfo, "Test").doClick();

        new JButtonOperator(jfo, "Done").doClick();
    }

    @Test
    public void testEditSlipCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the double slip
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(slip);
        jfo = new JFrameOperator("Edit Slip");

        new JButtonOperator(jfo, "Cancel").doClick();
    }

    @Test
    public void testEditXingDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        createBlocks();

        // Edit the level crossing
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(xing);
        jfo = new JFrameOperator("Edit Level Crossing");

        // Select AC block
        JComboBoxOperator blk_cbo_AC = new JComboBoxOperator(jfo, 0);
        Assert.assertNotNull(blk_cbo_AC);
        blk_cbo_AC.selectItem(1);

        // Select BD block
        JComboBoxOperator blk_cbo_BD = new JComboBoxOperator(jfo, 1);
        Assert.assertNotNull(blk_cbo_BD);
        blk_cbo_BD.selectItem(2);

        // Enable Hide
        new JCheckBoxOperator(jfo, 0).doClick();

        new JButtonOperator(jfo, "Done").doClick();
    }

    @Test
    public void testEditXingCancel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // Edit the level crossing
        LayoutTrackEditors trackEditor = new LayoutTrackEditors(layoutEditor);
        trackEditor.editLayoutTrack(xing);
        jfo = new JFrameOperator("Edit Level Crossing");

        new JButtonOperator(jfo, "Cancel").doClick();
    }

    // from here down is testing infrastructure
    public void createTurnouts() {
        Turnout turnout1 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IS101");
        turnout1.setUserName("Turnout 101");
        turnout1.setCommandedState(Turnout.CLOSED);
        Turnout turnout2 = InstanceManager.getDefault(jmri.TurnoutManager.class).provideTurnout("IS102  ");
        turnout2.setUserName("Turnout 102");
        turnout2.setCommandedState(Turnout.CLOSED);
    }

    public void createBlocks() {
        Block block1 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB1");
        block1.setUserName("Blk 1");
        Block block2 = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock("IB2");
        block2.setUserName("Blk 2");
    }

    @BeforeClass
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            jmri.util.JUnitUtil.resetInstanceManager();
            jmri.util.JUnitUtil.initInternalTurnoutManager();
            jmri.util.JUnitUtil.initInternalSensorManager();
            layoutEditor = new LayoutEditor();
        }
    }

    @AfterClass
    public static void afterClass() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;

        JUnitUtil.tearDown();
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 75.0);

            dxo = new LayoutTurnout("Double Xover",
                    LayoutTurnout.DOUBLE_XOVER, point, 33.0, 1.1, 1.2, layoutEditor);

            point = MathUtil.add(point, delta);
            slip = new LayoutSlip("Double Slip",
                    point, 0.0, layoutEditor, LayoutTurnout.DOUBLE_SLIP);

            point = MathUtil.add(point, delta);
            xing = new LevelXing("Level Xing",
                    point, layoutEditor);
        }
    }

    @After
    public void tearDown() {
        if(dxo != null){
           dxo.remove();
           dxo.dispose();
           dxo = null;
        }

        if(slip != null){
           slip.remove();
           slip.dispose();
           slip = null;
        }

        if(xing != null){
           xing.remove();
           xing.dispose();
           xing = null;
        }

        if(segment != null){
           segment.remove();
           segment.dispose();
           segment = null;
        }

        jmri.util.JUnitUtil.tearDown();
    }

//     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTrackEditorsTest.class);
}
