package jmri.jmrix.maple;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialSensor class
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SerialSensorTest {

    @Test
    public void testCTor() {
        SerialSensor t = new SerialSensor("KS1"); // does not need the _memo
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        new MapleSystemConnectionMemo("K", "Maple");
    }

    @After
    public void tearDown() {

        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
