package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XBeeSensorTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeSensor class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XBeeSensorTest extends TestCase {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    public void testCtor() {
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS1234", "XBee Sensor Test", tc) {
            /**
             *
             */
            private static final long serialVersionUID = -2967402890226508680L;

            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    public void testCtorAddressPinName() {
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS123:4", "XBee Sensor Test", tc) {
            /**
             *
             */
            private static final long serialVersionUID = -4934402912485188869L;

            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    public void testCtor16BitHexNodeAddress() {
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCSABCD:4", "XBee Sensor Test", tc) {
            /**
             *
             */
            private static final long serialVersionUID = -1923632424973837859L;

            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    public void testCtor16BitHexStringNodeAddress() {
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCSAB CD:4", "XBee Sensor Test", tc) {
            /**
             *
             */
            private static final long serialVersionUID = -4200619366651260048L;

            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    public void testCtor64BitHexStringNodeAddress() {
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS00 13 A2 00 40 A0 4D 2D:4", "XBee Sensor Test", tc) {
            /**
             *
             */
            private static final long serialVersionUID = -3310604485155063844L;

            @Override
            public void requestUpdateFromLayout() {
            }
        };
        Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public XBeeSensorTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeSensorTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XBeeSensorTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XBeeTrafficController() {
            public void setInstance() {
            }
        };
        memo = new XBeeConnectionMemo();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensorTest.class.getName());

}
