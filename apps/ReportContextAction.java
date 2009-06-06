// ReportContextAction.java

package apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;
import jmri.util.JmriInsets;

/**
 * Swing action to display the JMRI context for the user
 *
 * @author	Bob Jacobsen    Copyright (C) 2007
 * @author  Matt Harris Copyright (C) 2008
 *
 * @version         $Revision: 1.17 $
 */
public class ReportContextAction extends AbstractAction {

    public ReportContextAction() { super();}

    javax.swing.JTextArea pane;
    
    public void actionPerformed(ActionEvent ev) {

		JFrame frame = new jmri.util.JmriJFrame(){};  // JmriJFrame to ensure fits on screen
		
        pane = new javax.swing.JTextArea();
        pane.append("\n"); // add a little space at top
        pane.setEditable(false);
 
        JScrollPane  scroll = new JScrollPane(pane);
        frame.getContentPane().add(scroll);
        
        jmri.jmrit.mailreport.ReportContext r = new jmri.jmrit.mailreport.ReportContext();
        addString(r.getReport());
        
        pane.append("\n"); // add a little space at bottom

		frame.pack();

        // start scrolled to top
        JScrollBar b = scroll.getVerticalScrollBar();
        b.setValue(b.getMaximum());

        // show
		frame.setVisible(true);
	
	}
		
	void addString(String val) {
        pane.append(val+"\n");	    
    }
	void addProperty(String prop) {
        addString(prop+": "+System.getProperty(prop)+"  ");	    
    }
    
    /** 
     * Provide screen - size information.  This is
     * based on the jmri.util.JmriJFrame calculation, 
     * but isn't refactored to there because we 
     * also want diagnostic info
     */
    public void addScreenSize() {
        try {
            // Find screen size. This throws null-pointer exceptions on
            // some Java installs, however, for unknown reasons, so be
            // prepared to fal back.
            JFrame dummy = new JFrame();
            try {
                Insets insets = dummy.getToolkit().getScreenInsets(dummy.getGraphicsConfiguration());
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:"+screen.height+", w:"+screen.width+" Inset t:"+insets.top+", b:"+insets.bottom
                        +"; l:"+insets.left+", r:"+insets.right);
            } catch (NoSuchMethodError e) {
                Dimension screen = dummy.getToolkit().getScreenSize();
                addString("Screen size h:"+screen.height+", w:"+screen.width
                            +" (No Inset method available)");
            }
        } catch (Throwable e2) {
            // failed, fall back to standard method
            addString("(Cannot sense screen size due to "+e2.toString()+")");
        }
        
        try {
            // Find screen resolution. Not expected to fail, but just in case....
            int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
            addString("Screen resolution: "+dpi);
        } catch (Throwable e2) {
            addString("Screen resolution not available");
        }
        
        // look at context
        //Rectangle virtualBounds = new Rectangle();
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            addString("Environment max bounds: "+ge.getMaximumWindowBounds());
            
            try {
                GraphicsDevice[] gs = ge.getScreenDevices();
                for (int j = 0; j < gs.length; j++) { 
                    GraphicsDevice gd = gs[j];
                    GraphicsConfiguration[] gc = gd.getConfigurations();
                    for (int i=0; i < gc.length; i++) {
                        addString("bounds["+0+"] = "+gc[i].getBounds());
                        // virtualBounds = virtualBounds.union(gc[i].getBounds());
                    }
                } 
            } catch (Throwable e2) {
                addString("Exception getting device bounds "+e2.getMessage());
            }
        } catch (Throwable e1) {
            addString("Exception getting max window bounds "+e1.getMessage());
        }
        // Return the insets using a custom class
        // which should return the correct values under
        // various Linux window managers
        try {
            Insets jmriInsets = JmriInsets.getInsets();
            addString("JmriInsets t:"+jmriInsets.top+", b:"+jmriInsets.bottom
                     +"; l:"+jmriInsets.left+", r:"+jmriInsets.right);
        }
        catch (Throwable e) {
            addString("Exception getting JmriInsets" + e.getMessage());
        }
    }
}

/* @(#)ReportContextAction.java */
