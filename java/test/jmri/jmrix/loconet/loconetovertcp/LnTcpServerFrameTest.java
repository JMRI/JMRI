package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

/**
 * Test simple functioning of LnTcpServerFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerFrameTest extends jmri.util.JmriJFrameTestBase {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getDefault();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = Mockito.mock(LocoNetSystemConnectionMemo.class);
        InstanceManager.store(memo,LocoNetSystemConnectionMemo.class);
        if(!GraphicsEnvironment.isHeadless()){
          frame = LnTcpServerFrame.getDefault();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
