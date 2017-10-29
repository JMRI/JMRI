package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialSensorTest {

    private OakTreeSystemConnectionMemo _memo = null;

    @Test
    public void testCTor() {
        SerialSensor t = new SerialSensor("OS1"); // does not need the _memo
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCTor2() {
        SerialSensor t2 = new SerialSensor("OS2", "sensor2");
        Assert.assertNotNull("exists",t2);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        _memo = new OakTreeSystemConnectionMemo("O", "Oaktree");
    }

    @After
    public void tearDown() {

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
