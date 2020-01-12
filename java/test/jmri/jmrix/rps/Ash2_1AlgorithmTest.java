package jmri.jmrix.rps;

import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.Ash2_1Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class Ash2_1AlgorithmTest extends AbstractAlgorithmTestBase {

    @Override
    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_1Algorithm(pts, vs);
    }

}
