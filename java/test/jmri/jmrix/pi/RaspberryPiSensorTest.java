package jmri.jmrix.pi;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioProvider;
import jmri.JmriException;
import jmri.Sensor;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class RaspberryPiSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Override
    @Test
    public void testCreate() {
        // RaspberryPi Sensors always have a known state
        Assert.assertEquals("initial state 1", Sensor.ACTIVE, t.getState());
        Assert.assertEquals("initial state 2", "Active", t.describeState(t.getState()));
    }

    @Override
    @Test
    public void testDebounce() throws JmriException {
        t.setSensorDebounceGoingActiveTimer(81L);
        Assert.assertEquals("timer", 81L, t.getSensorDebounceGoingActiveTimer());

        t.setSensorDebounceGoingInActiveTimer(31L);
        Assert.assertEquals("timer", 31L, t.getSensorDebounceGoingInActiveTimer());

        Assert.assertEquals("initial state", Sensor.ACTIVE, t.getState());
        t.setOwnState(Sensor.INACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.ACTIVE, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("2nd state", Sensor.INACTIVE, t.getState());

	t.setOwnState(Sensor.ACTIVE); // next is considered to run immediately, before debounce
        Assert.assertEquals("post-set state", Sensor.INACTIVE, t.getState());
        jmri.util.JUnitUtil.waitFor(()->{return t.getState() == t.getRawState();}, "raw state = state");
        Assert.assertEquals("Final state", Sensor.ACTIVE, t.getState());
    }

    @Override
    @Test
    @Ignore("Base class test does not function correctly for RaspberryPi Sensors")
    @ToDo("provide mock raspberry pi implementation so code can be tested using parent class test")
    public void testAddListener() throws JmriException {
    }

    @Test
    public void testGetPullResistance(){
        Assert.assertEquals("default pull state", jmri.Sensor.PullResistance.PULL_DOWN, t.getPullResistance());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        GpioProvider myprovider = new PiGpioProviderScaffold();
        GpioFactory.setDefaultProvider(myprovider);
        JUnitUtil.setUp();
        t = new RaspberryPiSensor("PiS1");
    }

    @Override
    @After
    public void tearDown() {
	t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(RaspberryPiSensorTest.class);

}
