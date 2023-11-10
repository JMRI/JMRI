package jmri.jmrit.turnoutoperations;

import jmri.Turnout;
import jmri.TurnoutOperation;
import jmri.jmrix.can.*;
import jmri.util.JmriJFrame;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2023
 */
@Timeout(10)
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class TurnoutOperationFrameTest {

    @Test
    public void testCTor() {
        jf = new JmriJFrame("Turnout Operation Frame Test");
        TurnoutOperationFrame t = new TurnoutOperationFrame(jf);
        Assert.assertNotNull("exists",t);
        t.dispose();
    }

    @Test
    public void testOperationFrame() {
        // Open Automation pane to test Automation menu
        jf = new JmriJFrame("Turnout Operation Frame Test with close");
        ThreadingUtil.runOnGUI(() -> {
            TurnoutOperationFrame tof = new TurnoutOperationFrame(jf);
            Assert.assertNotNull(tof);
        });

        // create dialog (bypassing menu)
        JDialogOperator am = new JDialogOperator(Bundle.getMessage("TurnoutOperationEditorTitle"));
        Assert.assertNotNull("found Automation menu dialog", am);
        am.getQueueTool().waitEmpty();
        // close pane
        new JButtonOperator(am, Bundle.getMessage("ButtonOK")).pushNoBlock(); // instead of .push();
        am.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);

        am.waitClosed();
    }

    @Test
    public void testCopyRenameDelete() {

        // create a CBUS connection so that the Retry options which require
        // a CommandStation or SensorManager are displayed.
        memo = new CanSystemConnectionMemo();
        tcis = new TrafficControllerScaffold();

        memo.setTrafficController(tcis);
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.configureManagers();

        // Open Automation pane to test Automation menu
        jf = new JmriJFrame("Turnout Operation Frame Test with close");
        ThreadingUtil.runOnGUI(() -> {
            TurnoutOperationFrame tof = new TurnoutOperationFrame(jf);
            Assertions.assertNotNull(tof);
        });

        // create dialog (bypassing menu)
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("TurnoutOperationEditorTitle"));
        Assertions.assertNotNull(jdo, "found Automation menu dialog");
        jdo.getQueueTool().waitEmpty();

        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(jdo);
        Assertions.assertEquals(3, jtpo.getTabCount());
        jtpo.selectPage(new jmri.SensorTurnoutOperation().getName());
        jtpo.getQueueTool().waitEmpty();
        jdo.getQueueTool().waitEmpty();

        // click the copy button , then OK with no input
        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("EnterNewNameTitle"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jdo,Bundle.getMessage("MenuItemCopy")).pushNoBlock();
        JUnitUtil.waitFor(()->{return !(t.isAlive());}, "dialog finished");
        jdo.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);
        Assertions.assertEquals(3, jtpo.getTabCount(),"no name entered so no new operator created");

        // click the copy button , then Cancel
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("EnterNewNameTitle"), Bundle.getMessage("ButtonCancel"));
        new JButtonOperator(jdo,Bundle.getMessage("MenuItemCopy")).pushNoBlock();
        // jdo.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);
        JUnitUtil.waitFor(()-> !(t2.isAlive()), "copy cancel dialog finished");
        Assertions.assertEquals(3, jtpo.getTabCount(),"cancel pressed so no new operator created");

        Thread t3 = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JUnitUtil.waitFor(150);
            JDialogOperator jdoo = new JDialogOperator(Bundle.getMessage("EnterNewNameTitle"));
            Assertions.assertNotNull(jdoo);
            // jdoo.getQueueTool().waitEmpty();
            new JTextFieldOperator(jdoo,0).typeText("New Name for Copy of Sensor Operator");
            new JButtonOperator(jdoo, Bundle.getMessage("ButtonOK")).pushNoBlock();
        });
        t3.setName("New Operator Name Close Dialog Thread");
        t3.start();

        new JButtonOperator(jdo, Bundle.getMessage("MenuItemCopy")).pushNoBlock();// instead of .push();
        JUnitUtil.waitFor(100);
        JUnitUtil.waitFor(()-> !(t3.isAlive()), "New Name for Sensor Operator dialog finished");

        jdo.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);
        Assertions.assertEquals(4, jtpo.getTabCount(),"New Operator created");

        jtpo.selectPage("New Name for Copy of Sensor Operator");
        jtpo.getQueueTool().waitEmpty();
        jdo.getQueueTool().waitEmpty();

        Turnout tOut = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class).provide("MT+11");
        Assertions.assertDoesNotThrow(() -> tOut.provideFirstFeedbackSensor("ISMySensor"));
        tOut.setFeedbackMode(Turnout.ONESENSOR);

        TurnoutOperation turnoutOp = jmri.InstanceManager.getDefault(jmri.TurnoutOperationManager.class).getOperation("New Name for Copy of Sensor Operator");
        Assertions.assertNotNull(turnoutOp);
        tOut.setTurnoutOperation(turnoutOp);
        Assertions.assertNotNull(tOut.getTurnoutOperation());
        Assertions.assertEquals("New Name for Copy of Sensor Operator", turnoutOp.getName());

        Thread t4 = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdoo = new JDialogOperator(Bundle.getMessage("EnterNewNameTitle"));
            Assertions.assertNotNull(jdoo);
            new JTextFieldOperator(jdoo).enterText("Changed Name");
            new JButtonOperator(jdoo, Bundle.getMessage("ButtonOK")).pushNoBlock();
        });
        t4.setName("Operator ReName Close Dialog Thread");
        t4.start();

        new JButtonOperator(jdo, Bundle.getMessage("Rename")).pushNoBlock(); // instead of .push();

        JUnitUtil.waitFor(100);

        JUnitUtil.waitFor(()->{return !(t4.isAlive());}, "Rename Sensor Operator dialog finished");
        jdo.getQueueTool().waitEmpty();
        Assertions.assertEquals(4, jtpo.getTabCount(),"No New Operator created");

        jtpo.selectPage("Changed Name");
        jtpo.getQueueTool().waitEmpty();

        jdo.getQueueTool().waitEmpty();
        JUnitUtil.waitFor(100);

        TurnoutOperation changedOper = tOut.getTurnoutOperation();
        Assertions.assertNotNull(changedOper);
        Assertions.assertEquals("Changed Name", turnoutOp.getName());

        Thread t5 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("WarningTitle"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jdo, Bundle.getMessage("ButtonDelete")).pushNoBlock(); // instead of .push();
        JUnitUtil.waitFor(()-> !(t5.isAlive()), "confirm delete dialog finished");
        JUnitUtil.waitFor(100);
        Assertions.assertEquals(3, jtpo.getTabCount(),"New Operator deleted");

        new JButtonOperator(jdo, Bundle.getMessage("ButtonCancel")).pushNoBlock(); // instead of .push();
        jdo.waitClosed();

    }

    private JmriJFrame jf;
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tcis = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        if (jf !=null) {
            JUnitUtil.dispose(jf);
        }
        if ( tcis != null ){
            tcis.terminateThreads();
        }
        if ( memo != null ) {
            memo.dispose();
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationFrameTest.class);

}
