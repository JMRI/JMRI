package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Lnsv1DevicesTest {

    Lnsv1Device lnsv1d1;
    Lnsv1Device lnsv1d2;

    @Test
    public void testCTor() {
        Lnsv1Devices ld = new Lnsv1Devices();
        Assertions.assertNotNull(ld, "Lnsv1DeviceManager exists");
    }

    @Test
    void addDevice() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d1);
        assertEquals(1, ld.size(), "get DeviceList size ");
    }

    @Test
    void removeAllDevices() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d1);
        assertEquals(1, ld.size(), "get DeviceList size 1");
        ld.removeAllDevices();
        assertEquals(0, ld.size(), "get empty DeviceList");
    }

    @Test
    void isDeviceExistant() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d1);
        ld.addDevice(lnsv1d2);
        assertEquals(1, ld.isDeviceExistant(lnsv1d2), "isDeviceExistant");
    }

    @Test
    void deviceExists() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d1);
        Assertions.assertTrue(ld.deviceExists(lnsv1d1), "isDeviceExistant");
    }

    @Test
    void getDevice() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d2);
        Assertions.assertEquals(ld.getDevice(0).getDeviceName(), "Lnsv1Mod_4_6");
    }

    @Test
    void getDevices() {
        Lnsv1Devices ld = new Lnsv1Devices();
        ld.addDevice(lnsv1d1);
        ld.addDevice(lnsv1d2);
        assertEquals(ld.getDevices()[0].getDeviceName(), "Lnsv1Mod_81_8");
    }

    @Test
    void size() {
        Lnsv1Devices ld = new Lnsv1Devices();
        assertEquals(0, ld.size(), "get DeviceList size ");
    }

    private LocoNetSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo);
        lnsv1d1 = new Lnsv1Device(81, 8, 2, 8, "Lnsv1Mod_81_8", "Decoder_81", 12);
        lnsv1d2 = new Lnsv1Device(4, 6, 18, 6, "Lnsv1Mod_4_6", "Decoder_4", 106);

    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
