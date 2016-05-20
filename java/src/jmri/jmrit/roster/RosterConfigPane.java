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
import jmri.util.FileUtil;

/**
 * Provide GUI to configure Roster defaults.
 *
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2003, 2007
 * @version	$Revision$
 */
public class RosterConfigPane extends JPanel {

    JLabel filename;
    JTextField owner = new JTextField(20);
    JFileChooser fc;
    JPanel parent;
    
    public RosterConfigPane() {
        fc = new JFileChooser(FileUtil.getUserFilesPath());
        // filter to only show the roster.xml file
        FileFilter filt = new FileFilter(){
            public boolean accept(File f) {
                if (f.getName().equals("roster.xml")) return true;
                else if (f.isDirectory()) return true;
                else return false;
            }
            public String getDescription() { return "roster.xml only"; }
        };
        
        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
        fc.setDialogTitle(rb.getString("DialogTitleMove"));
        fc.setFileFilter(filt);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(rb.getString("LabelMoveLocation")));

        p.add(filename = new JLabel(Roster.getFileLocation()));
        // don't show default location, so it's not deemed a user selection
        // and saved
        if (FileUtil.getUserFilesPath().equals(Roster.getFileLocation()))
            filename.setText("");
        JButton b = new JButton(rb.getString("ButtonSetDots"));

        parent = this;
        b.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.roster.JmritRosterBundle");
                // prompt with instructions
                if (JOptionPane.OK_OPTION != JOptionPane.showConfirmDialog(parent.getTopLevelAncestor(), 
                rb.getString("DialogMsgMoveWarning"), 
                rb.getString("DialogMsgMoveQuestion"),
                JOptionPane.OK_CANCEL_OPTION
                )) return;
                
                // get the file
                fc.rescanCurrentDirectory();
                fc.showOpenDialog(null);
                if (fc.getSelectedFile()==null) return; // cancelled
                if (!fc.getSelectedFile().getName().equals("roster.xml")) return; // wrong file
                filename.setText(fc.getSelectedFile().getParent()+File.separator);
                validate();
                if (getTopLevelAncestor()!=null) ((JFrame)getTopLevelAncestor()).pack();
            }
        });
        p.add(b);
        b = new JButton(rb.getString("ButtonReset"));
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
        p2.add(new JLabel(rb.getString("LabelDefaultOwner")));
        owner.setText(RosterEntry.getDefaultOwner());
        p2.add(owner);
        add(p2);
    }

    public String getDefaultOwner() {
        return owner.getText();
    }
    
    public void setDefaultOwner(String defaultOwner){
        owner.setText(defaultOwner);
    }
    
    public String getSelectedItem() {
        return filename.getText();
    }

}

