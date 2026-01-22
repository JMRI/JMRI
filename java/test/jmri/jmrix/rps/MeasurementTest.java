package jmri.jmrix.rps;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class MeasurementTest {

    @Test
    public void testCtorAndID() {
        Reading r = new Reading("21", new double[]{0., 0., 0.});
        Measurement m = new Measurement(r);
        Assertions.assertEquals( "21", m.getId(), "ID ok");
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
