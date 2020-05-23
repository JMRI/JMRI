package jmri.jmrix.bachrus;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SpeedoSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.bachrus.SpeedoSystemConnectionMemo class
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
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SpeedoSystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
