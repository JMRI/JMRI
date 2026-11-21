package jmri.jmrix.loconet.uhlenbrock.wlan_63860;

import jmri.jmrix.loconet.uhlenbrock.UhlenbrockConnectionTypeList;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the Uhlenbrock 63860 WLAN adapter.
 */
public class Wlan63860AdapterTest {

    @Test
    public void testCTor() {
        Wlan63860Adapter t = new Wlan63860Adapter();
        Assertions.assertNotNull(t, "exists");
        Assertions.assertEquals(UhlenbrockConnectionTypeList.UHLEN, t.getManufacturer());
    }

    @Test
    public void testFactoryDefaults() {
        Wlan63860Adapter t = new Wlan63860Adapter();
        Assertions.assertEquals("192.168.0.111", t.getHostName(), "factory default address");
        Assertions.assertEquals(1234, t.getPort(), "factory default port");
    }

    @Test
    public void testConnectionConfigAdapter() {
        ConnectionConfig cc = new ConnectionConfig();
        cc.setInstance();
        Assertions.assertTrue(cc.getAdapter() instanceof Wlan63860Adapter);
        Assertions.assertEquals(1234, cc.getAdapter().getPort());
    }

    @Test
    public void testTitles() {
        Assertions.assertEquals("LocoNet-USB and WLAN Interface 63860 (WLAN)", new ConnectionConfig().name());
        Assertions.assertEquals("LocoNet-USB und WLAN Interface 63860 (WLAN)",
                Bundle.getMessage(java.util.Locale.GERMANY, "WLAN_63860Title"));
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
