// DecoderProAction.java

 package apps.gui3.paned;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import jmri.util.swing.*;

// for ugly code
import jmri.progdebugger.*;
import jmri.jmrit.symbolicprog.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import jmri.jmrit.roster.*;
import org.jdom.*;

/**
 * Action to produce a new, standalone DecoderPro window.
 *
 * Ignores WindowInterface.
 *
 * @author		Bob Jacobsen Copyright (C) 2010
 * @version		$Revision: 1.8 $
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
    	        null);  // no toolbar
    	        
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

        JComponent l = createPane(); // new JLabel("Display of a particular pane will go here");
        //l.setPreferredSize(new java.awt.Dimension(100, 200));
        retval.add(l);

        retval.add(Box.createVerticalGlue());
        retval.add(new JSeparator());

        // toolbar alternate for programming
        JToolBar bar = JToolBarUtil.loadToolBar(
            new java.io.File("xml/config/apps/decoderpro/ProgramingButtons.xml"),
            wi, null);
        bar.setOrientation(JToolBar.HORIZONTAL);
        retval.add(bar);
        
        return retval;
    }
    
    JComponent createLeft() {
        JPanel retval = new JPanel();
        retval.setLayout(new BoxLayout(retval, BoxLayout.Y_AXIS));
        float defaultXAlignment = 0.f;
        
        JToolBar bar = JToolBarUtil.loadToolBar(
            new java.io.File("xml/config/apps/decoderpro/GlobalProgButtons.xml"),
            wi, null);
        bar.setOrientation(JToolBar.VERTICAL);
        bar.setAlignmentX(defaultXAlignment);
        retval.add(bar);
        
        retval.add(Box.createRigidArea(new Dimension(10,10)));
        retval.add(new JSeparator());
        
        bar = new JToolBar("Panes");
        bar.setOrientation(JToolBar.VERTICAL);
        bar.setAlignmentX(defaultXAlignment);
        JList list = new JList(new String[]{
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
            });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bar.add(list);
        retval.add(bar);
        
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
    
    // amazingly ugly temp pane code
    static String result = null;
    static int colCount = -1;
    static int varCount = -1;
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
