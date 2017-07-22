package jmri.jmrix.lenz.swing.mon;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class XNetMonActionTest {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        XNetMonAction t = new XNetMonAction();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
    }

    @After
    public void tearDown() {
        jmri.InstanceManager.deregister(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(XNetMonActionTest.class.getName());

}
