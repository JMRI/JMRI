package jmri.jmrix.nce.usbinterface;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of UsbInterfacePanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class UsbInterfacePanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        panel = new UsbInterfacePanel();
        helpTarget="package.jmri.jmrix.nce.usbinterface.UsbInterfacePanel";
        title="NCE_: USB Interface Configuration";
    }

    @Override
    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
