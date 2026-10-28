package jmri.jmrit.beantable.beanedit;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import jmri.CommonTurnoutOperation;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.TurnoutOperation;
import jmri.TurnoutOperationManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.*;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TurnoutEditActionTest {

    @Test
    public void testCTor() {
        TurnoutEditAction t = new TurnoutEditAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    @DisabledIfHeadless
    public void testApplyNamedAutomationKeepsEditedValues() {
        // issue #15471 follow-up: values edited for a named automation must reach the turnout
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setFeedbackMode(Turnout.DIRECT);
        TurnoutEditAction action = new TurnoutEditAction();
        action.setBean(to);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        new JTabbedPaneOperator(efo).selectPage(Bundle.getMessage("Feedback"));
        new JComboBoxOperator(efo, new AutomationBoxChooser()).selectItem("NoFeedback");
        new JSpinnerOperator(efo, 0).setValue(3);   // times to try is listed first
        new JSpinnerOperator(efo, 1).setValue(600); // interval
        new JButtonOperator(efo, Bundle.getMessage("ButtonApply")).push();

        TurnoutOperation attached = to.getTurnoutOperation();
        Assert.assertNotNull("turnout has an automation", attached);
        Assert.assertEquals("edited interval took effect", 600, ((CommonTurnoutOperation) attached).getInterval());
        Assert.assertEquals("edited times to try took effect", 3, ((CommonTurnoutOperation) attached).getMaxTries());

        TurnoutOperation shared = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("NoFeedback");
        Assert.assertEquals("shared NoFeedback left alone", 300, ((CommonTurnoutOperation) shared).getInterval());

        JUnitUtil.dispose(efo.getWindow());

        // reopening must show the saved values, and applying again must keep them
        TurnoutEditAction action2 = new TurnoutEditAction();
        action2.setBean(to);
        ThreadingUtil.runOnGUI(() -> action2.actionPerformed(null));

        efo = new JFrameOperator("Edit Turnout IT1");
        new JTabbedPaneOperator(efo).selectPage(Bundle.getMessage("Feedback"));
        Assert.assertEquals("reopened window shows saved times to try", 3, new JSpinnerOperator(efo, 0).getValue());
        Assert.assertEquals("reopened window shows saved interval", 600, new JSpinnerOperator(efo, 1).getValue());
        new JButtonOperator(efo, Bundle.getMessage("ButtonApply")).push();

        Assert.assertEquals("second apply keeps interval", 600, ((CommonTurnoutOperation) to.getTurnoutOperation()).getInterval());
        Assert.assertEquals("second apply keeps times to try", 3, ((CommonTurnoutOperation) to.getTurnoutOperation()).getMaxTries());

        JUnitUtil.dispose(efo.getWindow());
    }

    @Test
    @DisabledIfHeadless
    public void testApplyWithoutEditsKeepsSharedAutomation() {
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setFeedbackMode(Turnout.DIRECT);
        TurnoutEditAction action = new TurnoutEditAction();
        action.setBean(to);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        new JTabbedPaneOperator(efo).selectPage(Bundle.getMessage("Feedback"));
        new JComboBoxOperator(efo, new AutomationBoxChooser()).selectItem("NoFeedback");
        new JButtonOperator(efo, Bundle.getMessage("ButtonApply")).push();

        TurnoutOperation shared = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("NoFeedback");
        Assert.assertSame("unchanged automation stays linked to the shared one", shared, to.getTurnoutOperation());

        JUnitUtil.dispose(efo.getWindow());
    }

    @Test
    @DisabledIfHeadless
    public void testReopenShowsTypeOfTurnoutsOwnCopy() {
        new jmri.RawTurnoutOperation(); // internal turnouts only offer NoFeedback, so add Raw
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setFeedbackMode(Turnout.DIRECT);
        to.setInhibitOperation(false);
        TurnoutOperation op = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("Raw").makeNonce(to);
        ((CommonTurnoutOperation) op).setInterval(700);
        to.setTurnoutOperation(op);
        TurnoutEditAction action = new TurnoutEditAction();
        action.setBean(to);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        new JTabbedPaneOperator(efo).selectPage(Bundle.getMessage("Feedback"));
        Assert.assertEquals("shows Raw", "Raw", new JComboBoxOperator(efo, new AutomationBoxChooser()).getSelectedItem());
        Assert.assertEquals("shows the copy's interval", 700, new JSpinnerOperator(efo, 1).getValue());
        new JButtonOperator(efo, Bundle.getMessage("ButtonApply")).push();

        Assert.assertSame("Apply keeps the turnout's own copy", op, to.getTurnoutOperation());

        JUnitUtil.dispose(efo.getWindow());
    }

    @Test
    @DisabledIfHeadless
    public void testApplyWithNewNameAttachesNamedAutomation() {
        Turnout to = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT1");
        to.setFeedbackMode(Turnout.DIRECT);
        TurnoutEditAction action = new TurnoutEditAction();
        action.setBean(to);
        ThreadingUtil.runOnGUI(() -> action.actionPerformed(null));

        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        new JTabbedPaneOperator(efo).selectPage(Bundle.getMessage("Feedback"));
        new JComboBoxOperator(efo, new AutomationBoxChooser()).selectItem("NoFeedback");
        new JSpinnerOperator(efo, 1).setValue(600);
        new JTextFieldOperator(efo, new AutomationNameFieldChooser()).setText("MyNoFeedback");
        new JButtonOperator(efo, Bundle.getMessage("ButtonApply")).push();

        TurnoutOperation named = InstanceManager.getDefault(TurnoutOperationManager.class).getOperation("MyNoFeedback");
        Assert.assertNotNull("named automation created", named);
        Assert.assertSame("turnout uses the named automation", named, to.getTurnoutOperation());
        Assert.assertEquals("named automation has edited interval", 600, ((CommonTurnoutOperation) named).getInterval());

        JUnitUtil.dispose(efo.getWindow());
    }

    // finds the automation combo box by its "NoFeedback" entry
    private static class AutomationBoxChooser implements ComponentChooser {
        @Override
        public boolean checkComponent(Component comp) {
            if (comp instanceof JComboBox) {
                JComboBox<?> cb = (JComboBox<?>) comp;
                for (int i = 0; i < cb.getItemCount(); i++) {
                    if ("NoFeedback".equals(cb.getItemAt(i))) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "automation combo box";
        }
    }

    // finds the automation name field, the only plain text field on the Feedback tab
    private static class AutomationNameFieldChooser implements ComponentChooser {
        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass() == JTextField.class && comp.isShowing();
        }

        @Override
        public String getDescription() {
            return "automation name field";
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(TurnoutEditActionTest.class);

}
