package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultLogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class DefaultLogixNGManagerTest {

    @Test
    public void testManager() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);
        
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getBeanTypeHandled() is correct", Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled());
        Assert.assertEquals("getSystemPrefix() is correct", "I", manager.getSystemPrefix());
        Assert.assertEquals("typeLetter() is correct", 'Q', manager.typeLetter());
        
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        Assert.assertEquals("getXMLOrder() is correct", Manager.LOGIXNGS, manager.getXMLOrder());
        
        Assert.assertEquals("bean type is correct", Bundle.getMessage("BeanNameLogixNG"), manager.getBeanTypeHandled(false));
        Assert.assertEquals("bean type is correct", Bundle.getMessage("BeanNameLogixNGs"), manager.getBeanTypeHandled(true));
    }
    
    @Test
    public void testValidSystemNameFormat() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);
        
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat(""));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("iQ1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("Iq1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("iq1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ:AUTO:1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1A"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQA"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1 "));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.VALID, manager.validSystemNameFormat("IQ11111"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1AA"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQ1X"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1"));
        Assert.assertEquals("validSystemNameFormat()", Manager.NameValidity.INVALID, manager.validSystemNameFormat("IQX1X"));
    }
    
    @Test
    public void testCreateNewLogixNG() {
        LogixNG_Manager manager = InstanceManager.getDefault(LogixNG_Manager.class);
        
        // Correct system name
        LogixNG logixNG = manager.createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);
        LogixNG logixNG_2 = manager.getLogixNG("IQ1");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getBySystemName("IQ1");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getLogixNG("Some name");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getByUserName("Some name");
        Assert.assertEquals("logixNGs are the same", logixNG, logixNG_2);
        logixNG_2 = manager.getLogixNG("Some other name");
        Assert.assertNull("logixNG not found", logixNG_2);
        
        // Correct system name. Neither system name or user name exists already
        logixNG = manager.createLogixNG("IQ2", "Other LogixNG");
        Assert.assertNotNull("exists", logixNG);
        
        // System name exists
        logixNG = manager.createLogixNG("IQ1", "Another name");
        Assert.assertNull("cannot create new", logixNG);
        
        // User name exists
        logixNG = manager.createLogixNG("IQ3", "Other LogixNG");
        Assert.assertNull("cannot create new", logixNG);
        
        // Bad system name
        boolean thrown = false;
        try {
            manager.createLogixNG("IQ4A", "Different name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        
        // Create LogixNG with user name
        logixNG = manager.createLogixNG("Only user name");
        Assert.assertNotNull("exists", logixNG);
        Assert.assertEquals("user name is correct", "Only user name", logixNG.getUserName());
    }
    
    @Test
    public void testSetupInitialConditionalNGTree() {
        // Correct system name
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);
        
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        Assert.assertNotNull("exists", conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        InstanceManager.getDefault(ConditionalNG_Manager.class)
                .setupInitialConditionalNGTree(conditionalNG);
        
        FemaleSocket child = conditionalNG.getChild(0);
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                child.getClass().getName());
        MaleSocket maleSocket = child.getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalActionSocket",
                maleSocket.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "Many",
                maleSocket.getLongDescription());
        MaleSocket maleSocket2 = maleSocket.getChild(0).getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultMaleDigitalActionSocket",
                maleSocket2.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "If E then A1 else A2",
                maleSocket2.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
