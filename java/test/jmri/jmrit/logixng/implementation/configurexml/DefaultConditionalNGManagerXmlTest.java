package jmri.jmrit.logixng.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
// import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultConditionalNGManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultConditionalNGManagerXml b = new DefaultConditionalNGManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        DefaultConditionalNGManagerXml b = new DefaultConditionalNGManagerXml();
        Assert.assertNotNull("exists", b);
        
        // Test loading a conditionalng without system name
        Element e = new Element("ConditionalNGs");
        Element e2 = new Element("ConditionalNG");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        JUnitAppender.assertWarnMessage("unexpected null in systemName [Element: <ConditionalNG/>]");
        
        // Fix this later!!!
/***************************        
        // Test load ConditionalNG without attribute "enable"
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        e2.addContent(new Element("systemName").addContent("IQC1001"));
        Element eConditionals = new Element("ConditionalNGs");
        e2.addContent(eConditionals);
        e.addContent(e2);
        b.loadConditionalNGs(e);
        
        // Test load ConditionalNG with bad conditionalng (no systemName in the conditionalNG)
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        e2.addContent(new Element("systemName").addContent("IQC1002"));
        eConditionals = new Element("ConditionalNGs");
        Element eConditional = new Element("ConditionalNG");
        eConditionals.addContent(eConditional);
        e2.addContent(eConditionals);
        e.addContent(e2);
        b.loadConditionalNGs(e);
//        JUnitAppender.assertWarnMessage("unexpected null in systemName [Element: <ConditionalNG/>]");
//        JUnitAppender.assertErrorMessage("exception thrown");
        
        // Test loading a ConditionalNG that already exists
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        String systemName = "IQ1001";
        Assert.assertNotNull("bean exists",
                InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName(systemName));
        e2.addContent(new Element("systemName").addContent(systemName));
        e.addContent(e2);
        b.loadConditionalNGs(e);
        
        // Test load ConditionalNG with attribute "enable" as empty string
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        e2.addContent(new Element("systemName").addContent("IQC1003"));
        eConditionals = new Element("ConditionalNGs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC1003");
        Assert.assertNotNull("bean is not null", conditionalNG);
        Assert.assertFalse("bean is not enabled", conditionalNG.isEnabled());
        
        // Test load ConditionalNG with attribute "enable" as invalid value
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        e2.addContent(new Element("systemName").addContent("IQC1004"));
        eConditionals = new Element("ConditionalNGs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "invalid value");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC1004");
        Assert.assertNotNull("bean is not null", conditionalNG);
        Assert.assertFalse("bean is not enabled", conditionalNG.isEnabled());
        
        // Test load ConditionalNG with attribute "enable" as yes
        e = new Element("ConditionalNGs");
        e2 = new Element("ConditionalNG");
        e2.addContent(new Element("systemName").addContent("IQC1005"));
        eConditionals = new Element("ConditionalNGs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "yes");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).getBySystemName("IQC1005");
        Assert.assertNotNull("bean is not null", conditionalNG);
        Assert.assertTrue("bean is enabled", conditionalNG.isEnabled());
        
/*        
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("ConditionalNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA1"));
        b.loadConditionalNGs(e);
        
        e = new Element("ConditionalNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA2"));
        b.loadConditionalNGs(e);
/*        
        // Test trying to load a class with private constructor
        e = new Element("ConditionalNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$PrivateConstructorXml");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
        
        // Test trying to load a class which throws an exception
        e = new Element("ConditionalNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$ThrowExceptionXml");
        e.addContent(e2);
        b.loadConditionalNGs(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/
    }

    @Test
    public void testStore() {
        DefaultConditionalNGManagerXml b = new DefaultConditionalNGManagerXml();
        Assert.assertNotNull("exists", b);
        // Calling store() with null is OK.
        b.store((Object)null);
    }

    @Test
    public void testReplaceActionManagerWithoutConfigManager() {
/*        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogActionManager.class));
            }

        }
*/
        // register new one with InstanceManager
        MyManager pManager = new MyManager();
        InstanceManager.store(pManager, ConditionalNG_Manager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_CONDITIONALNGS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(ConditionalNG_Manager.class)
                        instanceof MyManager);
        
        // Test replacing the manager
        DefaultConditionalNGManagerXml b = new DefaultConditionalNGManagerXml();
        b.replaceConditionalNGManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(ConditionalNG_Manager.class)
                        instanceof MyManager);
        
        // Test replace the manager when where is no manager registered yet
        InstanceManager.deregister(
                InstanceManager.getDefault(ConditionalNG_Manager.class),
                ConditionalNG_Manager.class);
        
        Assert.assertNotNull("manager is not null",
                InstanceManager.getDefault(ConditionalNG_Manager.class));
    }
    
//    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {
        
        JUnitUtil.initConfigureManager();
/*        
        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogActionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogActionManager.class));
            }

        }
*/
        // register new one with InstanceManager
        MyManager pManager = new MyManager();
        InstanceManager.store(pManager, ConditionalNG_Manager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_CONDITIONALNGS);
        }
        
        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(ConditionalNG_Manager.class)
                        instanceof MyManager);
        
        // Test replacing the manager
        DefaultConditionalNGManagerXml b = new DefaultConditionalNGManagerXml();
        b.replaceConditionalNGManager();
        
        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(ConditionalNG_Manager.class)
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
    
/*    
    private class MyConditionalNG extends jmri.jmrit.logixng.implementation.DefaultConditionalNG {
        
        MyConditionalNG() {
            super("IQ9999");
        }
        
    }
    
/*    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends DefaultConditionalNGXml {
        private PrivateConstructorXml() {
        }
    }
    
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends DefaultConditionalNGXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }
*/    
    class MyManager extends DefaultConditionalNGManager {
    }
    
}
