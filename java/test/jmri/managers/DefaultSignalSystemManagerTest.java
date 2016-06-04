package jmri.managers;

import jmri.implementation.SignalSystemTestUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.managers.InternalTurnoutManager class.
 *
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

    public void testSearchOrder() throws Exception {
        try {  // need try-finally to ensure junk deleted from user area
            SignalSystemTestUtil.createMockSystem();

            // check that mock (test directory) system is present
            DefaultSignalSystemManager d = new DefaultSignalSystemManager();
            java.util.List<String> l = d.getListOfNames();
            Assert.assertTrue(l.contains(SignalSystemTestUtil.getMockSystemName()));

        } finally {
            SignalSystemTestUtil.deleteMockSystem();
        }
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

    public void testUniqueNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.size(); j++) {
                if ((i != j) && (l.get(i).equals(l.get(j)))) {
                    Assert.fail("Found " + l.get(i) + " at " + i + " and " + j);
                }
            }
        }
    }

    public void testUniqueSystemNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            jmri.SignalSystem si = d.getSystem(l.get(i));
            for (int j = 0; j < l.size(); j++) {
                jmri.SignalSystem sj = d.getSystem(l.get(j));
                if ((i != j) && (si.getSystemName().equals(sj.getSystemName()))) {
                    Assert.fail("Found system name " + si.getSystemName() + " at " + i + " and " + j);
                }
            }
        }
    }

    public void testUniqueUserNames() {
        DefaultSignalSystemManager d = new DefaultSignalSystemManager();
        java.util.List<String> l = d.getListOfNames();
        for (int i = 0; i < l.size(); i++) {
            jmri.SignalSystem si = d.getSystem(l.get(i));
            for (int j = 0; j < l.size(); j++) {
                jmri.SignalSystem sj = d.getSystem(l.get(j));
                if ((i != j) && (si.getUserName().equals(sj.getUserName()))) {
                    Assert.fail("Found user name " + si.getUserName() + " at " + i + " and " + j);
                }
            }
        }
    }

    // from here down is testing infrastructure
    public DefaultSignalSystemManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultSignalSystemManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
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

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
