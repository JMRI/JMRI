package jmri.jmrit.conditional;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.NamedBean.DisplayOptions;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
* Tests for the ConditionalListEdit Class.
*
* @author Dave Sand Copyright (C) 2017
*/
public class ConditionalListCopyTest {

    @Test
    public void testCtor() {
        assertNotNull( new ConditionalListCopy(), "ConditionalListCopy Constructor Return");
    }

    @Test
    @DisabledIfHeadless
    public void testCopyConditionalChangeName() {

        Logix x3 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX103", "Copy for IX102");  // NOI18N
        assertNotNull(x3);

        ConditionalListCopy listCopy = new ConditionalListCopy("IX102", x3);

        JFrameOperator copyFrame = new JFrameOperator(Bundle.getMessage("TitleCopyFromLogix",
                listCopy._curLogix.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));
        assertNotNull(copyFrame);

        // test no selection
        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("ReminderTitle"), "OK");
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N
        JUnitUtil.waitFor(() -> {return !t1.isAlive();},"Reminder OK Dialogue did not complete");

        Conditional cond1 = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");
        assertNotNull(cond1);
        new JListOperator(copyFrame, 0).selectItem(cond1.toString());  // NOI18N
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N

        // test copy first conditional
        JFrameOperator ciFrame = new JFrameOperator(Bundle.getMessage("TitleCopyConditional",
                cond1.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));  // NOI18N
        assertNotNull(ciFrame);
        new JTextFieldOperator(ciFrame, 0).setText("Copy 1");  // NOI18N
        new JButtonOperator(ciFrame, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        ciFrame.waitClosed();

        new JButtonOperator(copyFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
        copyFrame.waitClosed();
    }

    @Test
    @DisabledIfHeadless
    public void testCopyConditionalFullEdit() {

        Logix x3 = InstanceManager.getDefault(jmri.LogixManager.class).createNewLogix("IX103", "Copy for IX102");  // NOI18N
        assertNotNull(x3);

        ConditionalListCopy listCopy = new ConditionalListCopy("IX102", x3);

        JFrameOperator copyFrame = new JFrameOperator(Bundle.getMessage("TitleCopyFromLogix",
                listCopy._curLogix.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));
        assertNotNull(copyFrame);

        // press full edit button
        new JRadioButtonOperator(copyFrame, Bundle.getMessage("fullEditButton")).push();  // NOI18N

        Conditional cond1 = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");
        assertNotNull(cond1);
        new JListOperator(copyFrame, 0).selectItem(cond1.toString());  // NOI18N
        new JButtonOperator(copyFrame, Bundle.getMessage("CopyConditionalButton")).push();  // NOI18N

        // test copy first conditional with full edit frame
        JFrameOperator ciFrame = new JFrameOperator(Bundle.getMessage("TitleCopyConditional",
            cond1.getDisplayName(DisplayOptions.QUOTED_USERNAME_SYSTEMNAME)));  // NOI18N
        assertNotNull(ciFrame);
        new JTextFieldOperator(ciFrame, 0).setText("Copy 1");  // NOI18N
        new JButtonOperator(ciFrame, Bundle.getMessage("ButtonSave")).push();  // NOI18N
        ciFrame.waitClosed();
        JUnitUtil.dispose(ciFrame.getWindow());

        new JButtonOperator(copyFrame, Bundle.getMessage("ButtonDone")).push();  // NOI18N
        copyFrame.waitClosed();
        JUnitUtil.dispose(copyFrame.getWindow());
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
