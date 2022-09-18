package jmri.jmrix.ieee802154.xbee;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * XBeeSensorTest.java
 *
 * Test for the jmri.jmrix.ieee802154.xbee.XBeeSensor class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class XBeeSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    private XBeeTrafficController tc = null;
    XBeeConnectionMemo memo;

    @Test
    public void testCtorAddressPinName() {
        XBeeSensor s = new XBeeSensor("ABCS123:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexNodeAddress() {
        XBeeSensor s = new XBeeSensor("ABCSABCD:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexStringNodeAddress() {
        XBeeSensor s = new XBeeSensor("ABCSAB CD:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor64BitHexStringNodeAddress() {
        XBeeSensor s = new XBeeSensor("ABCS00 13 A2 00 40 A0 4D 2D:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        memo = new XBeeConnectionMemo();
        tc.setAdapterMemo(memo);
        memo.setTrafficController(tc);
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(memo));
        t = new XBeeSensor("ABCS1234", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }

            @Override
            public PullResistance getPullResistance() {
                return PullResistance.PULL_OFF;
            }
        };
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        Assertions.assertNotNull(tc);
        tc.terminate();
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();
    }

}
