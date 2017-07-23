package jmri.jmrix.can.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.can.TrafficController;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.CanSystemConnectionMemo;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CanNamedPaneActionTest {

    private TrafficController tc = null;
    private CanSystemConnectionMemo m = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Can Named Pane Test");
        CanNamedPaneAction t = new CanNamedPaneAction("Test Action",jf,"test",m);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        tc = new TestTrafficController();
        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");
    }

    @After
    public void tearDown() {
        tc = null;
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(CanNamedPaneActionTest.class.getName());

}
