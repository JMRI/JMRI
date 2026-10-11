package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JCheckBox;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.TurnoutManager;
import jmri.jmrit.beantable.turnout.TurnoutTableDataModel;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialTurnoutManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JemmyUtil;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 * Swing tests for the turnout table.
 *
 * @author Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class TurnoutTableWindowTest {

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() {

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
        assertNotNull( jcbo.getSelectedObjects(), "AR selected");

        // Find the Show Feedback information checkbox
        JLabelOperator jlo = new JLabelOperator(jfo,Bundle.getMessage("ShowFeedbackInfo"));
        JCheckBox jcb = (JCheckBox) jlo.getLabelFor();
        jcbo = new JCheckBoxOperator(jcb);
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        assertNotNull( jcbo.getSelectedObjects(), "FBbox selected");

        // Find the Show Lock information checkbox
        jlo = new JLabelOperator(jfo,Bundle.getMessage("ShowLockInfo"));
        jcb = (JCheckBox) jlo.getLabelFor();
        jcbo = new JCheckBoxOperator(jcb);
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        assertNotNull( jcbo.getSelectedObjects(), "LKbox selected");

        // Find the Show Turnout Speed details checkbox
        jlo = new JLabelOperator(jfo,Bundle.getMessage("ShowTurnoutSpeedDetails"));
        jcb = (JCheckBox) jlo.getLabelFor();
        jcbo = new JCheckBoxOperator(jcb);
        // Click checkbox to select Show feedback information
        jcbo.doClick();
        assertNotNull( jcbo.getSelectedObjects(), "TSbox selected");

        // Find the Add... button
        JButtonOperator jbo = new JButtonOperator(jfo,Bundle.getMessage("ButtonAdd"));
        // Click button to open Add Turnout pane
        jbo.doClick();

        // Find Add Turnout pane by name
        JFrameOperator afo = new JFrameOperator(Bundle.getMessage("TitleAddTurnout"));

        // Find hardware address field
        jlo = new JLabelOperator(afo,Bundle.getMessage("LabelHardwareAddress"));
        JTextField hwAddressField = (JTextField) jlo.getLabelFor();
        assertNotNull( hwAddressField, "hwAddressTextField");

        // Find system combobox
        JComboBoxOperator cbo = new JComboBoxOperator(afo); // finds first combo box.
        // set to "C/MRI"
        cbo.selectItem("CMRI");
        // set address to "a" (invalid for C/MRI, but valid for Internal)
        hwAddressField.setText("a");
        // test silent entry validation
        boolean _valid = hwAddressField.getInputVerifier().verify(hwAddressField);
        assertFalse( _valid, "invalid entry");

        // set address to "1"
        // The following line works on the CI servers, but not in some standalone cases
        JTextFieldOperator jtfo = new JTextFieldOperator(hwAddressField);
        jtfo.setText("1");

        jbo = new JButtonOperator(afo,Bundle.getMessage("ButtonCreate"));
        jbo.setEnabled(true); // skip validation

        assertEquals( "1", hwAddressField.getText(), "name content");

        cbo.selectItem("Internal");
        jtfo.setText("1");
        assertEquals( internal, cbo.getSelectedItem(),
                "Selected system item"); // this connection type is always available

        // Find the Add Create button
        jbo = new JButtonOperator(afo,Bundle.getMessage("ButtonCreate"));
        // Click button to add turnout
        jbo.doClick();
        // Ask to close Add pane
        afo.requestClose();
        
        // Open the Edit Turnout IT1 pane, 
        // Find the Edit button in EDITCOL of line 0 (for LT1)
        JTableOperator tbl = new JTableOperator(jfo, 0);
        tbl.clickOnCell(0, TurnoutTableDataModel.EDITCOL);

        // Find Edit Turnout pane by name
        JFrameOperator efo = new JFrameOperator("Edit Turnout IT1");
        // Find the Edit Cancel button
        jbo = new JButtonOperator(efo,Bundle.getMessage("ButtonCancel"));
        // Click button to cancel edit turnout
        jbo.doClick();
        // Ask to close Edit pane
        efo.requestClose();

        // Ask to close turnout table window
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

        // check that turnout was created
        assertNotNull(jmri.InstanceManager.turnoutManagerInstance().getTurnout("IT1"));
    }

    @Test
    @DisabledIfHeadless
    public void testMenus() {

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
        JUnitUtil.dispose(jfo.getWindow());
        jfo.waitClosed();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
