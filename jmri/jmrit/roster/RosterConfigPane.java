// RosterConfigPane.java

package jmri.jmrit.roster;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;

/**
 * Provide GUI to configure symbolic programmer defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.1 $
 */
public class RosterConfigPane extends JPanel {

    JLabel filename;
    JFileChooser fc = new JFileChooser();

    public RosterConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Roster directory:"));
        p.add(filename = new JLabel(Roster.getFileLocation()));
        JButton b = new JButton("Set ...");
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fc.setDialogTitle("Find desired roster.xml file");
                fc.showOpenDialog(null);
                if (fc.getSelectedFile()==null) return; // cancelled
                filename.setText(fc.getSelectedFile().getParent()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        p.add(b);
        b = new JButton("Reset");
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                filename.setText("");
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        p.add(b);
        add(p);
    }

    public String getSelectedItem() {
        return filename.getText();
    }

}

