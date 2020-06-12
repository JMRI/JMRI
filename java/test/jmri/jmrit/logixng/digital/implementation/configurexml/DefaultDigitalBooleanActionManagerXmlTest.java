package jmri.jmrit.logixng.digital.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.digital.boolean_actions.OnChange;
import jmri.jmrit.logixng.digital.boolean_actions.configurexml.OnChangeActionXml;
import jmri.jmrit.logixng.digital.implementation.DefaultDigitalBooleanActionManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import jmri.jmrit.logixng.digital.actions.ActionTurnout;
import jmri.jmrit.logixng.digital.actions.configurexml.ActionTurnoutXml;
import jmri.jmrit.logixng.DigitalBooleanActionManager;

/**
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultDigitalBooleanActionManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultDigitalBooleanActionManagerXml b = new DefaultDigitalBooleanActionManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        
        DefaultDigitalBooleanActionManagerXml b = new DefaultDigitalBooleanActionManagerXml();
        
        Element e = new Element("logixngDigitalExpressions");
        Element e2 = new Element("missing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot load class jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.digital.boolean_actions.configurexml.OnChangeActionXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDB1"));
        Element socketElement = new Element("socket");
        e2.addContent(socketElement);
        socketElement.addContent(new Element("socketName").addContent("A"));
        socketElement.addContent(new Element("systemName").addContent("IQDA2"));
        e2.setAttribute("whichChange", "CHANGE_TO_TRUE");
        b.loadActions(e);
        
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.digital.boolean_actions.configurexml.OnChangeActionXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDB2"));
        socketElement = new Element("socket");
        e2.addContent(socketElement);
        socketElement.addContent(new Element("socketName").addContent("A"));
        socketElement.addContent(new Element("systemName").addContent("IQDA2"));
        e2.setAttribute("whichChange", "CHANGE_TO_TRUE");
        b.loadActions(e);
        
        // Test trying to load a class with private constructor
        e = new Element("logixngDigitalExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.digital.implementation.configurexml.DefaultDigitalBooleanActionManagerXmlTest$PrivateConstructorXml");
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
        
        DefaultDigitalBooleanActionManagerXml b = new DefaultDigitalBooleanActionManagerXml();
        
        // If parameter is null, nothing should happen
        b.store(null);
        
        // Test store a named bean that has no configurexml class
        DigitalBooleanActionManager manager = InstanceManager.getDefault(DigitalBooleanActionManager.class);
        manager.registerAction(new MyDigitalBooleanAction());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.digital.implementation.configurexml.DefaultDigitalBooleanActionManagerXmlTest$MyDigitalBooleanAction");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.digital.implementation.configurexml.DefaultDigitalBooleanActionManagerXmlTest$MyDigitalBooleanAction");
    }
    
    @Test
    public void testReplaceActionManagerWithoutConfigManager() {
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalBooleanActionManagerXmlTest.MyManager pManager = new DefaultDigitalBooleanActionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalBooleanActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_BOOLEAN_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        instanceof DefaultDigitalBooleanActionManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultDigitalBooleanActionManagerXml b = new DefaultDigitalBooleanActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        instanceof DefaultDigitalBooleanActionManagerXmlTest.MyManager);
    }
    
    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {
        
        JUnitUtil.initConfigureManager();
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalBooleanActionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalBooleanActionManagerXmlTest.MyManager pManager = new DefaultDigitalBooleanActionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalBooleanActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_BOOLEAN_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        instanceof DefaultDigitalBooleanActionManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultDigitalBooleanActionManagerXml b = new DefaultDigitalBooleanActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(DigitalBooleanActionManager.class)
                        instanceof DefaultDigitalBooleanActionManagerXmlTest.MyManager);
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
    
    
    
    private class MyDigitalBooleanAction extends OnChange {
        
        MyDigitalBooleanAction() {
            super("IQDB9999", null, OnChange.ChangeType.CHANGE);
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
    
    class MyManager extends DefaultDigitalBooleanActionManager {
        MyManager() {
            super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
    }
    
}
