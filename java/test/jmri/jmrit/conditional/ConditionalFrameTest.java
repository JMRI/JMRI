package jmri.jmrit.conditional;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the ConditionalListEdit Class.
 *
 * @author Pete Crecssman Copyright (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class ConditionalFrameTest {

    @Test
    public void testCtor() {

        Conditional cond = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");  // NOI18N
        Assertions.assertNotNull(cond);

        ConditionalFrame f = new ConditionalFrame("Test ConditionalCopyFrameTest", cond, null);  // NOI18N
        Assertions.assertNotNull(f);
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initLogixManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        CreateTestObjects.createTestObjects();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
