package jmri.jmrix.secsi;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SecsiSystemConnectionMemo();
        scm.setTrafficController(new SerialTrafficControlScaffold(scm));
        scm.configureManagers();
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
