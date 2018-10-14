package jmri.jmrit.symbolicprog;

import java.awt.GraphicsEnvironment;
import javax.swing.JLabel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class FactoryResetActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JLabel jl = new JLabel("test table model");
        ResetTableModel rtm = new ResetTableModel(jl,jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer());
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("factory reset action test");
        FactoryResetAction t = new FactoryResetAction("Test Action",rtm,jf);
        Assert.assertNotNull("exists",t);
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(FactoryResetActionTest.class);

}
