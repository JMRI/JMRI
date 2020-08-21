package jmri.jmrix.loconet.usb_dcs52.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcs52UsbModeSelectPaneTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Dcs52UsbModeSelectPane();
        helpTarget="package.jmri.jmrix.loconet.usb_dcs52.swing.Dcs52UsbModeSelect";
        title=Bundle.getMessage("MenuItemUsbDcs52ModeSelect");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs52SelectPaneTest.class);

}
