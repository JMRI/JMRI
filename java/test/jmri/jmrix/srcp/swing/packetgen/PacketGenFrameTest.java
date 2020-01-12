package jmri.jmrix.srcp.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * @author Paul Bender Copyright(C) 2016
 */
public class PacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    private jmri.jmrix.srcp.SRCPSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.srcp.SRCPSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.srcp.SRCPSystemConnectionMemo.class,memo);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new PacketGenFrame(memo);
        }

    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
