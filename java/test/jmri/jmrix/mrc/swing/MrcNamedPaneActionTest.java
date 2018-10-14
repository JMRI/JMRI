package jmri.jmrix.mrc.swing;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.mrc.MrcInterfaceScaffold;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MrcNamedPaneActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MrcSystemConnectionMemo memo = new MrcSystemConnectionMemo();
        MrcInterfaceScaffold tc = new MrcInterfaceScaffold();
        memo.setMrcTrafficController(tc);
        jmri.InstanceManager.store(memo, MrcSystemConnectionMemo.class);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("MRC Named Pane Action Test");
        MrcNamedPaneAction t = new MrcNamedPaneAction("Test Action",jf,"test",memo);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MrcNamedPaneActionTest.class);

}
