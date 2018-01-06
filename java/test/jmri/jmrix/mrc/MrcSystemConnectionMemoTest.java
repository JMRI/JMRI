package jmri.jmrix.mrc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of MrcMonPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MrcSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

    jmri.jmrix.mrc.MrcSystemConnectionMemo memo = null;

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        scm = memo = new jmri.jmrix.mrc.MrcSystemConnectionMemo();
        jmri.jmrix.mrc.MrcInterfaceScaffold tc = new jmri.jmrix.mrc.MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, jmri.jmrix.mrc.MrcSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
