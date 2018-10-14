package jmri.jmrit.roster;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2007
 * @author      Matthew Harris  Copyright (C) 2008, 2010
 */
public class RosterConfigPane extends JPanel {

    JLabel filename;
    JTextField owner = new JTextField(20);
    
    public RosterConfigPane() {
        
        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(rb.getString("LabelMoveLocation")));

        p.add(filename = new JLabel(Roster.getFileLocation()));
        // don't show default location, so it's not deemed a user selection
        // and saved
        if (FileUtil.getUserFilesPath().equals(Roster.getFileLocation()))
            filename.setText("");

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel(rb.getString("LabelDefaultOwner")));
        owner.setText(RosterEntry.getDefaultOwner());
        p2.add(owner);
        add(p2);
    }

    public String getDefaultOwner() {
        return owner.getText();
    }
    public String getSelectedItem() {
        return filename.getText();
    }

}

