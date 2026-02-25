package jmri.jmrit.beantable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.LightManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Swing tests for the light table.
 *
 * @author Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class LightTableWindowTest {

    @Test
    @DisabledIfHeadless
    public void testShowAndClose() {

        // ask for the window to open
        LightTableAction a = new LightTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleLightTable"));
        new QueueTool().waitEmpty();

        // Find the Add... button and open Add Light pane
        JUnitUtil.pressButton(ft, Bundle.getMessage("ButtonAdd"));

        // Find Add Light pane by name
        JmriJFrame fa = JmriJFrame.getFrame(Bundle.getMessage("TitleAddLight"));
        assertNotNull( fa, "Add window found");

        // Find hardware address field
        JTextField hwAddressField = JTextFieldOperator.findJTextField(fa, new NameComponentChooser("hwAddressTextField"));
        assertNotNull( hwAddressField, "hwAddressTextField");

        // set to "1"
        new JTextFieldOperator(hwAddressField).typeText("1");
        JButton createButton = JButtonOperator.findJButton(fa, new NameComponentChooser("createButton"));
        createButton.setEnabled(true); // skip validation

        new QueueTool().waitEmpty();
        assertEquals( "1", hwAddressField.getText(), "name content");

        // Find system combobox
        JComboBox<?> prefixBox = JComboBoxOperator.findJComboBox(fa, new NameComponentChooser("prefixBox"));
        assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        LightManager internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class).getLightManager();
        assertEquals( internal, prefixBox.getSelectedItem(),
            "Selected system item"); // this connection type is always available

        // Find and click the Add Create button to add turnout
        JUnitUtil.pressButton(fa, Bundle.getMessage("ButtonCreate"));
        // Ask to close Add pane
        new JFrameOperator(fa).dispose();

        // don't test edit pane, identical to create pane
        // Ask to close turnout table window
        new JFrameOperator(ft).dispose();

        new QueueTool().waitEmpty();

        // check that light was created
        assertNotNull(jmri.InstanceManager.lightManagerInstance().getLight("IL1"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initTimeProviderManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
