package jmri.jmrix.loconet.usb_dcs240.swing;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class Dcs240UsbModeSelectPaneTest extends jmri.util.swing.JmriPanelTest {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Dcs240UsbModeSelectPane();
        helpTarget="package.jmri.jmrix.loconet.usb_dcs240.swing.Dcs240UsbModeSelect";
        title=Bundle.getMessage("MenuItemUsbDcs240ModeSelect");
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs240SelectPaneTest.class);

}
