package jmri.jmrix.roco.z21;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Z21PredefinedMetersTest {

    private Z21InterfaceScaffold tc;
    private Z21SystemConnectionMemo memo;
    private Z21PredefinedMeters mm;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        // This test requires a registred connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.roco.z21.Z21Adapter();
        pa.setSystemPrefix("X");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.roco.z21.ConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        // infrastructure objects
        tc = new Z21InterfaceScaffold();
        memo = new Z21SystemConnectionMemo();
        memo.setTrafficController(tc);  
        memo.setRocoZ21CommandStation(new RocoZ21CommandStation());
        mm = new Z21PredefinedMeters(memo);
    }

    @AfterEach
    public void tearDown(){
        tc.terminateThreads();
        memo = null;
        tc = null;
        mm = null;
        JUnitUtil.tearDown(); 
    }

    // private final static Logger log = LoggerFactory.getLogger(DCCppMultiMeterTest.class);

}
