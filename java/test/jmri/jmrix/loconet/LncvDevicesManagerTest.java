package jmri.jmrix.loconet;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LncvDevicesManagerTest {

    LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNotNull(lcdm, "LncvDeviceManager exists");
    }

    @Test
    void testGetDeviceList() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNotNull(lcdm.getDeviceList(), "LncvDeviceManager List exists");
    }

    @Test
    void testGetDeviceCount() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        jmri.InstanceManager.setDefault(jmri.jmrit.roster.RosterConfigManager.class, new RosterConfigManager());
        Assertions.assertEquals(0, lcdm.getDeviceCount(), "LncvDeviceManager List empty");
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assertions.assertEquals(1, lcdm.getDeviceCount(), "LncvDeviceManager List added 1");
    }

    @Test
    void testGetDevice() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNull(lcdm.getDevice(5000, 8), "LncvDeviceManager List exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }

}
