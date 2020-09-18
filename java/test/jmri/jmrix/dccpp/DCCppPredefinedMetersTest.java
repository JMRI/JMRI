package jmri.jmrix.dccpp;

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
public class DCCppPredefinedMetersTest {

    private DCCppInterfaceScaffold tc;
    private DCCppPredefinedMeters mm;
    
    @Test
    public void testCurrentReply() {
        mm.message(DCCppReply.parseDCCppReply("a10")); // a syntactically valid current reply
        Assert.assertEquals("current level percentage 100.0 - 0.0", (10.0 / DCCppConstants.MAX_CURRENT) * 100, getCurrent(), 0.05);
    }

    public double getCurrent() {
        return InstanceManager.getDefault(MeterManager.class).getBySystemName("DVBaseStationCurrent").getKnownAnalogValue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // This test requires a registred connection config since ProxyMeterManager
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
