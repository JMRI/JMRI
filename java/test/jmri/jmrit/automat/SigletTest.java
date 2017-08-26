package jmri.jmrit.automat;

import jmri.*;
import jmri.util.*;

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
public class SigletTest {

    @Test
    public void testBasics() throws JmriException {
        Siglet t = new Siglet() {
            public void defineIO() {
                defined = true;
                setInputs(new NamedBean[]{is1, is2});
            }
            public void setOutput() {
                output = true;
            }
        };
        Assert.assertNotNull("exists",t);
        
        t.setName("foo");
        Assert.assertEquals("foo", t.getName());
        
        Assert.assertFalse(defined);
        t.start();
        
        JUnitUtil.waitFor( ()->{ return defined; }, "defineIO run"); 
        Assert.assertTrue(output); // first cycle included

        output = false;
        is1.setState(Sensor.ACTIVE);
        
        JUnitUtil.waitFor( ()->{ return output; }, "setOutput run again"); 
    }


    @Test
    public void testNoInpuUnNamed() throws JmriException {
        Siglet t = new Siglet() {
            public void defineIO() {
                defined = true;
            }
            public void setOutput() {
                output = true;
            }
        };
        Assert.assertNotNull("exists",t);
        
        Assert.assertFalse(defined);
        t.start();

        jmri.util.JUnitAppender.assertErrorMessage("Siglet start invoked (without a name), but no inputs provided"); 
    }

    @Test
    public void testNoInputNamed() throws JmriException {
        Siglet t = new Siglet() {
            public void defineIO() {
                defined = true;
            }
            public void setOutput() {
                output = true;
            }
        };
        Assert.assertNotNull("exists",t);
        
        t.setName("foo");
        Assert.assertEquals("foo", t.getName());
        
        Assert.assertFalse(defined);
        t.start();

        jmri.util.JUnitAppender.assertErrorMessage("Siglet start invoked for \"foo\", but no inputs provided"); 
    }

    // The minimal setup for log4J
    Sensor is1;
    Sensor is2;
    volatile boolean defined;
    volatile boolean output;

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        is1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        is2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        defined = false;
        output = false;
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SigletTest.class.getName());

}
