package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of 2nd algorithm for reducing Readings
 * <p>
 * This algorithm was provided by Robert Ashenfelter based in part on the work
 * of Ralph Bucher in his paper "Exact Solution for Three Dimensional Hyperbolic
 * Positioning Algorithm and Synthesizable VHDL Model for Hardware
 * Implementation".
 * <p>
 * Neither Ashenfelter nor Bucher provide any guarantee as to the intellectual
 * property status of this algorithm. Use it at your own risk.
 *
 *
 * Here is a summary of the features of the new program from Robert Ashenfelter:
 *
 * <ol>
 * <li> It is completely iterative. No more exact solutions for sets of three
 * receivers. No more weighted averages of such solutions.
 *
 * <li> Although both the old and the new versions can accept an unlimited
 * number of receivers, the old version only processes a maximum of 15 while the
 * new version processes up to 50.
 *
 * <li> The accuracy of the new version is approximately the same as for the old
 * version, perhaps marginally better. However for more than 15 receivers it is
 * significantly better.
 *
 * <li> It has been designed to specifically reject receiver measurements with
 * gross errors, i.e. those which are so large that there is no possible
 * position solution when they are combined with other measurements. It does so
 * much better than version 1.1. (However, version 1.1 has deficiencies in this
 * regard and is not as good at this as version 1.0.)
 *
 * <li> It is slightly faster.
 * </ol>
 *
 * Here is a description of the new program.
 * <p>
 * As before the first thing it does is sort the receivers in order of
 * increasing time delay, discarding those that failed or are too far or too
 * near, and using the closest ones. There is a maximum that are used, now set
 * at 50.
 * <p>
 * Next it discards those receivers with gross measurement errors. All possible
 * pairs of receivers are checked to see if the sum of their measured ranges is
 * less than, or the difference is greater than, the distance between the
 * receivers. Counts are maintained for each receiver and the one with the
 * largest count is booted out. The proceedure is repeated until there are no
 * more failures. If fewer than three receivers are left there can be no
 * solution and an error code is returned.
 * <p>
 * Two iterative techniques are used which I call "One-At-A-Time" and
 * "All-Together." The first looks at one receiver at a time and moves the
 * estimated position directly toward or away from it such that the distance is
 * equal to the measured value. This simple technique usually converges quite
 * rapidly. The second technique accumulates the adjustments for all receivers
 * and then computes and applies an average for all. It is not as fast but is
 * ultimately more accurate.
 * <p>
 * The solution proceeds in four stages, the first two of which are like the
 * preliminary solution in version 1.1. Stage 0 does 50 One-At-A-Time iterations
 * with the receivers in the sorted order. As in version 1.1, it starts from a
 * position far, far below any likely final point. Stage 1 continues with the
 * receivers chosen at random until it has iterated 1000 times. The procedure
 * usually converges in 20-50 iterations, however for occasional positions the
 * convergence is much slower. The random order is used because the procedure
 * was occasionally observed to get stuck in a loop when using a repetitive
 * fixed order.
 * <p>
 * Stage 2 continues the One-At-A-Time technique for an additional 250
 * iterations with the receivers in reverse order ending with the closest
 * receiver. Weights are applied assuming that close measurements are more
 * accurate than distant ones. The weights fade out during the stage so that at
 * the end the adjustments are very small. This fade-out fixes a problem with
 * the One-At-A-Time technique in that it gives undue weight to the last
 * receiver. The result at this point is quite good and the program could well
 * stop here but it doesn't. Stage 3 runs the All-Together iteration 15 times,
 * also using weights according to distance, to produce a more refined result.
 * <p>
 * The program always runs through all the iterations regardless of how fast or
 * slow the solution converges. Only at the end does it compute the variance of
 * the residuals (differences between measured receiver distances and those from
 * the computed position) to check the result. The execution time ranges from
 * 0.8 millisecond with 3 receivers to 1.3 millisecond with 50 or more receivers
 * (1.0 GHz Pentium III).
 * <p>
 * Input/output is the same as for versions 1.0 and 1.1. As before, the function
 * returns 0 if all seems well and 1 if there are fewer than 3 usable receivers
 * (with the reported position outside the known universe). A return value of 2
 * indicates that the variance of the residuals exceeds a fixed threshold so the
 * reported position is questionable. The threshold is set at 30 microseconds
 * which is equivalent to a standard deviation of 0.4 inch or 1.0 cm. This is
 * about as small as I dare set it. Usually the reported position is garbage
 * (because of errors in the input data) when the return value is 2, but it
 * could be close if the input is merely excessively noisy. Likewise, the
 * reported position is usally OK when the return value is 0 but this cannot be
 * guaranteed. After all, errors in the data could happen to mimic good values
 * for a wrong position. These return values tend to less reliable when the
 * program is overloaded with too many large errors.
 * <p>
 * The restrictions on the configuration of transmitters and receivers,
 * necessary to prevent the program from reporting a spurious position, are the
 * same as those for version 1.1.
 * <p>
 * As before, I have tested the program with a large number of different
 * receiver configurations having from 3 to 100 receivers and with many
 * transmitter locations. In addition to small random measurement errors, I have
 * added the simulation of large errors to the tests. To simulate such an error,
 * I pick a random receiver and replace its time delay with a random value
 * between 0 and 35,000 microseconds (equivalent to 0 to 40 feet). Depending on
 * the configuration, this may result in a gross error or the error may be too
 * small for this but still so large as to cause the solution to fail. Some
 * results for four receivers and one large error are that with Larry's small
 * initial test layout the program rejects the error and computes a correct
 * position 90% of the time, while with a more-typical layout it may be only
 * 50%. Performance improves to 80% correct for the typical layout with six
 * receivers.
 *
 * @author	Robert Ashenfelter Copyright (C) 2007
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public class Ash2_0Algorithm extends AbstractCalculator {

    public Ash2_0Algorithm(Point3d[] sensors, double vsound, int offset) {
        this(sensors, vsound);
        this.offset = offset;
    }

    public Ash2_0Algorithm(Point3d[] sensors, double vsound) {
        this.sensors = Arrays.copyOf(sensors, sensors.length);
        this.Vs = vsound;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
    }

    public Ash2_0Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this(null, vsound);
        sensors = new Point3d[3];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
    }

    public Ash2_0Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
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

        prep(r);

        RetVal result = RPSpos(nr, Tr, Xr, Yr, Zr, Vs, Xt, Yt, Zt);
        Xt = result.x;
        Yt = result.y;
        Zt = result.z;
        Vs = result.vs;

        // must convert to new code 
        int code;
        switch (result.code) {
            case 0: // ok
                code = 4;
                break;
            case 1: // less than 3 receivers
                code = 0;
                break;
            case 2: // variance too large
                code = -Tr.length;
                break;
            default:
                log.error("Unexpected error code: " + result.code);
                code = 0;
        }
        log.debug("old code=" + result.code + " new code=" + code);

        log.debug("x = " + Xt + " y = " + Yt + " z0 = " + Zt + " code = " + code);
        return new Measurement(r, Xt, Yt, Zt, Vs, code, "Ash2_0Algorithm");
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

