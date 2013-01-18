// InstanceManagerTest.java

package jmri;

import jmri.managers.TurnoutManagerScaffold;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Test InstanceManager
 *
 * @author			Bob Jacobsen
 * @version $Revision$
 */
public class InstanceManagerTest extends TestCase implements InstanceManagerAutoDefault {

    public void testDefaultPowerManager() {
        PowerManager m = new PowerManagerScaffold();
        
        InstanceManager.setPowerManager(m);
        
        Assert.assertTrue("power manager present", InstanceManager.powerManagerInstance()==m);        
    }
    
    public void testSecondDefaultPowerManager() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();
        
        InstanceManager.setPowerManager(m1);
        InstanceManager.setPowerManager(m2);
        
        Assert.assertTrue("power manager present", InstanceManager.powerManagerInstance()==m2);        
    }
    
    public void testDefaultProgrammerManager() {
        ProgrammerManager m = new jmri.progdebugger.DebugProgrammerManager();
        
        InstanceManager.setProgrammerManager(m);
        
        Assert.assertTrue("programmer manager present", InstanceManager.programmerManagerInstance()==m);        
    }
    
    public void testSecondDefaultProgrammerManager() {
        ProgrammerManager m1 = new jmri.progdebugger.DebugProgrammerManager();
        ProgrammerManager m2 = new jmri.progdebugger.DebugProgrammerManager();
        
        InstanceManager.setProgrammerManager(m1);
        InstanceManager.setProgrammerManager(m2);
        
        Assert.assertTrue("2nd instance is default", InstanceManager.programmerManagerInstance()==m2);        
    }
    
    // Testing new load store
    public void testGenericStoreAndGet() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;
        
        InstanceManager.store(m1, PowerManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved same object", m1, m2);
    }
    
    public void testGenericStoreList() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = new PowerManagerScaffold();
        
        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(m2, PowerManager.class);

        Assert.assertEquals("list length", 2, 
                        InstanceManager.getList(PowerManager.class).size());
        Assert.assertEquals("retrieved 1st PowerManager", m1, 
                        InstanceManager.getList(PowerManager.class).get(0));
        Assert.assertEquals("retrieved 2nd PowerManager", m2, 
                        InstanceManager.getList(PowerManager.class).get(1));
    }
    
    public void testGenericStoreAndGetTwoDifferentTypes() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;
        TurnoutManager t1 = new TurnoutManagerScaffold();
        TurnoutManager t2;
        
        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.store(t1, TurnoutManager.class);
        m2 = InstanceManager.getDefault(PowerManager.class);
        t2 = InstanceManager.getDefault(TurnoutManager.class);

        Assert.assertEquals("retrieved same PowerManager", m1, m2);
        Assert.assertEquals("retrieved same TurnoutManager", t1, t2);
    }
    

    public void testGenericStoreAndReset() {
        PowerManager m1 = new PowerManagerScaffold();
        PowerManager m2 = null;
        
        InstanceManager.store(m1, PowerManager.class);
        InstanceManager.reset(PowerManager.class);
        m1 = new PowerManagerScaffold();
        InstanceManager.store(m1, PowerManager.class);
        
        m2 = InstanceManager.getDefault(PowerManager.class);

        Assert.assertEquals("retrieved second PowerManager", m1, m2);
    }
    
    public static class OkAutoCreate implements InstanceManagerAutoDefault {
        public OkAutoCreate() { System.out.println(); }
    }

    public void testAutoCreateOK() {
    
        OkAutoCreate obj = InstanceManager.getDefault(OkAutoCreate.class);
        Assert.assertNotNull(obj);
    }
    
    public class NoAutoCreate {}
    
    public void testAutoCreateNotOK() {
        NoAutoCreate obj = InstanceManager.getDefault(NoAutoCreate.class);
        Assert.assertNull(obj);
    }
    
    /**
     * Test of types that have defaults, even with
     * no system attached.
     */
    public void testAllDefaults() {
        Assert.assertNotNull(InstanceManager.sensorManagerInstance() );
        Assert.assertNotNull(InstanceManager.turnoutManagerInstance() );
        Assert.assertNotNull(InstanceManager.lightManagerInstance() );
        Assert.assertNotNull(InstanceManager.signalHeadManagerInstance() );
        Assert.assertNotNull(InstanceManager.signalMastManagerInstance() );
        Assert.assertNotNull(InstanceManager.signalSystemManagerInstance() );
        Assert.assertNotNull(InstanceManager.signalGroupManagerInstance() );
        Assert.assertNotNull(InstanceManager.blockManagerInstance() );
        Assert.assertNotNull(InstanceManager.oBlockManagerInstance() );
        Assert.assertNotNull(InstanceManager.warrantManagerInstance() );
        Assert.assertNotNull(InstanceManager.sectionManagerInstance() );
        Assert.assertNotNull(InstanceManager.transitManagerInstance() );
        Assert.assertNotNull(InstanceManager.routeManagerInstance() );
        Assert.assertNotNull(InstanceManager.layoutBlockManagerInstance() );
        Assert.assertNotNull(InstanceManager.conditionalManagerInstance() );
        Assert.assertNotNull(InstanceManager.logixManagerInstance() );
        Assert.assertNotNull(InstanceManager.timebaseInstance() );
        Assert.assertNotNull(InstanceManager.clockControlInstance() );
        Assert.assertNotNull(InstanceManager.signalGroupManagerInstance() );
        Assert.assertNotNull(InstanceManager.reporterManagerInstance() );
        Assert.assertNotNull(InstanceManager.catalogTreeManagerInstance() );
        Assert.assertNotNull(InstanceManager.memoryManagerInstance() );
        Assert.assertNotNull(InstanceManager.audioManagerInstance() );
        Assert.assertNotNull(InstanceManager.rosterIconFactoryInstance() ); 
    }
    
	// from here down is testing infrastructure
	public InstanceManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {InstanceManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(InstanceManagerTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp();
        resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    private void resetInstanceManager() {
        new jmri.InstanceManager(){
            protected void init() {
                super.init();
                root = this;
            }
        };
    }
    
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InstanceManagerTest.class.getName());

}
