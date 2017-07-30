package jmri.jmrit.automat;

import jmri.util.JUnitUtil;
import jmri.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractAutomatonTest {

    @Test
    public void testCTor() {
        AbstractAutomaton t = new AbstractAutomaton();
        Assert.assertNotNull("exists",t);
    }

    boolean done;

    @Test
    public void testTestingCompletion() {
        // more of a test of infrastructure
        done = false;
        AbstractAutomaton a = new AbstractAutomaton(){
            public boolean handle() {
                done = true;
                return false; // done
            }
        };
        
        a.start();
        JUnitUtil.waitFor(()->{return done;}, "done");
    }
    
    Sensor sensor1;
    Sensor sensor2;
    Sensor sensor3;
    Sensor sensor4;
    
    @Test
    public void testSensorInterlock() throws JmriException {
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        AbstractAutomaton a = new AbstractAutomaton(){
            public boolean handle() {
                if (sensor1.getKnownState() == Sensor.ACTIVE) {
                    done = true;
                    return false; // done
                }
                return true; // repeat
            }
        };
        
        a.start();
        
        sensor1.setKnownState(Sensor.ACTIVE);
        
        JUnitUtil.waitFor(()->{return done;}, "done");
    }

    @Test
    public void testWaitChange() throws JmriException {
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        AbstractAutomaton a = new AbstractAutomaton(){
            public boolean handle() {
                waitChange(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
                done = true;
                return false; // done
            }
        };
        
        log.debug("before start test automat");
        a.start();
        
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractAutomatonTest.class.getName());

}
