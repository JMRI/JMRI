package jmri.jmrix.rps;

import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.Ash2_2Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class Ash2_2AlgorithmTest extends AbstractAlgorithmTestBase {

    @Override
    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Ash2_2Algorithm(pts, vs);
    }

}