// ----------------------------------------------------
//	RPS  POSITION  SOLVER	Version 2.0	by R. C. Ashenfelter    1-14-07

    /*							*
     *  This algorithm was provided by Robert Ashenfelter	*
     *  who provides no guarantee as to its usability,	*
     *  correctness nor intellectual property status.	*
     *  Use it at your own risk.				*
     *							*/
    int offset = 0;			//  Offset (usec), add to delay

    final static int TMAX = 35000;			//  Max. allowable delay (usec)
    final static int TMIN = 150;		//  Min. allowable delay (usec)
    final static int SMAX = 30;			//  Max. OK std. dev. (usec)
    final static int NMAX = 50;			//  Max. no. of receivers used

    //  Compute RPS Position using
    @SuppressFBWarnings(value = "IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN") // it's secretly FORTRAN..
    RetVal RPSpos(int nr, double Tr[], double Xr[], double Yr[], double Zr[],// many
            double Vs, double Xt, double Yt, double Zt) {//         receivers

        int i, j, k, ns, cmax;
        int[] ce = new int[NMAX];
        double Rq;
        double[] Rs = new double[NMAX];
        double[] Xs = new double[NMAX];
        double[] Ys = new double[NMAX];
        double[] Zs = new double[NMAX];
        double x, y, z, Rmax;
        double Ww, Xw, Yw, Zw, w, q;
        double err, var, vmax, vmin;

        j = k = 0;
        var = 0;

        vmax = SMAX * SMAX * Vs * Vs;
        vmin = 1.0 * Vs * Vs;
        ns = 0;
        Rs[NMAX - 1] = TMAX;
        Rmax = Vs * TMAX;//  Sort receivers by delay
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
        while (cmax != 0) {//    Repetitively discard
            cmax = 0;//              worst offender
            for (i = 0; i < ns; i++) {
                if (ce[i] >= cmax) {
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

        if (ns < 3) {//   Failed:
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(1, Xt, Yt, Zt, Vs);
        }//   Too few usable receivers

        x = y = 0.0;
        z = -100000.0;//  Iterative solution
        for (i = 0; i < 1250; i++) {//   One-At-A-Time iteration
            if (i < 50) {
                j = k = i % ns;//    Stage 0
            } else if (i < 1000) {//     Receivers in order
                while ((j = (int) Math.floor((ns) * Math.random())) == k) {
                    //    Stage 1
                }
                k = j;
            }//    Receivers random order
            else {
                j = (1249 - i) % ns;//    Stage 2
            }
            if (i < 750) {
                w = 1.0;//     Receivers reverse order
            } else {
                w = 1.0 - Rs[j] / Rmax;
                w = w * w;
            }//    Weight by distance
            if (i >= 1000) {
                w *= 5.0 - 0.004 * i;//		  with fade out
            }
            q = Math.sqrt((Xs[j] - x) * (Xs[j] - x) + (Ys[j] - y) * (Ys[j] - y) + (Zs[j] - z) * (Zs[j] - z));
            q = w * (1.0 - Rs[j] / q);//    Adjustment factor
            x += q * (Xs[j] - x);//    Position adjustments
            y += q * (Ys[j] - y);
            z += q * (Zs[j] - z);
        }

        for (i = 0; i < 15; i++) {//   Stage 3
            Ww = Xw = Yw = Zw = var = 0.0;//    All-Together iteration
            for (j = 0; j < ns; j++) {//     For all receivers
                q = Math.sqrt((Xs[j] - x) * (Xs[j] - x) + (Ys[j] - y) * (Ys[j] - y) + (Zs[j] - z) * (Zs[j] - z));
                err = q - Rs[j];//      Residual error
                q = 1.0 - Rs[j] / q;//      Adjustment factor
                w = 1.0 - Rs[j] / Rmax;
                w = w * w;//      Weight by distance
                Xw += w * (x + q * (Xs[j] - x));//      Accumulate averages
                Yw += w * (y + q * (Ys[j] - y));
                Zw += w * (z + q * (Zs[j] - z));
                Ww += w;
                var += w * err * err;
            }
            x = Xw / Ww;
            y = Yw / Ww;
            z = Zw / Ww;//     Avg. adjusted position
            var = var / Ww;
        }

        Xt = x;
        Yt = y;
        Zt = z;//  Computed position
        if ((var > vmax) || ((ns == 3) && (var > vmin))) {
            return new RetVal(2, Xt, Yt, Zt, Vs);
        }//  Failed:  variance too big
        return new RetVal(0, Xt, Yt, Zt, Vs);//   Success!    (probably...)
    }//  End of RPSpos()

// ----------------------------------------------------
    /**
     * Internal class to handle return value.
     *
     * More of a struct, really
     */
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD") // t not formally needed
    static class RetVal {

        RetVal(int code, double x, double y, double z, double vs) {
            this.code = code;
            this.x = x;
            this.y = y;
            this.z = z;
            this.vs = vs;
        }
        int code;
        double x, y, z, t, vs;
    }

    private final static Logger log = LoggerFactory.getLogger(Ash2_0Algorithm.class);

}
