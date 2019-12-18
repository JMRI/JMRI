package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PacketGenFrameTest extends jmri.util.JmriJFrameTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame = new PacketGenFrame();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PacketGenFrameTest.class);

}
