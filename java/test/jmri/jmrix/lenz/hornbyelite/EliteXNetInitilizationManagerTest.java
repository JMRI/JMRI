package jmri.jmrix.lenz.hornbyelite;

import org.apache.log4j.Logger;
import jmri.jmrix.lenz.hornbyelite.HornbyEliteCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetListenerScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * EliteXNetInitilizationManagerTest.java
 *
 * Description:	    tests for the jmri.jmrix.lenz.EliteXNetInitilizationManager class
 * @author			Paul Bender
 * @version         $Revision$
 */
public class EliteXNetInitilizationManagerTest extends TestCase {

    public void testCtor() {

// infrastructure objects
                XNetInterfaceScaffold t = new XNetInterfaceScaffold(new HornbyEliteCommandStation());
                XNetListenerScaffold l = new XNetListenerScaffold();

        XNetSystemConnectionMemo memo=new XNetSystemConnectionMemo(t);

        EliteXNetInitilizationManager m = new EliteXNetInitilizationManager(memo){
            protected int getInitTimeout() {
                return 50;   // shorten, because this will fail & delay test
            }  
        };
        Assert.assertNotNull("exists", t);
        Assert.assertNotNull("exists", l);
        Assert.assertNotNull("exists", m);
        Assert.assertNotNull("exists", memo);
    }

	// from here down is testing infrastructure
        public EliteXNetInitilizationManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", EliteXNetInitilizationManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(EliteXNetInitilizationManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
    }
    protected void tearDown() throws Exception { 
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }

    static Logger log = Logger.getLogger(EliteXNetInitilizationManagerTest.class.getName());

}
