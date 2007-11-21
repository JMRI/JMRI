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
 * @version         $Revision: 1.2 $
 */
public class ReportContextAction extends AbstractAction {

    public ReportContextAction() { super();}

    java.awt.Container pane;
    
    public void actionPerformed(ActionEvent ev) {

		JFrame frame = new jmri.util.JmriJFrame();  // JmriJFrame to ensure fits on screen
		
        pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
 
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

        addProperty("biff");

        addProperty("user.name");
        addProperty("user.home");
        addProperty("user.dir");


		frame.pack();
		frame.setVisible(true);
	
	}
		
	void addString(String val) {
        pane.add(new JLabel(val));	    
    }
	void addProperty(String prop) {
        addString(prop+": "+System.getProperty(prop)+"  ");	    
    }
}

/* @(#)ReportContextAction.java */
