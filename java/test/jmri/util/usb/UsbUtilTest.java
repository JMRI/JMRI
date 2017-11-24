package jmri.util.usb;

import java.io.UnsupportedEncodingException;
import javax.usb.UsbDevice;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbException;
import jmri.util.JUnitAppender;
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
public class UsbUtilTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void testGetFullProductName() {
        UsbDevice mockDevice = Mockito.mock(UsbDevice.class);
        try {
            // test MFG and product name complete
            Mockito.when(mockDevice.getManufacturerString()).thenReturn("Foo");
            Mockito.when(mockDevice.getProductString()).thenReturn("Bar");
            Assert.assertEquals("Foo Bar", UsbUtil.getFullProductName(mockDevice));
            // test product name contains MFG name
            Mockito.when(mockDevice.getProductString()).thenReturn("FooBar");
            Assert.assertEquals("FooBar", UsbUtil.getFullProductName(mockDevice));
            // test no MFG or product name
            Mockito.when(mockDevice.getManufacturerString()).thenReturn(null);
            Mockito.when(mockDevice.getProductString()).thenReturn(null);
            Assert.assertEquals(null, UsbUtil.getFullProductName(mockDevice));
            // test exceptions thrown by device
            Mockito.when(mockDevice.getManufacturerString()).thenThrow(UsbException.class);
            Assert.assertEquals(null, UsbUtil.getFullProductName(mockDevice));
            JUnitAppender.assertErrorMessage("Unable to read data from " + mockDevice.toString());
            Mockito.when(mockDevice.getManufacturerString()).thenThrow(UnsupportedEncodingException.class);
            Assert.assertEquals(null, UsbUtil.getFullProductName(mockDevice));
            JUnitAppender.assertErrorMessage("Unable to read data from " + mockDevice.toString());
            Mockito.when(mockDevice.getManufacturerString()).thenThrow(UsbDisconnectedException.class);
            Assert.assertEquals(null, UsbUtil.getFullProductName(mockDevice));
            JUnitAppender.assertErrorMessage("Unable to read data from " + mockDevice.toString());
            // test that unexpected exception is not caught
            try {
                Mockito.when(mockDevice.getManufacturerString()).thenThrow(NullPointerException.class);
                Assert.assertEquals("", UsbUtil.getFullProductName(mockDevice));
                Assert.fail("NPE not thrown");
            } catch (NullPointerException ex) {
                // catch exception to not break test
            }
            JUnitAppender.assertErrorMessage("Unable to read data from " + mockDevice.toString());
        } catch (UsbException | UnsupportedEncodingException | UsbDisconnectedException ex) {
            // try/catch to match API, however mock object will not throw these
        }
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
