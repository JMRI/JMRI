package jmri.jmrix.ieee802154.serialdriver;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialSystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class SerialSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SerialSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SerialSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm = null;
        JUnitUtil.tearDown();
    }

}
