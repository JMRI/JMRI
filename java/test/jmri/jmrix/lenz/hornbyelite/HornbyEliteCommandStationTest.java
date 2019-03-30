package jmri.jmrix.lenz.hornbyelite;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * HornbyEliteCommandStationTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.lenz.hornbyelite.HornbyEliteCommandStation class
 *
 * @author	Paul Bender
 */
public class HornbyEliteCommandStationTest {

    @Test
    public void testCtor() {

        HornbyEliteCommandStation c = new HornbyEliteCommandStation();
        Assert.assertNotNull(c);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
