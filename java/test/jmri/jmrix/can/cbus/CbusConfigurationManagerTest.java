package jmri.jmrix.can.cbus;

import java.io.IOException;
import java.io.File;
import java.util.Collection;

import jmri.*;
import jmri.jmrix.can.*;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorDataModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusConfigurationManagerTest {

    @Test
    public void testCTor() {
        assertNotNull(t, "exists");
    }

    @Test
    public void testDisabled() {

        memo.setDisabled(true);
        
        assertNotNull(t);
        
        assertFalse( t.provides(PowerManager.class) );
        
        assertNull( t.get(PowerManager.class) );
        
        assertNull( t.get(PowerManager.class) );
        assertNull( t.get(ThrottleManager.class) );
        assertNull( t.get(TurnoutManager.class) );
        assertNull( t.get(SensorManager.class) );
        assertNull( t.get(ReporterManager.class) );
        assertNull( t.get(LightManager.class) );
        assertNull( t.get(CommandStation.class) );
        assertNull( t.get(CbusPreferences.class) );
        assertNull( t.get(CabSignalManager.class) );
        
    }

    @Test
    public void testProvides() {
        
        assertTrue( t.provides(AddressedProgrammerManager.class) );
        assertTrue( t.provides(GlobalProgrammerManager.class) );
        assertTrue( t.provides(ThrottleManager.class) );
        assertTrue( t.provides(PowerManager.class) );
        assertTrue( t.provides(SensorManager.class) );
        assertTrue( t.provides(TurnoutManager.class) );
        assertTrue( t.provides(ReporterManager.class) );
        assertTrue( t.provides(LightManager.class) );
        assertTrue( t.provides(CommandStation.class) );
        assertTrue( t.provides(CbusPreferences.class) );
        assertTrue( t.provides(CabSignalManager.class) );
        assertTrue( t.provides(ConsistManager.class) );
        assertTrue( t.provides(ClockControl.class) );
        assertTrue( t.provides(CbusSimulator.class)); // created by default
        assertTrue( t.provides(CbusSlotMonitorDataModel.class)); // created by default
        
        assertFalse( t.provides(CbusNodeTableDataModel.class) ); // not created by default
        assertFalse( t.provides(CbusEventTableDataModel.class) ); // not created by default
        
        assertFalse( t.provides(CbusSensor.class) );
        
    }

    @Test
    public void testGet() {
        
        assertNull( t.get(jmri.jmrix.can.cbus.CbusSensor.class) );

        assertNotNull( t.get(AddressedProgrammerManager.class) );
        assertNotNull( t.get(GlobalProgrammerManager.class) );
        assertNotNull( t.get(ThrottleManager.class) );
        assertNotNull( t.get(PowerManager.class) );
        assertNotNull( t.get(SensorManager.class) );
        assertNotNull( t.get(TurnoutManager.class) );
        assertNotNull( t.get(ReporterManager.class) );
        assertNotNull( t.get(LightManager.class) );
        assertNotNull( t.get(CommandStation.class) );
        assertNotNull( t.get(CbusPreferences.class) );
        assertNotNull( t.get(CabSignalManager.class) );
        
        assertNull( t.get(CbusNodeTableDataModel.class) );
        assertNull( t.get(CbusEventTableDataModel.class) );
        
        assertNull( t.provide(CbusSensor.class) );
    }

    @Test
    public void testConfigureDisabled() {
        memo.setDisabled(true);
        t.configureManagers();
        assertNull( t.get(LightManager.class) );
        assertEquals(0, tcis.numListeners(),"No listeners " + tcis.getListeners());
    
    }

    @Test
    public void testProvideEventModel() {
        assertFalse(memo.provides(CbusEventTableDataModel.class));
        CbusEventTableDataModel evModel = t.provide(CbusEventTableDataModel.class);
        assertNotNull( evModel );
        assertNotNull( memo.get(CbusEventTableDataModel.class),"event table found in memo" );
        assertTrue((memo.get(CbusEventTableDataModel.class) == evModel)," same ev model");
        assertTrue(memo.provides(CbusEventTableDataModel.class));
        evModel.skipSaveOnDispose();
    }

    @Test
    public void testProvideNodeModel() {
        assertFalse(memo.provides(CbusNodeTableDataModel.class));
        assertNotNull( t.provide(CbusNodeTableDataModel.class) );
        assertTrue(memo.provides(CbusNodeTableDataModel.class));
        assertNotNull( memo.get(CbusNodeTableDataModel.class),"node table found in memo" );
        assertTrue((memo.get(CbusNodeTableDataModel.class) == t.provide(CbusNodeTableDataModel.class))," same node model");
    }

    @Test
    public void testGetMeters() {
        t.configureManagers();
        assertNotNull( InstanceManager.getDefault(MeterManager.class).getBySystemName("MVCBUSCurrentMeter") );
        assertNotNull( InstanceManager.getDefault(MeterManager.class).getBySystemName("MVCBUSVoltageMeter") );
    }

    @Test
    public void testNodeEventManagerDispose(){
        assertEquals(0, tcis.numListeners(),"no tcis listeners after memo creation");
        ((CbusNodeTableDataModel)t.provide(CbusNodeTableDataModel.class)).setBackgroundAllocateListener(false);
        assertEquals(2, tcis.numListeners(),"2 tcis listeners");
        ((CbusEventTableDataModel)t.provide(CbusEventTableDataModel.class)).skipSaveOnDispose();
        assertEquals(3, tcis.numListeners(),"3 tcis listeners");
        memo.dispose();
        assertEquals(0, tcis.numListeners(),"All listeners removed " + tcis.getListeners());
    }

    @Test
    public void testDisposeAllManagers() {
        assertEquals(0, tcis.numListeners(),"no tcis listeners after memo creation");
        ((CbusNodeTableDataModel)t.provide(CbusNodeTableDataModel.class)).setBackgroundAllocateListener(false);
        ((CbusEventTableDataModel)t.provide(CbusEventTableDataModel.class)).skipSaveOnDispose();

        assertEquals(3, tcis.numListeners(),"3 tcis listeners");
    
        memo.configureManagers();

        assertTrue(tcis.numListeners() > 5,"6 tcis listeners as of April 2022");
        
        memo.dispose();

        assertEquals(0, tcis.numListeners(),"All listeners removed " + tcis.getListeners());
        
    }
    
    public static Collection<Class<?>> classData() {
        Collection<Class<?>> toReturn = new java.util.HashSet<>(12);
        toReturn.add(CbusPreferences.class);
        toReturn.add(PowerManager.class);
        toReturn.add(CommandStation.class);
        toReturn.add(ThrottleManager.class);
        toReturn.add(ClockControl.class);
        toReturn.add(SensorManager.class);
        toReturn.add(TurnoutManager.class);
        toReturn.add(ReporterManager.class);
        toReturn.add(LightManager.class);
        toReturn.add(CabSignalManager.class);
        toReturn.add(CbusPredefinedMeters.class);
        toReturn.add(CbusSimulator.class);
        toReturn.add(CbusSlotMonitorDataModel.class);
        return toReturn;
    }
    
    @ParameterizedTest(name = "{arguments}")
    @MethodSource("classData")
    public void testGetClass(Class<?> classToTest ) {
    
        assertEquals(0, tcis.numListeners(),"no tcis listeners before test");

        assertNull(memo.getFromMap(classToTest));
        assertNotNull(memo.get(classToTest));
        assertNotNull(memo.getFromMap(classToTest), classToTest.getCanonicalName() + " not added to memo classObjectMap");
        assertNotNull(InstanceManager.getNullableDefault(classToTest));

        memo.dispose();
        assertNull(memo.getFromMap(classToTest));
        assertEquals(0, tcis.numListeners(),"All listeners removed " + tcis.getListeners());
    
    }

    @Test
    public void testGetDisposeSimulator() {
        
        CbusSimulator simA = memo.getFromMap(CbusSimulator.class);
        assertNull(simA);
        CbusSimulator sim = memo.get(CbusSimulator.class);
        assertNotNull(sim);
        simA = memo.getFromMap(CbusSimulator.class);
        assertNotNull(simA);
        assertTrue(sim == simA);
        
        t.disposeOf(sim, CbusSimulator.class);
        simA = memo.getFromMap(CbusSimulator.class);
        assertNull(simA);
    }

    @Test
    public void testGetDisposeSlotMonitor() {
        
        CbusSlotMonitorDataModel smdm = memo.getFromMap(CbusSlotMonitorDataModel.class);
        assertNull(smdm);
        CbusSlotMonitorDataModel smdm2 = memo.get(CbusSlotMonitorDataModel.class);
        assertNotNull(smdm2);
        smdm = memo.getFromMap(CbusSlotMonitorDataModel.class);
        assertNotNull(smdm);
        assertTrue(smdm2 == smdm);
        
        t.disposeOf(smdm2, CbusSlotMonitorDataModel.class);
        smdm = memo.getFromMap(CbusSlotMonitorDataModel.class);
        assertNull(smdm);
    }

    private CanSystemConnectionMemo memo;
    private TrafficControllerScaffold tcis; // needed for DCC programming mgr?
    private CbusConfigurationManager t;

    @BeforeEach
    public void setUp(@TempDir File tempDir) throws IOException  {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager( new jmri.profile.NullProfile( tempDir));

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
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        t = memo.get(CbusConfigurationManager.class);
    }

    @AfterEach
    public void tearDown() {
        CbusEventTableDataModel evmod = memo.get( CbusEventTableDataModel.class);
        if ( evmod != null ) {
            evmod.skipSaveOnDispose();
        }
        t.dispose();
        tcis.terminateThreads();
        memo.dispose();
        t = null;
        tcis = null;
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConfigurationManagerTest.class);

}
