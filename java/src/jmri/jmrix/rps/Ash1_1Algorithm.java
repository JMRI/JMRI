package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of version 1.1 algorithm for reducing Readings
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
 * The following is a summary of this version from Robert Ashenfelter:
 * <p>
 * When the receivers are in a plane or nearly so there is a spurious solution
 * on the other side of the plane from the correct solution and in certain
 * situations the version 1.0 program may choose the wrong solution.
 * <p>
 * It turns out that those situations are when the receiver configuration is not
 * sufficiently non-planar for the size of the measurement errors. The greater
 * the errors, the greater the non-planarity must be to avoid he bug.
 * <p>
 * I had hoped to be able to devise some measure of the non-planarity of the
 * receiver configuration and to set some threshold below which the program
 * would switch to a different algorithm but this didn't seem to be working out
 * very well. After trying several things, I have chosen to use an iterative
 * procedure to determine an approximate solution.
 * <p>
 * Here is a description of the new program.
 * <p>
 * As before the first thing it does is sort the receivers in order of
 * increasing time delay, discarding those that failed or are too far or too
 * near, and using the closest ones. There is a maximum that are used, still set
 * at 15.
 * <p>
 * Next it computes a preliminary transmitter position which is used to
 * discriminate against spurious solutions and to compute weights. This is the
 * part of the program that has been changed to fix the bug. The new algorithm
 * looks at one receiver at a time and moves the estimated position directly
 * toward or away from it such that the distance is equal to the measured value.
 * After going through the receivers once in order, it then chooses them at
 * random until it has iterated some fixed number of times. This is set at 1000
 * although the procedure usually converges in 20-50 iterations; for occasional
 * positions the convergence is much slower. The random order is used because
 * the procedure was occasionally observed to get stuck in a loop when using a
 * repetitive fixed order. Rather than start with the origin as the initial
 * position, it now starts from a position far, far below. This removes the
 * restriction that the origin must be below the receivers.
 * <p>
 * Finally, as before, the transmitter position is computed as a weighted
 * average of the GPS solutions for all possible sets of three receivers. (For
 * 15 receivers, that's 455 solutions.) The weights are the same as before.
 * Unless one of them chooses a spurious solution, both versions of the program
 * produce the same computed position.
 * <p>
 * Restrictions:
 * <ol>
 * <li>The origin can be anywhere, but the z-axis must be vertical with positive
 * z upward.
 *
 * <li>In general, the transmitter should be below some or all of the receivers.
 * How much below depends on the receiver configuration.
 *
 * <li>If the receivers are in a plane, or nearly so, the transmitter must
 * absolutely be below the plane. As it approaches the plane (such that the
 * lines-of-sight to the receivers make shallow angles with respect to the
 * plane), the accuracy degrades and ultimately the program may report failure.
 * If above the plane, the program reports incorrect positions.
 *
 * <li>If the receivers are not in a plane, it may be possible to move the
 * transmitter up among them. In general it should remain inside or below the
 * volume of space contained by the receivers. However if the configuration is
 * sufficiently non-planar the transmitter can go farther. But the limits are
 * uncertain and there is no warning as it approaches a limit; the reported
 * position suddenly jumps to somewhere else, or perhaps it jumps back and forth
 * depending on measurement errors. An extreme example is 8 receivers at the
 * corners of a cube, which is about as non-planar as it gets. In this case the
 * transmitter can go outside the cube by several times the width of the cube,
 * both laterally and vertically, before the program gets into trouble.
 * </ol>
 * <p>
 * I have tested the program with nearly 20 different receiver configurations
 * having from 3 to 100 receivers. Most were tested at 60 or more transmitter
 * locations and with infinitesimal, nominal (+/-0.25 inches--Walter's spec.),
 * and large (+/-2.5 inches) measurement errors. Half of the configurations
 * consisted of a 10 x 20-foot room with the ceiling 5 feet above the lowest
 * transmitter positions and the receivers (from 3 to 18) located on the walls
 * and/or ceiling. Other configurations are Larry Wade's rather-small oval test
 * track with 4 receivers and a 14-foot square and an 8-foot cube (mentioned
 * above). Large, 100-receiver configurations include a 25 x 10 x 5-foot space
 * with receivers randomly located throughout (rather unrealistic) and a 100 x
 * 10 x 5-foot space with receivers arranged in 4 rows of 25, one row on each
 * long wall and two rows on the ceiling. Performance (i.e. accuracy of the
 * measured transmitter position) is excellent throughout this latter space.
 * <p>
 * Two other configurations are 20-foot-diameter geodesic domes with receivers
 * located at the vertices of the triangular faces of the domes, one with 16
 * receivers and one with 46. Performance is good throughout the interior of
 * these domes, but surprisingly it is no better with 46 receivers than with 16,
 * near the perimeter a bit worse. Presumably this is because of the limited
 * number of closest receivers used by the position program. In order to do
 * justice to this, or any other configuration with closely-spaced receivers,
 * the program needs to use data from more than the 15 receivers currently used.
 * <p>
 * As a result of all this testing, I feel pretty confident that version 1.1
 * works reliably if used within the restrictions listed above. But the
 * disclaimer about "usability and correctness" stays.
 * <p>
 * The execution time is increased a little by all those iterations. It now
 * ranges from 0.5 millisecond with 3 receivers to 1.9 millisecond with 15 or
 * more receivers (1.0 GHz Pentium III).
 *
 * @author	Robert Ashenfelter Copyright (C) 2006
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class Ash1_1Algorithm implements Calculator {

    public Ash1_1Algorithm(Point3d[] sensors, double vsound) {
        this.sensors = Arrays.copyOf(sensors, sensors.length);
        this.Vs = vsound;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
    }

    public Ash1_1Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this(null, vsound);
        sensors = new Point3d[3];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
    }

    public Ash1_1Algorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
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

        int nr = r.getNValues();
        if (nr != sensors.length) {
            log.error("Mismatch: " + nr + " readings, " + sensors.length + " receivers");
        }
        nr = Math.min(nr, sensors.length); // accept the shortest

        double[] Tr = new double[nr];
        double[] Xr = new double[nr];
        double[] Yr = new double[nr];
        double[] Zr = new double[nr];
        for (int i = 0; i < nr; i++) {
            Tr[i] = r.getValue(i);
            Xr[i] = sensors[i].x;
            Yr[i] = sensors[i].y;
            Zr[i] = sensors[i].z;
        }

        RetVal result = RPSpos(nr, Tr, Xr, Yr, Zr, Vs, Xt, Yt, Zt);
        Xt = result.x;
        Yt = result.y;
        Zt = result.z;
        Vs = result.vs;

        log.debug("x = " + Xt + " y = " + Yt + " z0 = " + Zt + " code = " + result.code);
        return new Measurement(r, Xt, Yt, Zt, Vs, result.code, "Ash1_1Algorithm");
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

    // Sensor position objects
    Point3d sensors[];

    /**
     * The following is the original algorithm, as provided by Ash as a C
     * routine
     */
//	RPS  POSITION  SOLVER	Version 1.1	by R. C. Ashenfelter   12-02-06

    /*							*
     *  This algorithm was provided by Robert Ashenfelter	*
     *  who provides no guarantee as to its usability,	*
     *  correctness nor intellectual property status.	*
     *  Use it at your own risk.				*
     *							*/
    static final int OFFSET = 0;    		//  Offset (usec), add to delay
    static final int TMAX = 35000;		//  Max. allowable delay (usec)
    static final int TMIN = 150;  		//  Min. allowable delay (usec)
    static final int NMAX = 15;			//  Max. no. of receivers used

    double x, y, z, x0, y0, z0, Rmax;
    double xi, yi, zi, ri, xj, yj, zj, rj, xk, yk, zk, rk;

    //  Compute RPS Position using
    @SuppressFBWarnings(value = "IP_PARAMETER_IS_DEAD_BUT_OVERWRITTEN") // it's secretly FORTRAN..
    RetVal RPSpos(int nr, double Tr[], double Xr[], double Yr[], double Zr[],// many
            double Vs, double Xt, double Yt, double Zt) {//         receivers

        int i, j, k, ns;
        double Rq;
        double[] Rs = new double[NMAX];
        double[] Xs = new double[NMAX];
        double[] Ys = new double[NMAX];
        double[] Zs = new double[NMAX];
        double Ww, Xw, Yw, Zw, w;

        k = 0;

        ns = 0;
        Rs[NMAX - 1] = TMAX;
        Rmax = Vs * TMAX;//  Sort receivers by delay
        for (i = 0; i < nr; i++) {
            if (Tr[i] == 0.0) {
                continue;//   Discard failures
            }
            Rq = Vs * (Tr[i] + OFFSET);//   Compute range from delay
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

        if (ns < 3) {//   Failed:
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(1, Xt, Yt, Zt, Vs);
        }//   Too few usable receivers

        x = y = 0.0;
        z = -100000.0;//  Initial solution
        for (i = 0; i < 1000; i++) {//     to reject spurious sol.
//if (i%10 == 1) printf("\n%4d   %8.3lf %8.3lf %8.3lf",i,x,y,z);
            if (i < ns) {
                j = i;//     and to calc. weights
            } else {
                while ((j = (int) Math.floor(
                        (ns) * Math.random()
                ))
                        == k) {
                    // Iterative solution
                }
            }

            k = j;
            w = Math.sqrt((Xs[j] - x) * (Xs[j] - x) + (Ys[j] - y) * (Ys[j] - y) + (Zs[j] - z) * (Zs[j] - z));
            w = Rs[j] / w;
            x = w * (x - Xs[j]) + Xs[j];//          with
            y = w * (y - Ys[j]) + Ys[j];
            z = w * (z - Zs[j]) + Zs[j];
        }// 1000 random receivers

        Ww = Xw = Yw = Zw = 0.0;//  Final solution
        for (i = 0; i < ns - 2; i++) {//   Weighted average
            xi = Xs[i];
            yi = Ys[i];
            zi = Zs[i];
            ri = Rs[i];//            of all sets
            for (j = i + 1; j < ns - 1; j++) {
                xj = Xs[j];
                yj = Ys[j];
                zj = Zs[j];
                rj = Rs[j];
                for (k = j + 1; k < ns; k++) {
                    xk = Xs[k];
                    yk = Ys[k];
                    zk = Zs[k];
                    rk = Rs[k];
                    if (gps3() == 0) {//    Solve for each set
                        if ((w = wgt()) > 0.0) {//     Add to averages
                            Ww += w;
                            Xw += w * x0;
                            Yw += w * y0;
                            Zw += w * z0;
                        }
                    }
                }
            }
        }

        if (Ww > 0.0) {
            Xt = Xw / Ww;
            Yt = Yw / Ww;
            Zt = Zw / Ww;//   Computed position
            return new RetVal(0, Xt, Yt, Zt, Vs);
        }//   Success
        else {
            Xt = Yt = Zt = 9.9999999e99;
            return new RetVal(2, Xt, Yt, Zt, Vs);
        }//   Failed:  No solution
    }//  End of RPSpos()

    double wgt() {// Weighting Function
        double w;

        w = (1 - ri / Rmax) * (1 - rj / Rmax) * (1 - rk / Rmax);//			 Ranges
        w *= 1.0 - Math.pow(((x - xi) * (x - xj) + (y - yi) * (y - yj) + (z - zi) * (z - zj)) / ri / rj, 2.);//Angles
        w *= 1.0 - Math.pow(((x - xi) * (x - xk) + (y - yi) * (y - yk) + (z - zi) * (z - zk)) / ri / rk, 2.);
        w *= 1.0 - Math.pow(((x - xj) * (x - xk) + (y - yj) * (y - yk) + (z - zj) * (z - zk)) / rj / rk, 2.);
        w *= 0.05 + Math.abs((zi + zj + zk - 3 * z) / (ri + rj + rk));//		    Verticality
        w *= (((yk - yi) * (zj - zi) - (yj - yi) * (zk - zi)) * (x - xi)
                + ((zk - zi) * (xj - xi) - (zj - zi) * (xk - xi)) * (y - yi)
                + ((xk - xi) * (yj - yi) - (xj - xi) * (yk - yi)) * (z - zi)) / (ri * rj * rk);//	 Volume
        w = Math.abs(w);
        if ((w > 0.5) || (w < .0000005)) {
            w = 0.0;
        }
        return (w);
    }

    int gps3() {// GPS Position Solver
        @SuppressWarnings("unused")
        double xij, yij, zij, rij, xik, yik, zik, rik;// Inputs (global variables)
        @SuppressWarnings("unused")
        double xjk, yjk, zjk, rjk;//     sat. position, range:
        double Ax, Ay, Az, Bx, By, Bz, Dx, Dy, Dz;//        xi, yi, zi, ri
        @SuppressWarnings("unused")
        double Ca, Cb, Cc, Cd, Ce, Cf, Ci, Cj, Cx, Cy, Cz;//  xj, yj, zj, rj
        double x1, y1, z1, x2, y2, z2, e1, e2;//	   xk, yk, zk, rk

        xik = xi - xk;
        yik = yi - yk;
        zik = zi - zk;//  Solve with absolute ranges
        xjk = xj - xk;
        yjk = yj - yk;
        zjk = zj - zk;
        Ci = (xi * xi - xk * xk + yi * yi - yk * yk + zi * zi - zk * zk - ri * ri + rk * rk) / 2;
        Cj = (xj * xj - xk * xk + yj * yj - yk * yk + zj * zj - zk * zk - rj * rj + rk * rk) / 2;
        Dz = xik * yjk - xjk * yik;
        Dy = zik * xjk - zjk * xik;
        Dx = yik * zjk - yjk * zik;

        if ((Math.abs(Dx) > Math.abs(Dy)) && (Math.abs(Dx) > Math.abs(Dz))) {//    Favoring x-axis
            Ay = (zik * xjk - zjk * xik) / Dx;
            By = (zjk * Ci - zik * Cj) / Dx;
            Az = (yjk * xik - yik * xjk) / Dx;
            Bz = (yik * Cj - yjk * Ci) / Dx;
            Ax = Ay * Ay + Az * Az + 1.0;
            Bx = (Ay * (yk - By) + Az * (zk - Bz) + xk) / Ax;
            Cx = Bx * Bx - (By * By + Bz * Bz - 2 * yk * By - 2 * zk * Bz + yk * yk + zk * zk + xk * xk - rk * rk) / Ax;
            if (Cx < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;//   If no solution,
                return 1;
            }//    make it  far, far away.
            x1 = Bx + Math.sqrt(Cx);
            y1 = Ay * x1 + By;
            z1 = Az * x1 + Bz;
            x2 = 2 * Bx - x1;
            y2 = Ay * x2 + By;
            z2 = Az * x2 + Bz;
        } else if (Math.abs(Dy) > Math.abs(Dz)) {//          Favoring y-axis
            Az = (xik * yjk - xjk * yik) / Dy;
            Bz = (xjk * Ci - xik * Cj) / Dy;
            Ax = (zjk * yik - zik * yjk) / Dy;
            Bx = (zik * Cj - zjk * Ci) / Dy;
            Ay = Az * Az + Ax * Ax + 1.0;
            By = (Az * (zk - Bz) + Ax * (xk - Bx) + yk) / Ay;
            Cy = By * By - (Bz * Bz + Bx * Bx - 2 * zk * Bz - 2 * xk * Bx + zk * zk + xk * xk + yk * yk - rk * rk) / Ay;
            if (Cy < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;//   If no solution,
                return 1;
            }//    make it  far, far away.
            y1 = By + Math.sqrt(Cy);
            z1 = Az * y1 + Bz;
            x1 = Ax * y1 + Bx;
            y2 = 2 * By - y1;
            z2 = Az * y2 + Bz;
            x2 = Ax * y2 + Bx;
        } else {//          Favoring z-axis
            if (Dz == 0.0) {
                x0 = y0 = z0 = 9.9999999e99;//   If no solution,
                return 1;
            }//    make it  far, far away.
            Ax = (yik * zjk - yjk * zik) / Dz;
            Bx = (yjk * Ci - yik * Cj) / Dz;
            Ay = (xjk * zik - xik * zjk) / Dz;
            By = (xik * Cj - xjk * Ci) / Dz;
            Az = Ax * Ax + Ay * Ay + 1.0;
            Bz = (Ax * (xk - Bx) + Ay * (yk - By) + zk) / Az;
            Cz = Bz * Bz - (Bx * Bx + By * By - 2 * xk * Bx - 2 * yk * By + xk * xk + yk * yk + zk * zk - rk * rk) / Az;
            if (Cz < 0.0) {
                x0 = y0 = z0 = 9.9999999e99;//   If no solution,
                return 1;
            }//    make it  far, far away.
            z1 = Bz + Math.sqrt(Cz);
            x1 = Ax * z1 + Bx;
            y1 = Ay * z1 + By;
            z2 = 2 * Bz - z1;
            x2 = Ax * z2 + Bx;
            y2 = Ay * z2 + By;
        }

        e1 = (x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1);// Pick solution closest
        e2 = (x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2);//   to x, y, z
        if (e1 <= e2) {//       (also global inputs)
            x0 = x1;
            y0 = y1;
            z0 = z1;
        }//Solution (global variables)
        else {
            x0 = x2;
            y0 = y2;
            z0 = z2;
        }//  GPS Position = x0, y0, z0
        return 0;
    }

// ******************
    private final static Logger log = LoggerFactory.getLogger(Ash1_1Algorithm.class);

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

}
