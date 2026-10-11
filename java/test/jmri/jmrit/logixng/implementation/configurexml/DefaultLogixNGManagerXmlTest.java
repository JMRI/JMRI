package jmri.jmrit.logixng.implementation.configurexml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.implementation.DefaultLogixNGManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test DefaultLogixNGManagerXml
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultLogixNGManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultLogixNGManagerXml b = new DefaultLogixNGManagerXml();
        assertNotNull( b, "exists");
    }

    @Disabled("Fix later")
    @Test
    public void testLoad() {
        DefaultLogixNGManagerXml b = new DefaultLogixNGManagerXml();
        assertNotNull( b, "exists");

        // Test loading a logixng without system name
        Element e = new Element("LogixNGs");
        Element e2 = new Element("LogixNG");
        e.addContent(e2);
        b.loadLogixNGs(e);
        JUnitAppender.assertWarnMessage("unexpected null in systemName [Element: <LogixNG/>]");


        // Test load LogixNG without attribute "enable"
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        e2.addContent(new Element("systemName").addContent("IQ1001"));
        Element eConditionals = new Element("conditionalngs");
        e2.addContent(eConditionals);
        e.addContent(e2);
        b.loadLogixNGs(e);

        // Test load LogixNG with bad conditionalng (no systemName in the conditionalNG)
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        e2.addContent(new Element("systemName").addContent("IQ1002"));
        eConditionals = new Element("conditionalngs");
        Element eConditional = new Element("conditionalng");
        eConditionals.addContent(eConditional);
        e2.addContent(eConditionals);
        e.addContent(e2);
        b.loadLogixNGs(e);
//        JUnitAppender.assertWarnMessage("unexpected null in systemName [Element: <conditionalng/>]");
//        JUnitAppender.assertErrorMessage("exception thrown");

        // Test loading a LogixNG that already exists
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        String systemName = "IQ1001";
        assertNotNull( InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName(systemName),
            "bean exists");
        e2.addContent(new Element("systemName").addContent(systemName));
        e.addContent(e2);
        b.loadLogixNGs(e);

        // Test load LogixNG with attribute "enable" as empty string
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        e2.addContent(new Element("systemName").addContent("IQ1003"));
        eConditionals = new Element("conditionalngs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "");
        e.addContent(e2);
        b.loadLogixNGs(e);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1003");
        assertNotNull( logixNG, "bean is not null");
        assertFalse( logixNG.isEnabled(), "bean is not enabled");

        // Test load LogixNG with attribute "enable" as invalid value
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        e2.addContent(new Element("systemName").addContent("IQ1004"));
        eConditionals = new Element("conditionalngs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "invalid value");
        e.addContent(e2);
        b.loadLogixNGs(e);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1004");
        assertNotNull( logixNG, "bean is not null");
        assertFalse( logixNG.isEnabled(), "bean is not enabled");

        // Test load LogixNG with attribute "enable" as yes
        e = new Element("LogixNGs");
        e2 = new Element("LogixNG");
        e2.addContent(new Element("systemName").addContent("IQ1005"));
        eConditionals = new Element("conditionalngs");
        e2.addContent(eConditionals);
        e2.setAttribute("enabled", "yes");
        e.addContent(e2);
        b.loadLogixNGs(e);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1005");
        assertNotNull( logixNG, "bean is not null");
        assertTrue( logixNG.isEnabled(), "bean is enabled");

/*
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("LogixNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA1"));
        b.loadLogixNGs(e);

        e = new Element("LogixNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.actions.configurexml.AnalogActionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAA2"));
        b.loadLogixNGs(e);
/*
        // Test trying to load a class with private constructor
        e = new Element("LogixNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$PrivateConstructorXml");
        e.addContent(e2);
        b.loadLogixNGs(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");

        // Test trying to load a class which throws an exception
        e = new Element("LogixNGs");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogActionManagerXmlTest$ThrowExceptionXml");
        e.addContent(e2);
        b.loadLogixNGs(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/
    }

    @Test
    public void testStore() {
        DefaultLogixNGManagerXml b = new DefaultLogixNGManagerXml();
        assertNotNull( b, "exists");
        // Calling store() with null is OK.
        b.store((Object)null);
    }

    @Disabled("LogixNG thread is already started so this test fails")
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
        InstanceManager.store(pManager, LogixNG_Manager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNGS);
        }

        assertInstanceOf( MyManager.class,
            InstanceManager.getDefault(LogixNG_Manager.class),
                "manager is a MyManager");

        // Test replacing the manager
        DefaultLogixNGManagerXml b = new DefaultLogixNGManagerXml();
        b.replaceLogixNGManager();

        assertFalse( InstanceManager.getDefault(LogixNG_Manager.class)
            instanceof MyManager,
                "manager is not a MyManager");

        // Test replace the manager when where is no manager registered yet
        InstanceManager.deregister(
                InstanceManager.getDefault(LogixNG_Manager.class),
                LogixNG_Manager.class);

        assertNotNull( InstanceManager.getDefault(LogixNG_Manager.class),
            "manager is not null");
    }

    @Disabled("LogixNG thread is already started so this test fails")
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
        InstanceManager.store(pManager, LogixNG_Manager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNGS);
        }

        assertInstanceOf( MyManager.class,
                InstanceManager.getDefault(LogixNG_Manager.class),
                "manager is a MyManager");

        // Test replacing the manager
        DefaultLogixNGManagerXml b = new DefaultLogixNGManagerXml();
        b.replaceLogixNGManager();

        assertFalse( InstanceManager.getDefault(LogixNG_Manager.class)
            instanceof MyManager,
                "manager is not a MyManager");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

/*
    private class MyLogixNG extends jmri.jmrit.logixng.implementation.DefaultLogixNG {

        MyLogixNG() {
            super("IQ9999");
        }

    }

/*
    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends DefaultLogixNGXml {
        private PrivateConstructorXml() {
        }
    }

    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends DefaultLogixNGXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }
*/
    static class MyManager extends DefaultLogixNGManager {
    }

}
