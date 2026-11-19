package jmri.jmrix.loconet.uhlenbrock.usb_63860;

import jmri.jmrix.loconet.uhlenbrock.UhlenbrockConnectionTypeList;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Uhlenbrock 63860 USB adapter.
 */
public class Usb63860AdapterTest {

    @Test
    public void testCTor() {
        Usb63860Adapter t = new Usb63860Adapter();
        Assertions.assertNotNull(t, "exists");
        Assertions.assertEquals(UhlenbrockConnectionTypeList.UHLEN, t.getManufacturer());
    }

    @Test
    public void testBaudRates() {
        Usb63860Adapter t = new Usb63860Adapter();
        Assertions.assertArrayEquals(new int[]{19200, 38400, 57600, 115200}, t.validBaudNumbers());
        Assertions.assertEquals(3, t.defaultBaudIndex(), "115200 is the factory default");
    }

    @Test
    public void testDefaults() {
        Usb63860Adapter t = new Usb63860Adapter();
        Assertions.assertFalse(java.util.Arrays.asList(t.getOptions()).contains("FlowControl"),
                "no flow control option offered");
        Assertions.assertEquals(Bundle.getMessage("PacketizerTypelnPacketizerStrict"), t.getOptionState(t.getOption4Name()),
                "strict packetizer by default");
    }

    @Test
    public void testTitles() {
        Assertions.assertEquals("LocoNet-USB and WLAN Interface 63860 (USB)", new ConnectionConfig().name());
        Assertions.assertEquals("LocoNet-USB und WLAN Interface 63860 (USB)",
                Bundle.getMessage(java.util.Locale.GERMANY, "USB_63860Title"));
    }

    @Test
    public void testOkToSendWithoutFlowControl() {
        Usb63860Adapter t = new Usb63860Adapter();
        // no serial port open; without flow control CTS must not be consulted
        Assertions.assertTrue(t.okToSend());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
