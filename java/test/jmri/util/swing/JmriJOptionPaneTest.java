package jmri.util.swing;

import java.util.Locale;

import javax.swing.*;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;

/**
 * Tests for JmriJOptionPane.
 * @author Steve Young Copyright (C) 2023
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class JmriJOptionPaneTest {

    @Test
    public void testShowConfirmDialogOkCanel() {

        JFrame frame = new JFrame("JFrame Window");
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Label in JFrame");

        panel.add(label);
        frame.add(panel);
        frame.pack();
        jmri.util.ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        JFrameOperator jfo = new JFrameOperator("JFrame Window");

        Thread canelButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "OptionPane Title", Bundle.getMessage("ButtonCancel"));

        int optionPressed = JmriJOptionPane.showConfirmDialog(panel, "Message", "OptionPane Title", JmriJOptionPane.OK_CANCEL_OPTION );
        JUnitUtil.waitFor(()-> !(canelButtonThread.isAlive()), "cancel dialog finished");
        Assertions.assertEquals(JmriJOptionPane.CANCEL_OPTION, optionPressed);

        Thread okButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "OptionPane Title", Bundle.getMessage("ButtonOK"));
        optionPressed = JmriJOptionPane.showConfirmDialog(panel, "Message", "OptionPane Title", JmriJOptionPane.OK_CANCEL_OPTION );
        JUnitUtil.waitFor(()-> !(okButtonThread.isAlive()), "ok dialog finished");  // NOI18N
        Assertions.assertEquals(JmriJOptionPane.OK_OPTION, optionPressed);

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testMessageDialogueNoOptions() {
        Thread okButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "Error Title", Bundle.getMessage("ButtonOK"));
        JmriJOptionPane.showMessageDialog(null, "testMessageDialogMessage", "Error Title", JmriJOptionPane.ERROR_MESSAGE);
        JUnitUtil.waitFor(()-> !( okButtonThread.isAlive()), "ok error dialog finished");
    }

    @Test
    public void testYesNoOptions() {

        JFrame frame = new JFrame("JFrame testYesNoOptions");
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Label in testYesNoOptions JFrame");

        panel.add(label);
        frame.add(panel);
        frame.pack();
        frame.setLocation(100, 100);
        jmri.util.ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        JFrameOperator jfo = new JFrameOperator("JFrame testYesNoOptions");

        Thread yesButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "Question Title", Bundle.getMessage("ButtonYes"));

        int result = JmriJOptionPane.showConfirmDialog(panel,"Yes or no message",
            "Question Title", JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.QUESTION_MESSAGE);
        JUnitUtil.waitFor(()-> !( yesButtonThread.isAlive()), "yes dialog finished");
        Assertions.assertEquals(JmriJOptionPane.YES_OPTION, result);

        Thread noButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "Question Title", Bundle.getMessage("ButtonNo"));
        result = JmriJOptionPane.showConfirmDialog(panel,"Yes or no message",
            "Question Title", JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.QUESTION_MESSAGE);
        JUnitUtil.waitFor(()-> !( noButtonThread.isAlive()), "no dialog finished");
        Assertions.assertEquals(JmriJOptionPane.NO_OPTION, result);

        jfo.requestClose();
        jfo.waitClosed();
    }
    
    @Test
    public void testModalNoFrameMessage() {
        Thread okButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "Error Title", Bundle.getMessage("ButtonOK"));
        JmriJOptionPane.showMessageDialog(null, "testMessageDialogMessage", "Error Title", JmriJOptionPane.ERROR_MESSAGE);
        JUnitUtil.waitFor(()-> !( okButtonThread.isAlive()), "ok error dialog finished");
    }

    @Test
    public void testShowStringInput() {
        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(UIManager.getString("OptionPane.inputDialogTitle",
            Locale.getDefault()));
            JTextFieldOperator jtfo = new JTextFieldOperator(jdo,0);
            Assertions.assertEquals("Initial Foo", jtfo.getText());

            jtfo.setText("New Foo");
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
            jbo.pushNoBlock();
        });
        t.setName("Close String Input Dialog Thread");
        t.start();
        String s = JmriJOptionPane.showInputDialog(null, "Enter a new String value for Foo", "Initial Foo");
        JUnitUtil.waitFor(()-> !( t.isAlive()), "string input dialog finished");
        Assertions.assertEquals("New Foo", s);
    }

    @Test
    public void testCancelInputString() {

        JFrame frame = new JFrame("JFrame testCancelInputString");
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Label in testCancelInputString JFrame");

        panel.add(label);
        frame.add(panel);
        frame.pack();
        frame.setLocation(150, 150);
        jmri.util.ThreadingUtil.runOnGUI( () -> frame.setVisible(true) );
        JFrameOperator jfo = new JFrameOperator("JFrame testCancelInputString");

        Thread t = new Thread(() -> {
            // constructor for jdo will wait until the dialog is visible
            JDialogOperator jdo = new JDialogOperator(UIManager.getString("OptionPane.inputDialogTitle", Locale.getDefault()));
            JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonCancel"));
            jbo.pushNoBlock();
        });
        t.setName("cancel String Input Dialog Thread");
        t.start();
        String s = JmriJOptionPane.showInputDialog(panel, "Cancel Enter a new String value for Foo", "Initial Foo Cancel");
        JUnitUtil.waitFor(()-> !( t.isAlive()), "cancel string input dialog finished");
        Assertions.assertNull( s);

        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testInfoDialog() {
        Thread okButtonThread = JemmyUtil.createModalDialogOperatorThread(
            UIManager.getString("OptionPane.messageDialogTitle", Locale.getDefault()), Bundle.getMessage("ButtonOK"));
        JmriJOptionPane.showMessageDialog(null, "testInfoDialogMessage" );
        JUnitUtil.waitFor(()-> !( okButtonThread.isAlive()), "ok info dialog finished");
    }

    @Test
    public void testCustomOptionsDialog(){
        Thread okButtonThread = JemmyUtil.createModalDialogOperatorThread(
            "My Title", "Option B");
        int value = JmriJOptionPane.showOptionDialog(null, "My Custom Message", "My Title",
            JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE, null, new String[]{"Option A", "Option B", "Option C"}, "Option C");
        JUnitUtil.waitFor(()-> !( okButtonThread.isAlive()), "ok info dialog finished");
        Assertions.assertEquals(1, value,"returns Option B at position 1 in Array");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
