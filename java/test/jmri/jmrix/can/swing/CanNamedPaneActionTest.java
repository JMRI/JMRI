package jmri.jmrix.can.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CanNamedPaneActionTest {

    // private TrafficController tc = null;
    private CanSystemConnectionMemo m = null;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Can Named Pane Test");
        CanNamedPaneAction t = new CanNamedPaneAction("Test Action",jf,"test",m);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        m = new CanSystemConnectionMemo();
        m.setSystemPrefix("ABC");
    }

    @AfterEach
    public void tearDown() {
        m = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CanNamedPaneActionTest.class);

}
