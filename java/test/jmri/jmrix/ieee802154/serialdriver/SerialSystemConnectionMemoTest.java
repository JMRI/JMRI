package jmri.jmrix.ieee802154.serialdriver;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialSystemConnectionMemo class
 *
 * @author	Paul Bender
 */
public class SerialSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SerialSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
