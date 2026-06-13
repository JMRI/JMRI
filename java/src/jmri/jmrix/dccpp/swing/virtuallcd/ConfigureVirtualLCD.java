package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.*;
import java.util.*;

import static jmri.jmrit.display.Editor.ICONS;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.swing.*;

import jmri.jmrit.display.*;
import jmri.jmrit.display.PositionableFactory.DoAfter;
import jmri.jmrix.dccpp.*;
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * Configure a VirtualLCD display.
 *
 * @author Daniel Bergqvist (C) 2026
 */
public class ConfigureVirtualLCD extends JmriJFrame {

    private static ConfigureVirtualLCD editPositionableFrame;

    private final Editor editor;
    private final VirtualLCDConfiguration virtualLCDConfiguration;
    private final DoAfter doAfter;
    private final Map<DCCppSystemConnectionMemo, Integer> highestDisplayNoMap = new HashMap<>();

    private final JComboBox<DCCppConnection> _memoComboBox = new JComboBox<>();
    private final JTextField _numColumnsTextField = new JTextField();
    private final JTextField _numRowsTextField = new JTextField();
    private final JComboBox<DisplayConfig> displayConfigComboBox = new JComboBox<>();
    private final JPanel selectedDisplays = new JPanel();
    private final JComboBox<Integer> displayNoComboBox = new JComboBox<>();
    private final JComboBox<Integer> minDisplayNoComboBox = new JComboBox<>();
    private final JComboBox<Integer> maxDisplayNoComboBox = new JComboBox<>();
    private final Map<Integer, JCheckBox> selectDisplayNoCheckBox = new HashMap<>();

    public static void createConfigureVirtualLCD(Editor editor, DoAfter doAfter) {
        editPositionableFrame = new ConfigureVirtualLCD(editor, null, doAfter);
        editPositionableFrame.initComponents();
    }

    public static void editConfigureVirtualLCD(
            Editor editor, VirtualLCDConfiguration virtualLCDConfiguration) {

        closeDialog(null);

        editPositionableFrame = new ConfigureVirtualLCD(
                editor, virtualLCDConfiguration, null);
        editPositionableFrame.initComponents();
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

    private ConfigureVirtualLCD(
            Editor editor,
            VirtualLCDConfiguration virtualLCDConfiguration,
            DoAfter doAfter) {

        super(Bundle.getMessage("AddVirtualLcdPositionable"), false, false);
        this.editor = editor;
        this.virtualLCDConfiguration = virtualLCDConfiguration;
        this.doAfter = doAfter;
    }

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrix.dccpp.swing.virtuallcd.VirtualLcdPositionableFactory", true);
        setLocation(50, 30);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        List<DCCppSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);
        for (DCCppSystemConnectionMemo connection : systemConnections) {
            DCCppConnection c = new DCCppConnection(connection);
            _memoComboBox.addItem(c);
            if (virtualLCDConfiguration != null && connection == virtualLCDConfiguration.getMemo()) {
                _memoComboBox.setSelectedItem(c);
            }
        }
        if (_memoComboBox.getSelectedIndex() == -1) {
            _memoComboBox.setSelectedIndex(0);
        }
        _memoComboBox.setToolTipText(Bundle.getMessage("ConnectionHint"));

        JLabel memoLabel = new JLabel(Bundle.getMessage("Connection"));
        JLabel displayNoLabel = new JLabel(Bundle.getMessage("DisplayNo"));

