package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test SwingToolsTest
 *
 * @author Daniel Bergqvist 2019
 */
public class Is_IsNot_EnumTest {

    @Test
    public void testEnum() {
        assertEquals( "is", Is_IsNot_Enum.Is.toString(), "toString is correct");
        assertEquals( "is not", Is_IsNot_Enum.IsNot.toString(), "toString is correct");
        assertSame( Is_IsNot_Enum.Is, Is_IsNot_Enum.valueOf("Is"), "Enum is correct");
        assertSame( Is_IsNot_Enum.IsNot, Is_IsNot_Enum.valueOf("IsNot"), "Enum is correct");
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
