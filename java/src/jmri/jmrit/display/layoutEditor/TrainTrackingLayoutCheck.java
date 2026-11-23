package jmri.jmrit.display.layoutEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.Turnout;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.trainrecord.SetUpCheck;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Finding;
import jmri.jmrit.trainrecord.TrainTrackingChecks.Kind;

import org.openide.util.lookup.ServiceProvider;

/**
 * Checks of Layout Editor panels for train tracking: a turnout or crossover
 * whose two routes lead into the same Block, so which of them a train took
 * can't be told from detection. That is so when:
 * <ul>
 * <li>both routes from a turnout, or both tracks at one end of a crossover,
 * lead into the same other Block, as a balloon loop or a run-round loop
 * drawn as one Block does;</li>
 * <li>both routes meet again at another turnout without leaving the
 * turnout's own Block, as a loop drawn inside one Block does.</li>
 * </ul>
 * A route is followed along track and anchors until it reaches another
 * Block or a turnout; level crossings and turntables are not followed
 * through. A turnout in a short Block of its own, whose routes leave it for
 * different Blocks, is the arrangement that tracking needs, and is not
 * reported.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@ServiceProvider(service = SetUpCheck.class)
public class TrainTrackingLayoutCheck implements SetUpCheck {

    /** How far a route is followed, as a guard against a malformed panel. */
    private static final int MOST_STEPS = 1000;

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public List<Finding> check() {
        Set<LayoutTurnout> turnouts = new LinkedHashSet<>();
        for (LayoutEditor le : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            turnouts.addAll(le.getLayoutTurnouts());
        }
        return check(turnouts);
    }

    /**
     * Check some turnouts and crossovers (not slips).
     *
     * @param turnouts the turnouts
     * @return the findings, possibly none
     */
    @Nonnull
    static List<Finding> check(@Nonnull Collection<LayoutTurnout> turnouts) {
        List<Finding> found = new ArrayList<>();
        Set<Set<Object>> reported = new HashSet<>();
        for (LayoutTurnout lt : turnouts) {
            LayoutTurnout.TurnoutType type = lt.getTurnoutType();
            if (LayoutTurnout.isTurnoutTypeTurnout(type)) {
                // A is the throat; B and C the two routes
                pair(lt, lt.getConnectB(), lt.getLayoutBlockB(), lt.getConnectC(), lt.getLayoutBlockC(), found,
                        reported);
            } else if (LayoutTurnout.isTurnoutTypeXover(type)) {
                // A-B and D-C are the two tracks: A and D at one end, B and C at the other
                pair(lt, lt.getConnectA(), lt.getLayoutBlock(), lt.getConnectD(), lt.getLayoutBlockD(), found,
                        reported);
                pair(lt, lt.getConnectB(), lt.getLayoutBlockB(), lt.getConnectC(), lt.getLayoutBlockC(), found,
                        reported);
            }
        }
        return found;
    }

    private static void pair(LayoutTurnout lt, @CheckForNull LayoutTrack first, @CheckForNull LayoutBlock firstOwn,
            @CheckForNull LayoutTrack second, @CheckForNull LayoutBlock secondOwn, List<Finding> found,
            Set<Set<Object>> reported) {
        if (firstOwn == null || firstOwn != secondOwn) {
            return; // the two routes are already in different Blocks here
        }
        Object a = leadsTo(first, lt, firstOwn);
        Object b = leadsTo(second, lt, secondOwn);
        if (a == null || a != b) {
            return;
        }
        if (a instanceof LayoutBlock) {
            Block block = ((LayoutBlock) a).getBlock();
            if (block == null || !reported.add(Set.of(lt, a))) {
                return;
            }
            found.add(new Finding(Kind.ROUTES_INTO_ONE_BLOCK, block, related(lt),
                    Bundle.getMessage("CheckRoutesIntoOneBlock", nameOf(lt), block.getDisplayName())));
        } else if (a instanceof LayoutTurnout) {
            Block block = firstOwn.getBlock();
            if (block == null || !reported.add(Set.of(lt, a))) {
                return;
            }
            List<NamedBean> related = related(lt);
            related.addAll(related((LayoutTurnout) a));
            found.add(new Finding(Kind.ROUTES_INTO_ONE_BLOCK, block, related,
                    Bundle.getMessage("CheckRoutesRejoinInBlock", nameOf(lt), nameOf((LayoutTurnout) a),
                            block.getDisplayName())));
        }
    }

    /**
     * Where a route leads, followed along track and anchors.
     *
     * @param first the track the route starts on
     * @param from  the turnout it starts from
     * @param own   the Block the turnout's end of the route is in
     * @return the first other Block reached, a turnout reached without
     *         leaving {@code own}, or null if the route ends or can't be
     *         followed
     */
    @CheckForNull
    static Object leadsTo(@CheckForNull LayoutTrack first, @Nonnull LayoutTrack from, @Nonnull LayoutBlock own) {
        LayoutTrack previous = from;
        LayoutTrack t = first;
        for (int steps = 0; t != null && steps < MOST_STEPS; steps++) {
            if (t instanceof TrackSegment) {
                TrackSegment ts = (TrackSegment) t;
                LayoutBlock lb = ts.getLayoutBlock();
                if (lb != null && lb != own) {
                    return lb;
                }
                LayoutTrack next = ts.getConnect1() == previous ? ts.getConnect2() : ts.getConnect1();
                previous = t;
                t = next;
            } else if (t instanceof PositionablePoint) {
                PositionablePoint pp = (PositionablePoint) t;
                LayoutTrack next = pp.getConnect1() == previous ? pp.getConnect2() : pp.getConnect1();
                previous = t;
                t = next;
            } else if (t instanceof LayoutTurnout) {
                LayoutBlock lb = ((LayoutTurnout) t).getLayoutBlock();
                return lb != null && lb != own ? lb : t;
            } else {
                return null; // a level crossing or turntable: not followed through
            }
        }
        return null;
    }

    private static List<NamedBean> related(LayoutTurnout lt) {
        List<NamedBean> related = new ArrayList<>();
        Turnout t = lt.getTurnout();
        if (t != null) {
            related.add(t);
        }
        return related;
    }

    private static String nameOf(LayoutTurnout lt) {
        Turnout t = lt.getTurnout();
        return t != null ? t.getDisplayName() : lt.getName();
    }
}
