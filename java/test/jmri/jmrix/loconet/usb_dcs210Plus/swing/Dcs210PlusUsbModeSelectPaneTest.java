package jmri.jmrix.loconet.usb_dcs210Plus.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcs210PlusUsbModeSelectPaneTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Dcs210PlusUsbModeSelectPane();
        helpTarget="package.jmri.jmrix.loconet.usb_dcs210Plus.swing.Dcs210PlusUsbModeSelect";
        title=Bundle.getMessage("MenuItemUsbDcs210PlusModeSelect");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs210PlusSelectPaneTest.class);

}
