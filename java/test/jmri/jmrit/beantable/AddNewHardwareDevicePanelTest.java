package jmri.jmrit.beantable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import jmri.swing.ManagerComboBox;

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
        AddNewHardwareDevicePanel t = new AddNewHardwareDevicePanel(new JTextField(), new JTextField(), new ManagerComboBox<>(),
                new JSpinner(), new JCheckBox(), okbutton, cancellistener, otherlistener, new JLabel());
        assertNotNull("exists", t);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.tearDown();
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
        AddNewHardwareDevicePanel instance = new AddNewHardwareDevicePanel(new JTextField(), new JTextField(), new ManagerComboBox<>(),
                new JSpinner(), new JCheckBox(), new JButton(), cancellistener, otherlistener, statusBar);
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
