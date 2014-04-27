package jmri.jmrix.ieee802154.xbee;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeTurnoutTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnout class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class XBeeTurnoutTest extends TestCase {

    public void testCtor() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeTurnout s = new XBeeTurnout("ABCS1234","XBee Turnout Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtorAddressPinName() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeTurnout s = new XBeeTurnout("ABCS123:4","XBee Turnout Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtor16BitHexNodeAddress() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeTurnout s = new XBeeTurnout("ABCSABCD:4","XBee Turnout Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtor16BitHexStringNodeAddress() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeTurnout s = new XBeeTurnout("ABCSAB CD:4","XBee Turnout Test",tc); 
        Assert.assertNotNull("exists",s);
    }

    public void testCtor64BitHexStringNodeAddress() {
        XBeeTrafficController tc = new XBeeTrafficController();
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc,"ABC"));
        tc.setAdapterMemo(memo);
        XBeeTurnout s = new XBeeTurnout("ABCS00 13 A2 00 40 A0 4D 2D:4","XBee Turnout Test",tc); 
        Assert.assertNotNull("exists",s);
    }

	// from here down is testing infrastructure

	public XBeeTurnoutTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", XBeeTurnoutTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(XBeeTurnoutTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(XBeeTurnoutTest.class.getName());

}
