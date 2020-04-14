package jmri.jmrix.lenz.swing.mon;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetMonActionTest {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        XNetMonAction t = new XNetMonAction();
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        jmri.InstanceManager.deregister(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XNetMonActionTest.class);
}
