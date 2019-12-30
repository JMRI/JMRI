package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;
import javax.swing.*;
import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.*;
import org.junit.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/**
 * Swing tests for the light table.
 *
 * @author	Bob Jacobsen Copyright 2009, 2010, 2017
 */
public class LightTableWindowTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // ask for the window to open
        LightTableAction a = new LightTableAction();
        a.actionPerformed(new java.awt.event.ActionEvent(a, 1, ""));

        // Find new table window by name
        JmriJFrame ft = JmriJFrame.getFrame(Bundle.getMessage("TitleLightTable"));
        new EventTool().waitNoEvent(0);

        // Find the Add... button and open Add Light pane
        JUnitUtil.pressButton(ft, Bundle.getMessage("ButtonAdd"));

        // Find Add Light pane by name
        JmriJFrame fa = JmriJFrame.getFrame(Bundle.getMessage("TitleAddLight"));
        Assert.assertNotNull("Add window found", fa);

        // Find hardware address field
        JTextField hwAddressField = JTextFieldOperator.findJTextField(fa, new NameComponentChooser("hwAddressTextField"));
        Assert.assertNotNull("hwAddressTextField", hwAddressField);

        // set to "1"
        new JTextFieldOperator(hwAddressField).enterText("1");
        JButton createButton = JButtonOperator.findJButton(fa, new NameComponentChooser("createButton"));
        createButton.setEnabled(true); // skip validation

        new EventTool().waitNoEvent(0);
        Assert.assertEquals("name content", "1", hwAddressField.getText());

        // Find system combobox
        JComboBox<?> prefixBox = JComboBoxOperator.findJComboBox(fa, new NameComponentChooser("prefixBox"));
        Assert.assertNotNull(prefixBox);
        // set to "Internal"
        prefixBox.setSelectedItem("Internal");
        LightManager internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class).getLightManager();
        Assert.assertEquals("Selected system item", internal, prefixBox.getSelectedItem()); // this connection type is always available

        // Find and click the Add Create button to add turnout
        JUnitUtil.pressButton(fa, Bundle.getMessage("ButtonCreate"));
        // Ask to close Add pane
        new JFrameOperator(fa).dispose();

        // don't test edit pane, identical to create pane
        // Ask to close turnout table window
        new JFrameOperator(ft).dispose();

        new EventTool().waitNoEvent(0);

        // check that light was created
        Assert.assertNotNull(jmri.InstanceManager.lightManagerInstance().getLight("IL1"));
    }

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
        JUnitUtil.tearDown();
    }
}
