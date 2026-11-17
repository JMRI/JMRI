package jmri.jmrit.trainrecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import jmri.BasicRosterEntry;
import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.InvokeOnLayoutThread;
import jmri.NamedBean;
import jmri.Path;
import jmri.Sensor;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.Turnout;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterSpeedProfile;

/**
 * Checks of how a layout and its trains are set up, for following trains:
 * the set-up problems that make a train record lose its train, or know less
 * about it than it could. They only read: they never change anything, and
 * each finding says what is wrong in the user's terms, for the user to
 * decide.
 * <p>
 * The checks:
 * <ul>
 * <li>a train with no length from any source;</li>
 * <li>a train whose lead loco has no roster entry or no speed profile, so
 * where it is between detectors can't be estimated;</li>
 * <li>a Block with no length, on the layout (with paths to other
 * Blocks);</li>
 * <li>a Block with a detector but no paths to other Blocks, so a train can't
 * be followed into or out of it;</li>
 * <li>a path from one Block to another with no path back;</li>
 * <li>a sensor used by two Blocks;</li>
 * <li>a Block with no detector whose paths depend on a turnout with no
 * feedback, so the way a train takes through it is only what was
 * commanded;</li>
 * <li>two detected Blocks joined through Blocks with no detector by more
 * than one shortest way that no turnout chooses between, as with parallel
 * tracks sharing a detector, so which way a train went can't be told.</li>
 * </ul>
 * Other parts of JMRI add their own checks as {@link SetUpCheck}s, such as
 * settings of theirs that disagree with others train tracking relies on.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.1
 */
public final class TrainTrackingChecks {

    private TrainTrackingChecks() {
    }

    /**
     * The kinds of finding, in the order they are listed.
     */
    public enum Kind {
        /** A train with no length from any source. */
        TRAIN_NO_LENGTH,
        /** A train whose lead loco has no roster entry or no speed profile. */
        TRAIN_NO_SPEED_PROFILE,
        /** A Block on the layout with no length. */
        BLOCK_NO_LENGTH,
        /** A detected Block with no paths to other Blocks. */
        BLOCK_NO_PATHS,
        /** A path from one Block to another with no path back. */
        PATH_ONE_WAY,
        /** A sensor used by more than one Block. */
        SENSOR_SHARED,
        /** A Block with no detector whose paths depend on a turnout with no feedback. */
        DARK_BLOCK_TURNOUT_NO_FEEDBACK,
        /** Detected Blocks joined through dark Blocks by several ways no turnout chooses between. */
        DARK_RUN_SEVERAL_WAYS,
        /** Settings in different parts of JMRI that disagree, from a {@link SetUpCheck}. */
        SETTINGS_DISAGREE
    }

    /**
     * One set-up problem.
     */
    @Immutable
    public static final class Finding {

        private final Kind kind;
        private final Object subject;
        private final List<NamedBean> related;
        private final String message;

        /**
         * @param kind    the kind of problem
         * @param subject what it is about: a {@link TrainRecord}, a
         *                {@link NamedBean}, or the name of the part of JMRI
         *                whose settings it is about
         * @param related other items it involves, for "Show me"
         * @param message what is wrong, in the user's terms
         */
        public Finding(@Nonnull Kind kind, @Nonnull Object subject, @Nonnull List<NamedBean> related,
                @Nonnull String message) {
            this.kind = Objects.requireNonNull(kind, "kind");
            this.subject = Objects.requireNonNull(subject, "subject");
            this.related = List.copyOf(related);
            this.message = Objects.requireNonNull(message, "message");
        }

        /**
         * @return the kind of problem
         */
        @Nonnull
        public Kind getKind() {
            return kind;
        }

        /**
         * @return what it is about: a {@link TrainRecord}, a
         *         {@link NamedBean}, or the name of the part of JMRI whose
         *         settings it is about
         */
        @Nonnull
        public Object getSubject() {
            return subject;
        }

        /**
         * @return other items it involves
         */
        @Nonnull
        public List<NamedBean> getRelated() {
            return related;
        }

