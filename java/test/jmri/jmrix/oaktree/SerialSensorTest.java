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
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    //private OakTreeSystemConnectionMemo _memo = null;

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkOnMsgSent() {}

    @Override
    public void checkOffMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void testCTor2() {
        SerialSensor t2 = new SerialSensor("OS2", "sensor2");
        Assert.assertNotNull("exists",t2);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        //_memo = new OakTreeSystemConnectionMemo("O", "Oaktree");
        t = new SerialSensor("OS1"); // does not need the _memo
    }

    @Override
    @After
    public void tearDown() {
	t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
