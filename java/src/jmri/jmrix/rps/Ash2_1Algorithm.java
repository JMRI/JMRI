package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of 2.1th algorithm for reducing Readings
 * <p>
 * This algorithm was provided by Robert Ashenfelter based in part on the work
 * of Ralph Bucher in his paper "Exact Solution for Three Dimensional Hyperbolic
 * Positioning Algorithm and Synthesizable VHDL Model for Hardware
 * Implementation".
 * <p>
 * Neither Ashenfelter nor Bucher provide any guarantee as to the intellectual
 * property status of this algorithm. Use it at your own risk.
 *
 * @author	Robert Ashenfelter Copyright (C) 2007
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class Ash2_1Algorithm extends AbstractCalculator {

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public Ash2_1Algorithm(Point3d[] sensors, double vsound, int offset) {
        this(sensors, vsound);
        this.offset = offset;
    }

    public Ash2_1Algorithm(Point3d[] sensors, double vsound) {
        this.sensors = Arrays.copyOf(sensors, sensors.length);
        this.Vs = vsound;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
    }

    public Ash2_1Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this(null, vsound);
        sensors = new Point3d[3];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
    }

    public Ash2_1Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
        this(null, vsound);
        sensors = new Point3d[4];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
        sensors[3] = sensor4;
    }

    double Vs;
    double Xt = 0.0;
    double Yt = 0.0;
    double Zt = 0.0;

    @Override
    public Measurement convert(Reading r) {

        if (log.isDebugEnabled()) {
            log.debug("Reading: " + r.toString());
            log.debug("Sensors: " + sensors.length);
            if (sensors.length >= 1 && sensors[0] != null) {
                log.debug("Sensor[0]: " + sensors[0].x + "," + sensors[0].y + "," + sensors[0].z);
            }
            if (sensors.length >= 2 && sensors[1] != null) {
                log.debug("Sensor[1]: " + sensors[1].x + "," + sensors[1].y + "," + sensors[1].z);
            }
        }

        prep(r);

        RetVal result = RPSpos(nr, Tr, Xr, Yr, Zr, Vs, Xt, Yt, Zt);
        Xt = result.x;
        Yt = result.y;
        Zt = result.z;
        Vs = result.vs;

        log.debug("x = " + Xt + " y = " + Yt + " z0 = " + Zt + " code = " + result.code);
        return new Measurement(r, Xt, Yt, Zt, Vs, result.code, "Ash2_1Algorithm");
    }

    /**
     * Seed the conversion using an estimated position
     */
    @Override
    public Measurement convert(Reading r, Point3d guess) {
        this.Xt = guess.x;
        this.Yt = guess.y;
        this.Zt = guess.z;

        return convert(r);
    }

    /**
     * Seed the conversion using a last measurement
     */
    @Override
    public Measurement convert(Reading r, Measurement last) {
        if (last != null) {
            this.Xt = last.getX();
            this.Yt = last.getY();
            this.Zt = last.getZ();
        }

        // if the last measurement failed, set back to origin
        if (this.Xt > 9.E99) {
            this.Xt = 0;
        }
        if (this.Yt > 9.E99) {
            this.Yt = 0;
        }
        if (this.Zt > 9.E99) {
            this.Zt = 0;
        }

        return convert(r);
    }

