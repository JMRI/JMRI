package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LnTcpServerFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerFrameTest {

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testGetDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getDefault();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getInstance();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = new LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
    }

    @After
    public void tearDown() {
        lnis = null;
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
