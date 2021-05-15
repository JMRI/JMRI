package jmri.jmrit.automat;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SigletTest {

    @Test
    public void testBasics() throws JmriException {
        Siglet t = new Siglet() {
            @Override
            public void defineIO() {
                defined = true;
                setInputs(new NamedBean[]{is1, is2});
            }
            @Override
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
        t.stop();
    }


    @Test
    public void testNoInpuUnNamed() throws JmriException {
        Siglet t = new Siglet() {
            @Override
            public void defineIO() {
                defined = true;
            }
            @Override
            public void setOutput() {
                output = true;
            }
        };
        Assert.assertNotNull("exists",t);

        Assert.assertFalse(defined);
        try {
            t.start();
        } catch (IllegalArgumentException e) {
            jmri.util.JUnitAppender.assertErrorMessage("Siglet start invoked (without a name), but no inputs provided");
        }
        t.stop();
    }

    @Test
    public void testNoInputNamed() throws JmriException {
        Siglet t = new Siglet() {
            @Override
            public void defineIO() {
                defined = true;
            }
            @Override
            public void setOutput() {
                output = true;
            }
        };
        Assert.assertNotNull("exists",t);

        t.setName("foo");
        Assert.assertEquals("foo", t.getName());

        Assert.assertFalse(defined);
        try {
            t.start();
        } catch (IllegalArgumentException e) {
            jmri.util.JUnitAppender.assertErrorMessage("Siglet start invoked for \"foo\", but no inputs provided");
        }
        t.stop();
    }

    Sensor is1;
    Sensor is2;
    volatile boolean defined;
    volatile boolean output;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        JUnitUtil.initInternalSensorManager();
        is1 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
        is2 = InstanceManager.getDefault(SensorManager.class).provideSensor("IS2");
        defined = false;
        output = false;
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SigletTest.class);

}
