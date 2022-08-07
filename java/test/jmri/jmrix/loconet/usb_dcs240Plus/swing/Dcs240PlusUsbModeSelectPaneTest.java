package jmri.jmrix.loconet.usb_dcs240Plus.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcs240PlusUsbModeSelectPaneTest extends jmri.util.swing.JmriPanelTest {


    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new Dcs240PlusUsbModeSelectPane();
        helpTarget="package.jmri.jmrix.loconet.usb_dcs240Plus.swing.Dcs240PlusUsbModeSelect";
        title=Bundle.getMessage("MenuItemUsbDcs240PlusModeSelect");
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbDcs240PlusSelectPaneTest.class);

}
