package jmri.jmrix.ieee802154.xbee;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeSensorTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.xbee.XBeeSensor class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class XBeeSensorTest extends TestCase {

    public void testCtor() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS1234","XBee Sensor Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtorAddressPinName() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCS123:4","XBee Sensor Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtorHexNodeAddress() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setSensorManager(new XBeeSensorManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeSensor s = new XBeeSensor("ABCSABCD:4","XBee Sensor Test",tc); 
        Assert.assertNotNull("exists",s);
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
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(XBeeSensorTest.class.getName());

}