        JPanel p = new JPanel();
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        if (virtualLCDConfiguration != null
                && virtualLCDConfiguration.isMemoEditable()) {
            p.add(memoLabel, c);
            memoLabel.setLabelFor(_memoComboBox);
        }
        c.gridy = 1;
        p.add(new JLabel(Bundle.getMessage("ConfigureVirtualLCD_NumColumns")), c);
        displayNoLabel.setLabelFor(_numColumnsTextField);
        c.gridy = 2;
        p.add(new JLabel(Bundle.getMessage("ConfigureVirtualLCD_NumRows")), c);
        displayNoLabel.setLabelFor(_numRowsTextField);
        c.gridy = 3;
        c.gridwidth = 2;
        c.anchor = java.awt.GridBagConstraints.WEST;
        p.add(new JLabel(Bundle.getMessage("ConfigureVirtualLCD_ColumsRowsHelp")), c);
        displayNoLabel.setLabelFor(displayNoComboBox);
        c.gridwidth = 1;
        c.gridy = 4;
        p.add(Box.createVerticalStrut(8), c);
        c.gridy = 5;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(displayNoLabel, c);
        displayNoLabel.setLabelFor(displayNoComboBox);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        if (virtualLCDConfiguration != null
                && virtualLCDConfiguration.isMemoEditable()) {
            p.add(_memoComboBox, c);
        }
        c.gridy = 1;
        c.fill = java.awt.GridBagConstraints.NONE;  // text field will expand
        _numColumnsTextField.setColumns(5);
        p.add(_numColumnsTextField, c);
        c.gridy = 2;
        _numRowsTextField.setColumns(5);
        p.add(_numRowsTextField, c);
        c.gridy = 5;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        for (DisplayConfig dc : DisplayConfig.values()) {
            displayConfigComboBox.addItem(dc);
            if (virtualLCDConfiguration != null
                    && dc == virtualLCDConfiguration.getDisplayConfig()) {
                displayConfigComboBox.setSelectedItem(dc);
            }
        }
        p.add(displayConfigComboBox, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
//        displayNoTextField.setToolTipText(Bundle.getMessage("DisplayNoHint"));


        JPanel allDisplays = new JPanel();
        allDisplays.add(new JLabel(Bundle.getMessage("ConfigureVirtualLCD_AllDisplays")));

        JPanel oneDisplay = new JPanel();
        oneDisplay.add(new JLabel(Bundle.getMessage("DisplayNo")));
        oneDisplay.add(displayNoComboBox);

        JPanel intervalDisplays = new JPanel();
        intervalDisplays.add(new JLabel(Bundle.getMessage("DisplayNo")));
        intervalDisplays.add(minDisplayNoComboBox);
        intervalDisplays.add(new JLabel(" - "));
        intervalDisplays.add(maxDisplayNoComboBox);

        selectedDisplays.setLayout(new BoxLayout(selectedDisplays, BoxLayout.Y_AXIS));

        JPanel cards;   // A panel that uses CardLayout
        CardLayout cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);   // Create the panel that contains the "cards".
        cards.setBorder(BorderFactory.createLineBorder(Color.black));
        cards.add(allDisplays, DisplayConfig.ConfigureVirtualLCD_AllDisplays.name());
        cards.add(oneDisplay, DisplayConfig.ConfigureVirtualLCD_OneDisplay.name());
        cards.add(intervalDisplays, DisplayConfig.ConfigureVirtualLCD_IntervalDisplay.name());
        cards.add(selectedDisplays, DisplayConfig.ConfigureVirtualLCD_SelectedDisplays.name());

        displayConfigComboBox.addItemListener(evt -> {
            cardLayout.show(cards, ((DisplayConfig) evt.getItem()).name());
        });

        c.gridwidth = 2;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 6;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(cards, c);

        c.gridy = 7;
        p.add(new JLabel(Bundle.getMessage("VirtualLcdPositionable_DialogTakesTime")), c);

        contentPane.add(p);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        panel5.add(cancel);
        cancel.addActionListener((e) -> closeDialog(editor));
        cancel.setToolTipText(Bundle.getMessage("CancelButtonHint"));

        if (virtualLCDConfiguration != null) {  // Edit configuration

            Dimension lcdSize = virtualLCDConfiguration.getLCDSize();
            if (lcdSize != null) {
                _numColumnsTextField.setText(Integer.toString(lcdSize.width));
                _numRowsTextField.setText(Integer.toString(lcdSize.height));
            }

            cardLayout.show(cards, virtualLCDConfiguration.getDisplayConfig().name());

            JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
            panel5.add(ok);
            ok.addActionListener((e) -> {
                var memo = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo;
                try {
                    updateVirtualLCD(memo);
                    closeDialog(editor);
                } catch (NumberFormatException ex) {
                    JmriJOptionPane.showMessageDialog(
                            editor,
                            Bundle.getMessage("ErrorDisplayNoMustBeInteger"),
                            Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                }
            });
            ok.setToolTipText(Bundle.getMessage("CreateButtonHint"));

        } else {    // Create new VirtualLCD icon on a panel

            JButton create = new JButton(Bundle.getMessage("ButtonCreate"));
            panel5.add(create);
            create.addActionListener((e) -> {
                var memo = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo;
                try {
                    addVirtualLCD(memo);
                    closeDialog(editor);
                } catch (NumberFormatException ex) {
                    JmriJOptionPane.showMessageDialog(
                            editor,
                            Bundle.getMessage("ErrorDisplayNoMustBeInteger"),
                            Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                }
            });
            create.setToolTipText(Bundle.getMessage("CreateButtonHint"));
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeDialog(editor);
            }
        });

        contentPane.add(panel5);

