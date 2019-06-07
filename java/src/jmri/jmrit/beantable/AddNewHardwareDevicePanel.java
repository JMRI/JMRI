package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;

/**
 * JPanel to create a new JMRI hardware device (used to add Turnout, Sensor, Reporter).
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 */
public class AddNewHardwareDevicePanel extends jmri.util.swing.JmriPanel {

    public AddNewHardwareDevicePanel(JTextField sysAddress, JTextField userName, JComboBox<String> prefixBox, JSpinner endRange, JCheckBox addRange,
            JButton addButton, ActionListener cancelListener, ActionListener rangeListener, JLabel statusBar) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (statusBar == null) statusBar = new JLabel("");
        _endRange = endRange;
        _range = addRange;
        // directly using the addButton from the table action allows to disable it from there
        // as long until a valid address is entered
        JPanel p;
        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        p.add(sysNameLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        p.add(sysAddressLabel, c);
        sysAddressLabel.setLabelFor(sysAddress);
        c.gridy = 2;
        p.add(userNameLabel, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL; // text field will expand
        c.gridy = 0;
        p.add(prefixBox, c);
        c.gridx = 3;
        p.add(addRange, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(sysAddress, c);
        sysAddress.setToolTipText(Bundle.getMessage("HardwareAddressToolTip")); // overridden in calling class
        c.gridx = 3;
        p.add(finishLabel, c);
        c.gridx = 4;
        p.add(endRange, c);
        c.gridx = 2;
        c.gridy = 2;
        p.add(userName, c);
        userName.setToolTipText(Bundle.getMessage("UserNameToolTip")); // fixed general instruction
        add(p);

        finishLabel.setEnabled(false);
        _endRange.setEnabled(false);

        // add status bar above buttons
        JPanel panelStatus = new JPanel();
        panelStatus.setLayout(new FlowLayout());
        statusBar.setFont(statusBar.getFont().deriveFont(0.9f * sysAddressLabel.getFont().getSize())); // a bit smaller
        statusBar.setForeground(Color.gray);
        panelStatus.add(statusBar);
        add(panelStatus);

        // cancel + add buttons at bottom of window
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));

        panelBottom.add(cancel);
        cancel.addActionListener(cancelListener);

        panelBottom.add(addButton);

        add(panelBottom);

        addRange.addItemListener(
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        rangeState();
                    }
                });
        prefixBox.addActionListener(rangeListener);
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysAddressLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }

    private void rangeState() {
        if (_range.isSelected()) {
            finishLabel.setEnabled(true);
            _endRange.setEnabled(true);
        } else {
            finishLabel.setEnabled(false);
            _endRange.setEnabled(false);
        }
    }

    JButton cancel = new JButton(Bundle.getMessage("ButtonClose")); // when Apply has been clicked at least once, this is not Revert/Cancel
    JSpinner _endRange;
    JCheckBox _range;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("SystemConnectionLabel"));
    JLabel sysAddressLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel finishLabel = new JLabel(Bundle.getMessage("LabelNumberToAdd"));
    JLabel statusBar;

}
