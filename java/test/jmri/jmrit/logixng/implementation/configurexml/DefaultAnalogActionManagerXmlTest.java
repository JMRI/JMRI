package jmri.jmrit.logixng.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.actions.AnalogActionMemory;
import jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml;
import jmri.jmrit.logixng.implementation.DefaultAnalogActionManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultAnalogActionManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultAnalogActionManagerXml b = new DefaultAnalogActionManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        DefaultAnalogActionManagerXml b = new DefaultAnalogActionManagerXml();
        
        Element e = new Element("logixngAnalogExpressions");
        Element e2 = new Element("missing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot load class jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
/*        
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA1"));
        e2.addContent(new Element("maleSocket"));
        b.loadActions(e);
        
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA2"));
        e2.addContent(new Element("maleSocket"));
        b.loadActions(e);
        
        // Test trying to load a class with private constructor
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$PrivateConstructorXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
        
        // Test trying to load a class which throws an exception
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$ThrowExceptionXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/        
//        System.out.format("Class name: %s%n", PrivateConstructorXml.class.getName());
    }

    @Ignore("Cannot load xml configurator")
    @Test
    public void testStore() {
        DefaultAnalogActionManagerXml b = new DefaultAnalogActionManagerXml();
        
        // If parameter is null, nothing should happen
        b.store(null);
        
        // Test store a named bean that has no configurexml class
        AnalogActionManager manager = InstanceManager.getDefault(AnalogActionManager.class);
        manager.registerAction(new MyAnalogAction());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$MyAnalogAction");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$MyAnalogAction");
    }
    
    @Test
    public void testReplaceActionManagerWithoutConfigManager() {
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogActionManager.class));
            }

        }

        // register new one with InstanceManager
        MyManager pManager = new MyManager();
        InstanceManager.store(pManager, AnalogActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_ANALOG_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(AnalogActionManager.class)
                        instanceof MyManager);
        
        // Test replacing the manager
        DefaultAnalogActionManagerXml b = new DefaultAnalogActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(AnalogActionManager.class)
                        instanceof MyManager);
    }
    
//    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {
        
        JUnitUtil.initConfigureManager();
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogActionManager.class));
            }

        }

        // register new one with InstanceManager
        MyManager pManager = new MyManager();
        InstanceManager.store(pManager, AnalogActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_ANALOG_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(AnalogActionManager.class)
                        instanceof MyManager);
        
        // Test replacing the manager
        DefaultAnalogActionManagerXml b = new DefaultAnalogActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(AnalogActionManager.class)
                        instanceof MyManager);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyAnalogAction extends AnalogActionMemory {
        
        MyAnalogAction() {
            super("IQAA9999", null);
        }
        
    }
    
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends AnalogActionMemoryXml {
        private PrivateConstructorXml() {
        }
    }
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends AnalogActionMemoryXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }
    
    
    class MyManager extends DefaultAnalogActionManager {
        
        @Override
        protected void registerSelf() {
            // We don't want to save config for this class
        }
        
    }
    
}
