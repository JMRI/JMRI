package jmri.jmrix.dcc4pc.swing;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import java.awt.GraphicsEnvironment;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcc4PcNamedPaneActionTest {

    private Dcc4PcSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Dcc4Pc Named Pane Test");
        Dcc4PcNamedPaneAction t = new Dcc4PcNamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Dcc4PcSystemConnectionMemo();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcNamedPaneActionTest.class.getName());

}
