package jmri.jmrix.loconet;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class LnPredefinedMetersTest {

    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;

    @Test
    public void testLnMeter() {
        LnPredefinedMeters lm = new LnPredefinedMeters(memo);
        Assert.assertNotNull("exists",lm);
        Assert.assertNotNull(InstanceManager.getDefault(MeterManager.class).getBySystemName("LVCommandStationCurrent"));
        Assert.assertNotNull(InstanceManager.getDefault(MeterManager.class).getBySystemName("LVCommandStationVoltage"));
        Assert.assertEquals("Reports in Amps", InstanceManager.getDefault(MeterManager.class).getBySystemName("LVCommandStationCurrent").getUnit(), Meter.Unit.NoPrefix);
        lm.requestUpdateFromLayout();
        // expect one messages
        Assert.assertEquals("sent", 1, lnis.outbound.size());
        // set CS
        Assert.assertEquals("message 1", "BB 79 41 00", lnis.outbound.get(0).toString());
        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x4B, 0x4D, 0x05, 0x32, 0x70, 0x00, 0x36, 0x00,
                0x33, 0x00, 0x00, 0x00, 0x1C, 0x7F, 0x68, 0x03, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);
        lm.message(msg);
        Assert.assertEquals(0.5f,getCurrent(lm),0); // 0.5AMps
    }

    public double getCurrent(LnPredefinedMeters lm) {
        return InstanceManager.getDefault(MeterManager.class).getBySystemName("LVCommandStationCurrent").getKnownAnalogValue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // This test requires a registred connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.loconet.loconetovertcp.LnTcpDriverAdapter();
        pa.setSystemPrefix("L");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.loconet.loconetovertcp.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        lnis.dispose();
        JUnitUtil.tearDown();
    }
}
