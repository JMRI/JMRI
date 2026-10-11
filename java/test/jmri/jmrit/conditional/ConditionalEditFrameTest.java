package jmri.jmrit.conditional;

import jmri.Conditional;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/*
* Tests for the ConditionalListEdit Class.
*
* @author Pete Crecssman Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class ConditionalEditFrameTest {

    @Test
    public void testCtor() {

        ConditionalListEdit listedit = new ConditionalListEdit("IX102");
        Assertions.assertNotNull(listedit);
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleEditLogix"));
        Assertions.assertNotNull(jfo);

        Conditional cond = InstanceManager.getDefault(jmri.ConditionalManager.class).getBySystemName("IX102C1");  // NOI18N
        Assertions.assertNotNull(cond);

        ConditionalEditFrame f = new ConditionalEditFrame("Test ConditionalEditFrame", cond, listedit);  // NOI18N
        Assertions.assertNotNull(f);
        f.initComponents();

        ThreadingUtil.runOnGUI( () -> {
            f.pack();
            f.setVisible(true);
        });

        JUnitUtil.dispose(jfo.getWindow());
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
