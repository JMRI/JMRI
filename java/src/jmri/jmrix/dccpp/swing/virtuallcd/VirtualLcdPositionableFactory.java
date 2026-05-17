package jmri.jmrix.dccpp.swing.virtuallcd;

// import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.FlowLayout;
import java.util.List;

import javax.annotation.Nonnull;
import javax.swing.*;

// import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.display.*;
// import static jmri.jmrit.display.Editor.CLOCK;
import static jmri.jmrit.display.Editor.ICONS;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 * A factory for a VirtualLCD that can be put on a panel.
 *
 * @author Daniel Bergqvist (C) 2026
 */
@ServiceProvider(service = PositionableFactory.class)
public class VirtualLcdPositionableFactory implements PositionableFactory {

    private JmriJFrame addPositionableFrame = null;


    public VirtualLcdPositionableFactory() {
    }

    @Nonnull
    @Override
    public String getIdentifier() {
        return "DCC-EX-VirtualDisplay";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return Bundle.getMessage("VirtualLCDFrameTitle");
    }

    @Override
    public boolean isEnabled() {
        return hasDccEx();
    }

    /**
     * Do we have a LocoNet connection?
     * @return true if we have LocoNet, false otherwise
     */
    public static boolean hasDccEx() {
        List<DCCppSystemConnectionMemo> list = jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);

        // We have at least one DCC-EX connection if the list is not empty
        return !list.isEmpty();
    }

    @Override
    public void addPositionable(@Nonnull Editor editor, DoAfter doAfter) {
        if (addPositionableFrame != null) {
            this.closeDialog(editor);
        }

        addPositionableFrame = new JmriJFrame(Bundle.getMessage("AddVirtualLcdPositionable"));
        addPositionableFrame.addHelpMenu(
                "package.jmri.jmrix.dccpp.swing.virtuallcd.VirtualLcdPositionableFactory", true);
        addPositionableFrame.setLocation(50, 30);
        Container contentPane = addPositionableFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        JComboBox<DCCppConnection> _memoComboBox = new JComboBox<>();
        List<DCCppSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);
        for (DCCppSystemConnectionMemo connection : systemConnections) {
            DCCppConnection c = new DCCppConnection(connection);
            _memoComboBox.addItem(c);
        }
        _memoComboBox.setSelectedItem(_memoComboBox.getItemAt(0));
        _memoComboBox.setToolTipText(Bundle.getMessage("ConnectionHint"));

        JTextField displayNoTextField = new JTextField(4);
        displayNoTextField.setText("0");
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
        p.add(displayNoTextField, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        displayNoTextField.setToolTipText(Bundle.getMessage("DisplayNoHint"));
        contentPane.add(p);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
        panel5.add(cancel);
        cancel.addActionListener((e) -> closeDialog(editor));
        cancel.setToolTipText(Bundle.getMessage("CancelButtonHint"));

        JButton create = new JButton(Bundle.getMessage("ButtonCreate"));
        panel5.add(create);
        create.addActionListener((e) -> {
            var memo = _memoComboBox.getItemAt(_memoComboBox.getSelectedIndex())._memo;
            try {
                int displayNo = Integer.parseInt(displayNoTextField.getText());
                addVirtualLCD(editor, memo, displayNo, doAfter);
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

        addPositionableFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                closeDialog(editor);
            }
        });

        contentPane.add(panel5);

        addPositionableFrame.pack();
        addPositionableFrame.setVisible(true);
    }

    void closeDialog(@Nonnull Editor editor) {
        addPositionableFrame.setVisible(false);
        addPositionableFrame.dispose();
        addPositionableFrame = null;
        editor.setVisible(true);
    }

    public void addVirtualLCD(
            Editor editor,
            DCCppSystemConnectionMemo memo,
            int displayNo,
            DoAfter doAfter) {

        VirtualLcdPositionable virtualLcdPositionable =
                new VirtualLcdPositionable(editor, memo, displayNo);
        virtualLcdPositionable.setDisplayLevel(ICONS);
        editor.setNextLocation(virtualLcdPositionable);
        try {
            editor.putItem(virtualLcdPositionable, true);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }
        if (doAfter != null) {
            doAfter.doAfter(virtualLcdPositionable);
        }
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


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLcdPositionableFactory.class);
}
