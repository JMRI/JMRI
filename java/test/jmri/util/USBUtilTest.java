package jmri.util;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
import org.mockito.Mockito;

@MockPolicy(Slf4jMockPolicy.class)

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
@RunWith(PowerMockRunner.class)
public class USBUtilTest {

    @Test
    public void testSerialNumberNull() throws UsbException,UnsupportedEncodingException {
       UsbDevice usbd = PowerMockito.mock(UsbDevice.class);
       Mockito.when(usbd.getSerialNumberString()).thenReturn(null);

       Assert.assertEquals("Null Serial Number returned","",USBUtil.getSerialNumber(usbd));
    }

    @Test
    public void testSerialNumber() throws UsbException,UnsupportedEncodingException {
       UsbDevice usbd = PowerMockito.mock(UsbDevice.class);
       Mockito.when(usbd.getSerialNumberString()).thenReturn("12345678");

       Assert.assertEquals("Null Serial Number returned","12345678",USBUtil.getSerialNumber(usbd));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.resetInstanceManager();  // logging is mocked.
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
    }

}
