package jmri.jmrit.beantable;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.tree.TreePath;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.tools.swing.ConditionalNGEditor;

import jmri.util.*;
import jmri.util.junit.rules.*;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.jupiter.api.*;
import org.junit.rules.Timeout;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;


/*
* Tests for the LogixNGModuleTableAction Class
* Re-created using JUnit4 with support for the new conditional editors
* @author Dave Sand Copyright (C) 2017 (for the LogixTableActionTest class)
* @author Daniel Bergqvist Copyright (C) 2019
*/
public class LogixNGModuleTableActionTest extends AbstractTableActionBase<Module> {

    static final ResourceBundle rbxLogixNGSwing = ResourceBundle.getBundle("jmri.jmrit.logixng.tools.swing.LogixNGSwingBundle");

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    @Test
    public void testCtor() {
        Assert.assertNotNull("LogixNGModuleTableActionTest Constructor Return", new LogixNGModuleTableAction());  // NOI18N
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LogixNGModuleTableAction Constructor Return", new LogixNGModuleTableAction("test"));  // NOI18N
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleLogixNGModuleTable");  // NOI18N
    }

    @Override
    @Test
    public void testGetClassDescription() {
        Assert.assertEquals("LogixNG Table Action class description", Bundle.getMessage("TitleLogixNGModuleTable"), a.getClassDescription());  // NOI18N
    }

    @Test
    @Override
    public void testAddThroughDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        AbstractLogixNGTableAction logixNGModuleTable = (AbstractLogixNGTableAction) a;
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        Module module = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM1");
        Assert.assertNull("LogixNG Module does not exist", module);

        // find the "Add... " button and press it.
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        JFrameOperator jf = new JFrameOperator(f1);
        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jf,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        //Enter IQ1 in the text field labeled "System Name:"
        JLabelOperator jlo = new JLabelOperator(jf, "LogixNG" + " " + Bundle.getMessage("ColumnSystemName") + ":");
//        JLabelOperator jlo = new JLabelOperator(jf,Bundle.getMessage("LabelSystemName"));
        ((JTextField)jlo.getLabelFor()).setText("IQM1");
        //and press create
        jmri.util.swing.JemmyUtil.pressButton(jf,Bundle.getMessage("ButtonCreate"));

        // Click button "Done" on the EditLogixNG frame
        String title = String.format("Edit Module %s", "IQM1");
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(frame);

        JMenuBarOperator mainbar = new JMenuBarOperator(jf2);
        mainbar.pushMenu(Bundle.getMessage("MenuFile")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("MenuFile"));
        JPopupMenu jpm = jmo.getPopupMenu();

        // Menu AutoCreate
        JMenuItem findMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertEquals(findMenuItem.getText(), "Close window");
        new JMenuItemOperator(findMenuItem).doClick();
/*
        // Test that we can open the LogixNGEdtior window twice
        logixNGModuleTable.editPressed("IQM101");  // NOI18N
        // Click button "Done" on the EditLogixNG frame
        title = String.format("Edit Module %s - %s", "IQM101", "Module 101");
        frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        jf2 = new JFrameOperator(frame);
        jf2.dispose();
        JUnitUtil.dispose(frame);
*/
        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);

        module = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM1");
        Assert.assertNotNull("LogixNG Module has been created", module);
    }

    @Disabled("Fix later")
    @Test
    @Override
    public void testEditButton() {
    }

    @Disabled("Fix later")
    @Test
    @Override
    public void testIncludeAddButton() {
    }

    /*.*
     * Check the return value of includeAddButton.
     * <p>
     * The table generated by this action includes an Add Button.
     *./
    @Override
    @Test
    public void testIncludeAddButton() {
        Assert.assertTrue("Default include add button", a.includeAddButton());  // NOI18N
    }
*/
    @Override
    public String getAddFrameName(){
        return Bundle.getMessage("TitleAddLogixNGModule");
    }
