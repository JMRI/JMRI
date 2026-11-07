package jmri.jmrit.simulation;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

/**
 * The track models of one simulated layout: which model a Block is followed
 * by, Block lengths, and warnings given once. Everything is read from a
 * {@link TrackSnapshot} of the layout's configuration, so it can be used from
 * the mover's own thread.
 * <p>
 * A Block drawn on a Layout Editor panel is followed on the drawing; an
 * OBlock through its Portals and paths; any other Block by its own paths,
 * each of which names the Block it leads to.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class TrackModels {

    /** Length used for a Block with none, mm. */
    static final double DEFAULT_LENGTH_MM = 300.0;

    private final Set<Object> warned = ConcurrentHashMap.newKeySet();
    private volatile TrackSnapshot snapshot = TrackSnapshot.empty();

    /**
     * @return the layout's configuration as last copied
     */
    TrackSnapshot snapshot() {
        return snapshot;
    }

    /**
     * Use a new snapshot of the layout's configuration.
     *
     * @param s the snapshot
     * @return the one it replaces
     */
    TrackSnapshot use(TrackSnapshot s) {
        TrackSnapshot old = snapshot;
        snapshot = s;
        return old;
    }

    /**
     * The length of a way through a Block: the path's own length where it has
     * one, otherwise the Block's. A Block with no length is taken as
     * {@link #DEFAULT_LENGTH_MM}, with a warning, as a train can't cross a
     * Block of no length.
     * <p>
     * {@link jmri.Path#getLengthMm()} is not used: with no length of its own a
     * path gives the length of the Block it leads to, which is the wrong Block
     * here.
     *
     * @param block      the Block
     * @param pathLength the path's own length, or 0 if it has none
     * @return length in mm
     */
    double length(Block block, float pathLength) {
        if (pathLength > 0) {
            return pathLength;
        }
        TrackSnapshot.BlockData d = snapshot.block(block);
        if (d != null && d.length > 0) {
            return d.length;
        }
        String name = name(block);
        if (firstWarning(block, "no length", name)) {
            log.warn("Block \"{}\" has no length; simulated trains take it as {} mm.", name, (int) DEFAULT_LENGTH_MM);
        }
        return DEFAULT_LENGTH_MM;
    }

    /**
     * @param block a Block
     * @return its name, for messages
     */
    String name(Block block) {
        return snapshot.name(block);
    }

    /**
     * Is this the first time a warning is to be logged on this layout? The
     * caller logs it only then, so each warning is logged once. Not
     * {@link jmri.util.LoggingUtil#warnOnce}: this is called on the mover's
     * own thread, and that keeps its record in a set that is not thread safe.
     *
     * @param about what the warning is about: its subject, the kind of
     *              warning, and the names it gives
     * @return true the first time for the same subject, kind and names; false
     *         after that
     */
    boolean firstWarning(Object... about) {
        return warned.add(java.util.Arrays.asList(about));
    }

    Message message(String key, Object... args) {
        return new Message(key, args);
    }

    /**
     * Where a train enters a Block from a neighbouring Block.
     *
     * @param block the Block
     * @param from  the neighbouring Block it came from, or null if it came
     *              from the end of the track
     * @return the entry
     * @throws IllegalArgumentException with a message saying why, if there
     *                                  is not exactly one such place
     */
    @Nonnull
    TrackEntry entryInto(@Nonnull Block block, @CheckForNull Block from) {
        TrackSnapshot snap = snapshot;
        String fromName = (from == null) ? message("EndOfTrackName").text() : name(from);
        TrackSnapshot.BlockData data = snap.block(block);
        if (data == null) {
            throw new IllegalArgumentException(message("NoPlace", block.getSystemName(), fromName).text());
        }
        if (block instanceof OBlock) {
            OBlock ob = (OBlock) block;
            if (from == null) {
                return new OBlockEntry(this, ob, null, null, false);
            }
            Portal found = null;
            for (Portal p : data.portals) {
                TrackSnapshot.PortalData pd = snap.portal(p);
                if (pd != null && pd.across(ob) == from) {
                    if (found != null) {
                        throw new IllegalArgumentException(message("MoreThanOnePlace", data.name, fromName).text());
                    }
                    found = p;
                }
            }
            if (found == null) {
                throw new IllegalArgumentException(message("NoPlace", data.name, fromName).text());
            }
            return new OBlockEntry(this, ob, found, null, false);
        }
        if (snap.isDrawn(block)) {
            List<LayoutEntry> entries = LayoutEntry.entriesInto(this, block, from);
            if (entries.isEmpty()) {
                throw new IllegalArgumentException(message("NoPlace", data.name, fromName).text());
            }
            if (entries.size() > 1) {
                throw new IllegalArgumentException(message("MoreThanOnePlace", data.name, fromName).text());
            }
            return entries.get(0);
        }
        if (from != null) {
            boolean neighbour = false;
            for (TrackSnapshot.BlockPath p : data.paths) {
                neighbour |= p.neighbour == from;
            }
            if (!neighbour) {
                throw new IllegalArgumentException(message("NoPlace", data.name, fromName).text());
            }
        }
        return new PathEntry(this, block, from, null);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackModels.class);
}
