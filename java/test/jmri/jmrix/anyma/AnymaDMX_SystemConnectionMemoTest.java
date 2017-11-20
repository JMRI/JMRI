package jmri.jmrix.anyma;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for AnymaDMX_SystemConnectionMemo class.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_SystemConnectionMemoTest extends SystemConnectionMemoTestBase {

    @Test
    public void testDefaultCtor() {
        Assert.assertNotNull("exists", scm);
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
        //AnymaDMX_TrafficController tc = new AnymaDMX_TrafficController();
        scm = new AnymaDMX_SystemConnectionMemo();
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