/*
    @Test
    @Override
    public void testAddThroughDialog() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1");
        Assert.assertNull("LogixNG does not exist", logixNG);

        // find the "Add... " button and press it.
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        JFrameOperator jf = new JFrameOperator(f1);
        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jf,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        //Enter IQ1 in the text field labeled "System Name:"
        JLabelOperator jlo = new JLabelOperator(jf, "LogixNG" + " " + Bundle.getMessage("ColumnSystemName") + ":");
//        JLabelOperator jlo = new JLabelOperator(jf,Bundle.getMessage("LabelSystemName"));
        ((JTextField)jlo.getLabelFor()).setText("IQ1");
        //and press create
        jmri.util.swing.JemmyUtil.pressButton(jf,Bundle.getMessage("ButtonCreate"));

        // Click button "Done" on the EditLogixNG frame
        String title = String.format("Edit LogixNG %s", "IQ1");
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);

        // Test that we can open the LogixNGEdtior window twice
        logixNGTable.editPressed("IQ101");  // NOI18N
        // Click button "Done" on the EditLogixNG frame
        title = String.format("Edit LogixNG %s - %s", "IQ101", "LogixNG 101");
        frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        jf2 = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);

        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ1");
        Assert.assertNotNull("LogixNG has been created", logixNG);
    }

    @Test
    @Override
    public void testEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.setEditorMode(AbstractLogixNGTableAction.EditMode.TREEEDIT);

        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ101");
        Assert.assertNotNull("LogixNG exists", logixNG);

        logixNGTable.editPressed("IQ101");  // NOI18N

        String title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
//        JFrame frame2 = JFrameOperator.waitJFrame(Bundle.getMessage("EditTitle"), true, true);  // NOI18N

        // Click button "New ConditionalNG" on the EditLogixNG frame
        JFrameOperator jf = new JFrameOperator(frame);
        jmri.util.swing.JemmyUtil.pressButton(jf,"New ConditionalNG");


        JDialogOperator addDialog = new JDialogOperator("Add ConditionalNG");  // NOI18N
        new JButtonOperator(addDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Close window
        JFrameOperator editConditionalNGFrameOperator = new JFrameOperator("Edit ConditionalNG " + logixNG.getConditionalNG(0));
        new JMenuBarOperator(editConditionalNGFrameOperator).pushMenu("File|Close Window", "|");

        Assert.assertNotNull(frame);
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(frame),Bundle.getMessage("ButtonDone"));
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testLogixNGBrowser() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.browserPressed("IQ101");  // NOI18N

        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("BrowserTitle"), true, true);  // NOI18N
        Assert.assertNotNull(frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testTreeEditor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                setProperty("jmri.jmrit.beantable.LogixNGModuleTableAction", "Edit Mode", "TREEEDIT");  // NOI18N
        a.actionPerformed(null);
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;
        JFrameOperator logixNGFrame = new JFrameOperator(Bundle.getMessage("TitleLogixNGModuleTable"));  // NOI18N
        Assert.assertNotNull(logixNGFrame);

        logixNGTable.editPressed("IQ104");  // NOI18N
        JFrameOperator cdlFrame = new JFrameOperator(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG"), "IQ104"));  // NOI18N
        Assert.assertNotNull(cdlFrame);
        new JMenuBarOperator(cdlFrame).pushMenuNoBlock(Bundle.getMessage("MenuFile")+"|"+rbxLogixNGSwing.getString("CloseWindow"), "|");  // NOI18N
        logixNGFrame.dispose();
    }

    @Test
    public void testAddLogixNGAutoName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNGModule"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        new JTextFieldOperator(addFrame, 1).setText("LogixNG 999");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG chk999 = jmri.InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class).getLogixNG("LogixNG 999");  // NOI18N
        Assert.assertNotNull("Verify 'LogixNG 999' Added", chk999);  // NOI18N

        // Add creates an edit frame; find and dispose
        JFrame editFrame = JFrameOperator.waitJFrame(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG2"), "IQ:AUTO:0001", "LogixNG 999"), true, true);  // NOI18N
        JUnitUtil.dispose(editFrame);

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testAddLogixNG() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNGModule"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(addFrame,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        new JTextFieldOperator(addFrame, 0).setText("IQ105");  // NOI18N
        new JTextFieldOperator(addFrame, 1).setText("LogixNG 105");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG chk105 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getLogixNG("LogixNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQ105 Added", chk105);  // NOI18N

        // Add creates an edit frame; find and dispose
        JFrame editFrame = JFrameOperator.waitJFrame(jmri.Bundle.formatMessage(rbxLogixNGSwing.getString("TitleEditLogixNG2"), "IQ105", "LogixNG 105"), true, true);  // NOI18N
        JUnitUtil.dispose(editFrame);

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteLogixNG() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQ102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"));  // NOI18N
        logixNGTable.deletePressed("IQ102");  // NOI18N
        t1.join();
        LogixNG chk102 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ102");  // NOI18N
        Assert.assertNotNull("Verify IQ102 Not Deleted", chk102);  // NOI18N

        // Delete IQ103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"));  // NOI18N
        logixNGTable.deletePressed("IQ103");  // NOI18N
        t2.join();
        LogixNG chk103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQ103");  // NOI18N
        Assert.assertNull("Verify IQ103 Is Deleted", chk103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText) {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }


    // Test that it's possible to
    // * Add a LogixNG
    // * Enable the LogixNG
    // * Add a ConditionalNG
    // * Add a IfThenElse
    // * Add a ExpressionSensor
    // * Add a ActionTurnout
    // After that, test that the LogixNG is executed properly
    @Test
    public void testAddAndRun() throws JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Sensor sensor1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Turnout turnout1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");

        // * Add a LogixNG
        AbstractLogixNGTableAction logixNGTable = (AbstractLogixNGTableAction) a;

        logixNGTable.actionPerformed(null); // show table
        JFrameOperator logixNGFrameOperator = new JFrameOperator(Bundle.getMessage("TitleLogixNGModuleTable"));  // NOI18N

        logixNGTable.addPressed(null);
        JFrameOperator addFrame = new JFrameOperator(Bundle.getMessage("TitleAddLogixNGModule"));  // NOI18N
        Assert.assertNotNull("Found Add LogixNG Frame", addFrame);  // NOI18N

        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(addFrame,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        new JTextFieldOperator(addFrame, 0).setText("IQ105");  // NOI18N
        new JTextFieldOperator(addFrame, 1).setText("LogixNG 105");  // NOI18N
        new JButtonOperator(addFrame, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        LogixNG logixNG = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getLogixNG("LogixNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQ105 Added", logixNG);  // NOI18N


        // Close Edit LogixNG frame by click on button "Done" on the EditLogixNG frame
        String title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        JFrame editLogixNGframe = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(editLogixNGframe);
        jmri.util.swing.JemmyUtil.pressButton(jf2,Bundle.getMessage("ButtonDone"));

        // Operate on the table
        JTableOperator tableOperator = new JTableOperator(logixNGFrameOperator);
        int columnSystemName = tableOperator.findColumn("System name");
        int columnEnabled = tableOperator.findColumn("Enabled");
        int columnMenu = tableOperator.findColumn("Menu");
        int row = tableOperator.findCellRow("IQ105", columnSystemName, 0);

        Assert.assertTrue("LogixNG is enabled on creation", logixNG.isEnabled());

        // Disable the LogixNG
        tableOperator.setValueAt(false, row, columnEnabled);

        Assert.assertFalse("LogixNG has been disabled", logixNG.isEnabled());

        // Enable the LogixNG
        tableOperator.setValueAt(true, row, columnEnabled);

        Assert.assertTrue("LogixNG has been enabled", logixNG.isEnabled());


        // Edit the LogixNG
        tableOperator.setValueAt("Edit", row, columnMenu);

        // Open Edit ConditionalNG  frame
        title = String.format("Edit LogixNG %s - %s", logixNG.getSystemName(), logixNG.getUserName());
        editLogixNGframe = JFrameOperator.waitJFrame(title, true, true);  // NOI18N


        // Click button "New ConditionalNG" on the EditLogixNG frame
        JFrameOperator jf = new JFrameOperator(editLogixNGframe);
        jmri.util.swing.JemmyUtil.pressButton(jf,"New ConditionalNG");

        JDialogOperator addDialog = new JDialogOperator("Add ConditionalNG");  // NOI18N
        new JTextFieldOperator(addDialog, 0).setText("IQC105");  // NOI18N
        new JTextFieldOperator(addDialog, 1).setText("ConditionalNG 105");  // NOI18N
        new JButtonOperator(addDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        ConditionalNG conditionalNG = jmri.InstanceManager.getDefault(ConditionalNG_Manager.class).getConditionalNG("ConditionalNG 105");  // NOI18N
        Assert.assertNotNull("Verify IQC105 Added", conditionalNG);  // NOI18N


        // https://www.javatips.net/api/org.netbeans.jemmy.operators.jtreeoperator

        // Get tree edit window
        title = String.format("Edit ConditionalNG %s - %s", conditionalNG.getSystemName(), conditionalNG.getUserName());
        JFrameOperator treeFrame = new JFrameOperator(title);
        JTreeOperator jto = new JTreeOperator(treeFrame);
        Assert.assertEquals("Initial number of rows in the tree", 1, jto.getRowCount());


        // We click on the root female socket to open the popup menu
        TreePath tp = jto.getPathForRow(0);

        JPopupMenu jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        JDialogOperator addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.COMMON);
        new JComboBoxOperator(addItemDialog, 1).selectItem("If then else");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Action is correct", "If Then Else",
                conditionalNG.getChild(0).getConnectedSocket().getLongDescription());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());


        // We click on the IfThenElse if-expression female socket to open the popup menu
        tp = jto.getPathForRow(1);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Get sensor");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the expression
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(Is_IsNot_Enum.Is);
        new JComboBoxOperator(addItemDialog, 2).setSelectedItem(ExpressionSensor.SensorState.Active);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());
        Assert.assertEquals("Expression is correct", "Sensor IS1 is Active",
                conditionalNG.getChild(0).getConnectedSocket().getChild(0).getConnectedSocket().getLongDescription());


        // We click on the IfThenElse then-action female socket to open the popup menu
        tp = jto.getPathForRow(2);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Set turnout");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(ActionTurnout.TurnoutState.Thrown);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());
        Assert.assertEquals("Expression is correct", "Set turnout IT1 to state Thrown",
                conditionalNG.getChild(0).getConnectedSocket().getChild(1).getConnectedSocket().getLongDescription());


        // Close EditConditionalNG window
        JFrameOperator editConditionalNGFrameOperator = new JFrameOperator("Edit ConditionalNG " + logixNG.getConditionalNG(0));
        new JMenuBarOperator(editConditionalNGFrameOperator).pushMenu("File|Close Window", "|");


        logixNG.getConditionalNG(0).setRunDelayed(false);

        // Test that the LogixNG is running
        sensor1.setState(Sensor.INACTIVE);
        turnout1.setState(Turnout.CLOSED);
        Assert.assertTrue("Sensor is inactive", sensor1.getState() == Sensor.INACTIVE);
        Assert.assertTrue("Turnout is closed", turnout1.getState() == Turnout.CLOSED);

        // Activate sensor. This should throw the turnout
        sensor1.setState(Sensor.ACTIVE);
        Assert.assertTrue("Sensor is active", sensor1.getState() == Sensor.ACTIVE);
        Assert.assertTrue("Turnout is thrown", turnout1.getState() == Turnout.THROWN);

        // Close Edit LogixNG frame
        JUnitUtil.dispose(editLogixNGframe);

        // Close LogixNG frame
        logixNGFrameOperator.dispose();
    }
*/

    @Test
    public void testEditModule() throws JmriException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//        AbstractLogixNGTableAction logixNGModuleTable = (AbstractLogixNGTableAction) a;
        a.actionPerformed(null);
        JFrame f = JFrameOperator.waitJFrame(getTableFrameName(), true, true);

        Module module = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM1");
        Assert.assertNull("LogixNG Module does not exist", module);

        // find the "Add... " button and press it.
        jmri.util.swing.JemmyUtil.pressButton(new JFrameOperator(f),Bundle.getMessage("ButtonAdd"));
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JFrame f1 = JFrameOperator.waitJFrame(getAddFrameName(), true, true);
        JFrameOperator jf = new JFrameOperator(f1);
        //disable "Auto System Name" via checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jf,Bundle.getMessage("LabelAutoSysName"));
        jcbo.doClick();
        //Enter IQ1 in the text field labeled "System Name:"
        JLabelOperator jlo = new JLabelOperator(jf, "LogixNG" + " " + Bundle.getMessage("ColumnSystemName") + ":");
