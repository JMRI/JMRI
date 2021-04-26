package jmri.jmrix.roco.z21;

import jmri.InstanceManager;
import jmri.MeterManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class Z21PredefinedMetersTest {

    private Z21InterfaceScaffold tc;
    private Z21SystemConnectionMemo memo;

    public double getCurrent() {
        return InstanceManager.getDefault(MeterManager.class).getBySystemName("ZVCommandStationCurrent").getKnownAnalogValue();
    }
    
    public double getVoltage() {
        return InstanceManager.getDefault(MeterManager.class).getBySystemName("ZVCommandStationVoltage").getKnownAnalogValue();
    }

    @Test
    public void testMeters() {
        byte msg[] = {
            (byte) 0x14, (byte) 0x00,   // Length
            (byte) 0x84, (byte) 0x00,   // Header
            (byte) 0x00, (byte) 0x00,   // Main current
            (byte) 0x00, (byte) 0x00,   // Prog current
            (byte) 0x00, (byte) 0x00,   // Filtered main current
            (byte) 0x00, (byte) 0x00,   // Temperature
            (byte) 0x00, (byte) 0x00,   // Supply voltage
            (byte) 0x00, (byte) 0x00,   // VCC voltage
            (byte) 0x00, (byte) 0x00,   // Central state, Central state ex
            (byte) 0x00, (byte) 0x00};  // reserved, reserved
        
        msg[4] = 8;   msg[5] = 0;   // 8 mA current
        Z21Reply reply = new Z21Reply(msg, 20);
        tc.sendTestMessage(reply);
        Assert.assertEquals(8, getCurrent(), 0.001 );
        
        msg[4] = 23;   msg[5] = 2;   // 535 mA current
        reply = new Z21Reply(msg, 20);
        tc.sendTestMessage(reply);
        Assert.assertEquals(535, getCurrent(), 0.001 );
        
        msg[14] = 8;   msg[15] = 0;   // 8 mV voltage
        reply = new Z21Reply(msg, 20);
        tc.sendTestMessage(reply);
        Assert.assertEquals(8, getVoltage(), 0.001 );
        
        msg[14] = 23;   msg[15] = 2;   // 535 mV voltage
        reply = new Z21Reply(msg, 20);
        tc.sendTestMessage(reply);
        Assert.assertEquals(535, getVoltage(), 0.001 );
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // This test requires a registred connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.roco.z21.Z21Adapter();
        pa.setSystemPrefix("Z");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.roco.z21.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        // infrastructure objects
        tc = new Z21InterfaceScaffold();
        memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);  
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        new Z21PredefinedMeters(memo);
    }

    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        memo = null;
        tc = null;
        JUnitUtil.tearDown(); 
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
