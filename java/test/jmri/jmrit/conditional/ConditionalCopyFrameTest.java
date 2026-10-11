package jmri.jmrit.conditional;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.netbeans.jemmy.operators.JFrameOperator;

import org.junit.jupiter.api.*;

/*
* Tests for the ConditionalListEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class ConditionalCopyFrameTest {

    @Test
    public void testCtor() {

        ConditionalListEdit listedit = new ConditionalListEdit("IX102");
        Assertions.assertNotNull(listedit);

        Conditional cond = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");  // NOI18N
        Assertions.assertNotNull(cond);

        ConditionalCopyFrame f = new ConditionalCopyFrame("Test ConditionalCopyFrameTest", cond, listedit);  // NOI18N
        Assertions.assertNotNull(f);

        JFrameOperator listEditOp = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));
        Assertions.assertNotNull(listEditOp);
        listEditOp.requestClose();
        JUnitUtil.dispose(listEditOp.getWindow());

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
