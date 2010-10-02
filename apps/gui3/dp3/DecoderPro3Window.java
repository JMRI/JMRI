// DecoderPro3Window.java

 package apps.gui3.dp3;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import java.util.List;
import java.util.ResourceBundle;

import jmri.util.swing.*;

// for ugly code
import jmri.progdebugger.*;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.roster.swing.*;


import org.jdom.*;

/**
 * Standalone DecoderPro3 Window (new GUI)
 *
 * Ignores WindowInterface.
 *
 * TODO:
 * Several methods are copied from PaneProgFrame and should be refactored
 * No programmer support yet (dummy object below)
 * Color only covering borders
 * No reset toolbar support yet
 * No glass pane support (See DecoderPro3Panes class and usage below)
 * Special panes (Roster entry, attributes, graphics) not included
 * How do you pick a programmer file? (hardcoded)
 * Initialization needs partial deferal, too for 1st pane to appear
 * 
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneSet
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.7 $
 */
 
public class DecoderPro3Window 
        extends jmri.util.swing.multipane.ThreePaneTLRWindow {

    public DecoderPro3Window() {
        super("DecoderPro", 
    	        new File("xml/config/apps/decoderpro/Gui3Menus.xml"), 
    	        null);  // no toolbar
    	        
    	getTop().add(createTop());
    	getLeft().add(createLowerLeft());
    	
    	getRight().add(createLowerRight());
    	getRight().setLayout(new BoxLayout(getRight(), BoxLayout.Y_AXIS));
    	
        setSize(getMaximumSize());
        setVisible(true);
    }
    
    jmri.jmrit.roster.swing.RosterTable rtable;
    ResourceBundle rb = ResourceBundle.getBundle("apps.gui3.dp3.DecoderPro3Bundle");
    
    JComponent createTop() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.X_AXIS));
        
        // left box
        retval.add(createUpperLeft());

        // set up roster table
         
        rtable = new RosterTable();
        retval.add(rtable);
        // add selection listener
        rtable.getTable().getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting()) {
                        locoSelected(rtable.getModel().getValueAt(e.getFirstIndex(), RosterTableModel.IDCOL).toString());
                    }
                }
            }
        );

        return retval;
    }
    
    JPanel createUpperLeft() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JButton b;

        p.add(b = new JButton(rb.getString("NewLocoButton"), new ImageIcon("resources/icons/misc/gui3/NewLocoButton.png")));
        b.setHorizontalAlignment(JButton.LEFT);
        b.setAlignmentX(0.0f);

        p.add(b = new JButton(rb.getString("IdentifyButton"), new ImageIcon("resources/icons/misc/gui3/IdentifyButton.png")));
        b.setHorizontalAlignment(JButton.LEFT);
        b.setAlignmentX(0.0f);

        JToggleButton t; 
        p.add(t = new JToggleButton(rb.getString("ModeButton"), new ImageIcon("resources/icons/misc/gui3/SliderUp.png")));
        t.setSelectedIcon(new ImageIcon("resources/icons/misc/gui3/SliderDown.png"));
        t.setHorizontalAlignment(JButton.LEFT);
        t.setAlignmentX(0.0f);
                
        p.add(new JSeparator());
        p.add(Box.createVerticalGlue());
        
        return p;
    }
    
    /**
     * An entry has been selected in the Roster Table, 
     * activate the bottom part of the window
     */
    void locoSelected(String id) {
        log.debug("locoSelected ID "+id);
        // convert to roster entry
        RosterEntry re = Roster.instance().entryFromTitle(id);
        
        // start making PaneSet
        
        DecoderPro3Panes pc = new DecoderPro3Panes();    // eventually has to handle glass pane
        PaneSet ps = new PaneSet(pc, re, programmer);
        XmlFile pf = new XmlFile(){};  // XmlFile is abstract
        String filename = "programmers"+File.separator+"Comprehensive.xml";
        try {
            // load programmer config from programmer tree
            ps.makePanes(pf.rootFromName(filename), re);
        }
        catch (Exception e) {
            log.debug("exception reading programmer file: "+filename, e);
        }
        
        List<PaneProgPane> list = ps.getList();

        // update the toolbar list of panes
        paneJList.setModel(
            new JList(list.toArray())
                .getModel());

        // load panes to lower right window
        paneSpace.removeAll();
        int count = 0;
        for (PaneProgPane p : list) {
            
            System.out.println("start: "+p);
            System.out.println("name: "+p.toString());
            System.out.println("n:    "+p.getComponents().length);
            System.out.println("--> "+p.getComponents()[0]);
            System.out.println("--> "+p.getComponents()[1]);
            System.out.println("end --------- ");
            
            
            javax.swing.border.TitledBorder border = new javax.swing.border.TitledBorder(p.getName());
            p.setBorder(border);
            paneSpace.add(p);
            
            p.setBackground(colors[count]);
            count++;
            if (count>=colors.length) count = 0;
            
        }
    }

    java.awt.Color[] colors = new java.awt.Color[]{
                                new java.awt.Color(238, 238, 238),
                                new java.awt.Color(249, 248, 231),
                                new java.awt.Color(216, 245, 246) };
    
    JPanel paneSpace = new JPanel(); // place where the panes go
    JScrollPane sp;
    
    JComponent createLowerRight() {

        paneSpace.setLayout(new BoxLayout(paneSpace, BoxLayout.Y_AXIS));

        JComponent l = new JLabel("Display of a particular pane will go here");
        l.setPreferredSize(new java.awt.Dimension(100, 200));
        paneSpace.add(l);
        
        sp = new JScrollPane(paneSpace);
        
        return sp;
    }
    
    JToolBar paneToolBar = new JToolBar("Panes");
    JList   paneJList = new JList(new String[]{  // really dummy content
                "<nothing yet>" 
            });
            
    JComponent createLowerLeft() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));
        float defaultXAlignment = 0.f;
                
        paneToolBar = new JToolBar("Panes");
        paneToolBar.setOrientation(JToolBar.VERTICAL);
        paneToolBar.setAlignmentX(defaultXAlignment);
        paneJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paneToolBar.add(paneJList);
        paneJList.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting() && paneJList.getSelectedValue() != null) {
                        showPane((PaneProgPane)paneJList.getSelectedValue());
                    }
                }
            }
        );

        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(2,2));
        p1.add(new JButton(rb.getString("LLProgReadChanges")));
        p1.add(new JButton(rb.getString("LLProgReadAll")));
        p1.add(new JButton(rb.getString("LLProgWriteChanges")));
        p1.add(new JButton(rb.getString("LLProgWriteAll")));
        paneToolBar.add(p1);
        
        JPanel p3 = new JPanel();
        p3.setLayout(new FlowLayout());
        p3.add(new JButton(rb.getString("LLSave"), new ImageIcon("resources/icons/misc/gui3/SaveIcon.png")));
        p3.add(new JButton(rb.getString("LLReset")));
        paneToolBar.add(p3);
        
        retval.add(paneToolBar);
        
        retval.add(new JSeparator());
                
        return retval;
    }
    
    /**
     * Move the pane display to a particular one
     */
    void showPane(PaneProgPane pane) {
        log.debug("show pane "+pane);

        if (pane == null) log.error("showPane invoked on null", new Exception(""));
        
        // position we want to go to 
        int dest = pane.getLocation().y;
        int end = paneSpace.size().height;
        
        // go there, but note this might be first time
        JScrollBar bar = sp.getVerticalScrollBar();
        bar.setMinimum(0);
        bar.setMaximum(end);
        bar.setValue(dest);
        
    }
    
    ProgDebugger programmer = new ProgDebugger();
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DecoderPro3Window.class.getName());
}

/* @(#)DecoderPro3Window.java */
