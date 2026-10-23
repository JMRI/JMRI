package jmri.jmrix.dccpp.swing.messages;

import java.awt.*;
import java.io.File;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.swing.*;

import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableFactory.DoAfter;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriColorChooser;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.WrapLayout;

/** Settings dialog for a DCC-EX Messages window or panel icon. */
public class ConfigureMessages extends JmriJFrame {

    private static ConfigureMessages editPositionableFrame;

    private final Editor editor;
    private final MessagesConfiguration messagesConfiguration;
    private final MessagesPositionable newPositionable;
    private final DoAfter doAfter;

    private final JComboBox<DCCppConnection> _memoComboBox = new JComboBox<>();
    final JTextField _expiryTextField = new JTextField(5);
    final JTextField _maxLinesTextField = new JTextField(5);
    final JCheckBox _raiseCheckBox = new JCheckBox();
    final JCheckBox _autoOpenCheckBox = new JCheckBox();
    final JCheckBox _playSoundCheckBox = new JCheckBox();
    final JTextField _soundFileTextField = new JTextField(25);
    final JTextField _columnsTextField = new JTextField(5);
    final JTextField _linesTextField = new JTextField(5);
    Color _foreground;
    Color _background;

    public static void createConfigureMessages(Editor editor, DoAfter doAfter) {
        closeDialog(null);
        MessagesPositionable pos = new MessagesPositionable(editor);
        editPositionableFrame = new ConfigureMessages(editor, pos.getMessagesPanel(), pos, doAfter);
        editPositionableFrame.initComponents();
    }

    public static void editConfigureMessages(@CheckForNull Editor editor, MessagesConfiguration config) {
        closeDialog(null);
        editPositionableFrame = new ConfigureMessages(editor, config, null, null);
        editPositionableFrame.initComponents();
    }

    @CheckForNull
    static ConfigureMessages getOpenDialog() {
        if (editPositionableFrame != null && editPositionableFrame.isDisplayable()) {
            return editPositionableFrame;
        }
        return null;
    }

    private static void closeDialog(@CheckForNull Editor editor) {
        if (editPositionableFrame != null) {
            editPositionableFrame.setVisible(false);
            editPositionableFrame.dispose();

            if (editor != null) {
                editor.setVisible(true);
            }
        }
    }

    private ConfigureMessages(@CheckForNull Editor editor, MessagesConfiguration config,
            @CheckForNull MessagesPositionable newPositionable, @CheckForNull DoAfter doAfter) {
        super(Bundle.getMessage(newPositionable != null ? "AddMessagesPositionable" : "EditMessages"), false, false);
        this.editor = editor;
        this.messagesConfiguration = config;
        this.newPositionable = newPositionable;
        this.doAfter = doAfter;
        _foreground = config.getMessageForeground();
        _background = config.getMessageBackground();
    }

    @Override
    public void initComponents() {
        addHelpMenu("html.hardware.dcc-ex.index_messages", true);
        setLocation(50, 30);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        List<DCCppSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);
        for (DCCppSystemConnectionMemo connection : systemConnections) {
            DCCppConnection c = new DCCppConnection(connection);
            _memoComboBox.addItem(c);
            if (connection == messagesConfiguration.getMemo()) {
                _memoComboBox.setSelectedItem(c);
            }
        }
        if (_memoComboBox.getSelectedIndex() == -1 && _memoComboBox.getItemCount() > 0) {
            _memoComboBox.setSelectedIndex(0);
        }
        _memoComboBox.setToolTipText(Bundle.getMessage("ConnectionHint"));

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;

        if (messagesConfiguration.isMemoEditable()) {
            addRow(p, c, "Connection", _memoComboBox);
        }

        _expiryTextField.setText(Integer.toString(messagesConfiguration.getExpirySeconds()));
        addRow(p, c, "ConfigureMessages_Expiry", _expiryTextField);
        _maxLinesTextField.setText(Integer.toString(messagesConfiguration.getMaxLines()));
        addRow(p, c, "ConfigureMessages_MaxLines", _maxLinesTextField);

        if (editor == null) {
            _raiseCheckBox.setSelected(messagesConfiguration.isRaiseOnMessage());
            addRow(p, c, "ConfigureMessages_Raise", _raiseCheckBox);
            _autoOpenCheckBox.setSelected(messagesConfiguration.isAutoOpen());
            addRow(p, c, "ConfigureMessages_AutoOpen", _autoOpenCheckBox);
        }

        _playSoundCheckBox.setSelected(messagesConfiguration.isPlaySound());
        addRow(p, c, "ConfigureMessages_PlaySound", _playSoundCheckBox);

        _soundFileTextField.setText(messagesConfiguration.getSoundFile());
        JButton chooseSound = new JButton(Bundle.getMessage("ConfigureMessages_Choose"));
        chooseSound.addActionListener(e -> chooseSoundFile());
        JPanel soundPanel = new JPanel();
        soundPanel.add(_soundFileTextField);
        soundPanel.add(chooseSound);
        addRow(p, c, "ConfigureMessages_SoundFile", soundPanel);

