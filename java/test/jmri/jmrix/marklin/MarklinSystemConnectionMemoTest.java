package jmri.jmrix.marklin;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * MarklinSystemConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.marklin.MarklinSystemConnectionMemo class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class MarklinSystemConnectionMemoTest extends SystemConnectionMemoTestBase<MarklinSystemConnectionMemo> {

    @Test
    public void testCtorWithoutParameter() {
        MarklinSystemConnectionMemo c = new MarklinSystemConnectionMemo();
        Assert.assertNotNull(c);
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        MarklinTrafficController tc = new MarklinTrafficController() {
            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm = new MarklinSystemConnectionMemo(tc);
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
