package jmri.jmrit.logixng.implementation.configurexml;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.expressions.configurexml.AnalogExpressionMemoryXml;
import jmri.jmrit.logixng.implementation.DefaultAnalogExpressionManager;
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
public class DefaultAnalogExpressionManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultAnalogExpressionManagerXml b = new DefaultAnalogExpressionManagerXml();
        Assert.assertNotNull("exists", b);
    }

    @Test
    public void testLoad() {
        DefaultAnalogExpressionManagerXml b = new DefaultAnalogExpressionManagerXml();

        Element e = new Element("logixngAnalogExpressions");
        Element e2 = new Element("missing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadExpressions(e);
        JUnitAppender.assertErrorMessage("cannot load class jmri.jmrit.logixng.this.class.does.not.exist.TestClassXml");
/*
        // Test loading the same class twice, in order to check field "xmlClasses"
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.expressions.configurexml.AnalogExpressionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAE1"));
        e2.addContent(new Element("maleSocket"));
        b.loadExpressions(e);

        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.expressions.configurexml.AnalogExpressionMemoryXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQAE2"));
        e2.addContent(new Element("maleSocket"));
        b.loadExpressions(e);

        // Test trying to load a class with private constructor
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogExpressionManagerXmlTest$PrivateConstructorXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadExpressions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");

        // Test trying to load a class which throws an exception
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogExpressionManagerXmlTest$ThrowExceptionXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadExpressions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/
//        System.out.format("Class name: %s%n", PrivateConstructorXml.class.getName());
    }

    @Ignore("Cannot load xml configurator")
    @Test
    public void testStore() {
        DefaultAnalogExpressionManagerXml b = new DefaultAnalogExpressionManagerXml();

        // If parameter is null, nothing should happen
        b.store(null);

        // Test store a named bean that has no configurexml class
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        manager.registerExpression(new DefaultAnalogExpressionManagerXmlTest.MyAnalogExpression());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogExpressionManagerXmlTest$MyAnalogExpression");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.implementation.configurexml.DefaultAnalogExpressionManagerXmlTest$MyAnalogExpression");
    }

    @Test
    public void testReplaceActionManagerWithoutConfigManager() {

        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogExpressionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogExpressionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultAnalogExpressionManagerXmlTest.MyManager pManager = new DefaultAnalogExpressionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, AnalogExpressionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_ANALOG_EXPRESSIONS);
        }

        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        instanceof DefaultAnalogExpressionManagerXmlTest.MyManager);

        // Test replacing the manager
        DefaultAnalogExpressionManagerXml b = new DefaultAnalogExpressionManagerXml();
        b.replaceExpressionManager();

        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        instanceof DefaultAnalogExpressionManagerXmlTest.MyManager);
    }

//    @Ignore("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {

        JUnitUtil.initConfigureManager();

        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.AnalogExpressionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.AnalogExpressionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultAnalogExpressionManagerXmlTest.MyManager pManager = new DefaultAnalogExpressionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, AnalogExpressionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_ANALOG_EXPRESSIONS);
        }

        Assert.assertTrue("manager is a MyManager",
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        instanceof DefaultAnalogExpressionManagerXmlTest.MyManager);

        // Test replacing the manager
        DefaultAnalogExpressionManagerXml b = new DefaultAnalogExpressionManagerXml();
        b.replaceExpressionManager();

        Assert.assertFalse("manager is not a MyManager",
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        instanceof DefaultAnalogExpressionManagerXmlTest.MyManager);
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



    private class MyAnalogExpression extends AnalogExpressionMemory {

        MyAnalogExpression() {
            super("IQAE9999", null);
        }

    }


    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class PrivateConstructorXml extends AnalogExpressionMemoryXml {
        private PrivateConstructorXml() {
        }
    }

    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    class ThrowExceptionXml extends AnalogExpressionMemoryXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }


    class MyManager extends DefaultAnalogExpressionManager {
    }

}
