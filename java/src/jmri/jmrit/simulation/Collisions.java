package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.CheckForNull;

import jmri.Block;

/**
 * Finds when two trains first touch during a step.
 * <p>
 * For each Block both trains could be in, their extents are compared in that
 * Block's own terms: directly when both follow the same way through it,
 * mirrored when they follow it opposite ways (head on), and, when their ways
 * foul each other (at a turnout or crossing), the trains touch as soon as both
 * are in the Blocks concerned. The time of first touching is found exactly:
 * it is one of the times at which an end of one train passes an end of the
 * other or a Block boundary, all worked out from the trains' motions.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class Collisions {

    /** Overlap smaller than this, in mm, is touching, not overlapping. */
    static final double TOUCH_MM = 1e-6;

    private Collisions() {
    }

    private enum Relation {
        SAME, REVERSE, FOUL
    }

    /** Two spans that may meet, and how. */
    private static final class Pair {

        final TrainState.Span a;
        final TrainState.Span b;
        final Relation relation;

        Pair(TrainState.Span a, TrainState.Span b, Relation relation) {
            this.a = a;
            this.b = b;
            this.relation = relation;
        }
    }

    /** A collision found: when and where. */
    static final class Found {

        final double time;
        final Block block;

        Found(double time, Block block) {
            this.time = time;
            this.block = block;
        }
    }

    /**
     * The pairs of spans, one of each train, where the trains could meet.
     */
    private static List<Pair> pairs(TrainState a, TrainState b) {
        List<Pair> pairs = new ArrayList<>();
        for (TrainState.Span sa : a.spans) {
            for (TrainState.Span sb : b.spans) {
                Route ra = sa.passage.route;
                Route rb = sb.passage.route;
                boolean sameBlock = sa.block() == sb.block();
                if (sameBlock && ra.sameAs(rb)) {
                    pairs.add(new Pair(sa, sb, Relation.SAME));
                } else if (sameBlock && ra.reverseOf(rb)) {
                    pairs.add(new Pair(sa, sb, Relation.REVERSE));
                } else if (ra.fouls(rb)) {
                    pairs.add(new Pair(sa, sb, Relation.FOUL));
                }
            }
        }
        return pairs;
    }

    /**
     * When do two trains first touch in a step?
     *
     * @param a  one train
     * @param ma its motion over the step
     * @param b  the other train
     * @param mb its motion
     * @return the first time and the Block, or null if they don't touch
     */
    @CheckForNull
    static Found first(TrainState a, Motion ma, TrainState b, Motion mb) {
        List<Pair> pairs = pairs(a, b);
        if (pairs.isEmpty()) {
            return null;
        }
        double t0 = ma.startTime();
        double t1 = ma.endTime();
        TreeSet<Double> times = new TreeSet<>();
        times.add(t0);
        times.add(t1);
        for (Pair p : pairs) {
            List<double[]> edges = edges(a, b, p);
            for (int i = 0; i < edges.size(); i++) {
                for (int j = i + 1; j < edges.size(); j++) {
                    roots(edges.get(i), edges.get(j), ma, mb, t0, t1, times);
                }
            }
        }
        Double[] ts = times.toArray(new Double[0]);
        for (int i = 0; i + 1 < ts.length; i++) {
            double mid = (ts[i] + ts[i + 1]) / 2;
            Block where = overlapAt(a, ma.position(mid), b, mb.position(mid), pairs);
            if (where != null) {
                return new Found(ts[i], where);
            }
        }
        return null;
    }

    /**
     * Are two trains, at these front positions, overlapping (or both in
     * fouling Blocks)?
     *
     * @param a  the first train
     * @param pa the first train's front position
     * @param b  the second train
     * @param pb the second train's front position
     * @return the Block where, or null
     */
    @CheckForNull
    static Block overlapAt(TrainState a, double pa, TrainState b, double pb) {
        List<Pair> pairs = pairs(a, b);
        return overlapAt(a, pa, b, pb, pairs);
    }

    @CheckForNull
    private static Block overlapAt(TrainState a, double pa, TrainState b, double pb, List<Pair> pairs) {
        for (Pair p : pairs) {
            double loA = Math.max(0, pa - a.length - p.a.start);
            double hiA = Math.min(p.a.length(), pa - p.a.start);
            if (hiA - loA <= TOUCH_MM) {
                continue;
            }
            double k = p.a.length() / p.b.length();
            double rb = (pb - b.length - p.b.start) * k;
            double fb = (pb - p.b.start) * k;
            double loB;
            double hiB;
            if (p.relation == Relation.REVERSE) {
                loB = Math.max(0, p.a.length() - fb);
                hiB = Math.min(p.a.length(), p.a.length() - rb);
            } else {
                loB = Math.max(0, rb);
                hiB = Math.min(p.a.length(), fb);
            }
            if (hiB - loB <= TOUCH_MM) {
                continue;
            }
            if (p.relation == Relation.FOUL || Math.min(hiA, hiB) - Math.max(loA, loB) > TOUCH_MM) {
                return p.a.block();
            }
        }
        return null;
    }

    /**
     * The positions compared for one pair of spans, each as {constant,
     * factor, train} meaning constant + factor x (front position of train),
     * train 0 for a, 1 for b, -1 for a constant.
     */
    private static List<double[]> edges(TrainState a, TrainState b, Pair p) {
        List<double[]> e = new ArrayList<>();
        double la = p.a.length();
        double k = la / p.b.length();
        e.add(new double[]{0, 0, -1});
        e.add(new double[]{la, 0, -1});
        e.add(new double[]{-p.a.start, 1, 0}); // front of a
        e.add(new double[]{-a.length - p.a.start, 1, 0}); // rear of a
        if (p.relation == Relation.REVERSE) {
            e.add(new double[]{la + p.b.start * k, -k, 1}); // front of b, mirrored
            e.add(new double[]{la + (b.length + p.b.start) * k, -k, 1}); // rear of b, mirrored
        } else {
            e.add(new double[]{-p.b.start * k, k, 1});
            e.add(new double[]{-(b.length + p.b.start) * k, k, 1});
        }
        return e;
    }

    /**
     * Add the times in [t0, t1] when two compared positions are equal.
     */
    private static void roots(double[] x, double[] y, Motion ma, Motion mb, double t0, double t1,
            TreeSet<Double> out) {
        if (x[2] < 0 && y[2] < 0) {
            return;
        }
        TreeSet<Double> cuts = new TreeSet<>();
        cuts.add(t0);
        cuts.add(t1);
        for (Motion m : new Motion[]{ma, mb}) {
            for (Motion.Piece pc : m.pieces) {
                cuts.add(pc.t0);
                cuts.add(pc.t1);
            }
        }
        Double[] cs = cuts.toArray(new Double[0]);
        for (int i = 0; i + 1 < cs.length; i++) {
            double u = cs[i];
            double w = cs[i + 1];
            if (w <= u) {
                continue;
            }
            double mid = (u + w) / 2;
            // g(u + d) = g0 + g1 d + g2 d^2
            double g0 = value(x, ma, mb, u) - value(y, ma, mb, u);
            double g1 = slope(x, ma, mb, mid, u) - slope(y, ma, mb, mid, u);
            double g2 = 0.5 * (accel(x, ma, mb, mid) - accel(y, ma, mb, mid));
            for (double d : solve(g2, g1, g0)) {
                if (d >= 0 && d <= w - u) {
                    out.add(u + d);
                }
            }
        }
    }

    private static Motion of(double[] e, Motion ma, Motion mb) {
        return e[2] == 0 ? ma : mb;
    }

    private static double value(double[] e, Motion ma, Motion mb, double t) {
        if (e[2] < 0) {
            return e[0];
        }
        return e[0] + e[1] * of(e, ma, mb).position(t);
    }

    private static Motion.Piece pieceAt(Motion m, double t) {
        for (Motion.Piece p : m.pieces) {
            if (t >= p.t0 && t <= p.t1) {
                return p;
            }
        }
        return m.pieces.get(m.pieces.size() - 1);
    }

    private static double slope(double[] e, Motion ma, Motion mb, double mid, double u) {
        if (e[2] < 0) {
            return 0;
        }
        return e[1] * pieceAt(of(e, ma, mb), mid).velocity(u);
    }

    private static double accel(double[] e, Motion ma, Motion mb, double mid) {
        if (e[2] < 0) {
            return 0;
        }
        return e[1] * pieceAt(of(e, ma, mb), mid).a;
    }

    /** Real roots of a d^2 + b d + c = 0. */
    private static double[] solve(double a, double b, double c) {
        if (Math.abs(a) < 1e-12) {
            if (Math.abs(b) < 1e-12) {
                return new double[0];
            }
            return new double[]{-c / b};
        }
        double disc = b * b - 4 * a * c;
        if (disc < 0) {
            return new double[0];
        }
        double sq = Math.sqrt(disc);
        double q = -0.5 * (b + Math.copySign(sq, b));
        if (q == 0) {
            return new double[]{0};
        }
        return new double[]{q / a, c / q};
    }
}
