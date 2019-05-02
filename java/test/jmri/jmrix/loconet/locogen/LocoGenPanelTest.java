package jmri.jmrix.loconet.locogen;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LocoGenPanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new LocoGenPanel();
        helpTarget="package.jmri.jmrix.loconet.locogen.LocoGenFrame";
        title=Bundle.getMessage("MenuItemSendPacket");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocoGenPanelTest.class);

}
