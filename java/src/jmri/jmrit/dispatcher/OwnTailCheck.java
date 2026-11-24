package jmri.jmrit.dispatcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BasicRosterEntry;
import jmri.Block;
import jmri.Path;
import jmri.Section;
import jmri.TrainRecord;
import jmri.TrainRecordManager;

/**
 * Says whether the Section a train is waiting for is occupied by that train's
 * own tail.
 * <p>
 * A train that stops with its tail short of the end of the Section behind it
 * keeps that Section occupied. If its next run goes back through that Section,
 * as when one train set-up follows another with the same loco, Dispatcher
 * never allocates it, since it never allocates an occupied Section: the train
 * waits for itself, and every train behind it waits too. This finds that case,
 * so that it can be reported with its cause.
 * <ul>
 * <li>With train tracking, the train's own record shows it in every occupied
 * Block of the Section: that is certain.</li>
 * <li>Without it, the occupied Blocks of the Section join a Block the train
 * itself occupies, with no unoccupied Block between: then it is probably the
 * train's own tail, though it could be another train standing against it.</li>
 * </ul>
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class OwnTailCheck {

    private OwnTailCheck() {
    }

    /**
     * Say why a Section is occupied, if it is by the train's own tail.
     *
     * @param trainName   the train's name
     * @param rosterEntry the train's roster entry, or null
     * @param trainBlocks the occupied Blocks of the Sections allocated to the
     *                    train
     * @param section     the occupied Section the train is waiting for
     * @param records     the train records, or null if train tracking is not
     *                    in use
     * @return how it is known that the train's own tail occupies the Section,
     *         or null if it is not
     */
    @CheckForNull
    static String ownTail(@Nonnull String trainName, @CheckForNull BasicRosterEntry rosterEntry,
            @Nonnull Collection<Block> trainBlocks, @Nonnull Section section,
            @CheckForNull TrainRecordManager records) {
        List<Block> occupied = occupiedBlocks(section);
        if (occupied.isEmpty() || trainBlocks.isEmpty()) {
            return null;
        }
        if (records != null) {
            TrainRecord record = rosterEntry != null ? records.getByRosterEntry(rosterEntry) : null;
            if (record == null) {
                record = records.getByName(trainName);
            }
            if (record != null && !record.isDisposed() && record.getBlocks().containsAll(occupied)) {
                return "train tracking has " + record.getName() + " in " + names(occupied);
            }
        }
        if (joined(trainBlocks, occupied)) {
            return names(occupied) + (occupied.size() == 1 ? " joins" : " join")
                    + " the train's own Blocks with no gap, so it is probably the train's own tail";
        }
        return null;
    }

    /**
     * @param section a Section
     * @return its occupied Blocks, in order
     */
    @Nonnull
    private static List<Block> occupiedBlocks(@Nonnull Section section) {
        List<Block> occupied = new ArrayList<>();
        for (Block b : section.getBlockList()) {
            if (b.getState() == Block.OCCUPIED) {
                occupied.add(b);
            }
        }
        return occupied;
    }

    /**
     * Whether every occupied Block given can be reached from the train's
     * occupied Blocks through occupied Blocks only.
     *
     * @param trainBlocks the train's occupied Blocks
     * @param occupied    the occupied Blocks of the Section
     * @return true if all are reached
     */
    private static boolean joined(@Nonnull Collection<Block> trainBlocks, @Nonnull List<Block> occupied) {
        Set<Block> reached = new HashSet<>();
        Deque<Block> todo = new ArrayDeque<>();
        for (Block b : trainBlocks) {
            if (b.getState() == Block.OCCUPIED && reached.add(b)) {
                todo.add(b);
            }
        }
        while (!todo.isEmpty()) {
            Block b = todo.remove();
            for (Path p : b.getPaths()) {
                Block next = p.getBlock();
                if (next != null && next.getState() == Block.OCCUPIED && occupied.contains(next) && reached.add(next)) {
                    todo.add(next);
                }
            }
        }
        return reached.containsAll(occupied);
    }

    @Nonnull
    private static String names(@Nonnull List<Block> blocks) {
        List<String> names = new ArrayList<>();
        for (Block b : blocks) {
            names.add(b.getDisplayName());
        }
        return (blocks.size() == 1 ? "Block " : "Blocks ") + String.join(", ", names);
    }
}
