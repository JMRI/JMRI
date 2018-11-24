package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.*;

/**
 * Tests Logix in detail by loading a set of 
 * configurations from a file (to simplify setup), 
 * then changing inputs and checking
 * outputs.  
 * <p>
 * Because the load is slow, it's done once
 * for this entire test class.
 *
 * @author Bob Jacobsen Copyright (C) 2018
 */
public class ActiveLogixTest {

    @BeforeClass
    public static void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();

        // load and activate sample file
        java.io.File f = new java.io.File("java/test/jmri/implementation/configurexml/load/ActiveLogixTestDefinitions.xml");
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {};
        cm.load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();
    }
    
    private void setSensor(Sensor s, int state) {
        try {
            s.setState(state);
        } catch (JmriException e) {
        }
    }
    
    @Test
    public void testTurnoutDrivesSensor() {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT101");
        Assert.assertNotNull(turnout);
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor("IS101");
        Assert.assertNotNull(sensor);
        
        ThreadingUtil.runOnLayout(()->{turnout.setCommandedState(Turnout.THROWN);});
        JUnitUtil.waitFor(()->{return sensor.getState()==Sensor.ACTIVE;},"sensor ACTIVE");
        
        ThreadingUtil.runOnLayout(()->{turnout.setCommandedState(Turnout.CLOSED);});
        JUnitUtil.waitFor(()->{return sensor.getState()==Sensor.INACTIVE;},"sensor INACTIVE");

        ThreadingUtil.runOnLayout(()->{turnout.setCommandedState(Turnout.THROWN);});
        JUnitUtil.waitFor(()->{return sensor.getState()==Sensor.ACTIVE;},"sensor ACTIVE");
        
    }

    @Test
    public void testSensorLogicDrivesTurnout() {
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT201");
        Assert.assertNotNull(turnout);
        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).getSensor("IS201");
        Assert.assertNotNull(sensor1);
        Sensor sensor2 = InstanceManager.getDefault(SensorManager.class).getSensor("IS202");
        Assert.assertNotNull(sensor2);
        Sensor sensor3 = InstanceManager.getDefault(SensorManager.class).getSensor("IS203");
        Assert.assertNotNull(sensor3);
        Sensor sensor4 = InstanceManager.getDefault(SensorManager.class).getSensor("IS204");
        Assert.assertNotNull(sensor4);
        Sensor sensor5 = InstanceManager.getDefault(SensorManager.class).getSensor("IS205");
        Assert.assertNotNull(sensor5);
        Sensor sensor6 = InstanceManager.getDefault(SensorManager.class).getSensor("IS206");
        Assert.assertNotNull(sensor6);
        
        // start all inactive
        ThreadingUtil.runOnLayout(()->{setSensor(sensor1, Sensor.INACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor2, Sensor.INACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor3, Sensor.INACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor4, Sensor.INACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor5, Sensor.INACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor6, Sensor.INACTIVE);});
        
        JUnitUtil.waitFor(()->{return turnout.getCommandedState()==Turnout.CLOSED;},"turnout CLOSED");
        
        ThreadingUtil.runOnLayout(()->{setSensor(sensor1, Sensor.ACTIVE);});
        ThreadingUtil.runOnLayout(()->{setSensor(sensor2, Sensor.ACTIVE);});
        JUnitUtil.waitFor(()->{return turnout.getCommandedState()==Turnout.THROWN;},"turnout THROWN");

        ThreadingUtil.runOnLayout(()->{setSensor(sensor2, Sensor.INACTIVE);});
        JUnitUtil.waitFor(()->{return turnout.getCommandedState()==Turnout.CLOSED;},"turnout CLOSED");

    }

    @Test
    public void testCheckFastClock() throws TimebaseRateException {
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT301");
        Assert.assertNotNull(turnout1);
        Turnout turnout2 = InstanceManager.getDefault(TurnoutManager.class).getTurnout("IT302");
        Assert.assertNotNull(turnout2);
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor("IS301");
        Assert.assertNotNull(sensor);
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        Assert.assertNotNull(timebase);
        timebase.setRun(false);
        Calendar cal = new GregorianCalendar();
        cal.set(2018, 1, 12, 2, 00, 00); // 02:00:00
        timebase.setTime(cal.getTime());
        
        // turnout change sets time
        ThreadingUtil.runOnLayout(()->{turnout1.setCommandedState(Turnout.THROWN);});
        ThreadingUtil.runOnLayout(()->{turnout2.setCommandedState(Turnout.CLOSED);});
        
        cal.set(2018, 1, 12, 4, 01, 00); // 04:01:00
        JUnitUtil.waitFor(()->{return timebase.getTime().equals(cal.getTime());},"date 04:01:00");
        JUnitUtil.waitFor(()->{return sensor.getState()==Sensor.ACTIVE;},"sensor ACTIVE");
        
        // turnout change sets time
        ThreadingUtil.runOnLayout(()->{turnout1.setCommandedState(Turnout.CLOSED);});
        ThreadingUtil.runOnLayout(()->{turnout2.setCommandedState(Turnout.THROWN);});
        
        cal.set(2018, 1, 12, 14, 02, 00); // 14:02:00
        JUnitUtil.waitFor(()->{return sensor.getState()==Sensor.INACTIVE;},"sensor INACTIVE");
        JUnitUtil.waitFor(()->{return timebase.getTime().equals(cal.getTime());},"date 14:02:00");
    }

    @Test
    public void testMemoryAccess() {
        Memory memory1 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM401");
        Assert.assertNotNull(memory1);
        Memory memory2 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM402");
        Assert.assertNotNull(memory1);
        Memory memory3 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM403");
        Assert.assertNotNull(memory3);
        Memory memory4 = InstanceManager.getDefault(MemoryManager.class).getMemory("IM404");
        Assert.assertNotNull(memory4);
   
        // test the IM401 to IM402 conditional
        ThreadingUtil.runOnLayout(()->{memory2.setValue("bbb");});
        ThreadingUtil.runOnLayout(()->{memory1.setValue("aaa");});
        JUnitUtil.waitFor(()->{return memory2.getValue().equals("aaa");},"Value aaa after aaa");

        ThreadingUtil.runOnLayout(()->{memory2.setValue("bbb");});
        ThreadingUtil.runOnLayout(()->{memory1.setValue("aatta");});
        JUnitUtil.waitFor(()->{return memory2.getValue().equals("bbb");},"Value bbb after aatta");
        
        ThreadingUtil.runOnLayout(()->{memory2.setValue("bbb");});
        ThreadingUtil.runOnLayout(()->{memory1.setValue("aAa");});
        JUnitUtil.waitFor(()->{return memory2.getValue().equals("aAa");},"Value aAa after aAa");

        // test the IM401 to IM402 conditional
        ThreadingUtil.runOnLayout(()->{memory4.setValue("No");});
        ThreadingUtil.runOnLayout(()->{memory2.setValue("100");});

        ThreadingUtil.runOnLayout(()->{memory3.setValue("200");});
        JUnitUtil.waitFor(()->{return memory4.getValue().equals("No");},"No after 200");
        
        ThreadingUtil.runOnLayout(()->{memory3.setValue("50");});
        JUnitUtil.waitFor(()->{return memory4.getValue().equals("OK");},"OK after 50");        
    }

    @Test
    public void testLightDrivesLight() {
        Light light1 = InstanceManager.getDefault(LightManager.class).getLight("IL501");
        Assert.assertNotNull(light1);
        Light light2 = InstanceManager.getDefault(LightManager.class).getLight("IL502");
        Assert.assertNotNull(light2);

        ThreadingUtil.runOnLayout(()->{light1.setState(Light.ON);});
        JUnitUtil.waitFor(()->{return light2.getState()==Light.OFF;},"light Off");
        
        ThreadingUtil.runOnLayout(()->{light1.setState(Light.OFF);});
        JUnitUtil.waitFor(()->{return light2.getState()==Light.ON;},"light On");
        
        ThreadingUtil.runOnLayout(()->{light1.setState(Light.ON);});
        JUnitUtil.waitFor(()->{return light2.getState()==Light.OFF;},"light Off");
        
    }

    @AfterClass
    public static void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

}
