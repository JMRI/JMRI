package jmri.jmrit.logix;

import java.util.HashMap;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@Timeout(10)
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class MergePromptTest {

    @Test
    public void testMergePromptDisplayClose() {

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator("Merge Prompt CTor Test");
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonNoMerge"));
            jbo.doClick();
            jdo.waitClosed();
        });
        t.setName("MergePrompt Dialog Close Thread");
        t.start();

        MergePrompt m = new MergePrompt("Merge Prompt CTor Test",new HashMap<>(),
                        new HashMap<>());
        Assertions.assertNotNull(m, "exists");
        jmri.util.ThreadingUtil.runOnGUI( () -> m.setVisible(true));

        JUnitUtil.waitFor( () -> !t.isAlive(),"Dialog closed");
        m.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(MergePromptTest.class.getName());

}
