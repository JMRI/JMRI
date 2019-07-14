package jmri.jmrix.rps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of RPS location-finding using GPS equations from Sam Storm van
 * Leeuwen {@literal <samsvl@nlr.nl>}, ported to Java by Norris Weimer
 * {@literal <norris.weimer@ualberta.ca>}, and ported to JMRI/RPS by Bob
 * Jacobsen.
 *
 * The original Pascal code and documentation is on these web pages
 * <a href="http://callisto.worldonline.nl/~samsvl/stdalone.pas">http://callisto.worldonline.nl/~samsvl/stdalone.pas</a>
 * <a href="http://callisto.worldonline.nl/~samsvl/satpos.htm">http://callisto.worldonline.nl/~samsvl/satpos.htm</a>
 * <a href="http://callisto.worldonline.nl/~samsvl/stdalone.htm">http://callisto.worldonline.nl/~samsvl/stdalone.htm</a>
 * There is also a link there to a C port of Sam's programs.
 *
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class Analytic_AAlgorithm extends AbstractCalculator {

    public Analytic_AAlgorithm(Point3d[] sensors, double vsound, int offset) {
        this(sensors, vsound);
        this.offset = offset;
    }

    public Analytic_AAlgorithm(Point3d[] sensors, double vsound) {
        this.sensors = Arrays.copyOf(sensors, sensors.length);
        this.Vs = vsound;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
    }

    public Analytic_AAlgorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this(null, vsound);
        sensors = new Point3d[3];
        sensors[0] = sensor1;
        sensors[1] = sensor2;
        sensors[2] = sensor3;
    }

    public Analytic_AAlgorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
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
        }

        log.debug("There should have been an earth-shattering kaboom!");
        // RetVal result = RPSpos(nr, Tr, Xr, Yr, Zr, Vs, Xt, Yt, Zt);

        double[][] Xs = new double[33][3];

        boolean[] SV = new boolean[33];
        for (int i = 0; i < 33; i++) {
            SV[i] = false;
        }

        double[] P = new double[33];
        double[] Xr = new double[3];
        Xr[0] = Xr[1] = Xr[2] = 0;

        nr = r.getNValues();
        if (nr != sensors.length - 1) {
            log.error("Mismatch: " + nr + " readings, " + (sensors.length - 1) + " receivers");
        }
        nr = Math.min(nr, sensors.length - 1); // accept the shortest

        // generate vectors
        int j = 0;
        for (int i = 0; i <= nr; i++) {
            if (sensors[i] != null) {
                P[j] = r.getValue(i) * Vs;
                Xs[j][0] = sensors[i].x;
                Xs[j][1] = sensors[i].y;
                Xs[j][2] = sensors[i].z;
                SV[j] = true;
                if (log.isDebugEnabled()) {
                    log.debug("  " + j + "th point at " + Xs[j][0] + "," + Xs[j][1] + "," + Xs[j][2] + " time=" + r.getValue(i) + " is distance " + P[j]);
                }
                j++;
            }
        }
        nr = j;
        log.debug("nr is " + nr);

        double[] result = solve(Xs, SV, P, Xr);

        if (result == null) {
            log.error("failed to converge");
            return new Measurement(r, -99999., -99999., -99999., Vs, -20, "Analytic_A");

        }

        Xt = result[0];
        Yt = result[1];
        Zt = result[2];

        // Vs = result.vs;
        log.debug("Result x = " + Xt + " y = " + Yt + " z0 = " + Zt + " time offset=" + result[3]);
        return new Measurement(r, Xt, Yt, Zt, Vs, nr, "Analytic_A");
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

    int offset = 0;

    /**
     * *************************************************************************
     *
     * @param	Xs	array with 3 columns and 32 rows, for the coordinates of the
     *           sat's
     * @param	SV	valid prn's
     * @param	P	 pseudoranges
     *
     * (note: arrays actually have 33 rows, but row 0 is unused, in order to
     * index by actual prn number)
     *
     * @param	Xr	input of initial guess ( user position in ECEF)
     * @return	[X, X, X, Cr]	output of final position and receiver clock error
     *         return null if calculation failed //do: throw exception instead
     */
    public double[] solve(double[][] Xs, boolean[] SV, double[] P, double[] Xr) {

        double[] R = new double[33];
        double[] L = new double[33];
        double[][] A = new double[33][4];
        double[] AL = new double[4];
        double[][] AA = new double[4][4];
        double[][] AAi = new double[4][4];
        double det;
        double[] D = new double[5];

        int it = 0; //iteration counter
        do {
            it++;

            for (int prn = 1; prn <= 32; prn++) {
                if (SV[prn]) {
                    //range from receiver to satellite
                    R[prn] = Math.sqrt((Xr[0] - Xs[prn][0]) * (Xr[0] - Xs[prn][0])
                            + (Xr[1] - Xs[prn][1]) * (Xr[1] - Xs[prn][1])
                            + (Xr[2] - Xs[prn][2]) * (Xr[2] - Xs[prn][2]));
                    //range residual value
                    L[prn] = P[prn] - R[prn];
                    //A is the geometry matrix or model matrix
                    for (int k = 0; k < 3; k++) {
                        A[prn][k] = (Xr[k] - Xs[prn][k]) / R[prn];
                    }
                    A[prn][3] = -1.0;
                }
            }

            //calculate A.L
            for (int k = 0; k <= 3; k++) {
                AL[k] = 0.0;
                for (int prn = 1; prn <= 32; prn++) {
                    if (SV[prn]) {
                        AL[k] += A[prn][k] * L[prn];
                    }
                }
            }

            //calculate A.A
            for (int k = 0; k <= 3; k++) {
                for (int i = 0; i <= 3; i++) {
                    AA[k][i] = 0.0;
                    for (int prn = 1; prn <= 32; prn++) {
                        if (SV[prn]) {
                            AA[k][i] += A[prn][k] * A[prn][i];
                        }
                    }
                }
            }

            //invert A.A
            //do: use a better procedure to solve these equations
            det = AA[0][0] * sub(AA, 0, 0) - AA[1][0] * sub(AA, 1, 0)
                    + AA[2][0] * sub(AA, 2, 0) - AA[3][0] * sub(AA, 3, 0);
            if (det == 0.0) {
                return null;
            }

            int j;
            int n;
            for (int k = 0; k <= 3; k++) {
                for (int i = 0; i <= 3; i++) {
                    n = k + i;
                    if (n % 2 != 0) //was:    odd(n) 
                    {
                        j = -1;
                    } else {
                        j = 1;
                    }
                    AAi[k][i] = j * sub(AA, i, k) / det;
                }
            }

            //calculate (invA.A).(A.L)
            for (int k = 0; k <= 3; k++) {
                D[k] = 0.0;
                for (int i = 0; i <= 3; i++) {
                    D[k] += AAi[k][i] * AL[i];
                }
            }

            //update position
            for (int k = 0; k < 3; k++) {
                Xr[k] += D[k];
            }

            // display how close
            if (log.isDebugEnabled()) {
                log.debug("  after " + it + ", delta is " + ((Math.abs(D[0]) + Math.abs(D[1]) + Math.abs(D[2]))));
            }
        } while ((it < 6) //there is something wrong if more than 6 iterations are required
                && ((Math.abs(D[0]) + Math.abs(D[1]) + Math.abs(D[2])) >= 1.0E-2));  //iteration criterion

        double Cr = D[3]; //receiver clock error

        if (it >= 6) {
            log.error("Can't solve, iteration limit exceeded.  it = " + it);
            return null;
        }

        return new double[]{Xr[0], Xr[1], Xr[2], Cr};
    }

    /**
     * *************************************************************************
     * finds the determinant of a minor of a 4 x 4 matrix
     *
     * @param A	input 4 x 4 array
     * @param r	the row to be deleted
     * @param c	the column to be deleted
     * @return subdet determinant of the resulting 3 x 3 matrix
     */
    public double sub(double[][] A, int r, int c) {

        double[][] B = new double[3][3];
        int i1, j1;

        //note: I changed how this part of the function was coded    - NW
        for (int i = 0; i < 3; i++) {
            i1 = i;
            if (i >= r) {
                i1++;
            }
            for (int j = 0; j < 3; j++) {
                j1 = j;
                if (j >= c) {
                    j1++;
                }
                B[i][j] = A[i1][j1];
            }
        }

        double subdet = B[0][0] * (B[1][1] * B[2][2] - B[1][2] * B[2][1])
                - B[1][0] * (B[0][1] * B[2][2] - B[2][1] * B[0][2])
                + B[2][0] * (B[0][1] * B[1][2] - B[0][2] * B[1][1]);

        return subdet;
    }

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
        @SuppressFBWarnings(value = "URF_UNREAD_FIELD")
        int code;
        @SuppressFBWarnings(value = "URF_UNREAD_FIELD")
        double x, y, z, vs;
    }

    private final static Logger log = LoggerFactory.getLogger(Analytic_AAlgorithm.class);

}
