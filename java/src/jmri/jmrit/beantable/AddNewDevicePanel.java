package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * JPanel to create a new JMRI device (used to add IdTag).
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 */
public class AddNewDevicePanel extends jmri.util.swing.JmriPanel {

    public AddNewDevicePanel(JTextField sys, JTextField userName,
            String addButtonLabel, ActionListener okListener, ActionListener cancelListener) {
        sysName = sys;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
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
        sysNameLabel.setLabelFor(sysName);
        c.gridy = 1;
        p.add(userNameLabel, c);
        userNameLabel.setLabelFor(userName);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(sysName, c);
        c.gridy = 1;
        p.add(userName, c);
        add(p);

        // button(s) at bottom of window
        JPanel panelBottom = new JPanel();
        panelBottom.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // only add a Cancel button when the OKbutton string is OK (so don't show on Picker Panels)
        if (addButtonLabel.equals("ButtonOK")) {
            panelBottom.add(cancel = new JButton(Bundle.getMessage("ButtonCancel")));
            cancel.addActionListener(cancelListener);
        }

        panelBottom.add(ok = new JButton(Bundle.getMessage(addButtonLabel)));
        ok.addActionListener(okListener);

        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {
                reset();
            }
        });

        add(panelBottom);

        reset();
        sysName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent a) {
                if (sysName.getText().length() > 0) {
                    setOK();
                }
            }
        });
    }

    void reset() {
        ok.setEnabled(false);
        ok.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
    }

    /**
     * Activate the OK button without user key activity.
     */
    public void setOK() {
        ok.setEnabled(true);
        ok.setToolTipText(null);
    }

    /**
     * Lock the System Name JTextField.
     */
    public void setSystemNameFieldIneditable() {
        sysName.setEditable(false);
        sysName.setBorder(null);
        sysName.setDisabledTextColor(Color.black);
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysNameLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }

    JButton ok;
    JButton cancel;
    JTextField sysName;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

}
