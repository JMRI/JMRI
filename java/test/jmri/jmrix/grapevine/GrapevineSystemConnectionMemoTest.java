package jmri.jmrix.grapevine;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the GrapevineSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class GrapevineSystemConnectionMemoTest extends SystemConnectionMemoTestBase<GrapevineSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new GrapevineSystemConnectionMemo();
        scm.setTrafficController(new SerialTrafficControlScaffold(scm));
        scm.configureManagers();
    }

    @Override
    @After
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
