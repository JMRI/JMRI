package jmri.jmrit.symbolicprog;

import java.awt.GraphicsEnvironment;

import javax.swing.JLabel;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(FactoryResetActionTest.class);

}
