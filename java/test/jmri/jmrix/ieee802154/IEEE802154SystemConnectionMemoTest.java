package jmri.jmrix.ieee802154;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * IEEE802154SystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo class
 *
 * @author Paul Bender
 */
public class IEEE802154SystemConnectionMemoTest extends SystemConnectionMemoTestBase<IEEE802154SystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new IEEE802154SystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
