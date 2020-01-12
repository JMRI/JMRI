package jmri.jmrit.automat;

import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class AbstractAutomatonTest {

    Sensor sensor1;
    Sensor sensor2;
    Sensor sensor3;
    Sensor sensor4;
    volatile boolean done;
    volatile boolean running;
    
    @Test
    public void testCTor() {
        AbstractAutomaton t = new AbstractAutomaton();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testTestingCompletion() {
        // more of a test of infrastructure
        done = false;
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                done = true;
                return false; // done
            }
        };
        
        a.start();
        JUnitUtil.waitFor(()->{return done;}, "done");
    }
        
    @Test
    public void testSensorInterlock() throws JmriException {
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
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
    public void testStop() throws JmriException {
        log.debug("start testWaitChange");

        done = false;
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                waitMsec(100000);
                done = true;
                return false; // done
            }
        };
        
        log.debug("before start test automat testStop");
        a.start();
        JUnitUtil.waitFor(()->{return a.isRunning();}, "running");
        a.stop();
        JUnitUtil.waitFor(()->{return ! a.isRunning();}, "stopped");
        Assert.assertTrue("didn't complete handle", ! done);
    }
    
    @Test
    public void testWaitSensorChange() throws JmriException {
        log.debug("start testWaitChange");
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        Sensor sensor5 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS5");
        Sensor sensor6 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS6");
        Sensor sensor7 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS7");
        Sensor sensor8 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS8");
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                waitSensorChange(new Sensor[]{sensor1, sensor2, sensor3, sensor4,
                                              sensor5, sensor6, sensor7, sensor8});
                done = true;
                return false; // done
            }
        };
        
        log.debug("before start test automat testWaitChange");
        a.start();
        JUnitUtil.waitFor(()->{return a.isWaiting();}, "waiting");
        
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
    }


    @Test
    public void testWaitChange() throws JmriException {
        log.debug("start testWaitChange");
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                waitChange(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
                done = true;
                return false; // done
            }
        };
        
        log.debug("before start test automat testWaitChange");
        a.start();
        JUnitUtil.waitFor(()->{return a.isWaiting();}, "waiting");
        
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
    }
    
    @Test
    public void testWaitChangePreCheckQuick() throws JmriException {
        log.debug("start testWaitChangePreCheck");
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                waitChange(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
                done = true;
                return false; // done
            }
        };
        
        a.waitChangePrecheck(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
        log.debug("before start test automat testWaitChangePreCheckQuick");
        a.start();
        
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
    }

    @Test
    public void testWaitChangePreCheckLater() throws JmriException {
        log.debug("start testWaitChangePreCheck");
        // more of a test of infrastructure
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                waitChange(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
                done = true;
                return false; // done
            }
        };
        
        a.waitChangePrecheck(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
        log.debug("before start test automat testWaitChangePreCheckLater");
        a.start();
        JUnitUtil.waitFor(()->{return a.isWaiting();}, "waiting");
        
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
    }

    @Test
    public void testWaitChangeBadPreCheck() throws JmriException {
        log.debug("start testWaitChangeBadPreCheck");
        // more of a test of infrastructure
        running = false;
        done = false;
        sensor1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        sensor2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        sensor3 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS3");
        sensor4 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS4");
        
        AbstractAutomaton a = new AbstractAutomaton(){
            @Override
            public boolean handle() {
                running = true;
                waitChange(new NamedBean[]{sensor2, sensor1, sensor3, sensor4}); // not same as precheck
                done = true;
                return false; // done
            }
        };
        
        a.waitChangePrecheck(new NamedBean[]{sensor1, sensor2, sensor3, sensor4});
        log.debug("before start test automat testWaitChangeBadPreCheck");
        a.start();
        JUnitUtil.waitFor(()->{return a.isWaiting();}, "waiting");
                
        log.debug("after start test automat, before change sensor");
        sensor2.setKnownState(Sensor.ACTIVE);
        
        log.debug("after change sensor, before waitFor");
        JUnitUtil.waitFor(()->{return done;}, "done");
        
        jmri.util.JUnitAppender.assertWarnMessage("Precheck ignored because of mismatch in bean 0");
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractAutomatonTest.class);

}