        if (editor != null) {
            addRow(p, c, "ConfigureMessages_Foreground", colorButton(true));
            addRow(p, c, "ConfigureMessages_Background", colorButton(false));
            Dimension size = messagesConfiguration.getMessagesSize();
            if (size != null) {
                _columnsTextField.setText(Integer.toString(size.width));
                _linesTextField.setText(Integer.toString(size.height));
            }
            addRow(p, c, "ConfigureMessages_Columns", _columnsTextField);
            addRow(p, c, "ConfigureMessages_Lines", _linesTextField);
            c.gridx = 0;
            c.gridwidth = 2;
            c.anchor = GridBagConstraints.WEST;
            p.add(new JLabel(Bundle.getMessage("ConfigureMessages_SizeHelp")), c);
        }

        contentPane.add(p);

        JPanel buttons = new JPanel();
        buttons.setLayout(new WrapLayout());
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        buttons.add(cancel);
        cancel.addActionListener(e -> cancel());
        cancel.setToolTipText(Bundle.getMessage("CancelButtonHint"));

        JButton ok = new JButton(Bundle.getMessage(newPositionable != null ? "ButtonCreate" : "ButtonOK"));
        buttons.add(ok);
        ok.addActionListener(e -> {
            try {
                applyTo(messagesConfiguration);
                if (newPositionable != null) {
                    addPositionable();
                }
                closeDialog(editor);
            } catch (NumberFormatException ex) {
                JmriJOptionPane.showMessageDialog(
                        this,
                        Bundle.getMessage("ErrorMessagesMustBeInteger"),
                        Bundle.getMessage("ErrorTitle"),
                        JmriJOptionPane.ERROR_MESSAGE);
            }
        });
        contentPane.add(buttons);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancel();
            }
        });

        pack();
        setVisible(true);
    }

    private void cancel() {
        if (newPositionable != null) {
            newPositionable.dispose();
        }
        closeDialog(editor);
    }

    private void addPositionable() {
        newPositionable.initComponents();
        newPositionable.setDisplayLevel(Editor.ICONS);
        editor.setNextLocation(newPositionable);
        try {
            editor.putItem(newPositionable, true);
        } catch (Positionable.DuplicateIdException e) {
            // putItem with a null id never throws this
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        if (doAfter != null) {
            doAfter.doAfter(newPositionable);
        }
    }

    private void addRow(JPanel p, GridBagConstraints c, String labelKey, JComponent field) {
        JLabel label = new JLabel(Bundle.getMessage(labelKey));
        label.setLabelFor(field);
        c.gridwidth = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 0;
        p.add(label, c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1.0;
        p.add(field, c);
        c.gridy++;
    }

    private JButton colorButton(boolean foreground) {
        JButton button = new JButton(Bundle.getMessage("ConfigureMessages_Choose"));
        button.setOpaque(true);
        button.setBackground(foreground ? _foreground : _background);
        button.addActionListener(e -> {
            String title = Bundle.getMessage(foreground ? "ConfigureMessages_Foreground" : "ConfigureMessages_Background");
            Color color = JmriColorChooser.showDialog(this, title, foreground ? _foreground : _background);
            if (color != null) {
                if (foreground) {
                    _foreground = color;
                } else {
                    _background = color;
                }
                button.setBackground(color);
            }
        });
        return button;
    }

    private void chooseSoundFile() {
        JFileChooser chooser = new JFileChooser(FileUtil.getExternalFilename("program:resources/sounds"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            _soundFileTextField.setText(FileUtil.getPortableFilename(file));
        }
    }

    /** Write the dialog's values to config; throws NumberFormatException, without changing anything, on bad input. */
    void applyTo(MessagesConfiguration config) {
        int expiry = Integer.parseInt(_expiryTextField.getText().trim());
        int maxLines = Integer.parseInt(_maxLinesTextField.getText().trim());
        Dimension size = null;
        if (editor != null && !_columnsTextField.getText().isBlank() && !_linesTextField.getText().isBlank()) {
            size = new Dimension(
                    Integer.parseInt(_columnsTextField.getText().trim()),
                    Integer.parseInt(_linesTextField.getText().trim()));
        }

        DCCppConnection connection = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex());
        if (config.isMemoEditable() && connection != null) {
            config.setMemo(connection._memo);
        }
        config.setExpirySeconds(expiry);
        config.setMaxLines(maxLines);
        if (editor == null) {
            config.setRaiseOnMessage(_raiseCheckBox.isSelected());
            config.setAutoOpen(_autoOpenCheckBox.isSelected());
        } else {
            config.setMessageForeground(_foreground);
            config.setMessageBackground(_background);
            config.setMessagesSize(size);
        }
        config.setPlaySound(_playSoundCheckBox.isSelected());
        config.setSoundFile(_soundFileTextField.getText().trim());
    }

    private static class DCCppConnection {

        private final DCCppSystemConnectionMemo _memo;

        public DCCppConnection(DCCppSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureMessages.class);

}
