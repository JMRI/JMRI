package jmri.jmrix.dccpp.swing.messages;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JLabel;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/** Tests for ConfigureMessages. */
@DisabledIfHeadless
public class ConfigureMessagesTest {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;
    private MessagesPanel panel;
    private EditorScaffold editor;

    private ConfigureMessages openDialog(EditorScaffold ed) {
        ThreadingUtil.runOnGUI(() -> ConfigureMessages.editConfigureMessages(ed, panel));
        ConfigureMessages dialog = ConfigureMessages.getOpenDialog();
        Assertions.assertNotNull(dialog);
        return dialog;
    }

    private static boolean hasLabel(Container container, String text) {
        for (Component c : container.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel) c).getText())) {
                return true;
            }
            if (c instanceof Container && hasLabel((Container) c, text)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testApplyWritesWindowFields() {
        ConfigureMessages dialog = openDialog(null);
        ThreadingUtil.runOnGUI(() -> {
            dialog._expiryTextField.setText("45");
            dialog._maxLinesTextField.setText("4");
            dialog._raiseCheckBox.setSelected(false);
            dialog._autoOpenCheckBox.setSelected(false);
            dialog._playSoundCheckBox.setSelected(true);
            dialog._soundFileTextField.setText("program:resources/sounds/Bell.wav");
            dialog.applyTo(panel);
        });
        Assertions.assertEquals(45, panel.getExpirySeconds());
        Assertions.assertEquals(4, panel.getMaxLines());
        Assertions.assertFalse(panel.isRaiseOnMessage());
        Assertions.assertFalse(panel.isAutoOpen());
        Assertions.assertTrue(panel.isPlaySound());
        Assertions.assertEquals("program:resources/sounds/Bell.wav", panel.getSoundFile());
    }

    @Test
    public void testDialogShowsCurrentValues() {
        panel.setExpirySeconds(12);
        panel.setAutoOpen(false);
        ConfigureMessages dialog = openDialog(null);
        Assertions.assertEquals("12", dialog._expiryTextField.getText());
        Assertions.assertFalse(dialog._autoOpenCheckBox.isSelected());
    }

    @Test
    public void testWindowDialogHidesPanelRows() {
        ConfigureMessages dialog = openDialog(null);
        Assertions.assertTrue(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_AutoOpen")));
        Assertions.assertFalse(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_Foreground")));
        Assertions.assertFalse(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_Columns")));
    }

    @Test
    public void testPanelDialogHidesWindowRows() {
        ThreadingUtil.runOnGUI(() -> editor = new EditorScaffold("ConfigureMessagesTest"));
        ConfigureMessages dialog = openDialog(editor);
        Assertions.assertTrue(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_Foreground")));
        Assertions.assertTrue(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_Columns")));
        Assertions.assertFalse(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_AutoOpen")));
        Assertions.assertFalse(hasLabel(dialog.getContentPane(), Bundle.getMessage("ConfigureMessages_Raise")));
    }

    @Test
    public void testPanelDialogAppliesColorsAndSize() {
        ThreadingUtil.runOnGUI(() -> editor = new EditorScaffold("ConfigureMessagesTest"));
        ConfigureMessages dialog = openDialog(editor);
        ThreadingUtil.runOnGUI(() -> {
            dialog._foreground = Color.RED;
            dialog._background = Color.YELLOW;
            dialog._columnsTextField.setText("30");
            dialog._linesTextField.setText("3");
            dialog.applyTo(panel);
        });
        Assertions.assertEquals(Color.RED, panel.getMessageForeground());
        Assertions.assertEquals(Color.YELLOW, panel.getMessageBackground());
        Assertions.assertEquals(new Dimension(30, 3), panel.getMessagesSize());
    }

    @Test
    public void testBlankColumnsMeansAutoSize() {
        panel.setMessagesSize(new Dimension(30, 3));
        ThreadingUtil.runOnGUI(() -> editor = new EditorScaffold("ConfigureMessagesTest"));
        ConfigureMessages dialog = openDialog(editor);
        ThreadingUtil.runOnGUI(() -> {
            dialog._columnsTextField.setText("");
            dialog._linesTextField.setText("");
            dialog.applyTo(panel);
        });
        Assertions.assertNull(panel.getMessagesSize());
    }

    @Test
    public void testBadNumberAppliesNothing() {
        ConfigureMessages dialog = openDialog(null);
        dialog._expiryTextField.setText("45");
        dialog._maxLinesTextField.setText("abc");
        dialog._autoOpenCheckBox.setSelected(false);
        Assertions.assertThrows(NumberFormatException.class, () -> dialog.applyTo(panel));
        Assertions.assertEquals(30, panel.getExpirySeconds());
        Assertions.assertTrue(panel.isAutoOpen());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        panel = new MessagesPanel(null, false);
        panel.setMemo(memo);
    }

    @AfterEach
    public void tearDown() {
        ConfigureMessages dialog = ConfigureMessages.getOpenDialog();
        if (dialog != null) {
            JUnitUtil.dispose(dialog);
        }
        if (editor != null) {
            JUnitUtil.dispose(editor);
        }
        panel.dispose();
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        editor = null;
        panel = null;
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
