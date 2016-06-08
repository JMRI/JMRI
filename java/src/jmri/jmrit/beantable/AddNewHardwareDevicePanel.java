package jmri.jmrit.beantable;

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
import javax.swing.JTextField;

/**
 * JPanel to create a new JMRI devices HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 */
public class AddNewHardwareDevicePanel extends jmri.util.swing.JmriPanel {

    public AddNewHardwareDevicePanel(JTextField sysAddress, JTextField userName, JComboBox<String> prefixBox, JTextField endRange, JCheckBox addRange,
            String addButtonLabel, ActionListener okListener, ActionListener cancelListener, ActionListener rangeListener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        _endRange = endRange;
        _range = addRange;
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
        c.gridy = 2;
        p.add(userNameLabel, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(prefixBox, c);
        c.gridx = 3;
        p.add(addRange, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(sysAddress, c);
        c.gridx = 3;
        p.add(finishLabel, c);
        c.gridx = 4;
        p.add(endRange, c);
        c.gridx = 2;
        c.gridy = 2;
        p.add(userName, c);
        add(p);

        finishLabel.setEnabled(false);
        _endRange.setEnabled(false);

        // cancel + add buttons at bottom of window
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));

        panelBottom.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
        cancel.addActionListener(cancelListener);

        panelBottom.add(ok = new JButton(Bundle.getMessage(addButtonLabel)));
        ok.addActionListener(okListener);

        add(panelBottom);

        addRange.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        rangeState();
                    }
                });
        prefixBox.addActionListener(rangeListener);

        /* System.out.println(jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class));
         java.util.List<jmri.jmrix.SystemConnectionMemo> list 
         = jmri.InstanceManager.getList(jmri.jmrix.SystemConnectionMemo.class);
         if (list != null) {
         for (jmri.jmrix.SystemConnectionMemo memo : list) {
         System.out.println(memo.getUserName());
         //if (menu != null) m.add(menu);
         }
         }*/
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

    JButton ok;
    JButton cancel;
    JTextField _endRange;
    JCheckBox _range;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("ColumnSystemName"));
    JLabel sysAddressLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    JLabel finishLabel = new JLabel(Bundle.getMessage("LabelNumberToAdd"));
}
