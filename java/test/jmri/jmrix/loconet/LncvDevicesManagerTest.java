package jmri.jmrix.loconet;

import jmri.jmrit.roster.RosterConfigManager;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LncvDevicesManagerTest {

    LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assert.assertNotNull("LncvDeviceManager exists", lcdm);
    }

    @Test
    void testGetDeviceList() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assert.assertNotNull("LncvDeviceManager List exists", lcdm.getDeviceList());
    }

    @Test
    void testGetDeviceCount() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        jmri.InstanceManager.setDefault(jmri.jmrit.roster.RosterConfigManager.class, new RosterConfigManager());
        Assert.assertEquals("LncvDeviceManager List empty", 0, lcdm.getDeviceCount());
        lcdm.message(new LocoNetMessage(new int[] {0xE5, 0x0F, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x00, 0x00, 0x08, 0x00, 0x00, 0x4D}));
        Assert.assertEquals("LncvDeviceManager List added 1", 1, lcdm.getDeviceCount());
    }

    @Test
    void testGetDevice() {
        LncvDevicesManager lcdm = new LncvDevicesManager(memo);
        Assert.assertNull("LncvDeviceManager List exists", lcdm.getDevice(5000, 8));
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
