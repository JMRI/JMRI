package jmri.util.usb;

import javax.usb.UsbDevice;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class UsbBrowserPanelTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void testCTor() {
        UsbDevice mockDevice = Mockito.mock(UsbDevice.class);
        UsbBrowserPanel t = new UsbBrowserPanel(){
           @Override
           protected UsbTreeNode getRootNode() {
              UsbTreeNode retval = new UsbTreeNode(mockDevice);
              retval.setUsbDevice(null);
              return retval;
           }
        };
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbBrowserPanelTest.class);

}
