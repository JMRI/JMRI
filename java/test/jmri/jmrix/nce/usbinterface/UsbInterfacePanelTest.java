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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
