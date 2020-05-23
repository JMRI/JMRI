package jmri.jmrix.openlcb;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.can.TestTrafficController;

/**
 * OlcbSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.openlcb.OlcbSystemConnectionMemo class
 *
 * @author Bob Jacobsen
 * @author Paul Bender Copyright (C) 2016
 */
public class OlcbSystemConnectionMemoTest extends SystemConnectionMemoTestBase<OlcbSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        scm.configureManagers();
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        scm.setTrafficController(tc);
    }

    @Override
    @After
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        scm = null;
        JUnitUtil.tearDown();

    }
}
