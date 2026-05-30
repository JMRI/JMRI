package jmri.jmrix.dccpp.swing.virtuallcd;

import java.awt.*;
import java.util.*;

import static jmri.jmrit.display.Editor.ICONS;

import java.util.List;

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

    private final Editor editor;
    private final VirtualLCDConfiguration virtualLCDConfiguration;
    private final CloseDialog closeDialog;
    private final DoAfter doAfter;
    private final Map<DCCppSystemConnectionMemo, Integer> highestDisplayNoMap = new HashMap<>();

    JComboBox<DisplayConfig> displayNoComboBox;
    private JPanel selectedDisplays;
    private JComboBox<Integer> minDisplayNoComboBox = new JComboBox<>();
    private JComboBox<Integer> maxDisplayNoComboBox = new JComboBox<>();
    private List<JCheckBox> selectDisplayNoCheckBox = new ArrayList<>();

    public ConfigureVirtualLCD(
            Editor editor,
            VirtualLCDConfiguration virtualLCDConfiguration,
            CloseDialog closeDialog) {

        this(editor, virtualLCDConfiguration, closeDialog, null);
    }

    public ConfigureVirtualLCD(
            Editor editor,
            VirtualLCDConfiguration virtualLCDConfiguration,
            CloseDialog closeDialog,
            DoAfter doAfter) {

        super(Bundle.getMessage("AddVirtualLcdPositionable"), false, false);
        this.editor = editor;
        this.virtualLCDConfiguration = virtualLCDConfiguration;
        this.closeDialog = closeDialog;
        this.doAfter = doAfter;
    }

    @Override
    public void initComponents() {
        addHelpMenu("package.jmri.jmrix.dccpp.swing.virtuallcd.VirtualLcdPositionableFactory", true);
        setLocation(50, 30);
        Container contentPane = getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JComboBox<DCCppConnection> _memoComboBox = new JComboBox<>();
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

        JTextField displayNoTextField = new JTextField(4);
        if (virtualLCDConfiguration != null) {
            displayNoTextField.setText(Integer.toString(virtualLCDConfiguration.getDisplayNo()));
        } else {
            displayNoTextField.setText("0");
        }
        JLabel memoLabel = new JLabel(Bundle.getMessage("Connection"));
        JLabel displayNoLabel = new JLabel(Bundle.getMessage("DisplayNo"));

        JPanel p;
        p = new JPanel();
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(memoLabel, c);
        memoLabel.setLabelFor(_memoComboBox);
        c.gridy = 1;
        p.add(displayNoLabel, c);
        displayNoLabel.setLabelFor(displayNoTextField);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(_memoComboBox, c);
        c.gridy = 1;
        displayNoComboBox = new JComboBox<>();
        for (DisplayConfig dc : DisplayConfig.values()) {
            displayNoComboBox.addItem(dc);
            if (dc == virtualLCDConfiguration.getDisplayConfig()) {
                displayNoComboBox.setSelectedItem(dc);
            }
        }
        p.add(displayNoComboBox, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
//        displayNoTextField.setToolTipText(Bundle.getMessage("DisplayNoHint"));
        contentPane.add(p);


        JPanel allDisplays = new JPanel();
        allDisplays.add(new JLabel(Bundle.getMessage("ConfigureVirtualLCD_AllDisplays")));

        JPanel oneDisplay = new JPanel();
        oneDisplay.add(new JLabel(Bundle.getMessage("DisplayNo")));
        oneDisplay.add(displayNoTextField);

        JPanel intervalDisplays = new JPanel();
        intervalDisplays.add(new JLabel(Bundle.getMessage("DisplayNo")));
        intervalDisplays.add(minDisplayNoComboBox);
        intervalDisplays.add(new JLabel(" - "));
        intervalDisplays.add(maxDisplayNoComboBox);

        selectedDisplays = new JPanel();
        selectedDisplays.setLayout(new BoxLayout(selectedDisplays, BoxLayout.Y_AXIS));

        JPanel cards;   // A panel that uses CardLayout
        CardLayout cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);   // Create the panel that contains the "cards".
        cards.add(allDisplays, DisplayConfig.ConfigureVirtualLCD_AllDisplays.name());
        cards.add(oneDisplay, DisplayConfig.ConfigureVirtualLCD_OneDisplay.name());
        cards.add(intervalDisplays, DisplayConfig.ConfigureVirtualLCD_IntervalDisplay.name());
        cards.add(selectedDisplays, DisplayConfig.ConfigureVirtualLCD_SelectedDisplays.name());
        cardLayout.show(cards, virtualLCDConfiguration.getDisplayConfig().name());
        contentPane.add(cards);

        displayNoComboBox.addItemListener(evt -> {
            cardLayout.show(cards, ((DisplayConfig) evt.getItem()).name());
        });

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        panel5.add(cancel);
        cancel.addActionListener((e) -> closeDialog.closeDialog(editor));
        cancel.setToolTipText(Bundle.getMessage("CancelButtonHint"));

        if (virtualLCDConfiguration != null) {
            JButton ok = new JButton(Bundle.getMessage("ButtonOK"));
            panel5.add(ok);
            ok.addActionListener((e) -> {
                var memo = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo;
                try {
                    int displayNo = Integer.parseInt(displayNoTextField.getText());
                    updateVirtualLCD(memo, displayNo);
                    closeDialog.closeDialog(editor);
                } catch (NumberFormatException ex) {
                    JmriJOptionPane.showMessageDialog(
                            editor,
                            Bundle.getMessage("ErrorDisplayNoMustBeInteger"),
                            Bundle.getMessage("ErrorTitle"),
                            JmriJOptionPane.ERROR_MESSAGE);
                }
            });
            ok.setToolTipText(Bundle.getMessage("CreateButtonHint"));
        } else {
            JButton create = new JButton(Bundle.getMessage("ButtonCreate"));
            panel5.add(create);
            create.addActionListener((e) -> {
                var memo = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo;
                try {
                    int displayNo = Integer.parseInt(displayNoTextField.getText());
                    addVirtualLCD(memo, displayNo);
                    closeDialog.closeDialog(editor);
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
                closeDialog.closeDialog(editor);
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
                            configureDisplaySelection(memo);
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
        minDisplayNoComboBox.removeAllItems();
        maxDisplayNoComboBox.removeAllItems();
        for (int i=0; i < highestDisplayNoMap.getOrDefault(memo,0); i++) {
            minDisplayNoComboBox.addItem(i);
            maxDisplayNoComboBox.addItem(i);
            JCheckBox cb = new JCheckBox(Bundle.getMessage("ConfigureVirtualLCD_SelectedDisplays_CheckBox", i));
            selectDisplayNoCheckBox.add(cb);
            selectedDisplays.add(cb);
        }
        this.pack();
    }

    private void addVirtualLCD(DCCppSystemConnectionMemo memo, int displayNo) {

        VirtualLcdPositionable virtLcdPositionable =
                new VirtualLcdPositionable(editor, memo, displayNo);
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

    private void updateVirtualLCD(DCCppSystemConnectionMemo memo, int displayNo) {

        virtualLCDConfiguration.setMemo(memo);
        virtualLCDConfiguration.setDisplayConfig(
                displayNoComboBox.getItemAt(displayNoComboBox.getSelectedIndex()));
        virtualLCDConfiguration.setDisplayNo(displayNo);
    }


    public interface CloseDialog {
        void closeDialog(Editor editor);
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
