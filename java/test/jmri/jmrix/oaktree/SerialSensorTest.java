package jmri.jmrix.oaktree;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SerialSensorTest extends jmri.implementation.AbstractSensorTestBase {

    //private OakTreeSystemConnectionMemo _memo = null;

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Test
    public void testCTor2() {
        SerialSensor t2 = new SerialSensor("OS2", "sensor2");
        Assert.assertNotNull("exists",t2);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        //_memo = new OakTreeSystemConnectionMemo("O", "Oaktree");
        t = new SerialSensor("OS1"); // does not need the _memo
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
