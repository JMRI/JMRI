package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The set-up check for a Block that takes its value from its Reporter's
 * current report.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainTrackingChecksReporterTest {

    private BlockManager blocks;
    private Block a;
    private Reporter reporter;

    private List<Finding> run() {
        return ThreadingUtil.runOnGUIwithReturn(
                () -> TrainTrackingChecks.run(new ArrayList<>(blocks.getNamedBeanSet()), List.of()));
    }

    private static List<Finding> ofKind(List<Finding> found, Kind kind) {
        List<Finding> some = new ArrayList<>();
        for (Finding f : found) {
            if (f.getKind() == kind) {
                some.add(f);
            }
        }
        return some;
    }

    @Test
    public void testCurrentReportIsFound() {
        a.setReporter(reporter);
        a.setReportingCurrent(true);

        List<Finding> found = ofKind(run(), Kind.REPORTER_CURRENT_REPORT);
        assertEquals(1, found.size(), "one finding");
        Finding f = found.get(0);
        assertSame(a, f.getSubject(), "about the Block");
        assertEquals(List.of(reporter), f.getRelated(), "with its Reporter, for Show me");
        assertTrue(f.getMessage().contains("\"A\"") && f.getMessage().contains("\"IR1\""),
                "names the Block and the Reporter: " + f.getMessage());
    }

    @Test
    public void testLastReportIsNotFound() {
        a.setReporter(reporter);
        a.setReportingCurrent(false);

        assertTrue(run().isEmpty(), "a Block using the last report is set up well");
    }

    @Test
    public void testNoReporterIsNotFound() {
        a.setReportingCurrent(true);

        assertTrue(run().isEmpty(), "Use Current means nothing without a Reporter");
    }

    @Test
    public void testFindingShowsTheHarm() {
        // what the check is about: with the current report, the Block loses
        // the train's name while the train is still in the Block
        a.setReporter(reporter);
        a.setReportingCurrent(true);
        ThreadingUtil.runOnLayout(() -> {
            InstanceManager.getDefault(SensorManager.class).provideSensor("ISA").setCommandedState(jmri.Sensor.ACTIVE);
            reporter.setReport("train 1");
        });
        assertEquals("train 1", a.getValue(), "the Block has the train's name");
        ThreadingUtil.runOnLayout(() -> reporter.setReport(null));
        assertEquals(Block.OCCUPIED, a.getState(), "the train is still in the Block");
        assertEquals(null, a.getValue(), "but the Block has lost its name");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initReporterManager();
        blocks = InstanceManager.getDefault(BlockManager.class);
        a = blocks.provideBlock("IBA");
        a.setUserName("A");
        a.setLength(1000);
        a.setSensor("ISA");
        Block b = blocks.provideBlock("IBB");
        b.setUserName("B");
        b.setLength(1000);
        b.setSensor("ISB");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        reporter = InstanceManager.getDefault(ReporterManager.class).provideReporter("IR1");
    }

    @AfterEach
    public void tearDown() {
        a = null;
        reporter = null;
        blocks = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
