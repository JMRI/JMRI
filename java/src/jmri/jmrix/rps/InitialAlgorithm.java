package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of 1st algorithm for reducing Readings.
 * <p>
 * This algorithm was provided by Robert Ashenfelter based in part on the work
 * of Ralph Bucher in his paper "Exact Solution for Three Dimensional Hyperbolic
 * Positioning Algorithm and Synthesizable VHDL Model for Hardware
 * Implementation".
 * <p>
 * Neither Ashenfelter nor Bucher provide any guarantee as to the intellectual
 * property status of this algorithm. Use it at your own risk.
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class InitialAlgorithm implements Calculator {

    public InitialAlgorithm(Point3d[] sensors, double vsound) {
        this(sensors[0], sensors[1], sensors[2], sensors[3], vsound);
    }

    public InitialAlgorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, double vsound) {
        this.sensor1 = sensor1;
        this.sensor2 = sensor2;
        this.sensor3 = sensor3;

        this.vsound = vsound;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
        xi = sensor1.x;
        yi = sensor1.y;
        zi = sensor1.z;

        xj = sensor2.x;
        yj = sensor2.y;
        zj = sensor2.z;

        xk = sensor3.x;
        yk = sensor3.y;
        zk = sensor3.z;

        ngps = 3;
    }

    public InitialAlgorithm(Point3d sensor1, Point3d sensor2, Point3d sensor3, Point3d sensor4, double vsound) {
        this(sensor1, sensor2, sensor3, vsound);

        this.sensor4 = sensor4;

        // load the algorithm variables
        //Point3d origin = new Point3d(); // defaults to 0,0,0
        xl = sensor4.x;
        yl = sensor4.y;
        zl = sensor4.z;

        ngps = 4;
    }

    double vsound;

    @Override
    public Measurement convert(Reading r) {
        ngps = r.getNValues();
        ri = r.getValue(1) * vsound;
        rj = r.getValue(2) * vsound;
        rk = r.getValue(3) * vsound;
        if (r.getNValues() > 3) {
            rl = r.getValue(4) * vsound;
        }

        log.debug("inputs " + ri + " " + rj + " " + rk + " " + rl);

        gps();

        log.debug("x = " + x0 + " y = " + y0 + " z0 = " + z0);
        return new Measurement(r, x0, y0, z0, vsound, -99, "Initial");
    }

    /**
     * Seed the conversion using an estimated position
     */
    @Override
    public Measurement convert(Reading r, Point3d guess) {
        this.x = guess.x;
        this.y = guess.y;
        this.z = guess.z;

        return convert(r);
    }

    /**
     * Seed the conversion using a last measurement
     */
    @Override
    public Measurement convert(Reading r, Measurement last) {
        if (last != null) {
            this.x = last.getX();
            this.y = last.getY();
            this.z = last.getZ();
        }

        // if the last measurement failed, set back to origin
        if (this.x > 9.E99) {
            this.x = 0;
        }
        if (this.y > 9.E99) {
            this.y = 0;
        }
        if (this.z > 9.E99) {
            this.z = 0;
        }

        return convert(r);
    }

    // Sensor position objects, up to 4
    Point3d sensor1;
    Point3d sensor2;
    Point3d sensor3;
    Point3d sensor4;

    /**
     * The following is the original algorithm, as provided by Ash as a C
     * routine
     */
    double x, y, z;
    double x0, y0, z0, r0;

    int ngps;

// positions of sensors
    double xi, yi, zi;
    double xj, yj, zj;
    double xk, yk, zk;
    double xl, yl, zl;

// distance to sensors (true input)
    double ri, rj, rk, rl;

