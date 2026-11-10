package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.List;

/**
 * Where a train is over one step: its position along its own route as a
 * function of time, made of parts of even acceleration. Within each part the
 * train moves one way only, so a position is passed at most once per part and
 * the time it is passed can be worked out exactly.
 * <p>
 * Units are those of {@link Velocity}: times in seconds from the start of the
 * step, positions in millimetres along the train's route (where its front
 * is), speeds in mm/s and accelerations in mm/s&sup2;.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class Motion {

    /** One part: from t0 to t1, starting at s0 with velocity v0 and acceleration a. */
    static final class Piece {

        final double t0;
        final double t1;
        final double s0;
        final double v0;
        final double a;

        Piece(double t0, double t1, double s0, double v0, double a) {
            this.t0 = t0;
            this.t1 = t1;
            this.s0 = s0;
            this.v0 = v0;
            this.a = a;
        }

        double position(double t) {
            double d = t - t0;
            return s0 + v0 * d + 0.5 * a * d * d;
        }

        double velocity(double t) {
            return v0 + a * (t - t0);
        }

        double end() {
            return position(t1);
        }

        /**
         * Direction of travel in this part.
         *
         * @return +1 when moving forward, -1 when moving back, or 0 when
         *         standing still
         */
        double direction() {
            double mid = velocity((t0 + t1) / 2);
            return Math.signum(mid);
        }

        /**
         * The time in this part when the position is x. The part moves one
         * way only, so x is reached at most once; the root used is the one
         * reached first, written in the form that stays accurate when the
         * acceleration is small.
         *
         * @param x the position
         * @return the time, within the part
         */
        double timeAt(double x) {
            double c = s0 - x;
            double d;
            if (v0 == 0.0) {
                d = (a == 0.0) ? 0.0 : Math.sqrt(Math.max(0.0, -2 * c / a));
            } else {
                double disc = Math.max(0.0, v0 * v0 - 2 * a * c);
                d = -2 * c / (v0 + Math.signum(v0) * Math.sqrt(disc));
            }
            return Math.max(t0, Math.min(t1, t0 + d));
        }

        @Override
        public String toString() {
            return "Piece[" + t0 + ".." + t1 + " s0=" + s0 + " v0=" + v0 + " a=" + a + "]";
        }
    }

    final List<Piece> pieces;

    private Motion(List<Piece> pieces) {
        this.pieces = pieces;
    }

    /**
     * Standing at a position from t0 to t1.
     *
     * @param s  position
     * @param t0 start
     * @param t1 end
     * @return the motion
     */
    static Motion still(double s, double t0, double t1) {
        List<Piece> p = new ArrayList<>();
        p.add(new Piece(t0, t1, s, 0.0, 0.0));
        return new Motion(p);
    }

    /**
     * The motion of a train with this velocity, starting at s, kept between
     * two limits: it can't move beyond either, and stands at a limit for as
     * long as its locos drive it towards it, moving away again as soon as they
     * drive it the other way.
     *
     * @param velocity the velocity
     * @param s        starting position
     * @param min      lowest position allowed
     * @param max      highest position allowed
     * @return the motion
     */
    static Motion of(Velocity velocity, double s, double min, double max) {
        List<Piece> result = new ArrayList<>();
        double pos = Math.max(min, Math.min(max, s));
        for (Velocity.Part part : velocity.parts) {
            // split each part where its velocity passes 0
            List<double[]> spans = new ArrayList<>();
            if ((part.v0 < 0 && part.v1 > 0) || (part.v0 > 0 && part.v1 < 0)) {
                double tz = part.t0 + (part.t1 - part.t0) * part.v0 / (part.v0 - part.v1);
                spans.add(new double[]{part.t0, tz, part.v0, 0.0});
                spans.add(new double[]{tz, part.t1, 0.0, part.v1});
            } else {
                spans.add(new double[]{part.t0, part.t1, part.v0, part.v1});
            }
            for (double[] sp : spans) {
                double t0 = sp[0];
                double t1 = sp[1];
                if (t1 <= t0) {
                    continue;
                }
                double a = (sp[3] - sp[2]) / (t1 - t0);
                Piece free = new Piece(t0, t1, pos, sp[2], a);
                double dir = free.direction();
                double limit = dir > 0 ? max : min;
                if (dir == 0 || (dir > 0 && pos >= max) || (dir < 0 && pos <= min)) {
                    result.add(new Piece(t0, t1, pos, 0.0, 0.0));
                    continue;
                }
                double end = free.end();
                if ((dir > 0 && end > limit) || (dir < 0 && end < limit)) {
                    double tl = free.timeAt(limit);
                    result.add(new Piece(t0, tl, pos, sp[2], a));
                    result.add(new Piece(tl, t1, limit, 0.0, 0.0));
                    pos = limit;
                } else {
                    result.add(free);
                    pos = end;
                }
            }
        }
        return new Motion(result);
    }

    double startTime() {
        return pieces.get(0).t0;
    }

    double endTime() {
        return pieces.get(pieces.size() - 1).t1;
    }

    /**
     * @param t a time within the step
     * @return the position at t
     */
    double position(double t) {
        for (Piece p : pieces) {
            if (t <= p.t1) {
                return p.position(Math.max(t, p.t0));
            }
        }
        return pieces.get(pieces.size() - 1).end();
    }

    /**
     * @param t a time within the step
     * @return the velocity just after t
     */
    double velocity(double t) {
        for (Piece p : pieces) {
            if (t < p.t1) {
                return p.velocity(Math.max(t, p.t0));
            }
        }
        return 0.0;
    }

    /**
     * @return position at the end of the step
     */
    double endPosition() {
        return pieces.get(pieces.size() - 1).end();
    }

    /**
     * The same motion up to time t, then standing still.
     *
     * @param t the time to stop
     * @return the stopped motion
     */
    Motion stoppedAt(double t) {
        List<Piece> result = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.t1 <= t) {
                result.add(p);
            } else if (p.t0 < t) {
                result.add(new Piece(p.t0, t, p.s0, p.v0, p.a));
            }
        }
        double s = position(t);
        result.add(new Piece(t, endTime(), s, 0.0, 0.0));
        return new Motion(result);
    }

    /**
     * A time when a point of the train goes into or out of one side of a
     * boundary.
     */
    static final class Crossing {

        final double time;
        /** true if, after this time, the point is on the "inside" side */
        final boolean inside;

        Crossing(double time, boolean inside) {
            this.time = time;
            this.inside = inside;
        }
    }

    /**
     * Find the times when a point of the train, at an offset from the position
     * this motion follows, goes into or out of one side of a boundary.
     *
     * @param offset   added to the position to give the point
     * @param boundary the boundary position
     * @param above    true if the inside is beyond the boundary (point &gt;
     *                 boundary), false if before it (point &lt; boundary)
     * @return the crossings in time order
     */
    List<Crossing> crossings(double offset, double boundary, boolean above) {
        List<Crossing> result = new ArrayList<>();
        boolean was = isInside(pieces.get(0).s0 + offset, boundary, above);
        for (Piece p : pieces) {
            boolean now = isInside(p.end() + offset, boundary, above);
            if (now != was) {
                result.add(new Crossing(p.timeAt(boundary - offset), now));
                was = now;
            }
        }
        return result;
    }

    /**
     * The "inside" test used for crossings.
     *
     * @param point    position of the point
     * @param boundary the boundary
     * @param above    true: inside when point &gt; boundary; false: inside when
     *                 point &lt; boundary
     * @return true if inside
     */
    static boolean isInside(double point, double boundary, boolean above) {
        return above ? point > boundary : point < boundary;
    }
}
