package jmri.jmrix.nce.usbinterface;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of UsbInterfacePanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class UsbInterfacePanelTest {

    @Test
    public void testCtor() {
        UsbInterfacePanel action = new UsbInterfacePanel();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testGetHelpTarget() {
        UsbInterfacePanel t = new UsbInterfacePanel();
        Assert.assertEquals("help target","package.jmri.jmrix.nce.usbinterface.UsbInterfacePanel",t.getHelpTarget());
    }

    @Test
    public void testGetTitle() {
        UsbInterfacePanel t = new UsbInterfacePanel();
        Assert.assertEquals("title","NCE_: USB Interface Configuration", t.getTitle());
    }

    @Test
    public void testInitComponents() throws Exception {
        UsbInterfacePanel t = new UsbInterfacePanel();
        // we are just making sure that initComponents doesn't cause an exception.
        t.initComponents();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
