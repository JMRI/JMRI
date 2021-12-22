package jmri.jmrix.mqtt;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Bob Jacobsen  copyright 2020
 */
public class MqttSignalMastTest {

    @Test
    public void testCTor() {
        MqttSignalMast t = new MqttSignalMast("IF$mqm:AAR-1946:PL-2-high($0001)");
        Assert.assertNotNull("exists",t);
    }

    MqttSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new MqttSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        memo = null;
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalMastTest.class);

}
