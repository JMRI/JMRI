package jmri.jmrix.powerline;

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

    @Test
    public void testCTor() {
        SerialTrafficController tc = new SerialTrafficController(){
            @Override
            public void sendSerialMessage(SerialMessage m,SerialListener reply) {
            }
        };
        SerialSystemConnectionMemo memo = new SerialSystemConnectionMemo();
        memo.setTrafficController(tc);
        SerialSensor t = new SerialSensor("PS1",tc);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SerialSensorTest.class);

}
