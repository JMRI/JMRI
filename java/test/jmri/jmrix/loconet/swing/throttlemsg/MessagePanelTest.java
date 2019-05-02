package jmri.jmrix.loconet.swing.throttlemsg;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MessagePanelTest extends jmri.util.swing.JmriPanelTest {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new MessagePanel();
        helpTarget = "package.jmri.jmrix.loconet.swing.throttlemsg.MessageFrame";
        title = Bundle.getMessage("MenuItemThrottleMessages");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MessagePanelTest.class);

}