// calculation subroutine
    boolean gps() {// GPS Position Solver
        double xie, yie, zie, rie, xje, yje, zje, rje;
        double xke, yke, zke, rke, xle, yle, zle, rle;// Inputs (global variables)
        double xij, yij, zij, rij, xik, yik, zik, rik;
        double xjk, yjk, zjk, rjk, xkl, ykl, zkl, rkl;//   ngps = # of satellites
        double Ax, Ay, Az, Bx, By, Bz, Dx, Dy, Dz;//       3 (abs.) or 4 (rel.)
        double Ca, Cb, Cc, Cd, Ce, Cf, Ci, Cj, Cx, Cy, Cz;
        double r0i, r0j, r0k, r0l, r01, r02;//     sat. position, range:
        double x1, y1, z1, x2, y2, z2, e1, e2;//       xi, yi, zi, ri
        //	  xj, yj, zj, rj
        //	  xk, yk, zk, rk
        //	  xl, yl, zl, rl (rel.)

        // in case of early error return, make it far away
        x0 = y0 = z0 = r0 = 9.9999999e99;

        // avoid "not initialized" errors from compiler
        r01 = -1;
        r02 = -1;

        // calculation
        if (ngps == 3) {//  Solve with absolute ranges
            xik = xi - xk;
            yik = yi - yk;
            zik = zi - zk;
            log.debug("xik=" + xik + " yik=" + yik);
            xjk = xj - xk;
            yjk = yj - yk;
            zjk = zj - zk;
            log.debug("xjk=" + xjk + " yjk=" + yjk);
            Ci = (xi * xi - xk * xk + yi * yi - yk * yk + zi * zi - zk * zk - ri * ri + rk * rk) / 2;
            Cj = (xj * xj - xk * xk + yj * yj - yk * yk + zj * zj - zk * zk - rj * rj + rk * rk) / 2;
            Dz = xik * yjk - xjk * yik;
            Dy = zik * xjk - zjk * xik;
            Dx = yik * zjk - yjk * zik;

            if ((Math.abs(Dx) > Math.abs(Dy)) && (Math.abs(Dx) > Math.abs(Dz))) {
                log.debug("case 1");
                Ay = (zik * xjk - zjk * xik) / Dx;
                By = (zjk * Ci - zik * Cj) / Dx;
                Az = (yjk * xik - yik * xjk) / Dx;
                Bz = (yik * Cj - yjk * Ci) / Dx;
                Ax = Ay * Ay + Az * Az + 1.0;
                Bx = (Ay * (yk - By) + Az * (zk - Bz) + xk) / Ax;
                Cx = Bx * Bx - (By * By + Bz * Bz - 2 * yk * By - 2 * zk * Bz + yk * yk + zk * zk + xk * xk - rk * rk) / Ax;
                if (Cx < 0.0) {
                    log.warn("Cx is " + Cx + ", less than 0, in 3 sensor case");
                    return false;
                }
                x1 = Bx + Math.sqrt(Cx);
                y1 = Ay * x1 + By;
                z1 = Az * x1 + Bz;
                x2 = 2 * Bx - x1;
                y2 = Ay * x2 + By;
                z2 = Az * x2 + Bz;
            } else if (Math.abs(Dy) > Math.abs(Dz)) {
                log.debug("case 2");
                Az = (xik * yjk - xjk * yik) / Dy;
                Bz = (xjk * Ci - xik * Cj) / Dy;
                Ax = (zjk * yik - zik * yjk) / Dy;
                Bx = (zik * Cj - zjk * Ci) / Dy;
                Ay = Az * Az + Ax * Ax + 1.0;
                By = (Az * (zk - Bz) + Ax * (xk - Bx) + yk) / Ay;
                Cy = By * By - (Bz * Bz + Bx * Bx - 2 * zk * Bz - 2 * xk * Bx + zk * zk + xk * xk + yk * yk - rk * rk) / Ay;
                if (Cy < 0.0) {
                    log.warn("Cy is " + Cy + ", less than 0, in 3 sensor case");
                    return false;
                }
                y1 = By + Math.sqrt(Cy);
                z1 = Az * y1 + Bz;
                x1 = Ax * y1 + Bx;
                y2 = 2 * By - y1;
                z2 = Az * y2 + Bz;
                x2 = Ax * y2 + Bx;
            } else {
                log.debug("case 3");
                if (Dz == 0.0) {
                    log.warn("Dz is 0 in 3 sensor case");
                    return false;
                }
                Ax = (yik * zjk - yjk * zik) / Dz;
                Bx = (yjk * Ci - yik * Cj) / Dz;
                Ay = (xjk * zik - xik * zjk) / Dz;
                By = (xik * Cj - xjk * Ci) / Dz;
                Az = Ax * Ax + Ay * Ay + 1.0;
                Bz = (Ax * (xk - Bx) + Ay * (yk - By) + zk) / Az;
                Cz = Bz * Bz - (Bx * Bx + By * By - 2 * xk * Bx - 2 * yk * By + xk * xk + yk * yk + zk * zk - rk * rk) / Az;
                if (Cz < 0.0) {
                    log.warn("Cz is " + Cz + ", less than 0, in 3 sensor case");
                    return false;
                }
                z1 = Bz + Math.sqrt(Cz);
                x1 = Ax * z1 + Bx;
                y1 = Ay * z1 + By;
                z2 = 2 * Bz - z1;
                x2 = Ax * z2 + Bx;
                y2 = Ay * z2 + By;
                log.debug("x1 = " + x1);
                log.debug("x2 = " + x2);
            }
        } else if (ngps == 4) {//  Solve with relative ranges
            xie = xi + 1e-9;
            yie = yi - 7e-9;
            zie = zi - 4e-9;
            rie = ri + 6e-9;
            xje = xj + 5e-9;
            yje = yj - 3e-9;
            zje = zj + 2e-9;
            rje = rj - 8e-9;
            xke = xk - 2e-9;
            yke = yk - 6e-9;
            zke = zk + 7e-9;
            rke = rk - 5e-9;
            xle = xl + 8e-9;
            yle = yl + 4e-9;
            zle = zl - 1e-9;
            rle = rl + 3e-9;
            xij = xie - xje;
            xik = xie - xke;
            xjk = xje - xke;
            xkl = xke - xle;
            yij = yie - yje;
            yik = yie - yke;
            yjk = yje - yke;
            ykl = yke - yle;
            zij = zie - zje;
            zik = zie - zke;
            zjk = zje - zke;
            zkl = zke - zle;
            rij = rie - rje;
            rik = rie - rke;
            rjk = rje - rke;
            rkl = rke - rle;
            Ci = (rik * (rij * rij + xie * xie - xje * xje + yie * yie - yje * yje + zie * zie - zje * zje)
                    - rij * (rik * rik + xie * xie - xke * xke + yie * yie - yke * yke + zie * zie - zke * zke)) / 2;
            Cj = (rkl * (rjk * rjk + xke * xke - xje * xje + yke * yke - yje * yje + zke * zke - zje * zje)
                    + rjk * (rkl * rkl + xke * xke - xle * xle + yke * yke - yle * yle + zke * zke - zle * zle)) / 2;
            Dx = rik * yij - rij * yik;
            Dx = ((Dx != 0.0) ? Dx : 1e-12);
            Dy = rjk * ykl - rkl * yjk;
            Dy = ((Dy != 0.0) ? Dy : 1e-12);
            Ca = (rij * xik - rik * xij) / Dx;
            Cb = (rij * zik - rik * zij) / Dx;
            Cc = Ci / Dx;
            Cd = (rkl * xjk - rjk * xkl) / Dy;
            Ce = (rkl * zjk - rjk * zkl) / Dy;
            Cf = Cj / Dy;
            Dx = Ca - Cd;
            Dx = ((Dx != 0.0) ? Dx : 1e-12);
            Ax = (Ce - Cb) / Dx;
            Bx = (Cf - Cc) / Dx;
            Ay = (Ca * Ax) + Cb;
            By = (Ca * Bx) + Cc;
            Ci = rik * rik + xie * xie - xke * xke + yie * yie - yke * yke
                    + zie * zie - zke * zke - 2 * Bx * xik - 2 * By * yik;
            Cj = 2 * (Ax * xik + Ay * yik + zik);
            Dz = 4 * rik * rik * (Ax * Ax + Ay * Ay + 1) - Cj * Cj;
            Dz = ((Dz != 0.0) ? Dz : 1e-12);
            Bz = (4 * rik * rik * (Ax * (xie - Bx) + Ay * (yie - By) + zie) - Ci * Cj) / Dz;
            Cz = Math.abs(Bz * Bz
                    - (4 * rik * rik * ((xie - Bx) * (xie - Bx) + (yie - By) * (yie - By) + zie * zie) - Ci * Ci) / Dz);
            z1 = Bz + Math.sqrt(Cz);
            x1 = Ax * z1 + Bx;
            y1 = Ay * z1 + By;
            z2 = 2 * Bz - z1;
            x2 = Ax * z2 + Bx;
            y2 = Ay * z2 + By;

            r0i = Math.sqrt((xi - x1) * (xi - x1) + (yi - y1) * (yi - y1) + (zi - z1) * (zi - z1)) - ri;// Check
            r0j = Math.sqrt((xj - x1) * (xj - x1) + (yj - y1) * (yj - y1) + (zj - z1) * (zj - z1)) - rj;//   solu-
            r0k = Math.sqrt((xk - x1) * (xk - x1) + (yk - y1) * (yk - y1) + (zk - z1) * (zk - z1)) - rk;//   tions
            r0l = Math.sqrt((xl - x1) * (xl - x1) + (yl - y1) * (yl - y1) + (zl - z1) * (zl - z1)) - rl;
            r01 = (r0i + r0j + r0k + r0l) / 4;
            e1 = Math.sqrt(((r0i - r01) * (r0i - r01) + (r0j - r01) * (r0j - r01)
                    + (r0k - r01) * (r0k - r01) + (r0l - r01) * (r0l - r01)) / 4);
            log.debug("e1 = " + e1);
            if (e1 > 1e-4) {
                x1 = y1 = z1 = r01 = 9.9999999e99;//	  solution 1 NG
            }
            r0i = Math.sqrt((xi - x2) * (xi - x2) + (yi - y2) * (yi - y2) + (zi - z2) * (zi - z2)) - ri;
            r0j = Math.sqrt((xj - x2) * (xj - x2) + (yj - y2) * (yj - y2) + (zj - z2) * (zj - z2)) - rj;
            r0k = Math.sqrt((xk - x2) * (xk - x2) + (yk - y2) * (yk - y2) + (zk - z2) * (zk - z2)) - rk;
            r0l = Math.sqrt((xl - x2) * (xl - x2) + (yl - y2) * (yl - y2) + (zl - z2) * (zl - z2)) - rl;
            r02 = (r0i + r0j + r0k + r0l) / 4;
            e2 = Math.sqrt(((r0i - r02) * (r0i - r02) + (r0j - r02) * (r0j - r02)
                    + (r0k - r02) * (r0k - r02) + (r0l - r02) * (r0l - r02)) / 4);
            log.debug("e2 = " + e2);
            if (e2 > 1e-4) {
                x2 = y2 = z2 = r02 = 9.9999999e99; //	  solution 2 NG
            }
        } else { //   Invalid value of ngps
            log.warn("ngps no good: " + ngps);
            return false;
        }

        e1 = (x - x1) * (x - x1) + (y - y1) * (y - y1) + (z - z1) * (z - z1);// Pick solution closest
        e2 = (x - x2) * (x - x2) + (y - y2) * (y - y2) + (z - z2) * (z - z2);//   to x, y, z
        if (e1 <= e2) {//       (also global inputs)
            x0 = x1;
            y0 = y1;
            z0 = z1;
            r0 = r01;
        }//Solution (global variables)
        else {//   GPS Position =
            x0 = x2;
            y0 = y2;
            z0 = z2;
            r0 = r02;
        }//    x0, y0, z0,
        return true;//     r0 = range offset (rel)
    }

    private final static Logger log = LoggerFactory.getLogger(InitialAlgorithm.class);

}
