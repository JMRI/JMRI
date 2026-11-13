package jmri.jmrit.simulation;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;

/**
 * A place where a train enters a Block, facing into it. Each track model has
 * its own kind of entry, saying where on its drawing or paths the train is.
 * Entries are values: two entries for the same place and direction are equal.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
abstract class TrackEntry {

    /**
     * @return the Block entered
     */
    @Nonnull
    abstract Block getBlock();

    /**
     * Follow the track from this entry to the far end of its Block, with the
     * turnouts as they are now set.
     *
     * @return the passage through the Block
     */
    @Nonnull
    abstract Passage follow();

    /**
     * The entry into the neighbouring Block at this same boundary, facing the
     * other way.
     *
     * @return the entry, or null if this entry is at the end of the track
     */
    @CheckForNull
    abstract TrackEntry reverse();

    /**
     * @return a short description for messages
     */
    abstract String describe();

    @Override
    public String toString() {
        return describe();
    }
}
