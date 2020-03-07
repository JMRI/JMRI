package jmri.util.usb;

import java.io.UnsupportedEncodingException;
import javax.usb.*;

import jmri.util.JUnitAppender;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbUtilTest {

    @Test
    public void testGetFullProductName() {
        // test MFG and product name complete
        Assert.assertEquals("Foo Bar", UsbUtil.getFullProductName(new UsbDeviceScaffold("Foo","Bar")));
        // test no MFG or product name
        Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold(null,null)));
        // test exceptions thrown by device
        Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
               throw new UsbException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UnsupportedEncodingException("foo");
            }
        }));
        Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UsbDisconnectedException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UsbDisconnectedException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        // test that unexpected exception is not caught
        try {
            Assert.assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
                @Override
                public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                    throw new NullPointerException("foo");
                }
            }));
            Assert.fail("NPE not thrown");
        } catch (NullPointerException ex) {
            // catch exception to not break test
        }
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
