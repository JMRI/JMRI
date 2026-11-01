package jmri.jmrit.roster.swing.speedprofile;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link SpeedStepRepeats} and the table that shows it.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SpeedStepRepeatsTest {

    private static SpeedStepRepeats.Run run(int step) {
        return new SpeedStepRepeats.Run(step / 126.0f, step);
    }

    private static List<Integer> steps(List<SpeedStepRepeats.Run> runs) {
        List<Integer> steps = new ArrayList<>();
        for (SpeedStepRepeats.Run r : runs) {
            steps.add(r.step);
        }
        return steps;
    }

    @Test
    public void testPlanRepeatsLowestStepsInTurn() {
        List<SpeedStepRepeats.Run> profiled = Arrays.asList(run(1), run(2), run(3), run(4), run(5));
        assertEquals(Arrays.asList(1, 2, 3, 1, 2, 3), steps(SpeedStepRepeats.plan(profiled, 3, 2)));
    }

    @Test
    public void testPlanTakesLowestWhenProfiledDownwards() {
        List<SpeedStepRepeats.Run> profiled = Arrays.asList(run(9), run(7), run(5), run(3), run(1));
        assertEquals(Arrays.asList(1, 3, 1, 3, 1, 3), steps(SpeedStepRepeats.plan(profiled, 2, 3)));
    }

    @Test
    public void testPlanWithMoreStepsAskedThanProfiled() {
        List<SpeedStepRepeats.Run> profiled = Arrays.asList(run(4), run(2));
        assertEquals(Arrays.asList(2, 4), steps(SpeedStepRepeats.plan(profiled, 10, 1)));
    }

    @Test
    public void testPlanCountsEachSettingOnce() {
        List<SpeedStepRepeats.Run> profiled = Arrays.asList(run(2), run(2), run(3));
        assertEquals(Arrays.asList(2, 3), steps(SpeedStepRepeats.plan(profiled, 2, 1)));
    }

    @Test
    public void testPlanWithNoneAsked() {
        List<SpeedStepRepeats.Run> profiled = Arrays.asList(run(1), run(2));
        assertTrue(SpeedStepRepeats.plan(profiled, 0, 3).isEmpty());
        assertTrue(SpeedStepRepeats.plan(new ArrayList<>(), 2, 3).isEmpty());
    }

    @Test
    public void testRecordAndStatistics() {
        SpeedStepRepeats repeats = new SpeedStepRepeats();
        SpeedStepRepeats.Run r = run(1);
        repeats.recordFirst(r, true, 20.0f);
        repeats.recordFirst(r, false, 18.0f);
        assertFalse(repeats.hasRepeats(), "a first run alone is not a repeat");
        assertTrue(repeats.getRepeated().isEmpty());

        assertEquals(21.0f, repeats.recordRepeat(r, true, 21.0f), 1e-6f);
        assertEquals(21.5f, repeats.recordRepeat(r, true, 22.0f), 1e-6f);
        assertEquals(21.0f, repeats.recordRepeat(r, true, 20.0f), 1e-6f, "the mean of the repeats so far");
        assertTrue(repeats.hasRepeats());

        SpeedStepRepeats.Entry e = repeats.getRepeated().get(r.key());
        assertNotNull(e);
        assertEquals(1, e.step);
        SpeedStepRepeats.Samples fwd = e.forward;
        assertEquals(20.0f, fwd.getFirst(), 1e-6f);
        assertEquals(Arrays.asList(21.0f, 22.0f, 20.0f), fwd.getRepeats());
        assertEquals(21.0f, fwd.mean(), 1e-6f);
        assertEquals(20.0f, fwd.min(), 1e-6f);
        assertEquals(22.0f, fwd.max(), 1e-6f);
        Float spread = fwd.spreadPercent();
        assertNotNull(spread);
        assertEquals(100.0f / 21.0f, spread, 1e-4f, "half the range, as a percentage of the mean");
        Float change = fwd.changePercent();
        assertNotNull(change);
        assertEquals(5.0f, change, 1e-4f, "the mean against the first run");

        SpeedStepRepeats.Samples rev = e.reverse;
        assertEquals(18.0f, rev.getFirst(), 1e-6f);
        assertTrue(rev.getRepeats().isEmpty());
        assertNull(rev.spreadPercent());
        assertNull(rev.changePercent());

        repeats.clear();
        assertFalse(repeats.hasRepeats());
    }

    @Test
    public void testOneRepeatHasNoSpread() {
        SpeedStepRepeats repeats = new SpeedStepRepeats();
        SpeedStepRepeats.Run r = run(2);
        repeats.recordRepeat(r, false, 30.0f);
        SpeedStepRepeats.Samples s = repeats.getRepeated().get(r.key()).reverse;
        assertNull(s.spreadPercent());
        assertNull(s.changePercent(), "no first run to compare with");
        assertEquals(30.0f, s.mean(), 1e-6f);
    }

    @Test
    public void testTableModel() {
        SpeedStepRepeats repeats = new SpeedStepRepeats();
        SpeedStepRepeats.Run r1 = run(1);
        SpeedStepRepeats.Run r2 = run(2);
        repeats.recordFirst(r1, true, 20.0f);
        repeats.recordFirst(r2, true, 40.0f); // not repeated: not shown
        repeats.recordRepeat(r1, true, 21.0f);
        repeats.recordRepeat(r1, true, 23.0f);
        repeats.recordRepeat(r1, false, 19.0f);

        SpeedStepRepeatsTable.RepeatsTableModel m = new SpeedStepRepeatsTable.RepeatsTableModel(repeats);
        assertEquals(2, m.getRowCount(), "step 1 forward and reverse");
        assertEquals(SpeedStepRepeatsTable.RepeatsTableModel.NUM_COLS, m.getColumnCount());
        assertEquals("1", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.STEP_COL));
        assertEquals("0.008", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.THROTTLE_COL));
        assertEquals(Bundle.getMessage("LabelTestStepFwd"), m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.DIRECTION_COL));
        assertEquals("20.0", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.FIRST_COL));
        assertEquals("22.0", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.MEAN_COL));
        assertEquals("21.0", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.MIN_COL));
        assertEquals("23.0", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.MAX_COL));
        assertEquals("±4.5", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.SPREAD_COL));
        assertEquals("+10.0", m.getValueAt(0, SpeedStepRepeatsTable.RepeatsTableModel.CHANGE_COL));

        assertEquals(Bundle.getMessage("LabelTestStepRev"), m.getValueAt(1, SpeedStepRepeatsTable.RepeatsTableModel.DIRECTION_COL));
        assertEquals("", m.getValueAt(1, SpeedStepRepeatsTable.RepeatsTableModel.FIRST_COL));
        assertEquals("19.0", m.getValueAt(1, SpeedStepRepeatsTable.RepeatsTableModel.MEAN_COL));
        assertEquals("", m.getValueAt(1, SpeedStepRepeatsTable.RepeatsTableModel.SPREAD_COL));
        assertEquals("", m.getValueAt(1, SpeedStepRepeatsTable.RepeatsTableModel.CHANGE_COL));

        for (int col = 0; col < m.getColumnCount(); col++) {
            assertFalse(m.getColumnName(col).isEmpty(), "column " + col + " has a name");
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
