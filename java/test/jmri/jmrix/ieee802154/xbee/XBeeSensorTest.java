package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * XBeeSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensor class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeSensorTest {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    @Test
    public void testCtor() {
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS1234", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtorAddressPinName() {
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS123:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexNodeAddress() {
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCSABCD:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexStringNodeAddress() {
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCSAB CD:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor64BitHexStringNodeAddress() {
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS00 13 A2 00 40 A0 4D 2D:4", "XBee Sensor Test", tc) {
            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        //apps.tests.Log4JFixture.setUp();
        tc = new XBeeInterfaceScaffold();
        memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        tc.setAdapterMemo(memo);
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
    }

}
