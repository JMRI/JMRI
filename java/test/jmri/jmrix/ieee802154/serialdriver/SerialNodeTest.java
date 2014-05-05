package jmri.jmrix.ieee802154.serialdriver;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SerialNodeTest.java
 *
 * Description:	    tests for the jmri.jmrix.ieee802154.serialdriver.SerialNode class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class SerialNodeTest extends TestCase {

    public void testCtor() {
        SerialNode m = new SerialNode();
        Assert.assertNotNull("exists",m);
    }

    public void testSetUserAddress() {
       // test the code to set the User address
       SerialNode node = new SerialNode();
       byte uad[]={(byte)0x6D,(byte)0x97};
       node.setUserAddress(uad);
       Assert.assertEquals("Node user address high byte",uad[0],node.getUserAddress()[0]);
       Assert.assertEquals("Node user address low byte",uad[1],node.getUserAddress()[1]);
     }

    public void testSetGlobalAddress() {
       // test the code to set the User address
       SerialNode node = new SerialNode();
       byte gad[]={(byte)0x00,(byte)0x13,(byte)0xA2,(byte)0x00,(byte)0x40,(byte)0xA0,(byte)0x4D,(byte)0x2D};
       node.setGlobalAddress(gad);
       for(int i=0; i< gad.length;i ++)
          Assert.assertEquals("Node global address byte " + i,gad[i],node.getGlobalAddress()[i]);

     }


	// from here down is testing infrastructure

	public SerialNodeTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SerialNodeTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SerialNodeTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(SerialNodeTest.class.getName());

}