//	RPS  POSITION  SOLVER	Version 2.1	by R. C. Ashenfelter    2-02-07
//						Return values modified	7-10-07

    /*							*
     *  This algorithm was provided by Robert Ashenfelter	*
     *  who provides no guarantee as to its usability,	*
     *  correctness nor intellectual property status.	*
     *  Use it at your own risk.				*
     *							*/
    int offset = 0;			//  Offset (usec), add to delay

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // for script access
    static public int TMAX = 35000;			//  Max. allowable delay (usec)

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // for script access
    static public int TMIN = 150;			//  Min. allowable delay (usec)

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // for script access
    static public int SMAX = 30;			//  Max. OK std. dev. (usec)

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // for script access
    static public int NMAX = 50;			//  Max. no. of receivers used

    //  Compute RPS Position  using
    RetVal RPSpos(int nr, double Tr[], double Xr[], double Yr[], double Zr[],//   many
            double Vs, double Xt, double Yt, double Zt) {//         receivers

        int i, j, jmax, k, ns, nss, nxx, nox, tov, S, cmax;
        int[] ce = new int[NMAX];

        double Rq;
        double[] Rs = new double[NMAX];
        double[] Xs = new double[NMAX];
        double[] Ys = new double[NMAX];
        double[] Zs = new double[NMAX];

        double x, y, z, Rmax;
        double Ww, Xw, Yw, Zw, w, q;
        double err, emax, thr, var, vmax, vmin, vold;

        j = k = jmax = nox = 0;
        w = 0;
        var = 0;

        vmax = SMAX * SMAX * Vs * Vs;
        vmin = 1.0 * Vs * Vs;
        ns = 0;
        Rmax = Vs * TMAX;
        Rs[NMAX - 1] = TMAX;//  Sort receivers by delay
        for (i = 0; i < nr; i++) {
            if (Tr[i] == 0.0) {
                continue;//   Discard failures
            }
            Rq = Vs * (Tr[i] + offset);//   Compute range from delay
            if ((Rq >= Rmax) || (Rq < Vs * TMIN)) {
                continue;//  Discard too long or short
            }
            if (ns == 0) {
                Rs[0] = Rq;
                Xs[0] = Xr[i];
                Ys[0] = Yr[i];
                Zs[0] = Zr[i];
                ns = 1;
            }//  1st entry
            else {
                j = ((ns == NMAX) ? (ns - 1) : (ns++));//   Keep no more than NMAX
                for (;; j--) {//   Bubble sort
                    if ((j > 0) && (Rq < Rs[j - 1])) {
                        Rs[j] = Rs[j - 1];
                        Xs[j] = Xs[j - 1];//    Move old entries
                        Ys[j] = Ys[j - 1];
                        Zs[j] = Zs[j - 1];
                    } else {
                        if ((j < NMAX - 1) || (Rq < Rs[j])) {//    Insert new entry
                            Rs[j] = Rq;
                            Xs[j] = Xr[i];
                            Ys[j] = Yr[i];
                            Zs[j] = Zr[i];
                        }
                        break;
                    }
                }
            }
        }

        for (i = 0; i < ns; i++) {
            ce[i] = 0;//   Reject gross errors
        }
        for (i = 0; i < ns - 1; i++) {
            for (j = i + 1; j < ns; j++) {
                q = Math.sqrt((Xs[i] - Xs[j]) * (Xs[i] - Xs[j])
                        + (Ys[i] - Ys[j]) * (Ys[i] - Ys[j]) + (Zs[i] - Zs[j]) * (Zs[i] - Zs[j]));
                if ((Rs[i] + Rs[j] < q) || (Rs[i] - Rs[j] > q) || (Rs[j] - Rs[i] > q)) {
                    ++ce[i];
                    ++ce[j];
                }
            }
        }// Count them
        cmax = 1;
        nxx = 0;
        while (cmax != 0) {//    Repetitively discard
            cmax = 0;//              worst offender
            for (i = 0; i < ns; i++) {
                if (ce[i] >= cmax) {
                    if (ce[i] > 0) {
                        nxx = ((ce[i] == cmax) ? nxx + 1 : 1);
                    }
                    cmax = ce[i];
                    j = i;
                }
            }//   Find it
            if (cmax > 0) {
                for (i = 0; i < ns; i++) {//     Adjust remaining counts
                    if (i == j) {
                        continue;
                    }
                    q = Math.sqrt((Xs[i] - Xs[j]) * (Xs[i] - Xs[j])
                            + (Ys[i] - Ys[j]) * (Ys[i] - Ys[j]) + (Zs[i] - Zs[j]) * (Zs[i] - Zs[j]));
                    if ((Rs[i] + Rs[j] < q) || (Rs[i] - Rs[j] > q) || (Rs[j] - Rs[i] > q)) {
                        --ce[i];
                    }
                }//    Adjustment
                for (i = j; i < ns - 1; i++) {//     Discard gross error
                    Rs[i] = Rs[i + 1];
                    Xs[i] = Xs[i + 1];
                    Ys[i] = Ys[i + 1];
                    Zs[i] = Zs[i + 1];//      Move old entries
                    ce[i] = ce[i + 1];
                }
                --ns;
            }
        }//    One less receiver
        nss = ns;

        if (ns < 3) {//   Failed:
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(0, Xt, Yt, Zt, Vs);
        }//   Too few usable receivers

        S = i = tov = 0;
        x = y = 0.0;
        z = -100000.0;//  Iterative solution
        while (S < 4) {
            if (S == 0) {//   Stage 0
                j = k = i % ns;//    Receivers in order
                w = 1.0;
            }//   No wgts.  No "All-Tog."
            else if (S == 1) {//   Stage 1
                while ((j = (int) Math.floor((ns) * Math.random())) == k) {
                    //    Receivers random order
                }
                k = j;
                w = 1.0;
            }//   No weights
            else if (S == 2) {//   Stage 2
                --k;
                j = k % ns;//    Receivers reverse order
                w = 1.0 - Rs[j] / Rmax;
                w = w * w;//    Weight by distance
                w *= 0.01 * (k + 1);
            }//		 with fade out
            else if (S == 3) {//   Stage 3
            }//   No "One-at-a-time"

            if (S < 3) {//   One-At-A-Time iteration
                q = Math.sqrt((Xs[j] - x) * (Xs[j] - x) + (Ys[j] - y) * (Ys[j] - y) + (Zs[j] - z) * (Zs[j] - z));
                q = w * (1.0 - Rs[j] / q);//    Adjustment factor
                x += q * (Xs[j] - x);//    Position adjustments
                y += q * (Ys[j] - y);
                z += q * (Zs[j] - z);
                ++i;
            }

            if (((S == 1) && (i % 50 == 0)) || ((S == 2) && (k == 0)) || (S == 3)) {
                Ww = Xw = Yw = Zw = emax = 0.0;//   All-Together iteration
                vold = var;
                var = 0.0;
                for (j = 0; j < ns; j++) {//    For all receivers
                    q = Math.sqrt((Xs[j] - x) * (Xs[j] - x) + (Ys[j] - y) * (Ys[j] - y) + (Zs[j] - z) * (Zs[j] - z));
                    err = q - Rs[j];
                    err = err * err;//     Residual error
                    q = 1.0 - Rs[j] / q;//     Adjustment factor
                    if (S >= 2) {
                        w = 1.0 - Rs[j] / Rmax;
                        w = w * w;
                    }//    Weight by distance
                    else {
                        w = 1.0;
                    }
                    Xw += w * (x + q * (Xs[j] - x));//     Accumulate averages
                    Yw += w * (y + q * (Ys[j] - y));
                    Zw += w * (z + q * (Zs[j] - z));
                    Ww += w;
                    var += w * err;
                    if (w * err > emax) {//     Capture max. outlier
                        emax = w * err;
                        jmax = j;
                    }
                }
                x = Xw / Ww;
                y = Yw / Ww;
                z = Zw / Ww;//    Avg. adjusted position
                var = var / Ww;
                i += ns;
                thr = (10.0 - 30.0 / ns) * Ww / ns;//    Outlier threshold
                if ((S >= 2) && (ns > 3) && (((ns > 4) && (emax > var * thr)) || (var > 3 * vmax))) {
                    tov = ((emax > var * thr) ? 0 : 1);
                    --ns;
                    nox = 0;//    If outlier too big
                    Rs[jmax] = Rs[ns];
                    Xs[jmax] = Xs[ns];//     Discard outlier entry
                    Ys[jmax] = Ys[ns];
                    Zs[jmax] = Zs[ns];
                } else {
                    ++nox;//    No discard
                }
                if ((S == 1) && (((var > 0.999 * vold) && (var < 3 * vmax))
                        || (var < vmin) || (i >= 750))) {
                    k = 200;
                    nox = 0;
                    ++S;
                }//  Advance to Stage 2
                if ((S == 2) && (k == 0)) {
                    k = 200;
                    if (((nox >= 2) && (var > 0.999 * vold)) || (var < vmin) || (i >= 2000)) {
                        nox = 0;
                        ++S;
                    }
                }// Advance to Stage 3
                if ((S == 3) && (((nox >= 1 + 110 / (ns + 5)) && (var > 0.999 * vold))
                        || (var < 0.1 * vmin) || (i >= 2500 - ns))) {
                    ++S;
                }
            }// Advance to done...

            if ((S == 0) && (i >= 50)) {
                k = j;
                var = 9e9;
                ++S;
            }
        }// Advance to Stage 1

        Xt = x;
        Yt = y;
        Zt = z;//  Computed position
        if ((var > vmax) || ((ns == 3) && (var > vmin))) {//   Failed:
            return new RetVal(-ns, Xt, Yt, Zt, Vs);
        }//	       variance too big
        if ((ns == 3) && ((nss > 4) || (nxx > 1) || (tov != 0))) {//   Questionable:   uncertain
            return new RetVal(1, Xt, Yt, Zt, Vs);
        }//		gross rejection
        if ((ns == 4) && ((nss > 5) || ((nss == 5) && (nxx == 1) && (tov == 1)))) {//   or
            return new RetVal(2, Xt, Yt, Zt, Vs);
        }//		       too many
        if ((ns >= 5) && (nss > (3 * ns - 3) / 2)) {
            return new RetVal(2, Xt, Yt, Zt, Vs);//	     outlier rejections
        }
        return new RetVal(ns, Xt, Yt, Zt, Vs);//   Success!    (probably...)
    }//  End of RPSpos()

// ----------------------------------------------------
    /**
     * Internal class to handle return value.
     *
     * More of a struct, really
     */
    static class RetVal {

        RetVal(int code, double x, double y, double z, double vs) {
            this.code = code;
            this.x = x;
            this.y = y;
            this.z = z;
            this.vs = vs;
        }
        int code;
        @SuppressFBWarnings(value = "UUF_UNUSED_FIELD")
        double x, y, z, t, vs;
    }

    private final static Logger log = LoggerFactory.getLogger(Ash2_1Algorithm.class);

}
