package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MrcMonPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MrcSystemConnectionMemoTest {

    jmri.jmrix.mrc.MrcSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", memo);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new jmri.jmrix.mrc.MrcSystemConnectionMemo();
        jmri.jmrix.mrc.MrcInterfaceScaffold tc = new jmri.jmrix.mrc.MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, jmri.jmrix.mrc.MrcSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
