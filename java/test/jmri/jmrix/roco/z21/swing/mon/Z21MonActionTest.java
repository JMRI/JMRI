package jmri.jmrix.roco.z21.swing.mon;

import jmri.util.JUnitUtil;
import jmri.jmrix.roco.z21.RocoZ21CommandStation;
import jmri.jmrix.roco.z21.Z21InterfaceScaffold;
import jmri.jmrix.roco.z21.Z21SystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Z21MonActionTest {

    private Z21SystemConnectionMemo memo = null;
    private Z21InterfaceScaffold tc = null; 

    @Test
    public void testCTor() {
        Z21MonAction t = new Z21MonAction();
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Z21SystemConnectionMemo();
        tc = new Z21InterfaceScaffold();
        memo.setTrafficController(tc);
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        jmri.InstanceManager.store(memo, jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        jmri.InstanceManager.deregister(memo, jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class);
        memo=null;
        tc.terminateThreads();
        tc=null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Z21MonActionTest.class);
}
