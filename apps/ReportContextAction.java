// ReportContextAction.java

package apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;

/**
 * Swing action to display the JMRI context for the user
 *
 * @author	    Bob Jacobsen    Copyright (C) 2007
 * @version         $Revision: 1.8 $
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
        
        addString("JMRI Version: "+jmri.Version.name()+"  ");	 

        addProperty("java.version");

        addString("Connection one: "+Apps.getConnection1()+"  ");
        addString("Connection two: "+Apps.getConnection2()+"  ");

        String prefs = jmri.jmrit.XmlFile.prefsDir();
        addString("Preferences directory: "+prefs+"  ");
        
        String prog = System.getProperty("user.dir");
        addString("Program directory: "+prog+"  ");

        addProperty("java.vendor");
        addProperty("java.home");

        addProperty("java.vm.version");
        addProperty("java.vm.vendor");
        addProperty("java.vm.name");

        addProperty("java.specification.version");
        addProperty("java.specification.vendor");
        addProperty("java.specification.name");

        addProperty("java.class.version");
        addProperty("java.class.path");
        addProperty("java.library.path");

        addProperty("java.compiler");
        addProperty("java.ext.dirs");
        		
        addProperty("os.name");
        addProperty("os.arch");
        addProperty("os.version");

        addProperty("user.name");
        addProperty("user.home");
        addProperty("user.dir");

        addScreenSize();
        
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
        
        // look at context
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        addString("Environment max bounds: "+ge.getMaximumWindowBounds());
        GraphicsDevice[] gs = ge.getScreenDevices();
        for (int j = 0; j < gs.length; j++) { 
            GraphicsDevice gd = gs[j];
            GraphicsConfiguration[] gc = gd.getConfigurations();
            for (int i=0; i < gc.length; i++) {
                addString("bounds["+0+"] = "+gc[i].getBounds());
                // virtualBounds = virtualBounds.union(gc[i].getBounds());
            }
        } 
    }
}

/* @(#)ReportContextAction.java */
