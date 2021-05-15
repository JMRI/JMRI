package jmri.jmrit.logixng;

import java.util.Locale;

import jmri.*;
import jmri.jmrit.logixng.Base.Lock;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG_Manager
 *
 * @author Daniel Bergqvist 2020
 */
public class LogixNG_ManagerTest {

    private void testSystemNameFormat(String prefix) {
        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"123"));

        // This is not valid since the dollar sign is missing after the prefix
        Assert.assertEquals("Validation is not correct",
                Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"Hello World"));

        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":AUTO:123"));

        // This is not valid since :AUTO: names must be digits only after :AUTO:
        Assert.assertEquals("Validation is not correct",
                Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":AUTO:Hello"));

        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"$123"));

        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"$Hello World"));

        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JMRI:Signal Mast System"));

        // This is invalid since JMRI is misspelled
        Assert.assertEquals("Validation is not correct",
                Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JRMI:Signal Mast System"));

        // Validation is correct
        Assert.assertEquals("Validation is correct",
                Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JMRI-LIB:Track Warrant System"));

        // This is invalid since JMRI is misspelled
        Assert.assertEquals("Validation is not correct",
                Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JRMI-LIB:Signal Mast System"));
    }

    private void testValidSystemNameFormat(String prefix) {
        testSystemNameFormat(prefix);         // LogixNG
        testSystemNameFormat(prefix+"C");     // ConditionalNG
        testSystemNameFormat(prefix+"AA");    // Analog Action
        testSystemNameFormat(prefix+"AE");    // Analog Expression
        testSystemNameFormat(prefix+"DA");    // Digital Action
        testSystemNameFormat(prefix+"DE");    // Digital Expression
        testSystemNameFormat(prefix+"SA");    // String Action
        testSystemNameFormat(prefix+"SE");    // String Expression
    }

    @Test
    public void testValidSystemNameFormat() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        testValidSystemNameFormat("IQ");
        testValidSystemNameFormat("I2Q");
        testValidSystemNameFormat("PQ");
        testValidSystemNameFormat("P55Q");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertEquals("beanType is correct", "ConditionalNG", conditionalNG.getBeanType());
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

}
