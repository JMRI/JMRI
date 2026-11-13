package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;

/**
 * An entry into a Block whose paths each name the neighbouring Block they lead
 * to, with the turnout settings that path needs: the plain Block paths a
 * panel without a Layout Editor drawing has.
 * <p>
 * Entering from a neighbour needs the path back to that neighbour to be set
 * (the train is on that track); leaving needs exactly one other path to be
 * set. The paths' settings are evaluated by the simulator, not with
 * {@link jmri.Path#checkPathSet()}, from a snapshot of the configuration.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class PathEntry extends TrackEntry {

    private final TrackModels models;
    private final Block block;
    @CheckForNull
    private final Block from;
    @CheckForNull
    private final Block towards;

    /**
     * @param models  the layout's track models
     * @param block   the Block entered
     * @param from    the neighbour entered from, or null from the end of the
     *                track
     * @param towards the neighbour the train must leave towards, or null to
     *                go whichever way the turnouts are set
     */
    PathEntry(TrackModels models, Block block, @CheckForNull Block from, @CheckForNull Block towards) {
        this.models = models;
        this.block = block;
        this.from = from;
        this.towards = towards;
    }

    @Override
    @Nonnull
    Block getBlock() {
        return block;
    }

    private List<TrackSnapshot.BlockPath> paths(TrackSnapshot snap) {
        TrackSnapshot.BlockData d = snap.block(block);
        return d == null ? java.util.Collections.emptyList() : d.paths;
    }

    @CheckForNull
    private TrackSnapshot.BlockPath pathTo(TrackSnapshot snap, @CheckForNull Block neighbour) {
        if (neighbour == null) {
            return null;
        }
        for (TrackSnapshot.BlockPath p : paths(snap)) {
            if (p.neighbour == neighbour) {
                return p;
            }
        }
        return null;
    }

    @Override
    @Nonnull
    Passage follow() {
        TrackSnapshot snap = models.snapshot();
        TrackSnapshot.BlockPath in = pathTo(snap, from);
        if (from != null && in != null && !snap.met(in.settings)) {
            return blocked(models.message("PointsAgainstEntering", models.name(block), models.name(from)));
        }
        List<TrackSnapshot.BlockPath> ways = new ArrayList<>();
        List<TrackSnapshot.BlockPath> set = new ArrayList<>();
        for (TrackSnapshot.BlockPath p : paths(snap)) {
            Block n = p.neighbour;
            if (n == null || n == block || n == from || (towards != null && n != towards)) {
                continue;
            }
            ways.add(p);
            if (snap.met(p.settings)) {
                set.add(p);
            }
        }
        if (ways.isEmpty()) {
            double length = models.length(block, in == null ? 0 : in.length);
            return new Passage(this, length, new PathRoute(block, from, null), null,
                    new PathEntry(models, block, null, from), Passage.Stop.END_OF_TRACK,
                    models.message("EndOfTrack", models.name(block)));
        }
        if (set.isEmpty()) {
            return blocked(models.message("PointsAgainstLeaving", models.name(block)));
        }
        if (set.size() > 1) {
            return blocked(models.message("MoreThanOneWay", models.name(block)));
        }
        TrackSnapshot.BlockPath out = set.get(0);
        Block next = out.neighbour;
        double length = models.length(block, out.length);
        return new Passage(this, length, new PathRoute(block, from, next), new PathEntry(models, next, block, null),
                new PathEntry(models, block, next, from), Passage.Stop.NONE, null);
    }

    private Passage blocked(Message reason) {
        return new Passage(this, models.length(block, 0), new PathRoute(block, from, null), null,
                from != null ? new PathEntry(models, block, null, from) : null, Passage.Stop.BLOCKED, reason);
    }

    @Override
    @CheckForNull
    TrackEntry reverse() {
        if (from == null) {
            return null;
        }
        return new PathEntry(models, from, block, null);
    }

    @Override
    String describe() {
        return models.name(block) + (from != null ? " from " + models.name(from) : " from end of track");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PathEntry)) {
            return false;
        }
        PathEntry e = (PathEntry) o;
        return e.block == block && e.from == from && e.towards == towards;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, from, towards);
    }

    /**
     * A way through a Block, from one neighbour to another; either may be
     * null for the end of the track or a way not yet known.
     */
    static final class PathRoute implements Route {

        final Block block;
        @CheckForNull
        final Block from;
        @CheckForNull
        final Block to;

        PathRoute(Block block, @CheckForNull Block from, @CheckForNull Block to) {
            this.block = block;
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean sameAs(@Nonnull Route other) {
            if (!(other instanceof PathRoute)) {
                return false;
            }
            PathRoute o = (PathRoute) other;
            return o.block == block && o.from == from && o.to == to;
        }

        @Override
        public boolean reverseOf(@Nonnull Route other) {
            if (!(other instanceof PathRoute)) {
                return false;
            }
            PathRoute o = (PathRoute) other;
            return o.block == block && o.from == to && o.to == from;
        }

        @Override
        public boolean fouls(@Nonnull Route other) {
            if (!(other instanceof PathRoute)) {
                return false;
            }
            return ((PathRoute) other).block == block && !sameAs(other) && !reverseOf(other);
        }

        @Override
        @Nonnull
        public Route reversed() {
            return new PathRoute(block, to, from);
        }
    }
}
