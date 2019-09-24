package jmri.jmrit.logixng.digital.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.digital.actions_with_change.OnChangeAction;
import jmri.jmrit.logixng.digital.actions_with_change.configurexml.OnChangeActionXml;
import jmri.jmrit.logixng.digital.implementation.DefaultDigitalActionWithChangeManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalActionWithChangeManager;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.configurexml.ActionTurnoutXml;

/**
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultDigitalActionWithChangeManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultDigitalActionWithChangeManagerXml b = new DefaultDigitalActionWithChangeManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        
        DefaultDigitalActionWithChangeManagerXml b = new DefaultDigitalActionWithChangeManagerXml();
        
        // Test the method load(Element element, Object o)
        b.load((Element)null, (Object)null);
        JUnitAppender.assertErrorMessage("Invalid method called");
        
        Element e = new Element("logixngDigitalExpressions");
        Element e2 = new Element("missing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot load class jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.logixemulator.actions.configurexml.ActionTurnoutXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDA1"));
        b.loadActions(e);
        
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.logixemulator.actions.configurexml.ActionTurnoutXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDA2"));
        b.loadActions(e);
        
        // Test trying to load a class with private constructor
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.logixemulator.implementation.configurexml.DefaultDigitalActionWithChangeManagerXmlTest$PrivateConstructorXml");
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
/*        
        // Test trying to load a class which throws an exception
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.logixemulator.implementation.configurexml.DefaultDigitalActionWithChangeManagerXmlTest$ThrowExceptionXml");
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/        
//        System.out.format("Class name: %s%n", PrivateConstructorXml.class.getName());
    }

    @Test
    public void testStore() {
        
        DefaultDigitalActionWithChangeManagerXml b = new DefaultDigitalActionWithChangeManagerXml();
        
        // If parameter is null, nothing should happen
        b.store(null);
        
        // Test store a named bean that has no configurexml class
        DigitalActionWithChangeManager manager = InstanceManager.getDefault(DigitalActionWithChangeManager.class);
        manager.registerAction(new MyDigitalActionWithChange());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.logixemulator.implementation.configurexml.DefaultDigitalActionWithChangeManagerXmlTest$MyDigitalActionWithChange");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.logixemulator.implementation.configurexml.DefaultDigitalActionWithChangeManagerXmlTest$MyDigitalActionWithChange");
    }
    
    @Test
    public void testReplaceActionManagerWithoutConfigManager() {
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalActionWithChangeManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalActionWithChangeManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalActionWithChangeManagerXmlTest.MyManager pManager = new DefaultDigitalActionWithChangeManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalActionWithChangeManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_ACTIONS_WITH_CHANGE);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                        instanceof DefaultDigitalActionWithChangeManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultDigitalActionWithChangeManagerXml b = new DefaultDigitalActionWithChangeManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                        instanceof DefaultDigitalActionWithChangeManagerXmlTest.MyManager);
    }
    
    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {
        
        JUnitUtil.initConfigureManager();
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalActionWithChangeManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalActionWithChangeManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalActionWithChangeManagerXmlTest.MyManager pManager = new DefaultDigitalActionWithChangeManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalActionWithChangeManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_ACTIONS_WITH_CHANGE);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                        instanceof DefaultDigitalActionWithChangeManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultDigitalActionWithChangeManagerXml b = new DefaultDigitalActionWithChangeManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(DigitalActionWithChangeManager.class)
                        instanceof DefaultDigitalActionWithChangeManagerXmlTest.MyManager);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyDigitalActionWithChange extends OnChangeAction {
        
        MyDigitalActionWithChange() {
            super("IQDA9999", null, OnChangeAction.ChangeType.CHANGE);
        }
        
    }
    
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends OnChangeActionXml {
        private PrivateConstructorXml() {
        }
    }
/*    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends OnChangeActionXml {
        @Override
        public boolean load(Element shared, Element perNode) {
            throw new JmriConfigureXmlException();
        }
    }
*/    
    
    class MyManager extends DefaultDigitalActionWithChangeManager {
        MyManager() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
    }
    
}
