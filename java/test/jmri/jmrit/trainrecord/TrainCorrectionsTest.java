package jmri.jmrit.trainrecord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.TrainRecord;
import jmri.TrainRecord.Reason.Rule;
import jmri.TrainRecordManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The operator's corrections: train is here, Block is empty, and the trains
 * offered for a Block.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class TrainCorrectionsTest {

    private TrainRecordManager manager;

    private static Block block(String name) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        return b;
    }

    @Test
    public void testTrainIsHere() {
        Block a = block("A");
        Block b = block("B");
        TrainRecord r = manager.newRecord("T1");
        ThreadingUtil.runOnLayout(() -> r.place(List.of(a), false));
        ThreadingUtil.runOnLayout(() -> TrainCorrections.trainIsHere(r, b));
        assertEquals(List.of(b), r.getBlocks(), "there and nowhere else");
        assertEquals(Rule.PLACED_BY_OPERATOR, Objects.requireNonNull(r.getReason(b)).getRule());
        assertTrue(TrainRecordReasons.why(r, b).endsWith("by the operator."), TrainRecordReasons.why(r, b));
        int history = r.getHistory().size();
        ThreadingUtil.runOnLayout(() -> TrainCorrections.trainIsHere(r, b));
        assertEquals(history, r.getHistory().size(), "already there: nothing changes");
    }

    @Test
    public void testBlockIsEmpty() {
        Block a = block("A");
        Block b = block("B");
        Block c = block("C");
        TrainRecord r1 = manager.newRecord("T1");
        TrainRecord r2 = manager.newRecord("T2");
        ThreadingUtil.runOnLayout(() -> {
            r1.place(List.of(b, a), true);
            r2.place(List.of(c), false);
        });
        ThreadingUtil.runOnLayout(() -> TrainCorrections.blockIsEmpty(b));
        assertEquals(List.of(a), r1.getBlocks(), "taken out of B");
        assertFalse(r1.isFrontKnown(), "its front was in B");
        ThreadingUtil.runOnLayout(() -> TrainCorrections.blockIsEmpty(c));
        assertFalse(r2.isPlaced(), "only in C: taken off the layout");
    }

    @Test
    public void testSuggestions() {
        Block a = block("A");
        Block b = block("B");
        Block far = block("Far");
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
        TrainRecord near = manager.newRecord("Z near");
        TrainRecord other = manager.newRecord("A other");
        TrainRecord here = manager.newRecord("Here");
        ThreadingUtil.runOnLayout(() -> {
            near.place(List.of(a), false);
            other.place(List.of(far), false);
            here.place(List.of(b), false);
        });
        assertEquals(List.of(near, other), TrainCorrections.suggestions(b), "next door first, then by name");
        assertTrue(TrainCorrections.isNextTo(near, b));
        assertFalse(TrainCorrections.isNextTo(other, b));
    }

    @Test
    public void testRecordForMarker() {
        RosterEntry entry = new RosterEntry();
        entry.setId("47012");
        entry.setDccAddress("4712");
        TrainRecord byEntry = ThreadingUtil.runOnGUIwithReturn(() -> TrainCorrections.recordForMarker(entry, "x"));
        assertSame(entry, Objects.requireNonNull(byEntry).getFirstRosterEntry());
        assertEquals("47012", byEntry.getName());
        assertSame(byEntry, ThreadingUtil.runOnGUIwithReturn(() -> TrainCorrections.recordForMarker(entry, null)));

        TrainRecord named = manager.newRecord("60009 Mail");
        assertSame(named, ThreadingUtil.runOnGUIwithReturn(() -> TrainCorrections.recordForMarker(null, "60009 Mail")));
        TrainRecord made = ThreadingUtil.runOnGUIwithReturn(() -> TrainCorrections.recordForMarker(null, "New one"));
        assertEquals("New one", Objects.requireNonNull(made).getName());
        assertNull(ThreadingUtil.runOnGUIwithReturn(() -> TrainCorrections.recordForMarker(null, " ")));
    }

    @Test
    public void testEveryoneMayCorrectByDefault() {
        assertTrue(PermissionsTrainTracking.mayCorrect());
        // Preferences > Permissions can show it
        assertTrue(jmri.swing.PermissionSwingTools.getPermissionSwingForClass(
                PermissionsTrainTracking.PERMISSION_CORRECT_TRAINS) != null);
    }

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(tempDir));
        JUnitUtil.initRosterConfigManager();
        manager = InstanceManager.getDefault(TrainRecordManager.class);
    }

    @AfterEach
    public void tearDown() {
        manager = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
