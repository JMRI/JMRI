package jmri.managers;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.Logix;
import jmri.LogixManager;

/**
 * Tests for the jmri.managers.DefaultLogixManager class.
 *
 * @author	Bob Jacobsen Copyright (C) 2015
 */
public class DefaultLogixManagerTest {

    @Test
    public void testCtor() {
        new DefaultLogixManager();
    }

    @Test
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

    @Test
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

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
