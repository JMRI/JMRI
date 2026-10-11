package jmri.jmrix.rps;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the rps.Reading class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class ReadingTest {

    @Test
    public void testCtorAndID() {
        double[] v = new double[]{0., 1., 2.};
        Reading r = new Reading("21", v);
        assertEquals( "21", r.getId(), "ID ok");
    }

    @Test
    public void testValues() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        assertEquals( 1, (int) val[1], "Value 1 array");
        assertEquals( 1, (int) r1.getValue(1), "Value 1 call ");
        assertEquals( 2, (int) val[2], "Value 2 array");
        assertEquals( 2, (int) r1.getValue(2), "Value 2 call ");
    }

    @Test
    public void testImmutable() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        double[] val = r1.getValues();
        val[1] = 3.;
        assertEquals( 1, (int) r1.getValue(1), "Value 1 call ");
        assertEquals( 2, (int) r1.getValue(2), "Value 2 call ");
    }

    @Test
    public void testCopyCtorID() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        assertEquals( "21", r2.getId(), "ID ok");
    }

    @Test
    public void testCopyCtorData() {
        Reading r1 = new Reading("21", new double[]{0., 1., 2.});
        Reading r2 = new Reading(r1);
        assertEquals( 1, (int) r2.getValue(1), "value 1");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
