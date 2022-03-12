package jmri.jmrix.dcc4pc.swing;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new Dcc4PcSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Dcc4PcNamedPaneActionTest.class);

}
