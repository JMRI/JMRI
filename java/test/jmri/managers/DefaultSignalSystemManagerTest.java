package jmri.managers;

import org.apache.log4j.Logger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 Tests for the jmri.managers.InternalTurnoutManager class.
 * @author	Bob Jacobsen Copyright 2009
 */
public class DefaultSignalSystemManagerTest extends TestCase {

    public void testGetListOfNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        Assert.assertTrue(l.contains("basic"));
        Assert.assertTrue(l.contains("AAR-1946"));
        Assert.assertTrue(l.contains("SPTCO-1960"));
    }
    
    public void testLoadBasicAspects() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        d.makeBean("basic");
    }
    
    public void testLoad() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        d.load();
        Assert.assertTrue(d.getSystemNameList().size() >= 2);
    }
    
    // from here down is testing infrastructure

    public DefaultSignalSystemManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultSignalSystemManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultSignalSystemManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    static Logger log = Logger.getLogger(DefaultSignalSystemManagerTest.class.getName());

}
