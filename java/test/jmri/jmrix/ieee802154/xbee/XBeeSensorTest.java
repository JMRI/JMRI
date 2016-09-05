package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * XBeeSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensor class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeSensorTest {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    @Test
    @Ignore("needs XBee Object from scaffold")
    public void testCtor() {
        memo.setSystemPrefix("ABC");
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
    @Ignore("needs XBee Object from scaffold")
    public void testCtorAddressPinName() {
        memo.setSystemPrefix("ABC");
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
    @Ignore("needs XBee Object from scaffold")
    public void testCtor16BitHexNodeAddress() {
        memo.setSystemPrefix("ABC");
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
    @Ignore("needs XBee Object from scaffold")
    public void testCtor16BitHexStringNodeAddress() {
        memo.setSystemPrefix("ABC");
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
    @Ignore("needs XBee Object from scaffold")
    public void testCtor64BitHexStringNodeAddress() {
        memo.setSystemPrefix("ABC");
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
        apps.tests.Log4JFixture.setUp();
        tc = new XBeeTrafficController() {
            public void setInstance() {
            }
        };
        memo = new XBeeConnectionMemo();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