//        JLabelOperator jlo = new JLabelOperator(jf,Bundle.getMessage("LabelSystemName"));
        ((JTextField)jlo.getLabelFor()).setText("IQM1");
        //and press create
        jmri.util.swing.JemmyUtil.pressButton(jf,Bundle.getMessage("ButtonCreate"));

        // Click button "Done" on the EditLogixNG frame
        String title = String.format("Edit Module %s", "IQM1");
        JFrame frame = JFrameOperator.waitJFrame(title, true, true);  // NOI18N
        JFrameOperator jf2 = new JFrameOperator(frame);


        // https://www.javatips.net/api/org.netbeans.jemmy.operators.jtreeoperator

        // Get tree edit window
        JTreeOperator jto = new JTreeOperator(jf2);
        Assert.assertEquals("Initial number of rows in the tree", 2, jto.getRowCount());


        // We click on the root female socket to open the popup menu
        TreePath tp = jto.getPathForRow(0);

        JPopupMenu jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Edit");
/*
        // First, we get a dialog that lets us select which action to add
        JDialogOperator addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.COMMON);
        new JComboBoxOperator(addItemDialog, 1).selectItem("If then else");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

//        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
//        Assert.assertEquals("Action is correct", "If Then Else",
//                conditionalNG.getChild(0).getConnectedSocket().getLongDescription());
//        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());


        // We click on the IfThenElse if-expression female socket to open the popup menu
        tp = jto.getPathForRow(1);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Get sensor");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the expression
        addItemDialog = new JDialogOperator("Add ? ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(Is_IsNot_Enum.Is);
        new JComboBoxOperator(addItemDialog, 2).setSelectedItem(ExpressionSensor.SensorState.Active);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

//        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
//        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());
//        Assert.assertEquals("Expression is correct", "Sensor IS1 is Active",
//                conditionalNG.getChild(0).getConnectedSocket().getChild(0).getConnectedSocket().getLongDescription());


        // We click on the IfThenElse then-action female socket to open the popup menu
        tp = jto.getPathForRow(2);

        jpm = jto.callPopupOnPath(tp);
        new JPopupMenuOperator(jpm).pushMenuNoBlock("Add");

        // First, we get a dialog that lets us select which action to add
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N
        // Select ExpressionSensor
        new JComboBoxOperator(addItemDialog, 0).setSelectedItem(Category.ITEM);
        new JComboBoxOperator(addItemDialog, 1).selectItem("Set turnout");
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        addItemDialog = new JDialogOperator("Add ! ");  // NOI18N

        // Select to use sensor IS1
        new JComboBoxOperator(addItemDialog, 0).setSelectedIndex(1);
        new JComboBoxOperator(addItemDialog, 1).setSelectedItem(ActionTurnout.TurnoutState.Thrown);
        new JButtonOperator(addItemDialog, Bundle.getMessage("ButtonCreate")).push();  // NOI18N

//        Assert.assertTrue("Is connected", conditionalNG.getChild(0).isConnected());
//        Assert.assertEquals("Num childs are correct", 3, conditionalNG.getChild(0).getConnectedSocket().getChildCount());
//        Assert.assertEquals("Expression is correct", "Set turnout IT1 to state Thrown",
//                conditionalNG.getChild(0).getConnectedSocket().getChild(1).getConnectedSocket().getLongDescription());


        // Close EditConditionalNG window
//        JFrameOperator editConditionalNGFrameOperator = new JFrameOperator("Edit ConditionalNG " + logixNG.getConditionalNG(0));
//        new JMenuBarOperator(editConditionalNGFrameOperator).pushMenu("File|Close Window", "|");
*/


        // Then we get a dialog that lets us set the system name, user name
        // and configure the action
        JDialogOperator editModuleDialog = new JDialogOperator("Edit - Root");  // NOI18N
        new JButtonOperator(editModuleDialog, Bundle.getMessage("ButtonOK")).push();  // NOI18N



        JMenuBarOperator mainbar = new JMenuBarOperator(jf2);
        mainbar.pushMenu(Bundle.getMessage("MenuFile")); // stops at top level
        JMenuOperator jmo = new JMenuOperator(mainbar, Bundle.getMessage("MenuFile"));
        jpm = jmo.getPopupMenu();
