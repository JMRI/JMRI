package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Z21XNetInitializationManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.roco.z21.z21XNetInitializationManager
 * class
 *
 * @author Paul Bender Copyright (C) 2015
 *
 */
public class Z21XNetInitializationManagerTest {

    @Test
    public void testCtor() {

// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo = new XNetSystemConnectionMemo(t);

        Z21XNetInitializationManager m = new Z21XNetInitializationManager(memo) {
            @Override
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager(); // drop ShutDownTask for jmri.jmrix.lenz.XNetInterfaceScaffold
        JUnitUtil.tearDown();
    }

}
