package jmri.jmrit.trainrecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.InstanceManager;
import jmri.InvokeOnLayoutThread;
import jmri.Path;
import jmri.TrainRecord;
import jmri.TrainRecord.Reason;
import jmri.TrainRecordManager;

/**
 * The operator's corrections to train tracking: "train is here", and "this
 * Block is empty". They change only the train records: nothing on the
 * layout, and nothing in Dispatcher or Warrants. Callers check
 * {@link PermissionsTrainTracking#mayCorrect()} first.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainCorrections {

    private TrainCorrections() {
    }

    /**
     * The train is in this Block, and nowhere else: it is placed there.
     *
     * @param record the train's record
     * @param block  the Block
     */
    @InvokeOnLayoutThread
    public static void trainIsHere(@Nonnull TrainRecord record, @Nonnull Block block) {
        if (record.getBlocks().equals(List.of(block))) {
            return;
        }
        record.place(List.of(block), false, Reason.Rule.PLACED_BY_OPERATOR);
    }

    /**
     * No train is in this Block: every train recorded there is taken out of
     * it, and one that was only there is taken off the layout.
     *
     * @param block the Block
     */
    @InvokeOnLayoutThread
    public static void blockIsEmpty(@Nonnull Block block) {
        for (TrainRecord r : manager().getRecordsIn(block)) {
            List<Block> rest = new ArrayList<>(r.getBlocks());
            boolean frontKept = r.isFrontKnown() && rest.indexOf(block) != 0;
            rest.remove(block);
            if (rest.isEmpty()) {
                r.unplace();
            } else {
                r.place(rest, frontKept, Reason.Rule.PLACED_BY_OPERATOR);
            }
        }
    }

    /**
     * The trains to offer for a Block, most likely first: those with an end
     * next to it, then the others, each by name.
     *
     * @param block the Block
     * @return the records not already in the Block
     */
    @Nonnull
    public static List<TrainRecord> suggestions(@Nonnull Block block) {
        List<TrainRecord> next = new ArrayList<>();
        List<TrainRecord> others = new ArrayList<>();
        for (TrainRecord r : manager().getRecords()) {
            if (r.isDisposed() || r.getBlocks().contains(block)) {
                continue;
            }
            (isNextTo(r, block) ? next : others).add(r);
        }
        Comparator<TrainRecord> byName = Comparator.comparing(TrainRecord::getName);
        next.sort(byName);
        others.sort(byName);
        next.addAll(others);
        return next;
    }

    /**
     * @param r     a record
     * @param block a Block
     * @return true if one of the train's ends is in a Block joined to it by
     *         a path
     */
    public static boolean isNextTo(@Nonnull TrainRecord r, @Nonnull Block block) {
        List<Block> blocks = r.getBlocks();
        if (blocks.isEmpty()) {
            return false;
        }
        for (Block end : List.of(blocks.get(0), blocks.get(blocks.size() - 1))) {
            for (Path p : block.getPaths()) {
                if (p.getBlock() == end) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * The record for a loco marker: the record of its roster entry, else
     * the record named by its text, else a new record for it.
     *
     * @param entry the marker's roster entry, or null
     * @param name  the marker's text, or null
     * @return the record, or null if the marker has neither
     */
    @CheckForNull
    @InvokeOnLayoutThread
    public static TrainRecord recordForMarker(@CheckForNull BasicRosterEntry entry, @CheckForNull String name) {
        TrainRecordManager m = manager();
        if (entry != null) {
            TrainRecord r = m.getByRosterEntry(entry);
            if (r == null) {
                r = m.getByName(entry.getId());
            }
            if (r == null) {
                r = m.newRecord(entry.getId());
            }
            if (r.getFirstRosterEntry() == null) {
                r.insertFirstRosterEntry(entry);
            }
            return r;
        }
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        TrainRecord r = m.getByName(name);
        return r != null ? r : m.newRecord(name);
    }

    private static TrainRecordManager manager() {
        return InstanceManager.getDefault(TrainRecordManager.class);
    }
}