//        JPopupMenu jpm = jmo.getPopupMenu();

        // Menu Close window
        JMenuItem findMenuItem = (JMenuItem) jpm.getComponent(0);
        Assert.assertEquals("Close window", findMenuItem.getText());
        new JMenuItemOperator(findMenuItem).doClick();

        Module tempModule = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM1");
        Assert.assertNotNull("LogixNG Module has been created", tempModule);

        JUnitUtil.dispose(f1);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testDeleteLogixNG() throws InterruptedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction moduleTable = (AbstractLogixNGTableAction) a;

        moduleTable.actionPerformed(null); // show table
        JFrame moduleFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", moduleFrame);  // NOI18N

        // Delete IQM102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQM102?");  // NOI18N
        moduleTable.deletePressed("IQM102");  // NOI18N
        t1.join();
        Module module102 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");  // NOI18N
        Assert.assertNotNull("Verify IQM102 Not Deleted", module102);  // NOI18N

        // Delete IQM103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQM103?");  // NOI18N
        moduleTable.deletePressed("IQM103");  // NOI18N
        t2.join();
        LogixNG module103 = jmri.InstanceManager.getDefault(LogixNG_Manager.class).getBySystemName("IQM103");  // NOI18N
        Assert.assertNull("Verify IQM103 Is Deleted", module103);  // NOI18N

        JUnitUtil.dispose(moduleFrame);
    }

    @Test
    public void testDeleteModuleWithDigitalAction() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction moduleTable = (AbstractLogixNGTableAction) a;

        Module module102 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_102 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA102", null);
        module102.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_102));

        Module module103 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_103 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA103", null);
        module103.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_103));

        moduleTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQM102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQM102 and its children?");  // NOI18N
        moduleTable.deletePressed("IQM102");  // NOI18N
        t1.join();
        Module mod102 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");  // NOI18N
        Assert.assertNotNull("Verify IQM102 Not Deleted", mod102);  // NOI18N
        MaleSocket digMany102 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA102");   // NOI18N
        Assert.assertNotNull("Verify IQDA102 Not Deleted", digMany102);  // NOI18N

        // Delete IQM103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQM103 and its children?");  // NOI18N
        moduleTable.deletePressed("IQM103");  // NOI18N
        t2.join();
        Module mod103 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");  // NOI18N
        Assert.assertNull("Verify IQM103 Is Deleted", mod103);  // NOI18N
        MaleSocket digMany103 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA103");   // NOI18N
        Assert.assertNull("Verify IQDA103 Is Deleted", digMany103);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    @Test
    public void testDeleteModuleWithTwoDigitalActions() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction moduleTable = (AbstractLogixNGTableAction) a;

        Module module102 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_102 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA102", null);
        module102.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_102));
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_112 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA112", null);
        digitalMany_102.getChild(0).connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_112));

        Module module103 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_103 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA103", null);
        module103.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_103));
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_113 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA113", null);
        digitalMany_103.getChild(0).connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_113));

        moduleTable.actionPerformed(null); // show table
        JFrame moduleFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", moduleFrame);  // NOI18N

        // Delete IQM102, respond No
        Thread t1 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), "Are you sure you want to delete IQM102 and its children?");  // NOI18N
        moduleTable.deletePressed("IQM102");  // NOI18N
        t1.join();
        Module mod102 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");  // NOI18N
        Assert.assertNotNull("Verify IQM102 Not Deleted", mod102);  // NOI18N
        MaleSocket digMany102 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA102");   // NOI18N
        Assert.assertNotNull("Verify IQDA102 Not Deleted", digMany102);  // NOI18N
        MaleSocket digMany112 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA112");   // NOI18N
        Assert.assertNotNull("Verify IQDA112 Not Deleted", digMany112);  // NOI18N

        // Delete IQM103, respond Yes
        Thread t2 = createModalDialogOperatorThread(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), "Are you sure you want to delete IQM103 and its children?");  // NOI18N
        moduleTable.deletePressed("IQM103");  // NOI18N
        t2.join();
        Module mod103 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");  // NOI18N
        Assert.assertNull("Verify IQM103 Is Deleted", mod103);  // NOI18N
        MaleSocket digMany103 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA103");   // NOI18N
        Assert.assertNull("Verify IQDA103 Is Deleted", digMany103);  // NOI18N
        MaleSocket digMany113 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA113");   // NOI18N
        Assert.assertNull("Verify IQDA113 Is Deleted", digMany113);  // NOI18N

        JUnitUtil.dispose(moduleFrame);
    }

    @Test
    public void testDeleteModuleWithDigitalActionWithListenerRef() throws InterruptedException, SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        AbstractLogixNGTableAction moduleTable = (AbstractLogixNGTableAction) a;

        PropertyChangeListener pcl = (PropertyChangeEvent evt) -> {
            // Do nothing
        };
        
        final String listenerRefs =
                "<html>\n" +
                "  <head>\n" +
                "    \n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <br>\n" +
                "    It is in use by 1 other objects including.\n" +
                "\n" +
                "    <ul>\n" +
                "      <li>\n" +
                "        A listener ref\n" +
                "      </li>\n" +
                "    </ul>\n" +
                "  </body>\n" +
                "</html>\n";

        Module module102 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_102 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA102", null);
        module102.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_102));
        digitalMany_102.addPropertyChangeListener(pcl, null, "A listener ref");
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_112 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA112", null);
        digitalMany_102.getChild(0).connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_112));

        Module module103 = InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");   // NOI18N
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_103 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA103", null);
        module103.getRootSocket().connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_103));
        digitalMany_103.addPropertyChangeListener(pcl, null, "A listener ref");
        jmri.jmrit.logixng.actions.DigitalMany digitalMany_113 =
                new jmri.jmrit.logixng.actions.DigitalMany("IQDA113", null);
        digitalMany_103.getChild(0).connect(
                InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(digitalMany_113));

        moduleTable.actionPerformed(null); // show table
        JFrame logixNGFrame = JFrameOperator.waitJFrame(Bundle.getMessage("TitleLogixNGModuleTable"), true, true);  // NOI18N
        Assert.assertNotNull("Found LogixNG Frame", logixNGFrame);  // NOI18N

        // Delete IQM102, respond No
        Thread t1 = createModalDialogOperatorThread_WithListenerRefs(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonNo"), listenerRefs);  // NOI18N
        moduleTable.deletePressed("IQM102");  // NOI18N
        t1.join();
        Module mod102 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM102");  // NOI18N
        Assert.assertNotNull("Verify IQM102 Not Deleted", mod102);  // NOI18N
        MaleSocket digMany102 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA102");   // NOI18N
        Assert.assertNotNull("Verify IQDA102 Not Deleted", digMany102);  // NOI18N
        MaleSocket digMany112 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA112");   // NOI18N
        Assert.assertNotNull("Verify IQDA112 Not Deleted", digMany112);  // NOI18N

        // Delete IQM103, respond Yes
        Thread t2 = createModalDialogOperatorThread_WithListenerRefs(Bundle.getMessage("QuestionTitle"), Bundle.getMessage("ButtonYes"), listenerRefs);  // NOI18N
        moduleTable.deletePressed("IQM103");  // NOI18N
        t2.join();
        Module mod103 = jmri.InstanceManager.getDefault(ModuleManager.class).getBySystemName("IQM103");  // NOI18N
        Assert.assertNull("Verify IQM103 Is Deleted", mod103);  // NOI18N
        MaleSocket digMany103 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA103");   // NOI18N
        Assert.assertNull("Verify IQDA103 Is Deleted", digMany103);  // NOI18N
        MaleSocket digMany113 = InstanceManager.getDefault(DigitalActionManager.class).getBySystemName("IQDA113");   // NOI18N
        Assert.assertNull("Verify IQDA113 Is Deleted", digMany113);  // NOI18N

        JUnitUtil.dispose(logixNGFrame);
    }

    Thread createModalDialogOperatorThread(String dialogTitle, String buttonText, String labelText) {
        RuntimeException e = new RuntimeException("Caller");
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            try {
            new JLabelOperator(jdo, labelText);     // Throws exception if not found
            } catch (Exception e2) {
                e.printStackTrace();
                throw e2;
            }
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    private JEditorPane findTextArea(Container container) {
        for (Component component : container.getComponents()) {
//            System.out.format("Component: %s,%n", component.getClass().getName());
            if (component instanceof JEditorPane) {
                return (JEditorPane) component;
            }
            if (component instanceof Container) {
                JEditorPane textArea = findTextArea((Container) component);
                if (textArea != null) return textArea;
            }
        }
        return null;
    }

    Thread createModalDialogOperatorThread_WithListenerRefs(String dialogTitle, String buttonText, String listenerRefs) {
        RuntimeException e = new RuntimeException("Caller");
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(dialogTitle);
            JButtonOperator jbo = new JButtonOperator(jdo, buttonText);
            try {
            JEditorPane textArea = findTextArea((Container) jdo.getComponent(0));
            Assert.assertNotNull(textArea);
            Assert.assertEquals(listenerRefs, textArea.getText());
//            if (textArea != null) {
//                System.out.format("TextArea found: '%s'%n", textArea.getText());
//            } else {
//                System.out.format("TextArea not found%n");
//            }
//            new JLabelOperator(jdo, labelText);     // Throws exception if not found
            } catch (Exception e2) {
                e.printStackTrace();
                throw e2;
            }
            jbo.pushNoBlock();
        });
        t.setName(dialogTitle + " Close Dialog Thread");
        t.start();
        return t;
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initLogixManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initLogixNGManager();

//        InstanceManager.getDefault(LogixNGPreferences.class).setLimitRootActions(false);

        InstanceManager.getDefault(UserPreferencesManager.class)
                .setSimplePreferenceState(ConditionalNGEditor.class.getName()+".AutoSystemName", true);

        FemaleSocketManager.SocketType socketType = InstanceManager.getDefault(FemaleSocketManager.class)
                .getSocketTypeByType("DefaultFemaleDigitalActionSocket");
        InstanceManager.getDefault(ModuleManager.class).createModule("IQM101", "Module 101", socketType);
        InstanceManager.getDefault(ModuleManager.class).createModule("IQM102", "Module 102", socketType);
        InstanceManager.getDefault(ModuleManager.class).createModule("IQM103", "Module 103", socketType);
        InstanceManager.getDefault(ModuleManager.class).createModule("IQM104", "Module 104", socketType);

        helpTarget = "package.jmri.jmrit.beantable.LogixNGModuleTable";
        a = new LogixNGModuleTableAction();
    }

    @AfterEach
    @Override
    public void tearDown() {
        a = null;
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGModuleTableActionTest.class);

}
