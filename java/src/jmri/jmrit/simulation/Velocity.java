package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * A velocity over a time interval, made of straight-line parts. The parts
 * cover the interval without gaps and need not join, so a velocity can change
 * at once (an emergency stop, or a decoder without momentum).
 * <p>
 * Times are in seconds from the start of one step of the simulator, so a
 * step's velocity runs from 0 to the step's length,
 * {@link SimulatedLayout#STEP_SECONDS}. When the step starts is kept by the
 * caller in nanoseconds on the {@link System#nanoTime()} scale (or the manual
 * clock in the package's tests); the times here don't depend on that epoch.
 * <p>
 * Velocities are in millimetres per second of the model, not scale speed,
 * along the train's own route: positive when the train moves front first,
 * negative when it moves rear first. A loco's velocity is the same, taken
 * the train's way, so a loco facing the rear runs forward as a negative
 * velocity.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class Velocity {

    /**
     * One straight-line part: velocity v0 at t0 changing evenly to v1 at t1.
     * Times in seconds from the step's start, velocities in mm/s.
     */
    static final class Part {

        final double t0;
        final double t1;
        final double v0;
        final double v1;

        Part(double t0, double t1, double v0, double v1) {
            this.t0 = t0;
            this.t1 = t1;
            this.v0 = v0;
            this.v1 = v1;
        }

        double at(double t) {
            if (t1 <= t0) {
                return v0;
            }
            return v0 + (v1 - v0) * (t - t0) / (t1 - t0);
        }

        @Override
        public String toString() {
            return "Part[" + t0 + ".." + t1 + ": " + v0 + ".." + v1 + "]";
        }
    }

    final List<Part> parts;

    Velocity(List<Part> parts) {
        this.parts = parts;
    }

    /**
     * Standing still from t0 to t1.
     *
     * @param t0 start
     * @param t1 end
     * @return the velocity
     */
    static Velocity still(double t0, double t1) {
        List<Part> p = new ArrayList<>();
        p.add(new Part(t0, t1, 0.0, 0.0));
        return new Velocity(p);
    }

    /**
     * The velocity of a loco over its decoder's ramps.
     *
     * @param ramps        the decoder's setting over time
     * @param forwardTable speeds when the decoder runs forward
     * @param reverseTable speeds when the decoder runs in reverse
     * @param sign         +1 if the loco's forward moves the train front first,
     *                     -1 if it moves the train rear first
     * @return the velocity, positive when the train moves front first
     */
    static Velocity ofLoco(List<DecoderModel.Ramp> ramps, SpeedTable forwardTable, SpeedTable reverseTable, double sign) {
        List<Part> parts = new ArrayList<>();
        for (DecoderModel.Ramp r : ramps) {
            if (r.u1 == r.u0) {
                double v = sign * speedOf(r.u0, forwardTable, reverseTable);
                parts.add(new Part(r.t0, r.t1, v, v));
                continue;
            }
            // split where the decoder setting passes 0 or a measured setting,
            // so that within each part the speed changes evenly
            List<Double> points = new ArrayList<>();
            points.add(r.u0);
            if (r.u0 < 0 && r.u1 > 0 || r.u0 > 0 && r.u1 < 0) {
                addSide(points, r.u0, 0.0, forwardTable, reverseTable);
                points.add(0.0);
                addSide(points, 0.0, r.u1, forwardTable, reverseTable);
            } else {
                addSide(points, r.u0, r.u1, forwardTable, reverseTable);
            }
            points.add(r.u1);
            for (int i = 0; i + 1 < points.size(); i++) {
                double ua = points.get(i);
                double ub = points.get(i + 1);
                double ta = timeOf(r, ua);
                double tb = timeOf(r, ub);
                if (tb > ta) {
                    parts.add(new Part(ta, tb,
                            sign * speedOf(ua, forwardTable, reverseTable),
                            sign * speedOf(ub, forwardTable, reverseTable)));
                }
            }
        }
        return new Velocity(parts);
    }

    private static void addSide(List<Double> points, double from, double to,
            SpeedTable forwardTable, SpeedTable reverseTable) {
        boolean forward = (from + to) >= 0;
        SpeedTable table = forward ? forwardTable : reverseTable;
        for (double b : table.breakpointsBetween(Math.abs(from), Math.abs(to))) {
            points.add(forward ? b : -b);
        }
    }

    private static double timeOf(DecoderModel.Ramp r, double u) {
        double f = (u - r.u0) / (r.u1 - r.u0);
        return r.t0 + f * (r.t1 - r.t0);
    }

    private static double speedOf(double u, SpeedTable forwardTable, SpeedTable reverseTable) {
        if (u >= 0) {
            return forwardTable.speed(u);
        }
        return -reverseTable.speed(-u);
    }

    /**
     * Value just after a time.
     *
     * @param t the time
     * @return the velocity just after t (at t for the last part's end)
     */
    double after(double t) {
        for (Part p : parts) {
            if (t >= p.t0 && t < p.t1) {
                return p.at(t);
            }
        }
        Part last = parts.get(parts.size() - 1);
        return last.v1;
    }

    /**
     * Value just before a time.
     */
    private double before(double t) {
        for (int i = parts.size() - 1; i >= 0; i--) {
            Part p = parts.get(i);
            if (t > p.t0 && t <= p.t1) {
                return p.at(t);
            }
        }
        return parts.get(0).v0;
    }

    /**
     * The velocity of locos coupled in one train: the slowest loco's at each
     * moment, as the train can go no faster than its slowest loco, when all
     * push the same way. Where locos push opposite ways the train does not
     * move.
     *
     * @param locos the velocity of each loco, over the same interval
     * @param t0    interval start
     * @param t1    interval end
     * @return the train's velocity
     */
    static Velocity slowest(List<Velocity> locos, double t0, double t1) {
        if (locos.size() == 1) {
            return locos.get(0);
        }
        TreeSet<Double> times = new TreeSet<>();
        times.add(t0);
        times.add(t1);
        for (Velocity v : locos) {
            for (Part p : v.parts) {
                times.add(p.t0);
                times.add(p.t1);
            }
        }
        List<Part> result = new ArrayList<>();
        Double[] ts = times.toArray(new Double[0]);
        for (int i = 0; i + 1 < ts.length; i++) {
            double a = ts[i];
            double b = ts[i + 1];
            if (b <= a) {
                continue;
            }
            // within (a, b) every loco's velocity is a straight line; cut where
            // any two cross, or any crosses 0, so that the slowest is one loco
            TreeSet<Double> cuts = new TreeSet<>();
            cuts.add(a);
            cuts.add(b);
            int n = locos.size();
            double[] va = new double[n];
            double[] vb = new double[n];
            for (int k = 0; k < n; k++) {
                va[k] = locos.get(k).after(a);
                vb[k] = locos.get(k).before(b);
            }
            for (int k = 0; k < n; k++) {
                addCrossing(cuts, a, b, va[k], vb[k], 0.0, 0.0);
                for (int m = k + 1; m < n; m++) {
                    addCrossing(cuts, a, b, va[k], vb[k], va[m], vb[m]);
                }
            }
            Double[] cs = cuts.toArray(new Double[0]);
            for (int j = 0; j + 1 < cs.length; j++) {
                double c0 = cs[j];
                double c1 = cs[j + 1];
                double mid = (c0 + c1) / 2;
                int chosen = -1;
                boolean positive = false;
                boolean negative = false;
                for (int k = 0; k < n; k++) {
                    double vm = va[k] + (vb[k] - va[k]) * (mid - a) / (b - a);
                    positive |= vm > 0;
                    negative |= vm < 0;
                    if (chosen < 0 || Math.abs(vm) < Math.abs(va[chosen] + (vb[chosen] - va[chosen]) * (mid - a) / (b - a))) {
                        chosen = k;
                    }
                }
                if (positive && negative) {
                    result.add(new Part(c0, c1, 0.0, 0.0));
                } else {
                    double v0 = va[chosen] + (vb[chosen] - va[chosen]) * (c0 - a) / (b - a);
                    double v1 = va[chosen] + (vb[chosen] - va[chosen]) * (c1 - a) / (b - a);
                    result.add(new Part(c0, c1, v0, v1));
                }
            }
        }
        return new Velocity(result);
    }

    private static void addCrossing(TreeSet<Double> cuts, double a, double b,
            double pa, double pb, double qa, double qb) {
        double da = pa - qa;
        double db = pb - qb;
        if ((da < 0 && db > 0) || (da > 0 && db < 0)) {
            cuts.add(a + (b - a) * da / (da - db));
        }
    }

    /**
     * Multiply by a factor, as for a train that runs faster or slower than its
     * locos' profiles.
     *
     * @param factor the multiplier
     * @return the scaled velocity
     */
    Velocity times(double factor) {
        List<Part> p = new ArrayList<>();
        for (Part x : parts) {
            p.add(new Part(x.t0, x.t1, x.v0 * factor, x.v1 * factor));
        }
        return new Velocity(p);
    }
}
