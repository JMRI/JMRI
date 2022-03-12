package jmri.jmrit.logixng.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.actions.StringActionMemory;
import jmri.jmrit.logixng.actions.configurexml.StringActionMemoryXml;
import jmri.jmrit.logixng.implementation.DefaultStringActionManager;
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
public class DefaultStringActionManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultStringActionManagerXml b = new DefaultStringActionManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        DefaultStringActionManagerXml b = new DefaultStringActionManagerXml();
        
        Element e = new Element("logixngStringExpressions");
        Element e2 = new Element("missing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot load class jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
/*        
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("logixngStringExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.StringActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQSA1"));
        e2.addContent(new Element("maleSocket"));
        b.loadActions(e);
        
        e = new Element("logixngStringExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.StringActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQSA2"));
        e2.addContent(new Element("maleSocket"));
        b.loadActions(e);
        
        // Test trying to load a class with private constructor
        e = new Element("logixngStringExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultStringActionManagerXmlTest$PrivateConstructorXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadActions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
        
        // Test trying to load a class which throws an exception
        e = new Element("logixngStringExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultStringActionManagerXmlTest$ThrowExceptionXml");
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
        DefaultStringActionManagerXml b = new DefaultStringActionManagerXml();
        
        // If parameter is null, nothing should happen
        b.store(null);
        
        // Test store a named bean that has no configurexml class
        StringActionManager manager = InstanceManager.getDefault(StringActionManager.class);
        manager.registerAction(new DefaultStringActionManagerXmlTest.MyStringAction());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.implementation.configurexml.DefaultStringActionManagerXmlTest$MyStringAction");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.implementation.configurexml.DefaultStringActionManagerXmlTest$MyStringAction");
    }
    
    @Test
    public void testReplaceActionManagerWithoutConfigManager() {
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.StringActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.StringActionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultStringActionManagerXmlTest.MyManager pManager = new DefaultStringActionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, StringActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_STRING_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(StringActionManager.class)
                        instanceof DefaultStringActionManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultStringActionManagerXml b = new DefaultStringActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(StringActionManager.class)
                        instanceof DefaultStringActionManagerXmlTest.MyManager);
    }
    
//    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {
        
        JUnitUtil.initConfigureManager();
        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.StringActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.StringActionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultStringActionManagerXmlTest.MyManager pManager = new DefaultStringActionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, StringActionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_STRING_ACTIONS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(StringActionManager.class)
                        instanceof DefaultStringActionManagerXmlTest.MyManager);
        
        // Test replacing the manager
        DefaultStringActionManagerXml b = new DefaultStringActionManagerXml();
        b.replaceActionManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(StringActionManager.class)
                        instanceof DefaultStringActionManagerXmlTest.MyManager);
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
    
    
    
    private class MyStringAction extends StringActionMemory {
        
        MyStringAction() {
            super("IQSA9999", null);
        }
        
    }
    
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends StringActionMemoryXml {
        private PrivateConstructorXml() {
        }
    }
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends StringActionMemoryXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }
    
    
    class MyManager extends DefaultStringActionManager {
    }
    
}
