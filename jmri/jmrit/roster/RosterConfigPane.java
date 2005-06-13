// RosterConfigPane.java

package jmri.jmrit.roster;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.4 $
 */
public class RosterConfigPane extends JPanel {

    JLabel filename;
    JTextField owner = new JTextField(20);
    JFileChooser fc = new JFileChooser();

    public RosterConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Roster directory:"));
        p.add(filename = new JLabel(Roster.getFileLocation()));
        JButton b = new JButton("Set ...");
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                fc.setDialogTitle("Find desired roster directory"); //  .xml file");
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.rescanCurrentDirectory();
                fc.showOpenDialog(null);
                if (fc.getSelectedFile()==null) return; // cancelled
                filename.setText(fc.getSelectedFile()+File.separator);//.getParent()+File.separator);
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

        JPanel p2 = new JPanel();
        p2.setLayout(new FlowLayout());
        p2.add(new JLabel("Default owner name: "));
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

