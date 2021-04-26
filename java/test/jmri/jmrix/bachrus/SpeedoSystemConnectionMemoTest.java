package jmri.jmrix.bachrus;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.bachrus.SpeedoSystemConnectionMemo class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SpeedoSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SpeedoSystemConnectionMemo> {

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SpeedoSystemConnectionMemo();
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
