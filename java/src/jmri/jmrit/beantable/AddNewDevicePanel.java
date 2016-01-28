// AddSensorPanel.java
package jmri.jmrit.beantable;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel to create a new JMRI devices HiJacked to serve other beantable tables.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @author Pete Cressman Copyright (C) 2010
 * @version $Revision$
 */
public class AddNewDevicePanel extends jmri.util.swing.JmriPanel {

    /**
     *
     */
    private static final long serialVersionUID = 5114030241732110250L;

    public AddNewDevicePanel(JTextField sys, JTextField userName,
            String addButtonLabel, ActionListener listener) {
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
        c.gridy = 1;
        p.add(userNameLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = java.awt.GridBagConstraints.WEST;
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
        p.add(sysName, c);
        c.gridy = 1;
        p.add(userName, c);
        add(p);

        add(ok = new JButton(Bundle.getMessage(addButtonLabel)));
        ok.addActionListener(listener);
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent a) {
                reset();
            }
        });

        reset();
        sysName.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent a) {
                if (sysName.getText().length() > 0) {
                    ok.setEnabled(true);
                    ok.setToolTipText(null);
                }
            }
        });
    }

    void reset() {
        ok.setEnabled(false);
        ok.setToolTipText(Bundle.getMessage("ToolTipWillActivate"));
    }

    public void addLabels(String labelSystemName, String labelUserName) {
        sysNameLabel.setText(labelSystemName);
        userNameLabel.setText(labelUserName);
    }

    JButton ok;
    JTextField sysName;
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    static final Logger log = LoggerFactory.getLogger(AddNewDevicePanel.class.getName());
}


/* @(#)AddNewDevicePanel.java */
