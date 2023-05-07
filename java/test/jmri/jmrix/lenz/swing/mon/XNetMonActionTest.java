package jmri.jmrix.lenz.swing.mon;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class XNetMonActionTest {

    private XNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        XNetMonAction t = new XNetMonAction();
        Assert.assertNotNull("exists", t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, XNetSystemConnectionMemo.class);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose(); // deregisters from instance manager
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XNetMonActionTest.class);
}
