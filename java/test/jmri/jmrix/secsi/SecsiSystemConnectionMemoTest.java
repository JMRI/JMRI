package jmri.jmrix.secsi;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the SecsiSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SecsiSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SecsiSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SecsiSystemConnectionMemo();
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