        // Add listeners for each memo
        for (DCCppSystemConnectionMemo memo : systemConnections) {
            memo.getDCCppTrafficController().addDCCppListener(DCCppInterface.CS_INFO, new DCCppListener(){
                @Override
                public void message(DCCppReply msg) {
                    if (msg.isLCDTextReply()) { // <@ display# line# "message text">
                        int displayNumber = msg.getLCDDisplayNumInt();
                        int highestDisplayNo = highestDisplayNoMap.getOrDefault(memo,0);
                        if (displayNumber > highestDisplayNo) {
                            highestDisplayNoMap.put(memo, displayNumber);
                            if (memo == _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo) {
                                configureDisplaySelection(memo);
                            }
//                            System.out.format("Higest display: %s:%d%n", memo.getUserName(), displayNumber);
                        }
                    }
                }

                @Override
                public void message(DCCppMessage msg) {
                }

                @Override
                public void notifyTimeout(DCCppMessage msg) {
                }
            });
        }

        pack();
        setVisible(true);
    }

    private void configureDisplaySelection(DCCppSystemConnectionMemo memo) {
        selectedDisplays.removeAll();
        displayNoComboBox.removeAllItems();
        minDisplayNoComboBox.removeAllItems();
        maxDisplayNoComboBox.removeAllItems();
        for (int i=0; i <= highestDisplayNoMap.getOrDefault(memo,0); i++) {
            displayNoComboBox.addItem(i);
            if (i == virtualLCDConfiguration.getDisplayNo()) {
                displayNoComboBox.setSelectedItem(i);
            }

            minDisplayNoComboBox.addItem(i);
            if (i == virtualLCDConfiguration.getMinDisplayNo()) {
                minDisplayNoComboBox.setSelectedItem(i);
            }

            maxDisplayNoComboBox.addItem(i);
            if (i == virtualLCDConfiguration.getMaxDisplayNo()) {
                maxDisplayNoComboBox.setSelectedItem(i);
            }

            JCheckBox cb = new JCheckBox(Bundle.getMessage("ConfigureVirtualLCD_SelectedDisplays_CheckBox", i));
            if (virtualLCDConfiguration.getSelectedDisplays().contains(i)) {
                cb.setSelected(true);
            }
            selectDisplayNoCheckBox.put(i, cb);
            selectedDisplays.add(cb);
        }
        this.pack();
    }

    private void addVirtualLCD(DCCppSystemConnectionMemo memo) {

        VirtualLcdPositionable virtLcdPositionable = new VirtualLcdPositionable(editor);
        updateVirtualLCD(memo, virtLcdPositionable.getVirtualLCDPanel());
        virtLcdPositionable.initComponents();
        virtLcdPositionable.setDisplayLevel(ICONS);
        editor.setNextLocation(virtLcdPositionable);
        try {
            editor.putItem(virtLcdPositionable, true);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        if (doAfter != null) {
            doAfter.doAfter(virtLcdPositionable);
        }
    }

    private void updateVirtualLCD(DCCppSystemConnectionMemo memo) {
        updateVirtualLCD(memo, virtualLCDConfiguration);
    }

    private void updateVirtualLCD(DCCppSystemConnectionMemo memo, VirtualLCDConfiguration virtLCDConfig) {
        virtLCDConfig.setMemo(memo);
        if (!_numColumnsTextField.getText().isBlank() && !_numRowsTextField.getText().isBlank()) {
            virtLCDConfig.setLCDSize(new Dimension(
                    Integer.parseInt(_numColumnsTextField.getText()),
                    Integer.parseInt(_numRowsTextField.getText())));
        } else {
            virtLCDConfig.setLCDSize(null);
        }
        virtLCDConfig.setDisplayConfig(displayConfigComboBox.getItemAt(displayConfigComboBox.getSelectedIndex()));
        if (displayNoComboBox.getSelectedIndex() >= 0) {
            virtLCDConfig.setDisplayNo(displayNoComboBox.getItemAt(displayNoComboBox.getSelectedIndex()));
        }
        if (minDisplayNoComboBox.getSelectedIndex() >= 0) {
            virtLCDConfig.setMinDisplayNo(minDisplayNoComboBox.getItemAt(minDisplayNoComboBox.getSelectedIndex()));
        }
        if (maxDisplayNoComboBox.getSelectedIndex() >= 0) {
            virtLCDConfig.setMaxDisplayNo(maxDisplayNoComboBox.getItemAt(maxDisplayNoComboBox.getSelectedIndex()));
        }
        Set<Integer> selectedDisplaysSet = new HashSet<>();
        for (var entry : selectDisplayNoCheckBox.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedDisplaysSet.add(entry.getKey());
            }
        }
        virtLCDConfig.setSelectedDisplays(selectedDisplaysSet);
    }


    private static class DCCppConnection {

        private DCCppSystemConnectionMemo _memo;

        public DCCppConnection(DCCppSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigureVirtualLCD.class);
}
