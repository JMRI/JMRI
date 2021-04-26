package jmri.jmrit.conditional;

import java.awt.GraphicsEnvironment;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/*
* Tests for the ConditionalListEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalCopyFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ConditionalListEdit listedit = new ConditionalListEdit("IX102");
        Assert.assertNotNull(listedit);

        Conditional cond = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");  // NOI18N
        Assert.assertNotNull(cond);

        ConditionalCopyFrame f = new ConditionalCopyFrame("Test ConditionalCopyFrameTest", cond, listedit);  // NOI18N
        Assert.assertNotNull(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.jmrit.conditional.CreateTestObjects.createTestObjects();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
