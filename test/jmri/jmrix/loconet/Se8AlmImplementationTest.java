
package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.TestCase;


public class Se8AlmImplementationTest extends TestCase {

    public Se8AlmImplementationTest(String s) {
	    super(s);
    }

    public void testRW() {
        Se8AlmImplementation alm = new Se8AlmImplementation(4, true);
        alm.setACon(3, 4);
        Assert.assertEquals("ACon", 4, alm.getACon(3));
        
    }
    
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    LocoNetInterfaceScaffold lnis;
    protected void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        log4jfixtureInst.setUp();
    }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
