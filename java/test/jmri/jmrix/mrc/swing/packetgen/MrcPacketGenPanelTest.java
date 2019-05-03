package jmri.jmrix.mrc.swing.packetgen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MrcPacketGenPanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new MrcPacketGenPanel();
        helpTarget = "package.jmri.jmrix.mrc.swing.packetgen.MrcPacketGenPanel";
        title = "MRC_: Send MRC command";
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MrcPacketGenPanelTest.class);

}