        /**
         * @return what is wrong, in the user's terms
         */
        @Nonnull
        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return kind + ": " + message;
        }
    }

    /**
     * Check the layout's Blocks and the train records, and run every
     * {@link SetUpCheck}.
     *
     * @return the findings, by kind, then by name
     */
    @Nonnull
    @InvokeOnLayoutThread
    public static List<Finding> run() {
        List<Finding> found = new ArrayList<>(run(InstanceManager.getDefault(BlockManager.class).getNamedBeanSet(),
                InstanceManager.getDefault(TrainRecordManager.class).getRecords()));
        for (SetUpCheck check : ServiceLoader.load(SetUpCheck.class)) {
            found.addAll(check.check());
        }
        sort(found);
        return found;
    }

    /**
     * Check some Blocks and train records.
     *
     * @param blocks  the Blocks
     * @param records the train records
     * @return the findings, by kind, then by name
     */
    @Nonnull
    @InvokeOnLayoutThread
    public static List<Finding> run(@Nonnull Collection<Block> blocks, @Nonnull Collection<TrainRecord> records) {
        List<Finding> found = new ArrayList<>();
        for (TrainRecord r : records) {
            if (r.isDisposed()) {
                continue;
            }
            checkTrain(r, found);
        }
        Map<Sensor, List<Block>> bySensor = new LinkedHashMap<>();
        for (Block b : blocks) {
            checkBlock(b, found);
            Sensor s = b.getSensor();
            if (s != null) {
                bySensor.computeIfAbsent(s, k -> new ArrayList<>()).add(b);
            }
        }
        bySensor.forEach((s, users) -> {
            if (users.size() > 1) {
                users.sort(Comparator.comparing(Block::getDisplayName));
                found.add(new Finding(Kind.SENSOR_SHARED, s, new ArrayList<>(users),
                        Bundle.getMessage("CheckSensorShared", s.getDisplayName(), names(users))));
            }
        });
        checkDarkRuns(blocks, found);
        sort(found);
        return found;
    }

    private static void sort(List<Finding> found) {
        found.sort(Comparator.comparing(Finding::getKind).thenComparing(f -> nameOf(f.getSubject())));
    }

    private static void checkTrain(TrainRecord r, List<Finding> found) {
        if (r.getHoldingLength() == null) {
            found.add(new Finding(Kind.TRAIN_NO_LENGTH, r, List.of(),
                    Bundle.getMessage("CheckTrainNoLength", r.getName())));
        }
        // the lead loco's: where a train is between detectors is estimated
        // from its lead loco's commands and speed profile
        BasicRosterEntry entry = r.getFirstRosterEntry();
        if (entry == null) {
            found.add(new Finding(Kind.TRAIN_NO_SPEED_PROFILE, r, List.of(),
                    Bundle.getMessage("CheckTrainNoRosterEntry", r.getName())));
        } else if (entry instanceof RosterEntry && !hasSpeeds(((RosterEntry) entry).getSpeedProfile())) {
            found.add(new Finding(Kind.TRAIN_NO_SPEED_PROFILE, r, List.of(),
                    Bundle.getMessage("CheckTrainNoSpeedProfile", r.getName(), entry.getId())));
        }
    }

    private static boolean hasSpeeds(@CheckForNull RosterSpeedProfile p) {
        return p != null && (p.hasForwardSpeeds() || p.hasReverseSpeeds());
    }

    private static void checkBlock(Block b, List<Finding> found) {
        List<Block> neighbours = neighbours(b);
        if (!neighbours.isEmpty() && !(b.getLengthMm() > 0)) {
            found.add(new Finding(Kind.BLOCK_NO_LENGTH, b, List.of(), Bundle.getMessage(
                    b.getSensor() == null ? "CheckDarkBlockNoLength" : "CheckBlockNoLength", b.getDisplayName())));
        }
        if (neighbours.isEmpty() && b.getSensor() != null) {
            found.add(new Finding(Kind.BLOCK_NO_PATHS, b, List.of(),
                    Bundle.getMessage("CheckBlockNoPaths", b.getDisplayName())));
        }
        for (Block n : neighbours) {
            if (!neighbours(n).contains(b)) {
                found.add(new Finding(Kind.PATH_ONE_WAY, b, List.of(n),
                        Bundle.getMessage("CheckPathOneWay", b.getDisplayName(), n.getDisplayName())));
            }
        }
        if (b.getSensor() == null) {
            List<Turnout> blind = new ArrayList<>();
            for (Path p : b.getPaths()) {
                for (BeanSetting s : p.getSettings()) {
                    NamedBean bean = s.getBean();
                    if (bean instanceof Turnout && ((Turnout) bean).getFeedbackMode() == Turnout.DIRECT
                            && !blind.contains(bean)) {
                        blind.add((Turnout) bean);
                    }
                }
            }
            blind.sort(Comparator.comparing(Turnout::getDisplayName));
            for (Turnout t : blind) {
                found.add(new Finding(Kind.DARK_BLOCK_TURNOUT_NO_FEEDBACK, b, List.of(t),
                        Bundle.getMessage("CheckDarkBlockTurnout", b.getDisplayName(), t.getDisplayName())));
            }
        }
    }

    /**
     * Detected Blocks joined through dark Blocks by more than one shortest
     * way along paths with no turnout settings: no turnout chooses between
     * them.
     */
    private static void checkDarkRuns(Collection<Block> blocks, List<Finding> found) {
        List<String> reported = new ArrayList<>();
        List<Block> sorted = new ArrayList<>(blocks);
        sorted.sort(Comparator.comparing(Block::getDisplayName));
        for (Block from : sorted) {
            if (from.getSensor() == null) {
                continue;
            }
            Map<Block, Integer> ways = waysThroughDark(from);
            List<Block> targets = new ArrayList<>(ways.keySet());
            targets.sort(Comparator.comparing(Block::getDisplayName));
            for (Block to : targets) {
                if (ways.get(to) < 2) {
                    continue;
                }
                String key = from.getDisplayName().compareTo(to.getDisplayName()) < 0
                        ? from.getDisplayName() + "\u0000" + to.getDisplayName()
                        : to.getDisplayName() + "\u0000" + from.getDisplayName();
                if (reported.contains(key)) {
                    continue;
                }
                reported.add(key);
                found.add(new Finding(Kind.DARK_RUN_SEVERAL_WAYS, from, List.of(to),
                        Bundle.getMessage("CheckDarkRunSeveralWays", from.getDisplayName(), to.getDisplayName())));
            }
        }
    }

    /**
     * From a detected Block, the detected Blocks reached through at least one
     * dark Block, each with the number of shortest ways, along paths with no
     * turnout settings.
     */
    private static Map<Block, Integer> waysThroughDark(Block from) {
        Map<Block, Integer> depth = new HashMap<>();
        Map<Block, Integer> ways = new HashMap<>();
        Map<Block, Integer> reached = new HashMap<>();
        Map<Block, Integer> reachedDepth = new HashMap<>();
        List<Block> frontier = new ArrayList<>();
        depth.put(from, 0);
        for (Block d : plainNeighbours(from)) {
            if (d.getSensor() == null && !depth.containsKey(d)) {
                depth.put(d, 1);
                ways.put(d, 1);
                frontier.add(d);
            }
        }
        int level = 1;
        while (!frontier.isEmpty()) {
            List<Block> next = new ArrayList<>();
            for (Block d : frontier) {
                for (Block n : plainNeighbours(d)) {
                    if (n == from) {
                        continue;
                    }
                    if (n.getSensor() != null) {
                        Integer at = reachedDepth.get(n);
                        if (at == null || at == level + 1) {
                            reachedDepth.put(n, level + 1);
                            reached.merge(n, ways.get(d), Integer::sum);
                        }
                        continue;
                    }
                    Integer seen = depth.get(n);
                    if (seen == null) {
                        depth.put(n, level + 1);
                        ways.put(n, ways.get(d));
                        next.add(n);
                    } else if (seen == level + 1) {
                        ways.merge(n, ways.get(d), Integer::sum);
                    }
                }
            }
            frontier = next;
            level++;
        }
        return reached;
    }

    /**
     * @param b a Block
     * @return the other Blocks its paths lead to, each once
     */
    private static List<Block> neighbours(Block b) {
        List<Block> found = new ArrayList<>();
        for (Path p : b.getPaths()) {
            Block n = p.getBlock();
            if (n != null && n != b && !found.contains(n)) {
                found.add(n);
            }
        }
        return found;
    }

    /**
     * @param b a Block
     * @return the other Blocks it leads to by paths with no turnout settings
     */
    private static List<Block> plainNeighbours(Block b) {
        List<Block> found = new ArrayList<>();
        for (Path p : b.getPaths()) {
            Block n = p.getBlock();
            if (n != null && n != b && p.getSettings().isEmpty() && !found.contains(n)) {
                found.add(n);
            }
        }
        return found;
    }

    private static String names(List<Block> blocks) {
        List<String> names = new ArrayList<>();
        for (Block b : blocks) {
            names.add(b.getDisplayName());
        }
        return String.join(", ", names);
    }

    private static String nameOf(Object o) {
        if (o instanceof TrainRecord) {
            return ((TrainRecord) o).getName();
        }
        if (o instanceof NamedBean) {
            return ((NamedBean) o).getDisplayName();
        }
        return String.valueOf(o);
    }
}
