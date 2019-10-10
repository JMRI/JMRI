package jmri.jmrit.beantable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddNewHardwareDevicePanelTest {

    @Test
    public void testCTor() {
        ActionListener createlistener = (ActionEvent e) -> {
        };
        ActionListener cancellistener = (ActionEvent e) -> {
        };
        ActionListener otherlistener = (ActionEvent e) -> {
        };
        JButton okbutton = new JButton("ButtonOK");
        okbutton.addActionListener(createlistener);
        JTextField systemName = new JTextField();
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        AddNewHardwareDevicePanel instance = new AddNewHardwareDevicePanel(
                systemName,
                new SystemNameValidator(systemName, manager),
                new JTextField(),
                new ManagerComboBox<>(Arrays.asList(manager)),
                new JSpinner(),
                new JCheckBox(),
                new JButton(),
                cancellistener,
                otherlistener,
                new JLabel());
        assertNotNull("exists", instance);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddNewHardwareDevicePanelTest.class);
    /**
     * Test of setStatusBarText method, of class AddNewHardwareDevicePanel.
     */
    @Test
    public void testSetStatusBarText() {
        ActionListener cancellistener = (ActionEvent e) -> {
        };
        ActionListener otherlistener = (ActionEvent e) -> {
        };
        JLabel statusBar = new JLabel();
        JTextField systemName = new JTextField();
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        AddNewHardwareDevicePanel instance = new AddNewHardwareDevicePanel(
                systemName,
                new SystemNameValidator(systemName, manager),
                new JTextField(),
                new ManagerComboBox<>(Arrays.asList(manager)),
                new JSpinner(),
                new JCheckBox(),
                new JButton(),
                cancellistener,
                otherlistener,
                statusBar);
        instance.setStatusBarText(null);
        assertEquals("null yields empty string", "", statusBar.getText());
        instance.setStatusBarText("foo bar");
        assertEquals("simple message", "foo bar", statusBar.getText());
        instance.setStatusBarText("  foo bar  ");
        assertEquals("trimmed message", "foo bar", statusBar.getText());
        instance.setStatusBarText("<html>foo bar</html>");
        assertEquals("single line html", "<html>foo bar</html>", statusBar.getText());
        instance.setStatusBarText("<html>foo<br>bar</html>");
        assertEquals("mutli line html", "<html>foo</html>", statusBar.getText());
    }

}
