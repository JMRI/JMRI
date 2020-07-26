package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * HornbyEliteCommandStationTest.java
 *
 * Test for the
 * jmri.jmrix.lenz.hornbyelite.HornbyEliteCommandStation class
 *
 * @author Paul Bender
 */
public class HornbyEliteCommandStationTest {

    @Test
    public void testCtor() {

        HornbyEliteCommandStation c = new HornbyEliteCommandStation();
        Assert.assertNotNull(c);
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
