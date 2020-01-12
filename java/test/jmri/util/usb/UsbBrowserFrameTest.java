package jmri.util.usb;

import javax.usb.UsbDevice;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbBrowserFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           UsbDevice mockDevice = new UsbDeviceScaffold("foo","bar");
           UsbBrowserPanel bp = new UsbBrowserPanel(){
              @Override
              protected UsbTreeNode getRootNode() {
                 UsbTreeNode retval = new UsbTreeNode(mockDevice);
                 retval.setUsbDevice(null);
                 return retval;
              }
           };
           frame = new UsbBrowserFrame(bp);
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbBrowserFrameTest.class);

}
