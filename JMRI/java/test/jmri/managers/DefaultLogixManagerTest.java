package jmri.managers;

import jmri.Logix;
import jmri.LogixManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.managers.DefaultLogixManager class.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixManagerTest extends TestCase {

    public void testCtor() {
        new DefaultLogixManager();
    }

    public void testCreateForms() {
        LogixManager m = new DefaultLogixManager();
        
        Logix l1 = m.createNewLogix("User name 1");
        Logix l2 = m.createNewLogix("User name 2");

        Assert.assertNotNull(m.getByUserName("User name 1"));
        Assert.assertNotNull(m.getByUserName("User name 2"));
        
        Assert.assertTrue(l1 != l2);
        Assert.assertTrue(! l1.equals(l2));
        
        Assert.assertNotNull(m.getBySystemName(l1.getSystemName()));
        Assert.assertNotNull(m.getBySystemName(l2.getSystemName()));

        Logix l3 = m.createNewLogix("IX03", "User name 3");

        Assert.assertTrue(l1 != l3);
        Assert.assertTrue(l2 != l3);
        Assert.assertTrue(! l1.equals(l3));
        Assert.assertTrue(! l2.equals(l3));

        // test of some fails
        Assert.assertNull(m.createNewLogix(l1.getUserName()));
        Assert.assertNull(m.createNewLogix(l1.getSystemName(),""));  
    }

    public void testEmptyUserName() {
        LogixManager m = new DefaultLogixManager();
        
        Logix l1 = m.createNewLogix("IX01", "");
        Logix l2 = m.createNewLogix("IX02", "");
        
        Assert.assertTrue(l1 != l2);
        Assert.assertTrue(! l1.equals(l2));
        
        Assert.assertNotNull(m.getBySystemName(l1.getSystemName()));
        Assert.assertNotNull(m.getBySystemName(l2.getSystemName()));

        m.createNewLogix("IX03", "User name 3");
        
        // test of some fails
        Assert.assertNull(m.createNewLogix(l1.getSystemName(),""));      
    }

    // from here down is testing infrastructure
    public DefaultLogixManagerTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DefaultLogixManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultLogixManagerTest.class);
        return suite;
    }

}
