package jmri.jmrix.dccpp;

import jmri.InstanceManager;
import jmri.Meter;
import jmri.MeterManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender       Copyright (C) 2017
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class DCCppPredefinedMetersTest {

    private DCCppInterfaceScaffold tc;
    private DCCppPredefinedMeters mm;
    
    @Test
    public void testMeterReplies() {
        mm.message(DCCppReply.parseDCCppReply("a10")); // a syntactically valid current reply
        Meter m = InstanceManager.getDefault(MeterManager.class).getBySystemName("DVC_CurrentPct");
        Assert.assertNotNull("verify meter was created", m);
        Assert.assertEquals("current level percentage 100.0 - 0.0", (10.0 / DCCppConstants.MAX_CURRENT) * 100, m.getKnownAnalogValue(), 0.05);

        mm.message(DCCppReply.parseDCCppReply("c PROGVolts 18.2 V Milli 9.0 24.0 0.1 18")); // new meter reply
        m = InstanceManager.getDefault(MeterManager.class).getBySystemName("DVV_PROGVolts");       
        Assert.assertNotNull("verify meter was created", m);
        Assert.assertEquals("verify value from meter reply", 18.2, m.getKnownAnalogValue(), 0.00001);
        Assert.assertEquals("DVV_PROGVolts", m.getSystemName());
        Assert.assertEquals(jmri.Meter.Unit.Milli, m.getUnit());
        Assert.assertEquals(9.0,   m.getMin(),   0.00001);
        Assert.assertEquals(24.0,  m.getMax(),   0.00001);
        Assert.assertEquals(0.1,   m.getResolution(), 0.00001);        

        mm.message(DCCppReply.parseDCCppReply("c MAINVolts 25.2 V Milli 9.0 24.0 0.1 18")); // new meter reply, value exceeds max value
        m = InstanceManager.getDefault(MeterManager.class).getBySystemName("DVV_MAINVolts");       
        Assert.assertNotNull("verify meter was created", m);
        Assert.assertEquals("verify value is capped at maxValue", 24.0, m.getKnownAnalogValue(), 0.00001);

        mm.message(DCCppReply.parseDCCppReply("c My-Current_Meter#1 0.3 C NoPrefix 0.0 5.0 0.01 4.9"));
        m = InstanceManager.getDefault(MeterManager.class).getBySystemName("DVC_My-Current_Meter#1");       
        Assert.assertNotNull("meter created with special chars in name", m);
}

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // This test requires a registered connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.dccpp.network.DCCppEthernetAdapter();
        pa.setSystemPrefix("D");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.dccpp.network.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        // infrastructure objects
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        
        DCCppSystemConnectionMemo memo = new DCCppSystemConnectionMemo(tc);
        mm = new DCCppPredefinedMeters(memo);
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppPredefinedMetersTest.class);
}
