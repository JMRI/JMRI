package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test LogixNG_Manager
 *
 * @author Daniel Bergqvist 2020
 */
public class LogixNG_ManagerTest {

    private void testSystemNameFormat(String prefix) {
        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"123"),
                "Validation is correct");

        // This is not valid since the dollar sign is missing after the prefix
        assertEquals( Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"Hello World"),
                "Validation is not correct");

        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":AUTO:123"),
                "Validation is correct");

        // This is not valid since :AUTO: names must be digits only after :AUTO:
        assertEquals( Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":AUTO:Hello"),
                "Validation is not correct");

        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"$123"),
                "Validation is correct");

        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+"$Hello World"),
                "Validation is correct");

        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JMRI:Signal Mast System"),
                "Validation is correct");

        // This is invalid since JMRI is misspelled
        assertEquals( Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JRMI:Signal Mast System"),
                "Validation is not correct");

        // Validation is correct
        assertEquals( Manager.NameValidity.VALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JMRI-LIB:Track Warrant System"),
                "Validation is correct");

        // This is invalid since JMRI is misspelled
        assertEquals( Manager.NameValidity.INVALID,
                LogixNG_Manager.validSystemNameFormat(prefix, prefix+":JRMI-LIB:Signal Mast System"),
                "Validation is not correct");
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
        assertEquals( "ConditionalNG", conditionalNG.getBeanType(), "beanType is correct");
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

}
