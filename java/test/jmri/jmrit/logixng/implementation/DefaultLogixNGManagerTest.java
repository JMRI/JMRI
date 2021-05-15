package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.actions.DigitalMany;
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

    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        try {
            DigitalActionManager digitalActionManager =
                    InstanceManager.getDefault(DigitalActionManager.class);

            FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
            MaleDigitalActionSocket actionManySocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionManySocket);
//            femaleSocket.setLock(Base.Lock.HARD_LOCK);

            femaleSocket = actionManySocket.getChild(0);
            MaleDigitalActionSocket actionIfThenSocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionIfThenSocket);
        } catch (SocketAlreadyConnectedException e) {
            // This should never be able to happen.
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testSetupInitialConditionalNGTree() {
        // Correct system name
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("IQ1", "Some name");
        Assert.assertNotNull("exists", logixNG);

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertNotNull("exists", conditionalNG);
        setupInitialConditionalNGTree(conditionalNG);

        FemaleSocket child = conditionalNG.getChild(0);
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalActionSocket",
                child.getClass().getName());
        MaleSocket maleSocket = child.getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultMaleDigitalActionSocket",
                maleSocket.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "Many",
                maleSocket.getLongDescription());
        MaleSocket maleSocket2 = maleSocket.getChild(0).getConnectedSocket();
        Assert.assertEquals("action is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultMaleDigitalActionSocket",
                maleSocket2.getClass().getName());
        Assert.assertEquals("action is of correct class",
                "If Then Else. Execute on change",
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
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
