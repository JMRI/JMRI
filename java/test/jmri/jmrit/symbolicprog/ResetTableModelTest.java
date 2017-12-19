package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ResetTableModelTest {

    @Test
    public void testCTor() {
        JLabel jl = new JLabel("test table model");
        ResetTableModel t = new ResetTableModel(jl,jmri.InstanceManager.getDefault(jmri.GlobalProgrammerManager.class).getGlobalProgrammer());
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ResetTableModelTest.class);

}
