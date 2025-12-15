package jmri.jmrit.logixng.implementation.configurexml;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.expressions.configurexml.ExpressionTurnoutXml;
import jmri.jmrit.logixng.implementation.DefaultDigitalExpressionManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.jdom2.Element;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class DefaultDigitalExpressionManagerXmlTest {

    @Test
    public void testCTor() {
        DefaultDigitalExpressionManagerXml b = new DefaultDigitalExpressionManagerXml();
        assertNotNull( b, "exists");
    }

    @Test
    public void testLoad() {
        DefaultDigitalExpressionManagerXml b = new DefaultDigitalExpressionManagerXml();

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
        e2.setAttribute("class", "jmri.jmrit.logixng.expressions.configurexml.ExpressionTurnoutXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDE1"));
        e2.addContent(new Element("maleSocket"));
        b.loadExpressions(e);

        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.expressions.configurexml.ExpressionTurnoutXml");
        e.addContent(e2);
        e2.addContent(new Element("systemName").addContent("IQDE2"));
        e2.addContent(new Element("maleSocket"));
        b.loadExpressions(e);

        // Test trying to load a class with private constructor
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultDigitalExpressionManagerXmlTest$PrivateConstructorXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadExpressions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");

        // Test trying to load a class which throws an exception
        e = new Element("logixngAnalogExpressions");
        e2 = new Element("existing_class");
        e2.setAttribute("class", "jmri.jmrit.logixng.implementation.configurexml.DefaultDigitalExpressionManagerXmlTest$ThrowExceptionXml");
        e2.addContent(new Element("maleSocket"));
        e.addContent(e2);
        b.loadExpressions(e);
        JUnitAppender.assertErrorMessage("cannot create constructor");
*/
//        System.out.format("Class name: %s%n", PrivateConstructorXml.class.getName());
    }

    @Disabled("Cannot load xml configurator")
    @Test
    public void testStore() {
        DefaultDigitalExpressionManagerXml b = new DefaultDigitalExpressionManagerXml();

        // If parameter is null, nothing should happen
        b.store(null);

        // Test store a named bean that has no configurexml class
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        manager.registerExpression(new DefaultDigitalExpressionManagerXmlTest.MyDigitalExpression());
        b.store(manager);
        JUnitAppender.assertErrorMessage("Cannot load configuration adapter for jmri.jmrit.logixng.implementation.configurexml.DefaultDigitalExpressionManagerXmlTest$MyDigitalExpression");
        JUnitAppender.assertErrorMessage("Cannot store configuration for jmri.jmrit.logixng.implementation.configurexml.DefaultDigitalExpressionManagerXmlTest$MyDigitalExpression");
    }

    @Test
    public void testReplaceActionManagerWithoutConfigManager() {

        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalExpressionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalExpressionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalExpressionManagerXmlTest.MyManager pManager = new DefaultDigitalExpressionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalExpressionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_EXPRESSIONS);
        }

        assertInstanceOf( DefaultDigitalExpressionManagerXmlTest.MyManager.class,
                InstanceManager.getDefault(DigitalExpressionManager.class),
                "manager is a MyManager");

        // Test replacing the manager
        DefaultDigitalExpressionManagerXml b = new DefaultDigitalExpressionManagerXml();
        b.replaceExpressionManager();

        assertFalse( InstanceManager.getDefault(DigitalExpressionManager.class)
            instanceof DefaultDigitalExpressionManagerXmlTest.MyManager,
                "manager is not a MyManager");
    }

//    @Disabled("When debug is enabled, jmri.configurexml.ConfigXmlManager.registerConfig checks if the manager has a XML class, which our fake manager doesn't have")
    @Test
    public void testReplaceActionManagerWithConfigManager() {

        JUnitUtil.initConfigureManager();

        // if old manager exists, remove it from configuration process
        if (InstanceManager.getNullableDefault(jmri.jmrit.logixng.DigitalExpressionManager.class) != null) {
            ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cmOD != null) {
                cmOD.deregister(InstanceManager.getDefault(jmri.jmrit.logixng.DigitalExpressionManager.class));
            }

        }

        // register new one with InstanceManager
        DefaultDigitalExpressionManagerXmlTest.MyManager pManager = new DefaultDigitalExpressionManagerXmlTest.MyManager();
        InstanceManager.store(pManager, DigitalExpressionManager.class);
        // register new one for configuration
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.registerConfig(pManager, jmri.Manager.LOGIXNG_DIGITAL_EXPRESSIONS);
        }

        assertInstanceOf( DefaultDigitalExpressionManagerXmlTest.MyManager.class,
            InstanceManager.getDefault(DigitalExpressionManager.class),
                "manager is a MyManager");

        // Test replacing the manager
        DefaultDigitalExpressionManagerXml b = new DefaultDigitalExpressionManagerXml();
        b.replaceExpressionManager();

        assertFalse( InstanceManager.getDefault(DigitalExpressionManager.class)
            instanceof DefaultDigitalExpressionManagerXmlTest.MyManager,
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



    private static class MyDigitalExpression extends ExpressionTurnout {

        MyDigitalExpression() {
            super("IQDE9999", null);
        }

    }


    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    static class PrivateConstructorXml extends ExpressionTurnoutXml {
        private PrivateConstructorXml() {
        }
    }

    // This class is loaded by reflection. The class cannot be private since
    // Spotbugs will in that case flag it as "is never used locally"
    static class ThrowExceptionXml extends ExpressionTurnoutXml {
        @Override
        public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
            throw new JmriConfigureXmlException();
        }
    }


    static class MyManager extends DefaultDigitalExpressionManager {
    }

}
