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
        Assert.assertNotNull("instancemanager getdefault MqttSystemConnectionMemo exists",memo);
        MqttSignalMast t = new MqttSignalMast("IF$mqm:AAR-1946:PL-2-high($0001)");
        Assert.assertNotNull("exists",t);
    }

    private MqttSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new MqttSystemConnectionMemo();
    }

    @AfterEach
    public void tearDown() {
        if ( memo != null ) {
            memo.dispose();
            memo = null;
        }
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(VirtualSignalMastTest.class);

}
