package jmri.jmrit.withrottle;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.TestConsistManager;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of WiFiConsistFile
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiFiConsistFileTest extends TestCase {

    public void testCtor() {
        WiFiConsistManager man = new WiFiConsistManager();
        WiFiConsistFile panel = new WiFiConsistFile(man);
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public WiFiConsistFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", WiFiConsistFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(WiFiConsistFileTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.tearDown();
    }
}
