package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

class LncvDevicesTest {

    @Test
    public void testCTor() {
        LncvDevices ld = new LncvDevices();
        Assertions.assertNotNull(ld, "LncvDeviceManager exists");
    }

    @Test
    @Disabled("Test requires further development")
    void addDevice() {
    }

    @Test
    @Disabled("Test requires further development")
    void removeAllDevices() {
    }

    @Test
    @Disabled("Test requires further development")
    void isDeviceExistant() {
    }

    @Test
    @Disabled("Test requires further development")
    void deviceExists() {
    }

    @Test
    @Disabled("Test requires further development")
    void getDevice() {
    }

    @Test
    @Disabled("Test requires further development")
    void getDevices() {
    }

    @Test
    @Disabled("Test requires further development")
    void size() {
    }

    private LocoNetSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
