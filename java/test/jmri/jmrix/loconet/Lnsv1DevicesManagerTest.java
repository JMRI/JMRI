package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Lnsv1DevicesManagerTest {

    private LocoNetSystemConnectionMemo memo;

    @Test
    public void testCTor() {
        Lnsv1DevicesManager lsvm = new Lnsv1DevicesManager(memo);
        Assertions.assertNotNull(lsvm, "Lnsv1DeviceManager exists");
        lsvm.dispose();
    }

    @Test
    void testGetDeviceList() {
        Lnsv1DevicesManager lsvm = new Lnsv1DevicesManager(memo);
        Assertions.assertNotNull(lsvm.getDeviceList(), "Lnsv1DeviceManager List exists");
        lsvm.dispose();
    }

    @Test
    void testGetDeviceCount() {
        JUnitUtil.initRosterConfigManager();
        Lnsv1DevicesManager lsvm = new Lnsv1DevicesManager(memo);
        Assertions.assertEquals(0, lsvm.getDeviceCount(), "Lnsv1DeviceManager List empty");
        lsvm.message(new LocoNetMessage(new int[] {0xE5, 0x10, 0x04, 0x50, 0x01, 0x06, 0x02, 0x13, 0x16, 0x7B, 0x00, 0x02, 0x00, 0x00, 0x00, 0x27}));
        JUnitUtil.waitThreadTerminated(Lnsv1DevicesManager.ROSTER_THREAD_NAME+memo.getSystemPrefix());
        Assertions.assertEquals(1, lsvm.getDeviceCount(), "Lnsv1DeviceManager List added 1");
        lsvm.dispose();
    }

    @Test
    void testGetDevice() {
        Lnsv1DevicesManager lsvm = new Lnsv1DevicesManager(memo);
        Assertions.assertNull(lsvm.getDevice(5000, 8), "Lnsv1DeviceManager List exists");
        lsvm.dispose();
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
