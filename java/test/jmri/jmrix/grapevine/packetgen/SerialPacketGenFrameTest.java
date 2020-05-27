package jmri.jmrix.grapevine.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialPacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SerialPacketGenFrame(memo);
        }
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialPacketGenFrameTest.class);

}
