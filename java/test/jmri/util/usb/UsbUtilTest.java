package jmri.util.usb;

import java.io.UnsupportedEncodingException;

import javax.usb.*;

import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class UsbUtilTest {

    @Test
    public void testGetFullProductName() {
        // test MFG and product name complete
        assertEquals("Foo Bar", UsbUtil.getFullProductName(new UsbDeviceScaffold("Foo","Bar")));
        // test no MFG or product name
        assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold(null,null)));
        // test exceptions thrown by device
        assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
               throw new UsbException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UnsupportedEncodingException("foo");
            }
        }));
        assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UsbDisconnectedException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        assertEquals(null, UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
            @Override
            public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                throw new UsbDisconnectedException("foo");
            }
        }));
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
        // test that unexpected exception is not caught
        Exception ex = assertThrows(NullPointerException.class, () -> {
            assertNotNull( UsbUtil.getFullProductName(new UsbDeviceScaffold("foo","bar"){
                @Override
                public String getManufacturerString() throws UsbException, UnsupportedEncodingException, UsbDisconnectedException {
                    throw new NullPointerException("foo");
                }
            }));
        });
        assertNotNull(ex);
        JUnitAppender.assertErrorMessageStartsWith("Unable to read data from ");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
