// AddSensorPanel.java
package jmri.jmrit.beantable;

import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel to create a new JMRI devices HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 * @version $Revision$
 */
public class AddNewBeanPanel extends jmri.util.swing.JmriPanel {

    /**
     *
     */
    private static final long serialVersionUID = -7238135491102630527L;

    public AddNewBeanPanel(JTextField sys, JTextField userName, JTextField endRange, JCheckBox addRange, JCheckBox autoSystem,
            String addButtonLabel, ActionListener listener) {
        sysName = sys;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        _endRange = endRange;
        _range = addRange;
        _autoSys = autoSystem;

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
        c.gridx = 0;
        c.gridy = 1;
        p.add(sysNameLabel, c);
        c.gridy = 2;
        p.add(userNameLabel, c);
        c.gridx = 2;
        c.gridy = 1;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        c.gridy = 0;
        p.add(autoSystem, c);
        c.gridx = 3;
        p.add(addRange, c);
        c.gridx = 2;
        c.gridy = 1;
        p.add(sys, c);
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

        add(ok = new JButton(Bundle.getMessage(addButtonLabel)));
        ok.addActionListener(listener);

        addRange.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        rangeState();
                    }
                });

        sysName.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent a) {
                if (sysName.getText().length() > 0) {
                    ok.setEnabled(true);
                    ok.setToolTipText(null);
                } else {
                    ok.setEnabled(false);
                }
            }
        });

        autoSystem.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        autoSystemName();
                    }
                });
    }

    private void autoSystemName() {
        if (_autoSys.isSelected()) {
            ok.setEnabled(true);
            sysName.setEnabled(false);
            sysNameLabel.setEnabled(false);
        } else {
            if (sysName.getText().length() > 0) {
                ok.setEnabled(true);
            } else {
                ok.setEnabled(false);
            }
            sysNameLabel.setEnabled(true);
            sysName.setEnabled(true);
            sysNameLabel.setEnabled(true);
        }
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
    JTextField sysName;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    JTextField _endRange;
    JCheckBox _range;
    JCheckBox _autoSys;
    JLabel finishLabel = new JLabel(Bundle.getMessage("LabelNumberToAdd"));

    static final Logger log = LoggerFactory.getLogger(AddNewBeanPanel.class.getName());
}


/* @(#)AddNewBeanPanel.java */
