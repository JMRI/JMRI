package jmri.jmrix.acela;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the AcelaSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AcelaSystemConnectionMemoTest extends SystemConnectionMemoTestBase<AcelaSystemConnectionMemo> {

    @Test
    public void testDefaultCtor() {
        Assert.assertNotNull("exists", new AcelaSystemConnectionMemo());
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
        AcelaTrafficController tc = new AcelaTrafficControlScaffold();
        scm = new AcelaSystemConnectionMemo(tc);
    }

    @Override
    @AfterEach
    public void tearDown() {
        scm.getTrafficController().terminateThreads();
        scm.dispose();
        JUnitUtil.tearDown();
    }

}
