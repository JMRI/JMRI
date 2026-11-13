package jmri.jmrit.dispatcher;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.Section;

/**
 * Checks whether a set of trains, each following its route from place to place,
 * can all keep running.
 * <p>
 * A train moves from one standing place on its route to the next, all or
 * nothing: every Section between, and the place itself, must be free of other
 * trains when it sets off. When it has arrived it holds the Sections it covers,
 * counted back from the end of the place by its length, and nothing else.
 * <p>
 * A set of trains can keep running if, from where they are, they can reach a
 * set of arrangements they can go round for ever, with every looping train
 * running on (laps of its route) and every other train at the end of its route.
 * The arrangements reachable by such moves are found, a looping train's step
 * counted round its lap, and the check looks for a strongly connected group of
 * them with a move of every looping train inside it. Looking only one move
 * ahead is not enough: two trains can each make a move that leaves the other
 * able to make one more, and still be unable to pass a move later.
 * <p>
 * The search stops after a set number of arrangements, and then answers
 * {@link Result#GAVE_UP}, which callers treat as unsafe.
 * <p>
 * Sections conflict if they are the same Section or share a Block.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class DeadlockCheck {

    /** The answer of a check. */
    public enum Result {
        /** The trains can keep running. */
        SAFE,
        /** No order of moves lets the trains keep running. */
        UNSAFE,
        /** The search stopped before finding an answer; treat as unsafe. */
        GAVE_UP
    }

    /** The default number of arrangements searched before giving up. */
    public static final int DEFAULT_BUDGET = 200000;

    /** One Section a train may use at a step of its route. */
    public static final class Option {

        private final Section section;
        private final int direction;
        private final boolean place;

        /**
         * @param section   the Section
         * @param direction its direction of travel, {@link Section#FORWARD} or {@link Section#REVERSE}
         * @param place     true if the train can stop here, standing clear of the move it makes next
         */
        public Option(@Nonnull Section section, int direction, boolean place) {
            this.section = section;
            this.direction = direction;
            this.place = place;
        }

        @Nonnull
        public Section getSection() {
            return section;
        }

        public int getDirection() {
            return direction;
        }

        public boolean isPlace() {
            return place;
        }
    }

    /** A train's route: its steps, and whether it loops or ends. */
    public static final class Route {

        private final String name;
        private final List<List<Option>> steps;
        private final boolean looping;
        private final long length;

        /**
         * @param name    the train's name, for messages
         * @param steps   for each step of the route, in order, the Sections the train may use there;
         *                for a looping route, one lap, after which the route starts again at step 0
         * @param looping true if the route repeats, false if the train stops at its last step
         * @param length  the train's length in mm, or 0 if not known
         */
        public Route(@Nonnull String name, @Nonnull List<List<Option>> steps, boolean looping, long length) {
            if (steps.isEmpty()) {
                throw new IllegalArgumentException("a route needs at least one step");
            }
            this.name = name;
            List<List<Option>> copy = new ArrayList<>();
            for (List<Option> s : steps) {
                copy.add(Collections.unmodifiableList(new ArrayList<>(s)));
            }
            this.steps = Collections.unmodifiableList(copy);
            this.looping = looping;
            this.length = Math.max(0, length);
        }

        @Nonnull
        public String getName() {
            return name;
        }

        public int size() {
            return steps.size();
        }

        public boolean isLooping() {
            return looping;
        }

        public long getLength() {
            return length;
        }

        /**
         * The Sections the train may use at a step, counting on from the end of
         * a looping route into its next lap.
         *
         * @param progress the step, counted from the start of the route
         * @return the options, or an empty list past the end of a route that doesn't loop
         */
        @Nonnull
        public List<Option> optionsAt(int progress) {
            if (progress < 0) {
                return Collections.emptyList();
            }
            if (looping) {
                return steps.get(progress % steps.size());
            }
            return progress < steps.size() ? steps.get(progress) : Collections.emptyList();
        }

        /**
         * @return what the route is, for comparing routes built at different
         *         times: whether it loops, the train's length, and each step's
         *         Sections with their directions and places
         */
        @Nonnull
        public List<Object> signature() {
            if (signature == null) {
                List<Object> k = new ArrayList<>();
                k.add(looping);
                k.add(length);
                for (List<Option> step : steps) {
                    for (Option o : step) {
                        k.add(o.getSection());
                        k.add(o.getDirection());
                        k.add(o.isPlace());
                    }
                    k.add(null);
                }
                signature = Collections.unmodifiableList(k);
            }
            return signature;
        }

        private List<Object> signature = null;

        /**
         * @param progress a step
         * @return true if a route that doesn't loop ends at this step
         */
        public boolean isEnd(int progress) {
            return !looping && progress >= steps.size() - 1;
        }
    }

    /**
     * Where a train is: the step its head is at, and the Sections it holds, head
     * first.
     */
    public static final class Position {

        private final int progress;
        private final List<Section> held;

        /**
         * @param progress the step of the route the train's head has reached
         * @param held     the Sections the train holds, the one at its head first
         */
        public Position(int progress, @Nonnull List<Section> held) {
            this.progress = progress;
            this.held = Collections.unmodifiableList(new ArrayList<>(held));
        }

        public int getProgress() {
            return progress;
        }

        @Nonnull
        public List<Section> getHeld() {
            return held;
        }
    }

    /** A move from one place to the next. */
    public static final class Move {

        private final List<Section> path;
        private final Position to;

        Move(List<Section> path, Position to) {
            this.path = Collections.unmodifiableList(path);
            this.to = to;
        }

        /**
         * @return the Sections the train must be given, in order, ending with the place
         */
        @Nonnull
        public List<Section> getPath() {
            return path;
        }

        /**
         * @return where the train is when it has arrived
         */
        @Nonnull
        public Position getTo() {
            return to;
        }
    }

    private final Set<Block> obstacles;
    private final int budget;
    private final Map<Section, Set<Block>> blocks = new HashMap<>();

    /**
     * @param obstacles Blocks occupied by something that isn't in the check, such as
     *                  a train with no route, which are taken as occupied throughout.
     *                  Blocks, not Sections: Sections overlap (Signal Mast Logic makes
     *                  its own over the same Blocks), and only the Blocks themselves
     *                  are in the way
     * @param budget    the most arrangements to search before giving up
     */
    public DeadlockCheck(@Nonnull Collection<Block> obstacles, int budget) {
        this.obstacles = new HashSet<>(obstacles);
        this.budget = budget;
    }

    /**
     * Check whether trains in the given positions can keep running.
     *
     * @param start each train's route and position
     * @return the answer
     */
    @Nonnull
    public Result check(@Nonnull Map<Route, Position> start) {
        return check(start, new ArrangementCache());
    }

    /**
     * What has been found out about the arrangements of one set of trains on
     * their routes: for each arrangement searched, whether the trains can keep
     * running from it, like a transposition table in game-tree search. With no
     * obstacles, which arrangements follow which depends only on the routes, so
     * the answers hold for as long as the same trains run the same routes, and
     * each check builds on the ones before: it looks up the arrangement, or
     * searches only the arrangements not yet known, stopping at the first from
     * which the trains are known to keep running. Use one cache only for checks
     * of the same routes, in the same order, with no obstacles.
     */
    public static final class ArrangementCache {

        private final Map<List<Object>, Boolean> known = new HashMap<>();
        private final int capacity;

        public ArrangementCache() {
            this(DEFAULT_CACHE_SIZE);
        }

        /**
         * @param capacity the most arrangements to keep; when full, it starts again
         */
        public ArrangementCache(int capacity) {
            this.capacity = capacity;
        }

        Boolean get(List<Object> k) {
            return known.get(k);
        }

        void put(List<Object> k, boolean good) {
            if (known.size() >= capacity) {
                known.clear();
            }
            known.put(k, good);
        }

        /**
         * @return the number of arrangements known
         */
        public int size() {
            return known.size();
        }
    }

    /** The most arrangements an ArrangementCache keeps by default. */
    public static final int DEFAULT_CACHE_SIZE = 1000000;

    /**
     * As {@link #check(Map)}, building on what earlier checks of the same
     * trains found.
     *
     * @param start where each train is now
     * @param cache what earlier checks of the same routes, with no obstacles, found
     * @return SAFE, UNSAFE, or GAVE_UP if the budget ran out first
     */
    @Nonnull
    public Result check(@Nonnull Map<Route, Position> start, @Nonnull ArrangementCache cache) {
        List<List<Route>> groups = groups(start);
        if (groups.size() == 1) {
            return checkGroup(start, cache, 0);
        }
        List<Route> all = new ArrayList<>(start.keySet());
        // trains in different groups never use the same track, so they can keep running together
        // if, and only if, each group can: searching each on its own adds their arrangements
        // instead of multiplying them
        Result result = Result.SAFE;
        for (int g = 0; g < groups.size(); g++) {
            Map<Route, Position> part = new LinkedHashMap<>();
            List<Integer> which = new ArrayList<>();
            for (Route r : groups.get(g)) {
                part.put(r, start.get(r));
                which.add(all.indexOf(r));
            }
            Result r = checkGroup(part, cache, which);
            if (r == Result.UNSAFE) {
                return r;
            }
            if (r == Result.GAVE_UP) {
                result = r;
            }
        }
        return result;
    }

    /*
     * The trains split into groups that never use the same track: two trains
     * are in one group if any Section of one's route, or held by it, shares a
     * Block with any of the other's, directly or through other trains.
     */
    private List<List<Route>> groups(Map<Route, Position> start) {
        List<Route> routes = new ArrayList<>(start.keySet());
        int n = routes.size();
        List<Set<Section>> uses = new ArrayList<>();
        for (Route r : routes) {
            Set<Section> u = new HashSet<>(start.get(r).getHeld());
            for (int i = 0; i < r.size(); i++) {
                for (Option o : r.optionsAt(i)) {
                    u.add(o.getSection());
                }
            }
            uses.add(u);
        }
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (find(parent, i) != find(parent, j) && share(uses.get(i), uses.get(j))) {
                    parent[find(parent, i)] = find(parent, j);
                }
            }
        }
        Map<Integer, List<Route>> byRoot = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            byRoot.computeIfAbsent(find(parent, i), x -> new ArrayList<>()).add(routes.get(i));
        }
        return new ArrayList<>(byRoot.values());
    }

    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private boolean share(Set<Section> a, Set<Section> b) {
        for (Section x : a) {
            for (Section y : b) {
                if (conflicts(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Result checkGroup(Map<Route, Position> start, ArrangementCache cache, Object group) {
        List<Route> routes = new ArrayList<>(start.keySet());
        Map<Route, Position> first = normalised(routes, start);
        List<Object> firstKey = key(routes, first, group);
        Boolean answer = cache.get(firstKey);
        if (answer != null) {
            return answer ? Result.SAFE : Result.UNSAFE;
        }
        if (keepsRunningInTurn(routes, start)) {
            cache.put(firstKey, true);
            return Result.SAFE;
        }
        // every arrangement the trains can reach that isn't known yet, each looping train's step taken round its lap
        Map<List<Object>, Integer> index = new HashMap<>();
        List<List<Object>> keys = new ArrayList<>();
        List<Map<Route, Position>> states = new ArrayList<>();
        List<List<int[]>> edges = new ArrayList<>();     // to state, moving route
        Deque<Integer> todo = new ArrayDeque<>();
        index.put(firstKey, 0);
        keys.add(firstKey);
        states.add(first);
        edges.add(new ArrayList<>());
        todo.push(0);
        while (!todo.isEmpty()) {
            int i = todo.pop();
            Map<Route, Position> state = states.get(i);
            for (int t = 0; t < routes.size(); t++) {
                Route r = routes.get(t);
                Position p = state.get(r);
                if (r.isEnd(p.getProgress())) {
                    continue;
                }
                for (Move m : moves(r, p, state)) {
                    Map<Route, Position> s = new LinkedHashMap<>(state);
                    s.put(r, normalised(r, m.getTo()));
                    List<Object> k = key(routes, s, group);
                    Boolean was = cache.get(k);
                    if (was != null) {
                        if (was) {
                            // reaches an arrangement the trains are known to keep running from
                            cache.put(firstKey, true);
                            return Result.SAFE;
                        }
                        continue;   // known to lead only to trains waiting for each other
                    }
                    Integer j = index.get(k);
                    if (j == null) {
                        if (states.size() >= budget) {
                            return Result.GAVE_UP;
                        }
                        j = states.size();
                        index.put(k, j);
                        keys.add(k);
                        states.add(s);
                        edges.add(new ArrayList<>());
                        todo.push(j);
                    }
                    edges.get(i).add(new int[]{j, t});
                }
            }
        }
        // all searched to the end: record for each whether the trains can keep running from it
        boolean[] good = canKeepRunning(routes, states, edges);
        for (int i = 0; i < keys.size(); i++) {
            cache.put(keys.get(i), good[i]);
        }
        return good[0] ? Result.SAFE : Result.UNSAFE;
    }

    /*
     * A quick way to show the trains can keep running: move them in turn and
     * see whether an arrangement comes round again with every looping train
     * having moved since, and every other train at its end. The first try moves
     * each time the train that can move that is furthest behind, as a part of
     * its lap, so the trains tend to come round together; later tries vary the
     * order and the choice of way, from a fixed seed so the answer is repeatable.
     * Finding such a cycle shows the trains can keep running; not finding one
     * shows nothing, and the full search decides.
     */
    private boolean keepsRunningInTurn(List<Route> routes, Map<Route, Position> start) {
        Random random = new Random(1);
        for (int attempt = 0; attempt < QUICK_TRIES; attempt++) {
            if (runInTurn(routes, start, attempt == 0 ? null : random)) {
                return true;
            }
        }
        return false;
    }

    private static final int QUICK_TRIES = 6;

    private boolean runInTurn(List<Route> routes, Map<Route, Position> start, Random random) {
        Map<Route, Position> state = normalised(routes, start);
        int[] moved = new int[routes.size()];
        Map<List<Object>, int[]> seen = new HashMap<>();
        int limit = 0;
        for (Route r : routes) {
            limit += 3 * r.size();
        }
        for (int step = 0; step <= limit; step++) {
            List<Object> k = key(routes, state);
            int[] before = seen.get(k);
            if (before != null) {
                boolean all = true;
                for (int t = 0; t < routes.size() && all; t++) {
                    Route r = routes.get(t);
                    all = r.isLooping() ? moved[t] > before[t] : r.isEnd(state.get(r).getProgress());
                }
                if (all) {
                    return true;
                }
            } else {
                seen.put(k, moved.clone());
            }
            List<Integer> able = new ArrayList<>();
            List<List<Move>> ways = new ArrayList<>();
            for (int t = 0; t < routes.size(); t++) {
                Route r = routes.get(t);
                if (r.isEnd(state.get(r).getProgress())) {
                    continue;
                }
                List<Move> m = moves(r, state.get(r), state);
                if (!m.isEmpty()) {
                    able.add(t);
                    ways.add(m);
                }
            }
            if (able.isEmpty()) {
                // nothing can move: done if every train is at its end and none loops
                for (Route r : routes) {
                    if (r.isLooping() || !r.isEnd(state.get(r).getProgress())) {
                        return false;
                    }
                }
                return true;
            }
            int pick = 0;
            for (int i = 1; i < able.size(); i++) {
                int a = able.get(i);
                int b = able.get(pick);
                if ((long) moved[a] * routes.get(b).size() < (long) moved[b] * routes.get(a).size()) {
                    pick = i;
                }
            }
            Move move = ways.get(pick).get(0);
            if (random != null && random.nextInt(4) == 0) {
                pick = random.nextInt(able.size());
                move = ways.get(pick).get(random.nextInt(ways.get(pick).size()));
            }
            int t = able.get(pick);
            Route r = routes.get(t);
            moved[t] += move.getTo().getProgress() - state.get(r).getProgress();   // steps along the route
            state = new LinkedHashMap<>(state);
            state.put(r, normalised(r, move.getTo()));
        }
        return false;
    }

    /*
     * For each arrangement, whether from it the trains can reach a set of
     * arrangements they can keep going round, every looping train moving in it
     * and every other train at its end: a strongly connected component of the
     * arrangements with a move of every looping train inside it. Tarjan's
     * algorithm, without recursion.
     */
    private static boolean[] canKeepRunning(List<Route> routes, List<Map<Route, Position>> states,
            List<List<int[]>> edges) {
        int n = states.size();
        int[] num = new int[n];
        int[] low = new int[n];
        int[] comp = new int[n];
        java.util.Arrays.fill(num, -1);
        java.util.Arrays.fill(comp, -1);
        boolean[] onStack = new boolean[n];
        Deque<Integer> sccStack = new ArrayDeque<>();
        int counter = 0;
        int comps = 0;
        for (int root = 0; root < n; root++) {
            if (num[root] != -1) {
                continue;
            }
            Deque<int[]> call = new ArrayDeque<>();     // node, next edge to look at
            call.push(new int[]{root, 0});
            num[root] = low[root] = counter++;
            sccStack.push(root);
            onStack[root] = true;
            while (!call.isEmpty()) {
                int[] f = call.peek();
                int v = f[0];
                if (f[1] < edges.get(v).size()) {
                    int w = edges.get(v).get(f[1]++)[0];
                    if (num[w] == -1) {
                        num[w] = low[w] = counter++;
                        sccStack.push(w);
                        onStack[w] = true;
                        call.push(new int[]{w, 0});
                    } else if (onStack[w]) {
                        low[v] = Math.min(low[v], num[w]);
                    }
                } else {
                    call.pop();
                    if (!call.isEmpty()) {
                        int u = call.peek()[0];
                        low[u] = Math.min(low[u], low[v]);
                    }
                    if (low[v] == num[v]) {
                        int w;
                        do {
                            w = sccStack.pop();
                            onStack[w] = false;
                            comp[w] = comps;
                        } while (w != v);
                        comps++;
                    }
                }
            }
        }
        // what each component has: a move of which looping trains, and whether every other train is at its end
        boolean[][] moved = new boolean[comps][routes.size()];
        boolean[] ended = new boolean[comps];
        java.util.Arrays.fill(ended, true);
        for (int v = 0; v < n; v++) {
            for (int t = 0; t < routes.size(); t++) {
                Route r = routes.get(t);
                if (!r.isLooping() && !r.isEnd(states.get(v).get(r).getProgress())) {
                    ended[comp[v]] = false;
                }
            }
            for (int[] e : edges.get(v)) {
                if (comp[e[0]] == comp[v]) {
                    moved[comp[v]][e[1]] = true;
                }
            }
        }
        // a component is live if the trains can go round it for ever: every looping train moves in it
        // and every other train is at its end. Tarjan's numbering puts a component after every one it
        // leads to, so taking them in order, each knows whether those after it can keep running.
        boolean[] good = new boolean[comps];
        for (int c = 0; c < comps; c++) {
            boolean all = ended[c];
            for (int t = 0; t < routes.size() && all; t++) {
                all = !routes.get(t).isLooping() || moved[c][t];
            }
            good[c] = all;
        }
        List<List<Integer>> members = new ArrayList<>();
        for (int c = 0; c < comps; c++) {
            members.add(new ArrayList<>());
        }
        for (int v = 0; v < n; v++) {
            members.get(comp[v]).add(v);
        }
        for (int c = 0; c < comps; c++) {
            for (int v : members.get(c)) {
                for (int[] e : edges.get(v)) {
                    if (comp[e[0]] != c && good[comp[e[0]]]) {
                        good[c] = true;
                    }
                }
            }
        }
        boolean[] result = new boolean[n];
        for (int v = 0; v < n; v++) {
            result[v] = good[comp[v]];
        }
        return result;
    }

    private static Map<Route, Position> normalised(List<Route> routes, Map<Route, Position> state) {
        Map<Route, Position> s = new LinkedHashMap<>();
        for (Route r : routes) {
            s.put(r, normalised(r, state.get(r)));
        }
        return s;
    }

    /*
     * A looping train's position with its step taken round its lap, so that
     * the same arrangement a lap later is the same arrangement.
     */
    private static Position normalised(Route r, Position p) {
        if (!r.isLooping()) {
            return p;
        }
        int step = Math.floorMod(p.getProgress(), r.size());
        return step == p.getProgress() ? p : new Position(step, p.getHeld());
    }

    /**
     * The moves a train can make now from where it is to its next place, with
     * every Section on the way free of the other trains and with none of its
     * Blocks an obstacle.
     *
     * @param route    the train's route
     * @param position where it is
     * @param state    where every train is
     * @return the moves, one for each choice of Sections on the way
     */
    @Nonnull
    public List<Move> moves(@Nonnull Route route, @Nonnull Position position, @Nonnull Map<Route, Position> state) {
        List<Section> others = new ArrayList<>();
        for (Map.Entry<Route, Position> e : state.entrySet()) {
            if (e.getKey() != route) {
                others.addAll(e.getValue().getHeld());
            }
        }
        List<Move> found = new ArrayList<>();
        extend(route, position, position.getProgress(), new ArrayList<>(), others, found);
        return found;
    }

    private void extend(Route route, Position from, int progress, List<Section> path, List<Section> others,
            List<Move> found) {
        int next = progress + 1;
        if (next - from.getProgress() > route.size()) {
            return;     // a whole lap with no place: the route has none
        }
        for (Option o : route.optionsAt(next)) {
            if (conflictsWithAny(o.getSection(), others) || !Collections.disjoint(blocksOf(o.getSection()), obstacles)) {
                continue;
            }
            List<Section> p = new ArrayList<>(path);
            p.add(o.getSection());
            if (o.isPlace() || route.isEnd(next)) {
                found.add(new Move(p, new Position(next, cover(route, from, p))));
            } else {
                extend(route, from, next, p, others, found);
            }
        }
    }

    /*
     * The Sections a train covers with its head at the end of the last Section of
     * the path: back along the path, then along what it held before, until its
     * length is covered. With no length known, the place alone.
     */
    private List<Section> cover(Route route, Position from, List<Section> path) {
        List<Section> behind = new ArrayList<>();
        for (int i = path.size() - 1; i >= 0; i--) {
            behind.add(path.get(i));
        }
        behind.addAll(from.getHeld());
        List<Section> held = new ArrayList<>();
        long covered = 0;
        for (Section s : behind) {
            if (held.contains(s)) {
                continue;
            }
            held.add(s);
            long length = s.getActualLength();
            if (route.getLength() <= 0 || length <= 0) {
                break;
            }
            covered += length;
            if (covered >= route.getLength()) {
                break;
            }
        }
        return held;
    }

    private static List<Object> key(List<Route> routes, Map<Route, Position> state) {
        return key(routes, state, 0);
    }

    // which trains of the set the arrangement is of is part of the key, so the groups of one set
    // of trains, which can differ with what the trains hold, can share an ArrangementCache
    private static List<Object> key(List<Route> routes, Map<Route, Position> state, Object group) {
        List<Object> k = new ArrayList<>();
        k.add(group);
        for (Route r : routes) {
            Position p = state.get(r);
            k.add(p.getProgress());
            k.add(p.getHeld());
        }
        return k;
    }

    private boolean conflictsWithAny(Section s, Collection<Section> others) {
        for (Section o : others) {
            if (conflicts(s, o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param a a Section
     * @param b another
     * @return true if they are the same Section or share a Block
     */
    boolean conflicts(@Nonnull Section a, @Nonnull Section b) {
        if (a == b) {
            return true;
        }
        Set<Block> ba = blocksOf(a);
        for (Block x : blocksOf(b)) {
            if (ba.contains(x)) {
                return true;
            }
        }
        return false;
    }

    private Set<Block> blocksOf(Section s) {
        return blocks.computeIfAbsent(s, x -> new HashSet<>(x.getBlockList()));
    }
}
