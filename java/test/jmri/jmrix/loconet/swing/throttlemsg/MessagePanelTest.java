package jmri.jmrix.loconet.swing.throttlemsg;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MessagePanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new MessagePanel();
        helpTarget = "package.jmri.jmrix.loconet.swing.throttlemsg.MessageFrame";
        title = Bundle.getMessage("MenuItemThrottleMessages");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MessagePanelTest.class);

}
