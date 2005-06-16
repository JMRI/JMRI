// RosterConfigPane.java

package jmri.jmrit.roster;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003
 * @version	$Revision: 1.5 $
 */
public class RosterConfigPane extends JPanel {

    JLabel filename;
    JTextField owner = new JTextField(20);
    JFileChooser fc = new JFileChooser();
    JPanel parent;
    
    public RosterConfigPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel("Roster info location:"));
        p.add(filename = new JLabel(Roster.getFileLocation()));
        JButton b = new JButton("Set ...");
        parent = this;
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                // prompt with instructions
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(parent.getTopLevelAncestor(), 
                "To use this option, you must already have alternate roster information (a\n"+
                "\"roster.xml\" file and a \"roster\" directory) available somewhere on your\n"+
                "computer other than the standard location. To use that non-standard roster\n"+
                "information, find and select its roster.xml file using the next panel.", 
                "Are you sure you want to continue?",
                JOptionPane.OK_CANCEL_OPTION
                )) return;
                
                // get the file
                FileFilter filt = new FileFilter(){
                    public boolean accept(File f) {
                        if (f.getName().equals("roster.xml")) return true;
                        else if (f.isDirectory()) return true;
                        else return false;
                    }
                    public String getDescription() { return "roster.xml only"; }
                };
                
                fc.setDialogTitle("Find desired roster.xml file");
                fc.rescanCurrentDirectory();
                fc.setFileFilter(filt);
                fc.showOpenDialog(null);
                if (fc.getSelectedFile()==null) return; // cancelled
                if (!fc.getSelectedFile().getName().equals("roster.xml")) return; // wrong file
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

