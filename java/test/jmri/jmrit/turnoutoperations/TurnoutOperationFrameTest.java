package jmri.jmrit.turnoutoperations;

import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class TurnoutOperationFrameTest {

    @Test
    public void testCTor() {
        JmriJFrame jf = new JmriJFrame("Turnout Operation Frame Test");
        TurnoutOperationFrame t = new TurnoutOperationFrame(jf);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(jf);
    }

    @Test
    public void testOperationFrame() {
        // Open Automation pane to test Automation menu
        JmriJFrame jf = new JmriJFrame("Turnout Operation Frame Test with close");
        ThreadingUtil.runOnGUI(() -> {
            TurnoutOperationFrame tof = new TurnoutOperationFrame(jf);
            Assert.assertNotNull(tof);
        });

        // create dialog (bypassing menu)
        JDialogOperator am = new JDialogOperator("Turnout Operation Editor"); // TODO I18N using Bundle
        Assert.assertNotNull("found Automation menu dialog", am);
        am.getQueueTool().waitEmpty();
        // close pane
        new JButtonOperator(am, "OK").pushNoBlock(); // instead of .push();
        am.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);

        am.waitClosed();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationFrameTest.class);

}
