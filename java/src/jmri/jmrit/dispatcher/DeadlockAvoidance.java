package jmri.jmrit.dispatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.Section;
import jmri.SectionManager;
import jmri.Transit;
import jmri.TransitSection;
import jmri.jmrit.dispatcher.DeadlockCheck.ArrangementCache;
import jmri.jmrit.dispatcher.DeadlockCheck.Move;
import jmri.jmrit.dispatcher.DeadlockCheck.Option;
import jmri.jmrit.dispatcher.DeadlockCheck.Position;
import jmri.jmrit.dispatcher.DeadlockCheck.Result;
import jmri.jmrit.dispatcher.DeadlockCheck.Route;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides, for {@link AutoAllocate}, whether a train may be given a Section
 * without the trains being able to end up waiting for each other for good.
 * <p>
 * A train is given track from one standing place on its Transit to the next,
 * all or nothing. A standing place is a Section it can stop in
 * ({@link AutoActiveTrain#canStopIn}), a Section marked safe in its Transit, the
 * end of its Transit and the place it turns back; a train driven by hand can
 * stop anywhere. The first Section of the run is allocated at once and the rest
 * are reserved for the train, and allocated as it asks for them.
 * <p>
 * A run is given only if, with it given, {@link DeadlockCheck} finds a way for
 * every Active Train to keep running. Sections occupied by anything that isn't
 * an Active Train must be clear of the run, but are taken to be cleared later,
 * so they don't count against the future.
 * <p>
 * Used only by the allocator thread, so nothing changes between the check and
 * the allocation.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
class DeadlockAvoidance {

    private final DispatcherFrame dispatcher;
    private final Map<String, String> reservedSections;
    private final int budget;

    // the Sections of each train's run still to be allocated, in order, and where the run ends
    private final Map<ActiveTrain, List<Section>> runs = new HashMap<>();
    private final Map<ActiveTrain, Position> runEnds = new HashMap<>();
    // what has been reported, so each reason is logged once until it changes
    private final Map<ActiveTrain, String> reported = new HashMap<>();
    // for each train refused, what the layout was then, so the same question isn't searched again
    private final Map<ActiveTrain, List<Object>> refusedAt = new HashMap<>();
    // trains reported as able to stop nowhere on their route
    private final Set<String> noPlaceReported = new HashSet<>();
    // when each train was first refused as unsafe, since its last run
    private final Map<ActiveTrain, Long> waitingSince = new HashMap<>();
    // trains last refused because their run would leave trains waiting for each other
    private final Set<ActiveTrain> refusedUnsafe = new HashSet<>();
    // the Sections each waiting train last asked to run through
    private final Map<ActiveTrain, List<Section>> wantedBy = new HashMap<>();
    // a train kept waiting too long, the Sections of its next run kept clear for it, and since when
    private ActiveTrain protectedFor = null;
    private final Set<Section> protectedSections = new HashSet<>();
    private long protectedAt = 0;

    // what checks have found, for each set of trains and routes: the same arrangements come round
    // again and again, and each check builds on the ones before. A few sets are kept, for trains
    // starting and stopping.
    private final Map<List<Object>, ArrangementCache> caches =
            new LinkedHashMap<List<Object>, ArrangementCache>(8, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<List<Object>, ArrangementCache> eldest) {
            return size() > CACHES_KEPT;
        }
    };
    static final int CACHES_KEPT = 4;

    static final long STARVED_MS = 30000;      // waiting this long gets a train's way kept clear
    static final long PROTECT_MS = 120000;     // for at most this long, then another may have a turn

    /**
     * @param dispatcher       the Dispatcher
     * @param reservedSections AutoAllocate's reservations, Section system name to train name, which
     *                         its choice of next Section and its checks already respect
     * @param budget           the most arrangements each check may search
     */
    DeadlockAvoidance(@Nonnull DispatcherFrame dispatcher, @Nonnull Map<String, String> reservedSections, int budget) {
        this.dispatcher = dispatcher;
        this.reservedSections = reservedSections;
        this.budget = budget;
    }

    /**
     * Decide whether the Section requested may be allocated now. If it starts a
     * run, the rest of the run is reserved for the train.
     *
     * @param ar the request
     * @return true to allocate the Section now
     */
    boolean mayAllocate(@Nonnull AllocationRequest ar) {
        ActiveTrain at = ar.getActiveTrain();
        Section s = ar.getSection();
        List<Section> run = runs.get(at);
        if (run != null && !run.isEmpty()) {
            // part of a run already given: allocate in order
            return run.get(0) == s;
        }
        if (aheadLimitReached(at)) {
            log.trace("{}: [{}] waits, as far ahead as its allocation setting allows", at.getTrainName(),
                    s.getDisplayName());
            return false;
        }
        Map<Route, Position> state = new LinkedHashMap<>();
        Map<ActiveTrain, Route> routes = new HashMap<>();
        for (ActiveTrain t : dispatcher.getActiveTrainsList()) {
            Route r = routeOf(t);
            if (r != null) {
                routes.put(t, r);
                state.put(r, positionOf(t, r));
            }
        }
        Route route = routes.get(at);
        if (route == null) {
            return true;    // nothing known to check against: allocate as before
        }
        // what stands in the way now, Active Train or not, decides the move; the trains
        // without an Active Train are taken to move on, so only Active Trains decide the future
        Set<Block> inTheWay = obstacles(state);
        long now = System.currentTimeMillis();
        if (protectedFor != null && (now - protectedAt > PROTECT_MS || !routes.containsKey(protectedFor))) {
            log.debug("{}: no longer kept clear for", protectedFor.getTrainName());
            waitingSince.remove(protectedFor);
            wantedBy.remove(protectedFor);
            protectedFor = null;
            protectedSections.clear();
        }
        List<Object> layout = snapshot(s, state, inTheWay);
        layout.add(protectedFor);
        if (layout.equals(refusedAt.get(at))) {
            starved(at, null, now, refusedUnsafe.contains(at));
            return false;   // nothing has changed since it was refused
        }
        DeadlockCheck check = new DeadlockCheck(inTheWay, budget);
        DeadlockCheck future = new DeadlockCheck(Collections.emptyList(), budget);
        Result last = null;
        List<Move> moves = check.moves(route, state.get(route), state);
        log.trace("{}: [{}] requested, at step {} of {}, {} way(s) to its next place", at.getTrainName(),
                s.getDisplayName(), state.get(route).getProgress(), route.size(), moves.size());
        for (Move m : moves) {
            if (m.getPath().get(0) != s || !allFree(m.getPath(), at, check)) {
                log.trace("{}: run {} is not free now", at.getTrainName(), names(m.getPath()));
                continue;
            }
            if (keptClear(at, m, check)) {
                log.trace("{}: run {} is kept clear for {}", at.getTrainName(), names(m.getPath()),
                        protectedFor.getTrainName());
                continue;
            }
            Map<Route, Position> after = new LinkedHashMap<>(state);
            after.put(route, m.getTo());
            long t0 = System.nanoTime();
            last = future.check(after, cacheFor(after));
            if (log.isDebugEnabled()) {
                log.debug("{}: check of run {} took {} ms: {}", at.getTrainName(), names(m.getPath()),
                        (System.nanoTime() - t0) / 1000000, last);
            }
            if (log.isTraceEnabled()) {
                log.trace("{}: run {} checked {}: {}", at.getTrainName(), names(m.getPath()), last, describe(after));
            }
            if (last == Result.SAFE) {
                List<Section> rest = new ArrayList<>(m.getPath());
                runs.put(at, rest);
                runEnds.put(at, m.getTo());
                for (Section x : rest) {
                    reservedSections.put(x.getSystemName(), at.getTrainName());
                }
                reported.remove(at);
                refusedAt.remove(at);
                waitingSince.remove(at);
                wantedBy.remove(at);
                refusedUnsafe.remove(at);
                if (protectedFor == at) {
                    protectedFor = null;
                    protectedSections.clear();
                }
                log.debug("{}: given {} to its next standing place", at.getTrainName(), names(rest));
                return true;
            }
        }
        refusedAt.put(at, layout);
        // the runs it wants, as if no other train were there, so a train shut out by others counts too
        List<Section> wanted = new ArrayList<>();
        for (Move m : future.moves(route, state.get(route), Collections.singletonMap(route, state.get(route)))) {
            if (m.getPath().get(0) == s) {
                wanted.addAll(m.getPath());
            }
        }
        if (last != null) {
            refusedUnsafe.add(at);
        } else {
            refusedUnsafe.remove(at);
        }
        starved(at, wanted, now, last != null);
        if (last == null) {
            log.trace("{}: [{}] waits for its run to be free", at.getTrainName(), s.getDisplayName());
            return false;
        }
        report(at, last == Result.GAVE_UP
                ? at.getTrainName() + ": [" + s.getDisplayName() + "] not allocated; the check of every train's"
                        + " route took too long, so it is treated as unsafe"
                : at.getTrainName() + ": [" + s.getDisplayName() + "] not allocated yet; given now, the trains "
                        + names(state.keySet()) + " could end up waiting for each other");
        return false;
    }

    /**
     * Tell this a Section has been allocated to a train.
     *
     * @param at the train
     * @param s  the Section
     */
    void allocated(@Nonnull ActiveTrain at, @Nonnull Section s) {
        List<Section> run = runs.get(at);
        if (run != null && !run.isEmpty() && run.get(0) == s) {
            run.remove(0);
            if (at.getTrainName().equals(reservedSections.get(s.getSystemName()))) {
                reservedSections.remove(s.getSystemName());
            }
            if (run.isEmpty()) {
                runs.remove(at);
                runEnds.remove(at);
            }
        }
    }

    /**
     * Forget a train's runs, as when it terminates or its reservations are
     * removed.
     *
     * @param trainName the train's name
     */
    void forget(@Nonnull String trainName) {
        waitingSince.keySet().removeIf(at -> at.getTrainName().equals(trainName));
        wantedBy.keySet().removeIf(at -> at.getTrainName().equals(trainName));
        refusedUnsafe.removeIf(at -> at.getTrainName().equals(trainName));
        if (protectedFor != null && protectedFor.getTrainName().equals(trainName)) {
            protectedFor = null;
            protectedSections.clear();
        }
        runs.keySet().removeIf(at -> at.getTrainName().equals(trainName));
        runEnds.keySet().removeIf(at -> at.getTrainName().equals(trainName));
        reported.keySet().removeIf(at -> at.getTrainName().equals(trainName));
        refusedAt.keySet().removeIf(at -> at.getTrainName().equals(trainName));
    }

    /*
     * What earlier checks found for these trains on these routes.
     */
    private ArrangementCache cacheFor(Map<Route, Position> state) {
        List<Object> k = new ArrayList<>();
        for (Route r : state.keySet()) {
            k.add(r.signature());
        }
        return caches.computeIfAbsent(k, x -> new ArrangementCache());
    }

    /*
     * Note a train refused: once it has been kept waiting too long for its run
     * to be free while others run, keep the Sections it wants clear until it
     * can go. A run refused as unsafe is waiting on other trains moving, which
     * keeping track clear can't bring about.
     */
    private void starved(ActiveTrain at, List<Section> wanted, long now, boolean unsafe) {
        if (unsafe) {
            // its run is free but would leave trains waiting for each other: keeping track clear
            // can't help, and could keep away the trains it is waiting for, so let them move
            waitingSince.remove(at);
            if (protectedFor == at) {
                log.debug("{}: no longer kept clear for, as its run is now waiting on other trains", at.getTrainName());
                protectedFor = null;
                protectedSections.clear();
            }
            return;
        }
        Long since = waitingSince.putIfAbsent(at, now);
        if (wanted != null) {
            wantedBy.put(at, wanted);
        }
        List<Section> runWanted = wantedBy.get(at);
        if (since == null || now - since < STARVED_MS || protectedFor != null || runWanted == null
                || runWanted.isEmpty()) {
            return;
        }
        protectedFor = at;
        protectedAt = now;
        protectedSections.addAll(runWanted);
        log.info("{} has waited {} s for {}; other trains will not be given them until {} can go",
                at.getTrainName(), (now - since) / 1000, names(protectedSections), at.getTrainName());
    }

    /*
     * Whether a run would leave a train standing in Sections kept clear for
     * another train kept waiting too long. Passing through them is allowed: a
     * train in the way may need to, to get out of the way.
     */
    private boolean keptClear(ActiveTrain at, Move move, DeadlockCheck check) {
        if (protectedFor == null || protectedFor == at) {
            return false;
        }
        for (Section x : move.getTo().getHeld()) {
            for (Section p : protectedSections) {
                if (check.conflicts(x, p)) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * What a decision depends on: the Section asked for, where every train is,
     * what each holds, and what else is in the way, with Section states.
     */
    private static List<Object> snapshot(Section asked, Map<Route, Position> state, Set<Block> inTheWay) {
        List<Object> k = new ArrayList<>();
        k.add(asked);
        for (Map.Entry<Route, Position> e : state.entrySet()) {
            k.add(e.getKey().getName());
            k.add(e.getKey().size());
            k.add(e.getValue().getProgress());
            k.add(e.getValue().getHeld());
        }
        k.add(new HashSet<>(inTheWay));
        return k;
    }

    /**
     * Check that the trains as they are now can all keep running, as when a
     * train is started, and log a warning naming them if they can't.
     *
     * @param at the train just started
     * @return the result
     */
    Result checkNow(@Nonnull ActiveTrain at) {
        Map<Route, Position> state = new LinkedHashMap<>();
        for (ActiveTrain t : dispatcher.getActiveTrainsList()) {
            Route r = routeOf(t);
            if (r != null) {
                state.put(r, positionOf(t, r));
            }
        }
        Result result = new DeadlockCheck(Collections.emptyList(), budget).check(state, cacheFor(state));
        if (result != Result.SAFE) {
            log.debug("{}: start checked {}: {}", at.getTrainName(), result, describe(state));
            log.warn("With {} started, the trains {} can't all run their Transits: they would end up waiting"
                    + " for each other. Terminate one of them, or change its Transit.",
                    at.getTrainName(), names(state.keySet()));
        }
        return result;
    }

    /**
     * Search the arrangements of the trains left after one terminates, so the
     * first request after it doesn't wait for the search.
     *
     * @param trainName the train's name
     */
    void prepare(@Nonnull String trainName) {
        Map<Route, Position> state = new LinkedHashMap<>();
        for (ActiveTrain t : dispatcher.getActiveTrainsList()) {
            if (!t.getTrainName().equals(trainName)) {
                Route r = routeOf(t);
                if (r != null) {
                    state.put(r, positionOf(t, r));
                }
            }
        }
        if (!state.isEmpty()) {
            new DeadlockCheck(Collections.emptyList(), budget).check(state, cacheFor(state));
        }
    }

    /*
     * Whether the train already has as much allocated ahead of it as its
     * allocation setting allows: a number of Sections, one run at a time for
     * safe Sections, or no limit.
     */
    private boolean aheadLimitReached(ActiveTrain at) {
        int method = at.getAllocateMethod();
        if (method == ActiveTrain.ALLOCATE_AS_FAR_AS_IT_CAN) {
            return false;
        }
        int ahead = 0;
        for (AllocatedSection as : at.getAllocatedSectionList()) {
            if (as.getSection().getOccupancy() != Section.OCCUPIED && !as.getExited()) {
                ahead++;
            }
        }
        if (method == ActiveTrain.ALLOCATE_BY_SAFE_SECTIONS) {
            return ahead > 0;
        }
        return ahead >= method;
    }

    /*
     * Whether every Section of a run is free now: not allocated or reserved to
     * another train, sharing no Block with one that is, and not occupied, or
     * else still allocated to this train behind it.
     */
    private boolean allFree(List<Section> path, ActiveTrain at, DeadlockCheck check) {
        List<Section> others = new ArrayList<>();
        for (ActiveTrain t : dispatcher.getActiveTrainsList()) {
            if (t != at) {
                for (AllocatedSection as : t.getAllocatedSectionList()) {
                    others.add(as.getSection());
                }
                List<Section> run = runs.get(t);
                if (run != null) {
                    others.addAll(run);
                }
            }
        }
        List<Section> own = new ArrayList<>();
        for (AllocatedSection as : at.getAllocatedSectionList()) {
            own.add(as.getSection());
        }
        for (Section x : path) {
            // the train's own Sections behind it, still to be released, will be free when it gets there
            if (!own.contains(x) && (x.getState() != Section.FREE || x.getOccupancy() == Section.OCCUPIED)) {
                return false;
            }
            String holder = reservedSections.get(x.getSystemName());
            if (holder != null && !holder.equals(at.getTrainName())) {
                return false;
            }
            for (Section o : others) {
                if (check.conflicts(x, o)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Blocks occupied by something other than an Active Train: occupied, and in
     * none of the Sections held in the check or allocated or reserved to any
     * train. Blocks, not Sections: a Section that overlaps a train's (as Signal
     * Mast Logic's own Sections do) is occupied by that train, not by something
     * else, and its other Blocks may be free.
     */
    private Set<Block> obstacles(Map<Route, Position> state) {
        Set<Section> held = new HashSet<>();
        for (Position p : state.values()) {
            held.addAll(p.getHeld());
        }
        for (ActiveTrain t : dispatcher.getActiveTrainsList()) {
            for (AllocatedSection as : t.getAllocatedSectionList()) {
                held.add(as.getSection());
            }
        }
        for (List<Section> run : runs.values()) {
            held.addAll(run);
        }
        Set<Block> trains = new HashSet<>();
        for (Section x : held) {
            trains.addAll(x.getBlockList());
        }
        Set<Block> found = new HashSet<>();
        for (Section x : InstanceManager.getDefault(SectionManager.class).getNamedBeanSet()) {
            for (Block b : x.getBlockList()) {
                if (b.getState() == Block.OCCUPIED && !trains.contains(b)) {
                    found.add(b);
                }
            }
        }
        return found;
    }

    /**
     * The route a train follows: its Transit's Sections from the first to its
     * end, back again if it reverses at the end, and round again if it is reset
     * when done.
     *
     * @param at the train
     * @return the route, or null if it has no Transit
     */
    Route routeOf(@Nonnull ActiveTrain at) {
        Transit transit = at.getTransit();
        if (transit == null) {
            return null;
        }
        int end = at.getEndBlockSectionSequenceNumber();
        if (end < 1) {
            end = transit.getMaxSequence();
        }
        boolean reverses = at.getReverseAtEnd();
        boolean resets = at.getResetWhenDone();
        List<List<Option>> steps = new ArrayList<>();
        for (int seq = 1; seq <= end; seq++) {
            // where it stops at the end, and where a train that runs back and forth turns
            boolean turn = (seq == end && (reverses || !resets)) || (seq == 1 && reverses && resets);
            steps.add(options(at, transit, seq, false, turn));
        }
        boolean looping = resets;
        if (reverses) {
            int last = resets ? 2 : 1;
            for (int seq = end - 1; seq >= last; seq--) {
                steps.add(options(at, transit, seq, true, seq == 1));
            }
        } else if (resets) {
            // a Transit that is run again starts again with its second Section, its
            // first and last being the same Section
            if (!sameSections(transit, 1, end)) {
                looping = false;
            } else {
                steps.remove(steps.size() - 1);
            }
        }
        if (!hasPlace(steps, looping)) {
            // a train that can stop nowhere on its route has nowhere better: every Section counts
            List<List<Option>> all = new ArrayList<>();
            for (List<Option> step : steps) {
                List<Option> list = new ArrayList<>();
                for (Option o : step) {
                    list.add(new Option(o.getSection(), o.getDirection(), true));
                }
                all.add(list);
            }
            steps = all;
            if (!noPlaceReported.contains(at.getTrainName())) {
                noPlaceReported.add(at.getTrainName());
                log.info("{} can't stop in any Section of its Transit, as each is shorter than the train and"
                        + " has no stopping sensor for it, so it is given track a Section at a time",
                        at.getTrainName());
            }
        }
        long length = at.getMaxTrainLengthMM();
        return new Route(at.getTrainName(), steps, looping, length);
    }

    /*
     * Whether a route has a place other than where it ends.
     */
    private static boolean hasPlace(List<List<Option>> steps, boolean looping) {
        int last = looping ? steps.size() : steps.size() - 1;
        for (int i = 0; i < last; i++) {
            for (Option o : steps.get(i)) {
                if (o.isPlace()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sameSections(Transit transit, int a, int b) {
        return !Collections.disjoint(transit.getSectionListBySeq(a), transit.getSectionListBySeq(b));
    }

    private List<Option> options(ActiveTrain at, Transit transit, int seq, boolean back, boolean turn) {
        List<Option> list = new ArrayList<>();
        for (TransitSection ts : transit.getTransitSectionListBySeq(seq)) {
            int dir = ts.getDirection();
            if (back) {
                dir = (dir == Section.FORWARD) ? Section.REVERSE : Section.FORWARD;
            }
            list.add(new Option(ts.getSection(), dir, turn || isPlace(at, ts, dir)));
        }
        return list;
    }

    private static boolean isPlace(ActiveTrain at, TransitSection ts, int dir) {
        if (ts.isSafe()) {
            return true;
        }
        AutoActiveTrain aat = at.getAutoActiveTrain();
        if (at.getMode() != ActiveTrain.AUTOMATIC || aat == null) {
            return true;    // a driver can stop anywhere
        }
        return aat.canStopIn(ts.getSection(), dir);
    }

    /**
     * Where a train is: the end of the run it has been given, or else its last
     * allocated Section, holding what it has allocated.
     *
     * @param at    the train
     * @param route its route
     * @return its position
     */
    Position positionOf(@Nonnull ActiveTrain at, @Nonnull Route route) {
        Position runEnd = runEnds.get(at);
        if (runEnd != null) {
            return runEnd;
        }
        List<Section> held = new ArrayList<>();
        List<AllocatedSection> list = at.getAllocatedSectionList();
        for (int i = list.size() - 1; i >= 0; i--) {
            held.add(list.get(i).getSection());
        }
        Section last = at.getLastAllocatedSection();
        int seq = at.getLastAllocatedSectionSeqNumber();
        if (last == null || seq < 1) {
            seq = Math.max(1, at.getStartBlockSectionSequenceNumber());
            if (held.isEmpty()) {
                Section start = at.getStartBlock() == null ? null : sectionHolding(at, seq);
                if (start != null) {
                    held.add(start);
                }
            }
            return new Position(seq - 1, held);
        }
        int end = at.getEndBlockSectionSequenceNumber();
        if (end < 1) {
            end = at.getTransit().getMaxSequence();
        }
        int progress = at.isAllocationReversed() ? (end - 1) + (end - seq) : seq - 1;
        return new Position(progress, held);
    }

    private static Section sectionHolding(ActiveTrain at, int seq) {
        for (Section s : at.getTransit().getSectionListBySeq(seq)) {
            if (s.containsBlock(at.getStartBlock())) {
                return s;
            }
        }
        return null;
    }

    private void report(ActiveTrain at, String why) {
        if (!why.equals(reported.get(at))) {
            reported.put(at, why);
            log.info("{}", why);
        }
    }

    private static String describe(Map<Route, Position> state) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<Route, Position> e : state.entrySet()) {
            b.append(e.getKey().getName()).append(" (length ").append(e.getKey().getLength()).append(", ")
                    .append(e.getKey().isLooping() ? "looping, " : "").append(e.getKey().size()).append(" steps) at ")
                    .append(e.getValue().getProgress()).append(" holding ").append(names(e.getValue().getHeld()))
                    .append("; ");
        }
        return b.toString();
    }

    private static String names(Iterable<?> items) {
        List<String> n = new ArrayList<>();
        for (Object o : items) {
            n.add(o instanceof Section ? ((Section) o).getDisplayName()
                    : o instanceof Route ? ((Route) o).getName() : String.valueOf(o));
        }
        return n.toString();
    }

    private static final Logger log = LoggerFactory.getLogger(DeadlockAvoidance.class);
}
