package jmri.util.usb;

import javax.usb.UsbDevice;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
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
public class UsbBrowserFrameTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        UsbDevice mockDevice = Mockito.mock(UsbDevice.class);
        UsbBrowserPanel bp = new UsbBrowserPanel(){
           @Override
           protected UsbTreeNode getRootNode() {
              UsbTreeNode retval = new UsbTreeNode(mockDevice);
              retval.setUsbDevice(null);
              return retval;
           }
        };
        UsbBrowserFrame t = new UsbBrowserFrame(bp);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
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

    // private final static Logger log = LoggerFactory.getLogger(UsbBrowserFrameTest.class);

}
