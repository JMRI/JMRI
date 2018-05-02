package jmri.jmrix.nce.usbinterface;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Test simple functioning of UsbInterfacePanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class UsbInterfacePanelTest extends jmri.util.swing.JmriPanelTest {

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        panel = new UsbInterfacePanel();
        helpTarget="package.jmri.jmrix.nce.usbinterface.UsbInterfacePanel";
        title="NCE_: USB Interface Configuration";
    }

    @Override
    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
