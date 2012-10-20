// ProxyLightManagerTest.java

package jmri.managers;

import java.beans.PropertyChangeListener;
import jmri.*;

import junit.framework.*;

/**
 * Test the ProxyReporterManager
 * @author			Bob Jacobsen    2003, 2006, 2008
 * @author			Mark Underwood  2012
 * @version	$Revision: 17977 $
 */

public class ProxyReporterManagerTest extends TestCase {

    public String getSystemName(int i) { return "IR"+i; }
    
    protected ReporterManager l = null;	// holds objects under test

    static protected boolean listenerResult = false;
    protected class Listen implements PropertyChangeListener {
	public void propertyChange(java.beans.PropertyChangeEvent e) {
	    listenerResult = true;
	}
    }

    public void testDispose(){
	l.dispose();  // all we're really doing here is making sure the method exists
    }
    
    public void testReporterPutGet() {
	// create
	Reporter t = l.newReporter(getSystemName(getNumToTest1()), "mine");
	// check
	Assert.assertTrue("real object returned ", t != null);
	Assert.assertTrue("user name correct ", t == l.getByUserName("mine"));
	Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    public void testDefaultSystemName() {
	// create
	Reporter t = l.provideReporter(""+getNumToTest3());
	// check
	Assert.assertTrue("real object returned ", t != null);
	t = l.getBySystemName(getSystemName(getNumToTest3()));
	Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest3())));
    }

    public void testSingleObject() {
	// test that you always get the same representation
	Reporter t1 = l.newReporter(getSystemName(getNumToTest1()), "mine");
	Assert.assertTrue("t1 real object returned ", t1 != null);
	Assert.assertTrue("same by user ", t1 == l.getByUserName("mine"));
	Assert.assertTrue("same by system ", t1 == l.getBySystemName(getSystemName(getNumToTest1())));
	
	Reporter t2 = l.newReporter(getSystemName(getNumToTest1()), "mine");
	Assert.assertTrue("t2 real object returned ", t2 != null);
	// check
	Assert.assertTrue("same new ", t1 == t2);
    }
    
    public void testMisses() {
	// try to get nonexistant lights
	Assert.assertTrue(null == l.getByUserName("foo"));
	Assert.assertTrue(null == l.getBySystemName("bar"));
    }
	
    public void testUpperLower() {
	Reporter t = l.provideReporter(""+getNumToTest2());
	String name = t.getSystemName();
	Assert.assertNull(l.getReporter(name.toLowerCase()));
    }

    public void testRename() {
	// get light
	Reporter t1 = l.newReporter(getSystemName(getNumToTest1()), "before");
	Assert.assertNotNull("t1 real object ", t1);
	t1.setUserName("after");
	Reporter t2 = l.getByUserName("after");
	Assert.assertEquals("same object", t1, t2);
	Assert.assertEquals("no old object", null, l.getByUserName("before"));
    }
	
    public void testTwoNames() {
	Reporter ir211 = l.provideReporter("LR211");
        Reporter lr211 = l.provideReporter("IR211");
       
        Assert.assertNotNull(ir211);
        Assert.assertNotNull(lr211);
        Assert.assertTrue(ir211 != lr211);
    }
    

    public void testDefaultNotInternal() {
        Reporter lut = l.provideReporter("211");
        
        Assert.assertNotNull(lut);
        Assert.assertEquals("IR211", lut.getSystemName());
    }
    
    public void testProvideUser() {
        Reporter l1 = l.provideReporter("211");
        l1.setUserName("user 1");
        Reporter l2 = l.provideReporter("user 1");
        Reporter l3 = l.getReporter("user 1");
        
        Assert.assertNotNull(l1);
        Assert.assertNotNull(l2);
        Assert.assertNotNull(l3);
        Assert.assertEquals(l1, l2);
        Assert.assertEquals(l3, l2);
        Assert.assertEquals(l1, l3);
        
        Reporter l4 = l.getReporter("JLuser 1");
        Assert.assertNull(l4);
    }
    
    /**
     * Number of light to test.  
     * Made a separate method so it can be overridden in 
     * subclasses that do or don't support various numbers
     */
    protected int getNumToTest1() { return 9; }
    protected int getNumToTest2() { return 7; }
    protected int getNumToTest3() { return 5; }
    
    // from here down is testing infrastructure

    public ProxyReporterManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ProxyReporterManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ProxyReporterManagerTest.class);
        return suite;
    }
    
    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        // create and register the manager object
        l = InstanceManager.reporterManagerInstance();
    }
    @Override
	protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ProxyReporterManagerTest.class.getName());
    
}
