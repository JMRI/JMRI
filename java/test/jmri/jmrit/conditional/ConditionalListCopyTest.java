package jmri.jmrit.conditional;

import java.awt.GraphicsEnvironment;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.NamedBean.DisplayOptions;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/*
* Tests for the ConditionalListEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalListCopyTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("ConditionalListCopy Constructor Return", new ConditionalListCopy());  // NOI18N
    }

    @Test
    public void CopyConditionalChangeNameTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Logix x3 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX103", "Copy for IX102");  // NOI18N
        Assert.assertNotNull(x3);

        ConditionalListCopy listCopy = new ConditionalListCopy("IX102", x3);

        JFrameOperator copyFrame = new JFrameOperator(Bundle.getMessage("TitleCopyFromLogix",
                listCopy._curLogix.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));
        Assert.assertNotNull(copyFrame);

        // test no selection
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N
        JDialogOperator jdo = new JDialogOperator(copyFrame, Bundle.getMessage("ReminderTitle"));
        new JButtonOperator(jdo, "OK").push();

        Conditional cond1 = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");
        Assert.assertNotNull(cond1);
        new JListOperator(copyFrame, 0).selectItem(cond1.toString());  // NOI18N
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N

        // test copy first conditional
        new Thread(() -> {
            JFrameOperator ciFrame = new JFrameOperator(Bundle.getMessage("TitleCopyConditional", "IX102"));  // NOI18N
            Assert.assertNotNull(ciFrame);
            new JTextFieldOperator(ciFrame, 0).setText("Copy 1");  // NOI18N
            new JButtonOperator(ciFrame, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        }).start();

        new JButtonOperator(copyFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
    }

    @Test
    public void CopyConditionalFullEditTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Logix x3 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX103", "Copy for IX102");  // NOI18N
        Assert.assertNotNull(x3);

        ConditionalListCopy listCopy = new ConditionalListCopy("IX102", x3);

        JFrameOperator copyFrame = new JFrameOperator(Bundle.getMessage("TitleCopyFromLogix",
                listCopy._curLogix.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));
        Assert.assertNotNull(copyFrame);

        // press full edit button
        new JRadioButtonOperator(copyFrame, Bundle.getMessage("fullEditButton")).push();  // NOI18N

        Conditional cond1 = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");
        Assert.assertNotNull(cond1);
        new JListOperator(copyFrame, 0).selectItem(cond1.toString());  // NOI18N
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N

        // test copy first conditional with full edit frame
        new Thread(() -> {
            JFrameOperator ciFrame = new JFrameOperator(Bundle.getMessage("TitleCopyConditional", "IX102"));  // NOI18N
            Assert.assertNotNull(ciFrame);
            new JTextFieldOperator(ciFrame, 0).setText("Copy 1");  // NOI18N
            new JButtonOperator(ciFrame, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        }).start();

        new JButtonOperator(copyFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
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
