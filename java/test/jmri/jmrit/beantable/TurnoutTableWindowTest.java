package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialTurnoutManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;
import org.junit.*;
import org.netbeans.jemmy.operators.*;

/**
 * Swing tests for the turnout table.
 *
 * @author	Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class TurnoutTableWindowTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TurnoutManager cmri = new SerialTurnoutManager(new CMRISystemConnectionMemo("C", "CMRI"));
        InstanceManager.setTurnoutManager(cmri);
        TurnoutManager internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class).getTurnoutManager();

        // ask for the window to open
        TurnoutTableAction a = new TurnoutTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JFrameOperator jfo = new JFrameOperator(Bundle.getMessage("TitleTurnoutTable"));

        // Find the Automatic retry checkbox
        JCheckBoxOperator jcbo = new JCheckBoxOperator(jfo,Bundle.getMessage("AutomaticRetry"));
        // Click checkbox to select automatic retry
        jcbo.doClick();
        Assert.assertNotNull("AR selected", jcbo.getSelectedObjects());

        // Find the Show Feedback information checkbox
        jcbo = new JCheckBoxOperator(jfo,Bundle.getMessage("ShowFeedbackInfo"));
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        Assert.assertNotNull("FBbox selected", jcbo.getSelectedObjects());

        // Find the Show Lock information checkbox
        jcbo = new JCheckBoxOperator(jfo,Bundle.getMessage("ShowLockInfo"));
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        Assert.assertNotNull("LKbox selected", jcbo.getSelectedObjects());

        // Find the Show Turnout Speed details checkbox
        jcbo = new JCheckBoxOperator(jfo,Bundle.getMessage("ShowTurnoutSpeedDetails"));
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        Assert.assertNotNull("TSbox selected", jcbo.getSelectedObjects());

        // Find the Add... button
        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("ButtonAdd"));
        // Click button to open Add Turnout pane
        jbo.doClick();

        // Find Add Turnout pane by name
        JFrameOperator afo = new JFrameOperator(Bundle.getMessage("TitleAddTurnout"));

        // Find hardware address field
        JLabelOperator jlo = new JLabelOperator(afo,Bundle.getMessage("LabelHardwareAddress"));
        JTextField hwAddressField = (JTextField) jlo.getLabelFor();
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // Find system combobox
        JComboBoxOperator cbo = new JComboBoxOperator(afo); // finds first combo box.
        // set to "C/MRI"
        cbo.selectItem("CMRI");
        // set address to "a" (invalid for C/MRI, but valid for Internal)
        hwAddressField.setText("a");
        // test silent entry validation
        boolean _valid = hwAddressField.getInputVerifier().verify(hwAddressField);
        Assert.assertEquals("invalid entry", false, _valid);

        // set address to "1"
        // The following line works on the CI servers, but not in some standalone cases
        JTextFieldOperator jtfo = new JTextFieldOperator(hwAddressField);
        jtfo.setText("1");

        jbo = new JButtonOperator(afo,Bundle.getMessage("ButtonCreate"));
        jbo.setEnabled(true); // skip validation

        Assert.assertEquals("name content", "1", hwAddressField.getText());

        cbo.selectItem("Internal");
        jtfo.setText("1");
        Assert.assertEquals("Selected system item", internal, cbo.getSelectedItem()); // this connection type is always available

        // Find the Add Create button
        jbo = new JButtonOperator(afo,Bundle.getMessage("ButtonCreate"));
        // Click button to add turnout
        jbo.doClick();
        // Ask to close Add pane
        afo.requestClose();

        // Open the Edit Turnout IT1 pane, how to find & click the LT1 Edit col button?
        // Find the Edit button in EDITCOL of line 0 (for LT1)
        //AbstractButtonFinder edfinder = new AbstractButtonFinder("Edit");
        //JButton editbutton = (JButton) edfinder.find(ft, 0);
        //Assert.assertNotNull(editbutton);
        // Click button to edit turnout
        //getHelper().enterClickAndLeave(new MouseEventData(this, editbutton));
        // open Edit pane by method instead
        Turnout it1 = InstanceManager.turnoutManagerInstance().getTurnout("IT1");
        a.editButton(it1); // open edit pane

        // Find Edit Turnout pane by name
        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        // Find the Edit Cancel button
        jbo = new JButtonOperator(efo,Bundle.getMessage("ButtonCancel"));
        // Click button to cancel edit turnout
        jbo.doClick();
        // Ask to close Edit pane
        efo.requestClose();

        // Ask to close turnout table window
        jfo.requestClose();

        // check that turnout was created
        Assert.assertNotNull(jmri.InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
    }

    @Test
    public void testMenus() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        TurnoutTableAction a = new TurnoutTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find Turnout table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleTurnoutTable"));
        JFrameOperator jfo = new JFrameOperator(ft);

        // no need to test Automation menu, has its own tests in jmri.jmrit.turnoutoperation

        // ask for the Speeds menu to open
        jmri.util.ThreadingUtil.runOnGUIEventually(() -> {
            a.setDefaultSpeeds(ft);
        });
        // Find new dialog window by name and then press the cancel button.
        JemmyUtil.pressDialogButton(Bundle.getMessage("TurnoutGlobalSpeedMessageTitle"), Bundle.getMessage("ButtonCancel"));

        // Ask to close table window
        jfo.requestClose();

    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
