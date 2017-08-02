package jmri.jmrix.lenz.swing.mon;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * XNetMonPaneTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.mon.XNetMonPane class
 *
 * @author	Paul Bender Copyright (C) 2014,2016
 */
public class XNetMonPaneTest extends jmri.jmrix.AbstractMonPaneTestBase {
        
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assert.assertNotNull(pane);
    }

    @Test
    public void testDefault() {
        jmri.util.swing.JmriNamedPaneAction f = new XNetMonPane.Default();
        Assert.assertNotNull(f);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        jmri.InstanceManager.store(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        pane = new XNetMonPane();
    }

    @After
    @Override
    public void tearDown() {
        jmri.InstanceManager.deregister(memo, jmri.jmrix.lenz.XNetSystemConnectionMemo.class);
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
