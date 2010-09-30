// DecoderProAction.java

 package apps.gui3.dp3;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import java.util.List;

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
 * No programmer support yet
 * No reset toolbar support yet
 * No glass pane support
 * Need better support for visible/non-visible panes
 * Special panes (Roster entry, attributes, graphics) not included
 * How do you pick a programmer file? (hardcoded)
 * 
 * @see jmri.jmrit.symbolicprog.tabbedframe.PaneSet
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.2 $
 */
 
public class DecoderPro3Window 
        extends jmri.util.swing.multipane.ThreePaneTLRWindow {

    public DecoderPro3Window() {
        super("DecoderPro", 
    	        new File("xml/config/apps/decoderpro/Gui3Menus.xml"), 
    	        null);  // no toolbar
    	        
    	getTop().add(createTop());
    	getLeft().add(createLeft());
    	getRight().add(createRight());
    	
        setSize(getMaximumSize());
        setVisible(true);
    }
    
    jmri.jmrit.roster.swing.RosterTable rtable;
    
    JComponent createTop() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.X_AXIS));
        
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
    
    void locoSelected(String id) {
        System.out.println("locoSelected ID "+id);
        // convert to roster entry
        RosterEntry re = Roster.instance().entryFromTitle(id);
        
        // start making PaneSet
                    //JFrame p = new PaneProgFrame(null, re,
                    //                             "dummy title", "programmers"+File.separator+"Comprehensive"+".xml",
                    //                             null, false){
                    //    protected JPanel getModePane() { return null; }
                    //};
        
        PaneSet ps = new PaneSet(null, re);
        XmlFile pf = new XmlFile(){};  // XmlFile is abstract
        String filename = "programmers"+File.separator+"Comprehensive.xml";
        try {
            // load programmer config from programmer tree
            ps.makePanes(pf.rootFromName(filename), re);
        }
        catch (Exception e) {
            System.out.println("exception reading programmer file: "+filename);
            // provide traceback too
            e.printStackTrace();
        }
        
        List<PaneProgPane> list = ps.getList();

        // update the toolbar list of panes
        paneJList.setModel(new JList(list.toArray())
            .getModel());
    }

    JPanel paneSpace = new JPanel();
    JComponent createRight() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));

        
        JComponent l = new JLabel("Display of a particular pane will go here");
        l.setPreferredSize(new java.awt.Dimension(100, 200));
        paneSpace.add(l);
        retval.add(paneSpace);

        retval.add(Box.createVerticalGlue());
        
        return retval;
    }
    
    JToolBar paneToolBar = new JToolBar("Panes");
    JList   paneJList = new JList(new String[]{  // really dummy content
                "<nothing yet>" 
            });
            
    JComponent createLeft() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));
        float defaultXAlignment = 0.f;
        
        JToolBar bar = JToolBarUtil.loadToolBar(
            new java.io.File("xml/config/apps/decoderpro/GlobalProgButtons.xml"),
            null, null);
        bar.setOrientation(JToolBar.VERTICAL);
        bar.setAlignmentX(defaultXAlignment);
        retval.add(bar);
        
        retval.add(Box.createRigidArea(new Dimension(10,10)));
        retval.add(new JSeparator());
        
        paneToolBar = new JToolBar("Panes");
        paneToolBar.setOrientation(JToolBar.VERTICAL);
        paneToolBar.setAlignmentX(defaultXAlignment);
        paneJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paneToolBar.add(paneJList);
        paneJList.addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (! e.getValueIsAdjusting()) {
                        showPane((PaneProgPane)paneJList.getSelectedValue());
                    }
                }
            }
        );
        
        retval.add(paneToolBar);
        
        retval.add(new JSeparator());
        
        bar = new JToolBar("Resets");
        bar.setOrientation(JToolBar.VERTICAL);
        bar.setAlignmentX(defaultXAlignment);
        bar.add(new JButton("Reset All")); 
        bar.add(new JButton("Reset Except Speed Table"));
        retval.add(bar);
        
        //retval.add(new JList(new String[]{
        //        "Reset All", 
        //        "Reset Except Speed Table"
        //    }));
        return retval;
    }
    
    void showPane(PaneProgPane pane) {
        System.out.println("show pane "+pane);
        paneSpace.removeAll();
        paneSpace.add(pane);
        System.out.println("pref "+pane.getPreferredSize());
        paneSpace.revalidate();
    }
    
    // amazingly ugly temp pane code
    String result = null;
    int colCount = -1;
    int varCount = -1;
    
    ProgDebugger p = new ProgDebugger();
    
    JComponent createPane() {
        // create a JDOM tree with just some elements
        Element root = null;
        Element pane1 = null;
        Document doc = null;

        root = new Element("programmer-config");
        doc = new Document(root);
        doc.setDocType(new DocType("programmer-config","programmer-config.dtd"));

        // add some elements
        root.addContent(new Element("programmer")
            .addContent(pane1 = new Element("pane")
                .setAttribute("name","Basic")
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .setAttribute("item", "Primary Address")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Start voltage")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Normal direction of motion")
                        )
                    )
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .setAttribute("item", "Address")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Normal direction of motion")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Normal direction of motion")
                        .setAttribute("format","checkbox")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Normal direction of motion")
                        .setAttribute("format","radiobuttons")
                        )
                    )
                )
            .addContent(new Element("pane")
                .setAttribute("name", "CV")
                .addContent(new Element("column")
                    .addContent(new Element("cvtable"))
                    )
                )
            .addContent(new Element("pane")
                .setAttribute("name", "Other")
                .addContent(new Element("column")
                    .addContent(new Element("display")
                        .setAttribute("item", "Address")
                        )
                    .addContent(new Element("display")
                        .setAttribute("item", "Normal direction of motion")
                        )
                    )
                )
            )
            ; // end of adding contents

        PaneProgFrame pFrame = new PaneProgFrame(null, new RosterEntry(),
                                                 "test frame", "programmers/Basic.xml",
                                                 p, false) {
            // dummy implementations
            protected JPanel getModePane() { return null; }
        };
        CvTableModel cvModel = new CvTableModel(new JLabel(), p);
        IndexedCvTableModel icvModel = new IndexedCvTableModel(new JLabel(), p);

        String[] args = {"CV", "Name"};
        VariableTableModel varModel = new VariableTableModel(null, args, cvModel, icvModel);
        varModel.setRow(0,new Element("variable")
                .setAttribute("label", "Primary Address")
                .setAttribute("CV", "1")
                .setAttribute("item", "Short Address")
                .addContent(new Element("shortAddressVal"))            
        );
        varModel.setRow(1,new Element("variable")
                .setAttribute("label", "Start Voltage")
                .setAttribute("CV", "2")
                .addContent(new Element("decVal"))            
        );
        varModel.setRow(0,new Element("variable")
                .setAttribute("label", "Normal direction of motion")
                .setAttribute("CV", "29")
                .addContent(new Element("enumVal")
                    .addContent(new Element("enumChoice").setAttribute("choice", "fwd"))
                    .addContent(new Element("enumChoice").setAttribute("choice", "rev"))
                )            
        );
        
        // create test object with special implementation of the newColumn(String) operation
        colCount = 0;
        PaneProgPane retval = new PaneProgPane(pFrame, "name", pane1, cvModel, icvModel, varModel, null) {
            };
            
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
