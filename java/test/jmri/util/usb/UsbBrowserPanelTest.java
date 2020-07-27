package jmri.util.usb;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import javax.usb.*;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbBrowserPanelTest {

    private UsbHubScaffold hub;

    @Test
    public void testCTor() {

        UsbBrowserPanel t = new UsbBrowserPanel(){
           @Override
           protected UsbTreeNode getRootNode() {
              UsbTreeNode retval = new UsbTreeNode(hub);
              retval.setUsbDevice(null);
              return retval;
           }
        };
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        hub = new UsbHubScaffold();
    }

    @AfterEach
    public void tearDown() {
        hub = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(UsbBrowserPanelTest.class);

    class UsbHubScaffold extends UsbDeviceScaffold implements UsbHub {

        public UsbHubScaffold() {
            super("jmri","testhub");
        }

        @Override
        public boolean isUsbHub() {
            return true;
        }

        @Override
        public byte getNumberOfPorts() {
            return 0;
        }

        @Override
        public List getUsbPorts() {
            return null;
        }

        @Override
        public UsbPort getUsbPort(byte b) {
            return null;
        }

        @Override
        public List getAttachedUsbDevices() {
            return new ArrayList<UsbDevice>();
        }

        @Override
        public boolean isRootUsbHub() {
            return true;
        }
    }

}
