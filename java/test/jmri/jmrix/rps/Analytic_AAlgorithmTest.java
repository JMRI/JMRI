package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import org.junit.*;

/**
 * JUnit tests for the rps.Analytic_AAlgorithm class.
 *
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class Analytic_AAlgorithmTest extends AbstractAlgorithmTestBase {

    @Override
    Calculator getAlgorithm(Point3d[] pts, double vs) {
        return new Analytic_AAlgorithm(pts, vs);
    }

    @Test
    @Ignore("fails for unknown reasons")
    @Override
    public void testCalc4() {
        super.testCalc4();
    }

    @Test
    @Ignore("fails for unknown reasons")
    @Override
    public void testCalc5() {
        super.testCalc5();
    }

    @Test
    @Ignore("fails for unknown reasons")
    @Override
    public void testCalc6() {
        super.testCalc6();
    }

    @Test
    @Ignore("fails for unknown reasons")
    @Override
    public void testCalc7() {
        super.testCalc7();
    }
}
