package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;

/**
 * An entry into an OBlock through one of its Portals, as on a Control Panel
 * Editor panel. A train follows the OPath from that Portal whose turnout
 * settings are all met, evaluated here from the settings themselves, as
 * copied in a snapshot of the configuration.
 * <p>
 * These panels have no drawn track, so the routes come from the same paths
 * JMRI uses: a weaker check than following a Layout Editor drawing.
 * Only the OBlock, OPath and Portal configuration is read; nothing of
 * Warrants or Tracker is used.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class OBlockEntry extends TrackEntry {

    private final TrackModels models;
    private final OBlock block;
    @CheckForNull
    private final Portal portal;
    @CheckForNull
    private final Portal towards;
    private final boolean towardsEnd;

    /**
     * @param models     the layout's track models
     * @param block      the OBlock entered
     * @param portal     the Portal entered through, or null from the end of
     *                   the track
     * @param towards    the Portal the train must leave through, or null
     * @param towardsEnd true if the train must leave by a path that ends in
     *                   the block (towards must then be null)
     */
    OBlockEntry(TrackModels models, OBlock block, @CheckForNull Portal portal, @CheckForNull Portal towards,
            boolean towardsEnd) {
        this.models = models;
        this.block = block;
        this.portal = portal;
        this.towards = towards;
        this.towardsEnd = towardsEnd;
    }

    @Override
    @Nonnull
    Block getBlock() {
        return block;
    }

    @CheckForNull
    private static Portal otherEnd(TrackSnapshot.BlockOPath p, @CheckForNull Portal end) {
        return p.from == end ? p.to : p.from;
    }

    private static boolean touches(TrackSnapshot.BlockOPath p, @CheckForNull Portal end) {
        return p.from == end || p.to == end;
    }

    @CheckForNull
    private Block across(TrackSnapshot snap, Portal q) {
        TrackSnapshot.PortalData d = snap.portal(q);
        return d == null ? null : d.across(block);
    }

    @Override
    @Nonnull
    Passage follow() {
        TrackSnapshot snap = models.snapshot();
        TrackSnapshot.BlockData data = snap.block(block);
        List<TrackSnapshot.BlockOPath> set = new ArrayList<>();
        boolean any = false;
        for (TrackSnapshot.BlockOPath op : (data == null) ? List.<TrackSnapshot.BlockOPath>of() : data.oPaths) {
            if (!touches(op, portal)) {
                continue;
            }
            Portal exit = otherEnd(op, portal);
            if ((towards != null && exit != towards) || (towardsEnd && exit != null)) {
                continue;
            }
            any = true;
            if (snap.met(op.settings)) {
                set.add(op);
            }
        }
        if (!any) {
            return blocked(models.message("NoPathThrough", models.name(block)));
        }
        if (set.isEmpty()) {
            return blocked(models.message("PointsAgainstLeaving", models.name(block)));
        }
        if (set.size() > 1) {
            return blocked(models.message("MoreThanOneWay", models.name(block)));
        }
        TrackSnapshot.BlockOPath path = set.get(0);
        Portal exit = otherEnd(path, portal);
        double length = models.length(block, path.length);
        OPathRoute route = new OPathRoute(block, path.path, portal, exit);
        OBlockEntry back = new OBlockEntry(models, block, exit, portal, portal == null);
        Block neighbour = (exit != null) ? across(snap, exit) : null;
        if (!(neighbour instanceof OBlock)) {
            return new Passage(this, length, route, null, back, Passage.Stop.END_OF_TRACK,
                    models.message("EndOfTrack", models.name(block)));
        }
        return new Passage(this, length, route, new OBlockEntry(models, (OBlock) neighbour, exit, null, false),
                back, Passage.Stop.NONE, null);
    }

    private Passage blocked(Message reason) {
        return new Passage(this, models.length(block, 0), new OPathRoute(block, null, portal, null), null, null,
                Passage.Stop.BLOCKED, reason);
    }

    @Override
    @CheckForNull
    TrackEntry reverse() {
        if (portal == null) {
            return null;
        }
        Block other = across(models.snapshot(), portal);
        if (!(other instanceof OBlock)) {
            return null;
        }
        return new OBlockEntry(models, (OBlock) other, portal, null, false);
    }

    @Override
    String describe() {
        TrackSnapshot.PortalData d = (portal == null) ? null : models.snapshot().portal(portal);
        return models.name(block) + (d != null ? " through " + d.name : " from end of track");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OBlockEntry)) {
            return false;
        }
        OBlockEntry e = (OBlockEntry) o;
        return e.block == block && e.portal == portal && e.towards == towards && e.towardsEnd == towardsEnd;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, portal, towards, towardsEnd);
    }

    /**
     * An OPath followed from one end to the other.
     */
    static final class OPathRoute implements Route {

        final OBlock block;
        @CheckForNull
        final OPath path;
        @CheckForNull
        final Portal from;
        @CheckForNull
        final Portal to;

        OPathRoute(OBlock block, @CheckForNull OPath path, @CheckForNull Portal from, @CheckForNull Portal to) {
            this.block = block;
            this.path = path;
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean sameAs(@Nonnull Route other) {
            if (!(other instanceof OPathRoute)) {
                return false;
            }
            OPathRoute o = (OPathRoute) other;
            return o.block == block && o.path == path && o.from == from && o.to == to;
        }

        @Override
        public boolean reverseOf(@Nonnull Route other) {
            if (!(other instanceof OPathRoute)) {
                return false;
            }
            OPathRoute o = (OPathRoute) other;
            return o.block == block && o.path == path && o.from == to && o.to == from;
        }

        @Override
        public boolean fouls(@Nonnull Route other) {
            if (!(other instanceof OPathRoute)) {
                return false;
            }
            return ((OPathRoute) other).block == block && !sameAs(other) && !reverseOf(other);
        }

        @Override
        @Nonnull
        public Route reversed() {
            return new OPathRoute(block, path, to, from);
        }
    }
}
