package jmri.jmrix.can.cbus;

import java.io.IOException;
import java.nio.file.Path;

import jmri.InstanceManager;
import jmri.MeterManager;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.swing.modeswitcher.SprogCbusSprog3PlusModeSwitcherFrame;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusConfigurationManagerTest {

    @Test
    public void testCTor() {
        t = new CbusConfigurationManager(memo);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDisabled() {

        memo.setDisabled(true);
        
        Assert.assertNotNull("exists",t);
        
        Assert.assertFalse( t.provides(jmri.PowerManager.class) );
        
        Assert.assertNull( t.get(jmri.PowerManager.class) );
        
        Assert.assertNull( t.getPowerManager() );
        Assert.assertNull( t.getThrottleManager() );
        Assert.assertNull( t.getTurnoutManager() );
        Assert.assertNull( t.getSensorManager() );
        Assert.assertNull( t.getReporterManager() );
        Assert.assertNull( t.getLightManager() );
        Assert.assertNull( t.getCommandStation() );
        Assert.assertNull( t.getCbusPreferences() );
        Assert.assertNull( t.getCabSignalManager() );
        
    }
    
    @Test
    public void testProvides() {
        
        Assert.assertTrue( t.provides(jmri.AddressedProgrammerManager.class) );
        Assert.assertTrue( t.provides(jmri.GlobalProgrammerManager.class) );
        Assert.assertTrue( t.provides(jmri.ThrottleManager.class) );
        Assert.assertTrue( t.provides(jmri.PowerManager.class) );
        Assert.assertTrue( t.provides(jmri.SensorManager.class) );
        Assert.assertTrue( t.provides(jmri.TurnoutManager.class) );
        Assert.assertTrue( t.provides(jmri.ReporterManager.class) );
        Assert.assertTrue( t.provides(jmri.LightManager.class) );
        Assert.assertTrue( t.provides(jmri.CommandStation.class) );
        Assert.assertTrue( t.provides(CbusPreferences.class) );
        Assert.assertTrue( t.provides(jmri.CabSignalManager.class) );
        
        Assert.assertFalse( t.provides(jmri.jmrix.can.cbus.CbusSensor.class) );
        
    }    
    
    @Test
    public void testGet() {
        
        Assert.assertNull( t.get(jmri.jmrix.can.cbus.CbusSensor.class) );

        Assert.assertNotNull( t.get(jmri.AddressedProgrammerManager.class) );
        Assert.assertNotNull( t.get(jmri.GlobalProgrammerManager.class) );
        Assert.assertNotNull( t.get(jmri.ThrottleManager.class) );
        Assert.assertNotNull( t.get(jmri.PowerManager.class) );
        Assert.assertNotNull( t.get(jmri.SensorManager.class) );
        Assert.assertNotNull( t.get(jmri.TurnoutManager.class) );
        Assert.assertNotNull( t.get(jmri.ReporterManager.class) );
        Assert.assertNotNull( t.get(jmri.LightManager.class) );
        Assert.assertNotNull( t.get(jmri.CommandStation.class) );
        Assert.assertNotNull( t.get(CbusPreferences.class) );
        Assert.assertNotNull( t.get(jmri.CabSignalManager.class) );
        
    }    
    
    @Test
    public void testGetMeters() {
        t.configureManagers();
        Assert.assertNotNull( InstanceManager.getDefault(MeterManager.class).getBySystemName("MVCBUSCurrentMeter") );
        Assert.assertNotNull( InstanceManager.getDefault(MeterManager.class).getBySystemName("MVCBUSVoltageMeter") );
    }
    
    @Test
    public void testgetClasses() {
        
        CbusDccProgrammerManager prm = new CbusDccProgrammerManager( new CbusDccProgrammer(memo), memo);
        t.setProgrammerManager(prm);
        Assert.assertTrue("programme manager",prm == t.get(jmri.GlobalProgrammerManager.class) );
        
        CbusPowerManager pm = t.getPowerManager();
        Assert.assertTrue("powermanager",pm == t.get(jmri.PowerManager.class) );
        
        CbusThrottleManager tm = new CbusThrottleManager(memo);
        t.setThrottleManager(tm);
        Assert.assertTrue("throttlemanager",tm == t.get(jmri.ThrottleManager.class) );
        
        CbusTurnoutManager tom = t.getTurnoutManager();
        Assert.assertTrue("turnoutmanager",tom == t.get(jmri.TurnoutManager.class) );
        
        CbusSensorManager sm = t.getSensorManager();
        Assert.assertTrue("sensormanager",sm == t.get(jmri.SensorManager.class) );

        CbusReporterManager rm = t.getReporterManager();
        Assert.assertTrue("reportermanager",rm == t.get(jmri.ReporterManager.class) );        

        CbusLightManager lm = t.getLightManager();
        Assert.assertTrue("lightmanager",lm == t.get(jmri.LightManager.class) );

        CbusCommandStation cs = t.getCommandStation();
        Assert.assertTrue("CommandStation",cs == t.get(jmri.CommandStation.class) );        
        
        CbusPreferences cbpref = t.getCbusPreferences();
        Assert.assertTrue("CbusPreferences",cbpref == t.get(CbusPreferences.class) );
        
        CbusCabSignalManager csm = t.getCabSignalManager();
        Assert.assertTrue("CbusCabSignalManager",csm == t.get(jmri.CabSignalManager.class) );
        
    }
        
    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis; // needed for DCC programming mgr?
    private CbusConfigurationManager t;
    private CbusPreferences prefs;
    
    @TempDir
    protected Path tempDir;
    
    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir.toFile()));

        // This test requires a registred connection config since ProxyMeterManager
        // auto creates system meter managers using the connection configs.
        InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.jmrix.NetworkPortAdapter pa = new jmri.jmrix.can.adapters.gridconnect.net.MergNetworkDriverAdapter();
        pa.setSystemPrefix("M");
        jmri.jmrix.ConnectionConfig cc = new jmri.jmrix.can.adapters.gridconnect.net.MergConnectionConfig(pa);
        InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class).add(cc);
        
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        prefs = new CbusPreferences();
        
        jmri.InstanceManager.store(prefs,CbusPreferences.class );
        
        t = new CbusConfigurationManager(memo);
    }

    @AfterEach
    public void tearDown() {
        
        t.dispose();
        tcis.terminateThreads();
        memo.dispose();
        t = null;
        tcis = null;
        memo = null;
        
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConfigurationManagerTest.class);

}
