package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//import static org.junit.jupiter.api.Assertions.*;

class LncvDevicesTest {

    @Test
    public void testCTor() {
        LncvDevices ld = new LncvDevices();
        Assert.assertNotNull("LncvDeviceManager exists", ld);
    }

    @Test
    void addDevice() {
    }

    @Test
    void removeAllDevices() {
    }

    @Test
    void isDeviceExistant() {
    }

    @Test
    void deviceExists() {
    }

    @Test
    void getDevice() {
    }

    @Test
    void getDevices() {
    }

    @Test
    void size() {
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        LocoNetSystemConnectionMemo memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
