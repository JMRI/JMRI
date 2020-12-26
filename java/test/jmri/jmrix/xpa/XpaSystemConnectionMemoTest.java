package jmri.jmrix.xpa;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.xpa.XpaSystemConnectionMemo class.
 *
 * @author Paul Bender
 */
public class XpaSystemConnectionMemoTest extends SystemConnectionMemoTestBase<XpaSystemConnectionMemo> {

    @Test
    public void testGetAndSetXpaTrafficController() {
        // first, check to see that an exception is 
        // thrown when null is passed. 
        boolean exceptionThrown = false;
        try {
            scm.setXpaTrafficController(null);
        } catch (IllegalArgumentException iae) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        scm.setXpaTrafficController(new XpaTrafficController());

        Assert.assertNotNull("TrafficController set correctly", scm.getXpaTrafficController());

    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        XpaTrafficController tc = new XpaTrafficControlScaffold();
        scm = new XpaSystemConnectionMemo();
        scm.setXpaTrafficController(tc);
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
