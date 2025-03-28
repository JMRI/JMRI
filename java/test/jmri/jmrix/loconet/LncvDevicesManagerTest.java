package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LncvDevicesManagerTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNotNull(lcdm, "LncvDeviceManager exists");
        lcdm.dispose();
    }

    @Test
    void testGetDeviceList() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNotNull(lcdm.getDeviceList(), "LncvDeviceManager List exists");
        lcdm.dispose();
    }

    @Test
    void testGetDeviceCount() {
        JUnitUtil.initRosterConfigManager();
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertEquals(0, lcdm.getDeviceCount(), "LncvDeviceManager List empty");
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        JUnitUtil.waitThreadTerminated(LncvDevicesManager.ROSTER_THREAD_NAME+memo.getSystemPrefix());
        Assertions.assertEquals(1, lcdm.getDeviceCount(), "LncvDeviceManager List added 1");
        lcdm.dispose();
    }

    @Test
    void testGetDevice() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assertions.assertNull(lcdm.getDevice(5000, 8), "LncvDeviceManager List exists");
        lcdm.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        var tc = memo.getLnTrafficController();
        if ( tc != null ) {
            tc.dispose();
        }
        memo = null;
        JUnitUtil.tearDown();
    }

}
