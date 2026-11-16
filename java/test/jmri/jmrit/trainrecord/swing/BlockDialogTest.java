package jmri.jmrit.trainrecord.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.TrainMakeupSource;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.beans.PropertyChangeSupport;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * The Block dialog shows the train a Block holds, why, and the Block's
 * history, and keeps up with changes.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class BlockDialogTest {

    @Test
    public void testShowsTrainWhyAndHistory() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        a.setUserName("LB4 Up Main");
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("47012 Coal empties");
        ThreadingUtil.runOnGUI(() -> r.place(List.of(a), false));

        BlockDialog d = ThreadingUtil.runOnGUIwithReturn(() -> BlockDialog.open(a, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "LB4 Up Main (IB1)"));
        assertSame(a, d.getBlock());
        assertSame(d, ThreadingUtil.runOnGUIwithReturn(() -> BlockDialog.open(a, true)), "one dialog per Block");
        new JLabelOperator(f, "47012 Coal empties");
        JTableOperator history = new JTableOperator(f);
        JUnitUtil.waitFor(() -> history.getRowCount() == 1, "the placing in the history");
        assertEquals("47012 Coal empties", history.getValueAt(0, 1));
        assertTrue(history.getValueAt(0, 2).toString().startsWith("Placed in LB4 Up Main"));

        ThreadingUtil.runOnGUI(r::unplace);
        new JLabelOperator(f, Bundle.getMessage("BlockDialogNoTrain"));
        JUnitUtil.waitFor(() -> history.getRowCount() == 1, "taking it off names no Block");

        new JButtonOperator(f, Bundle.getMessage("BlockDialogClose")).doClick();
        JUnitUtil.waitFor(() -> !f.isVisible(), "closed");
    }

    /** Knows train STF only: one stop ahead, North Industries. */
    private static final class StfSource extends PropertyChangeSupport implements TrainMakeupSource {

        @Override
        public TrainMakeupSource.TrainMakeup getMakeup(String trainName) {
            return "STF".equals(trainName)
                    ? new TrainMakeupSource.TrainMakeup(List.of(new TrainRecord.TrainComponent("Southport", 300.0)),
                            List.of(new TrainMakeupSource.Work("North Industries", 1, 2)))
                    : null;
        }
    }

    @Test
    public void testShowsWorkAtNextStop() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB1");
        InstanceManager.store(new StfSource(), TrainMakeupSource.class);
        TrainRecordManager manager = InstanceManager.getDefault(TrainRecordManager.class);
        TrainRecord stf = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("STF"));
        TrainRecord other = ThreadingUtil.runOnGUIwithReturn(() -> manager.newRecord("Other"));
        ThreadingUtil.runOnGUI(() -> {
            stf.place(List.of(a), false);
            other.place(List.of(a), false);
        });

        ThreadingUtil.runOnGUI(() -> BlockDialog.open(a, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", a.getDisplayName()));
        new JLabelOperator(f, Bundle.getMessage("BlockDialogNextStopWork", "North Industries", 1, 2));
        assertEquals("", BlockDialog.work(other), "no line for a train no source knows");
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testHeadingAndTwoTrains() throws jmri.JmriException {
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        jmri.SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
        Block a = bm.provideBlock("IB1");
        Block b = bm.provideBlock("IB2");
        Block c = bm.provideBlock("IB3");
        c.setUserName("Down Loop");
        a.setSensor("IS1");
        b.setSensor("IS2");
        c.setSensor("IS3");
        for (Block[] j : new Block[][]{{a, b}, {b, c}}) {
            j[0].addPath(new jmri.Path(j[1], 0, 0));
            j[1].addPath(new jmri.Path(j[0], 0, 0));
        }
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("6M45");
        ThreadingUtil.runOnGUI(() -> {
            try {
                sm.provideSensor("IS1").setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                throw new RuntimeException(e);
            }
            r.place(List.of(a), false);
        });
        ThreadingUtil.runOnGUI(() -> {
            try {
                sm.provideSensor("IS2").setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                throw new RuntimeException(e);
            }
        });
        JUnitUtil.waitFor(() -> r.getBlocks().contains(b), "entered IB2");

        ThreadingUtil.runOnGUI(() -> BlockDialog.open(b, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "IB2"));
        new JLabelOperator(f, Bundle.getMessage("BlockDialogHeadingTowards", "Down Loop"));

        // a second train placed in the same Block while the dialog is open is shown too
        TrainRecord r2 = InstanceManager.getDefault(TrainRecordManager.class).newRecord("Light engine");
        ThreadingUtil.runOnGUI(() -> r2.place(List.of(b), false));
        new JLabelOperator(f, "Light engine");
        new JLabelOperator(f, "6M45");
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testNoHeadingWhereTheWayOnIsNotKnown() throws jmri.JmriException {
        BlockManager bm = InstanceManager.getDefault(BlockManager.class);
        jmri.SensorManager sm = InstanceManager.getDefault(jmri.SensorManager.class);
        Block a = bm.provideBlock("IB1");
        Block b = bm.provideBlock("IB2");
        a.setSensor("IS1");
        b.setSensor("IS2");
        a.addPath(new jmri.Path(b, 0, 0));
        b.addPath(new jmri.Path(a, 0, 0));
        // two ways on from IB2, neither set by a turnout
        for (String n : new String[]{"IB3", "IB4"}) {
            Block c = bm.provideBlock(n);
            b.addPath(new jmri.Path(c, 0, 0));
            c.addPath(new jmri.Path(b, 0, 0));
        }
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("6M45");
        ThreadingUtil.runOnGUI(() -> {
            try {
                sm.provideSensor("IS1").setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                throw new RuntimeException(e);
            }
            r.place(List.of(a), false);
        });
        ThreadingUtil.runOnGUI(() -> {
            try {
                sm.provideSensor("IS2").setKnownState(jmri.Sensor.ACTIVE);
            } catch (jmri.JmriException e) {
                throw new RuntimeException(e);
            }
        });
        JUnitUtil.waitFor(() -> r.getBlocks().contains(b), "entered IB2");

        ThreadingUtil.runOnGUI(() -> BlockDialog.open(b, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "IB2"));
        new JLabelOperator(f, "6M45");
        String heading = Bundle.getMessage("BlockDialogHeadingTowards", "");
        assertTrue(labels(f.getContentPane()).stream().noneMatch(t -> t.startsWith(heading)),
                "no heading line: " + labels(f.getContentPane()));
        JUnitUtil.dispose(f.getWindow());
    }

    private static List<String> labels(java.awt.Container c) {
        List<String> found = new java.util.ArrayList<>();
        for (java.awt.Component k : c.getComponents()) {
            if (k instanceof javax.swing.JLabel && ((javax.swing.JLabel) k).getText() != null) {
                found.add(((javax.swing.JLabel) k).getText());
            }
            if (k instanceof java.awt.Container) {
                found.addAll(labels((java.awt.Container) k));
            }
        }
        return found;
    }

    @Test
    public void testEditValue() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");
        a.setValue("Old");
        ThreadingUtil.runOnGUI(() -> BlockDialog.open(a, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "IB2"));
        String title = javax.swing.UIManager.getString("OptionPane.inputDialogTitle");
        Thread answer = new Thread(() -> {
            org.netbeans.jemmy.operators.JDialogOperator dlg = new org.netbeans.jemmy.operators.JDialogOperator(title);
            new org.netbeans.jemmy.operators.JTextFieldOperator(dlg).setText("New");
            new JButtonOperator(dlg, "OK").push();
        }, "answer the value prompt");
        answer.start();
        new JButtonOperator(f, Bundle.getMessage("BlockDialogEditValue")).pushNoBlock();
        JUnitUtil.waitFor(() -> !answer.isAlive(), "the value entered");
        JUnitUtil.waitFor(() -> "New".equals(a.getValue()), "the Block's value set");
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testValueEditCanBeDisabled() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB2");
        ThreadingUtil.runOnGUI(() -> BlockDialog.open(a, false));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "IB2"));
        assertTrue(!new JButtonOperator(f, Bundle.getMessage("BlockDialogEditValue")).isEnabled(),
                "as for a Block Contents icon whose value edit is disabled");
        new JLabelOperator(f, Bundle.getMessage("BlockDialogNoTrain"));
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testConfirmAndClear() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB3");
        a.setUserName("LB4");
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB4");
        b.setUserName("LB3");
        a.addPath(new jmri.Path(b, 0, 0));
        b.addPath(new jmri.Path(a, 0, 0));
        TrainRecord r = InstanceManager.getDefault(TrainRecordManager.class).newRecord("47012 Coal empties");
        ThreadingUtil.runOnGUI(() -> r.place(List.of(b), false));

        ThreadingUtil.runOnGUI(() -> BlockDialog.open(a, true, true));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "LB4 (IB3)"));
        org.netbeans.jemmy.operators.JComboBoxOperator trainHere = new org.netbeans.jemmy.operators.JComboBoxOperator(f);
        JUnitUtil.waitFor(() -> trainHere.getItemCount() == 1, "the train next door offered");
        assertEquals(Bundle.getMessage("BlockDialogChoiceNextDoor", "47012 Coal empties"),
                trainHere.getItemAt(0).toString());
        new JButtonOperator(f, Bundle.getMessage("BlockDialogConfirm")).doClick();
        JUnitUtil.waitFor(() -> r.getBlocks().equals(List.of(a)), "the train is here");

        new JButtonOperator(f, Bundle.getMessage("BlockDialogClear")).doClick();
        JUnitUtil.waitFor(() -> !r.isPlaced(), "the Block is empty");
        JUnitUtil.dispose(f.getWindow());
    }

    @Test
    public void testNoCorrectionsFromAPanelThatDoesNotControlTheLayout() {
        Block a = InstanceManager.getDefault(BlockManager.class).provideBlock("IB5");
        ThreadingUtil.runOnGUI(() -> BlockDialog.open(a, true, false));
        JFrameOperator f = new JFrameOperator(Bundle.getMessage("BlockDialogTitle", "IB5"));
        assertTrue(!new JButtonOperator(f, Bundle.getMessage("BlockDialogConfirm")).isEnabled());
        assertTrue(!new JButtonOperator(f, Bundle.getMessage("BlockDialogClear")).isEnabled());
        JUnitUtil.dispose(f.getWindow());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
