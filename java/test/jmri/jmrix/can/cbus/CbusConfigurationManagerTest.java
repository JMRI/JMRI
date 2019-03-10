package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusConfigurationManagerTest {

    @Test
    public void testCTor() {
        CbusConfigurationManager t = new CbusConfigurationManager(new CanSystemConnectionMemo());
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDisabled() {

        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setDisabled(true);
        
        CbusConfigurationManager t = new CbusConfigurationManager(memo);
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
        
        t.dispose();
        t = null;
    }
    
    @Test
    public void testProvides() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        CbusConfigurationManager t = new CbusConfigurationManager(memo);
        
        Assert.assertTrue( t.provides(jmri.AddressedProgrammerManager.class) );
        Assert.assertTrue( t.provides(jmri.GlobalProgrammerManager.class) );
        Assert.assertTrue( t.provides(jmri.ThrottleManager.class) );
        Assert.assertTrue( t.provides(jmri.PowerManager.class) );
        Assert.assertTrue( t.provides(jmri.SensorManager.class) );
        Assert.assertTrue( t.provides(jmri.TurnoutManager.class) );
        Assert.assertTrue( t.provides(jmri.ReporterManager.class) );
        Assert.assertTrue( t.provides(jmri.LightManager.class) );
        Assert.assertTrue( t.provides(jmri.CommandStation.class) );
        Assert.assertFalse( t.provides(jmri.jmrix.can.cbus.CbusSensor.class) );
        
        tcis = null;
        memo = null;
        t.dispose();
        t = null;
        
    }    
    
    @Test
    public void testGet() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        CbusConfigurationManager t = new CbusConfigurationManager(memo);        
        
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
        
        tcis = null;
        memo = null;
        t.dispose();
        t = null;        
        
    }    
    
    @Test
    public void testgetClasses() {
        
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        TrafficControllerScaffold tcis = new TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        CbusConfigurationManager t = new CbusConfigurationManager(memo); 
        

        CbusDccProgrammerManager prm = new CbusDccProgrammerManager( new CbusDccProgrammer(tcis), memo);
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
        Assert.assertTrue("lightmanager",cs == t.get(jmri.CommandStation.class) );        
        
        tcis = null;
        memo = null;
        t.dispose();
        t = null;
        
        prm = null;
        pm = null;
        tm = null;
        tom = null;
        sm = null;
        rm = null;
        lm = null;
        cs = null;

    }
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusConfigurationManagerTest.class);

}
