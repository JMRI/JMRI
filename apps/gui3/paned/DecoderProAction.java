// DecoderProAction.java

 package apps.gui3.paned;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.swing.*;

/**
 * Action to produce a new, standalone DecoderPro window.
 *
 * Ignores WindowInterface.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.4 $
 */
 
public class DecoderProAction extends jmri.util.swing.JmriAbstractAction {

    /**
     * Enhanced constructor for placing the pane in various 
     * GUIs
     */
 	public DecoderProAction(String s, WindowInterface wi) {
    	super(s, new jmri.util.swing.sdi.JmriJFrameInterface());    	
    	// open menus, etc in separate windows for now    	
    }
     
 	public DecoderProAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, new jmri.util.swing.sdi.JmriJFrameInterface());
    }
       
    public void actionPerformed(ActionEvent e) {
        jmri.util.swing.multipane.ThreePaneTLRWindow mainFrame 
            = new jmri.util.swing.multipane.ThreePaneTLRWindow("DecoderPro", 
    	        new File("xml/config/apps/decoderpro/Gui3Menus.xml"), 
    	        new File("xml/config/apps/decoderpro/Gui3MainToolBar.xml"));
    	        
    	mainFrame.getTop().add(createTop());
    	mainFrame.getLeft().add(createLeft());
    	mainFrame.getRight().add(createRight());
    	
        mainFrame.setSize(mainFrame.getMaximumSize());
        mainFrame.setVisible(true);
    }
    
    JComponent createTop() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.X_AXIS));
        
        retval.add(new jmri.jmrit.roster.swing.RosterTable());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JButton b;
        p.add(b = new JButton("Identify"));
        b.setAlignmentX(0.5f);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        p2.add(new JLabel("Paged Mode"));
        p2.add(new JButton(">"));
        p.add(Box.createHorizontalGlue());
        p.add(p2);
        
        p.add(b = new JButton("New Locomotive"));
        b.setAlignmentX(0.5f);
        
        p.add(new JSeparator());
        p.add(Box.createVerticalGlue());
        retval.add(p);

        return retval;
    }
    
    JComponent createRight() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));

        JLabel l = new JLabel("Display of a particular pane will go here");
        l.setPreferredSize(new java.awt.Dimension(100, 200));
        retval.add(l);

        retval.add(Box.createVerticalGlue());
        retval.add(new JSeparator());

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JButton("Read These"));
        p.add(new JButton("Read These Changes"));
        p.add(new JButton("Write These"));
        p.add(new JButton("Write These Changes"));
        retval.add(p);
        return retval;
    }
    
    JComponent createLeft() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));
        
        retval.add(new JComboBox(new String[]{
            "Read All",
            "Read All Changes",
            "Write All",
            "Write All Changes"
        }));
        
        retval.add(new JSeparator());
        
        retval.add(new JList(new String[]{
                "Roster", 
                "Function Keys", 
                "Images", 
                "Main", 
                "Motor", 
                "Speed Control", 
                "Speed Table", 
                "Function Mapping", 
                "Lighting", 
                "CVs" 
            }));

        retval.add(new JSeparator());
        
        retval.add(new JList(new String[]{
                "Reset All", 
                "Reset Except Speed Table"
            }));
        return retval;
    }
    
    // never invoked, because we overrode actionPerformed above
    public void dispose() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    // never invoked, because we overrode actionPerformed above
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}

/* @(#)DecoderProAction.java */
