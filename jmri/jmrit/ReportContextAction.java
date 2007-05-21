// ReportContextAction.java

package jmri.jmrit;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;

/**
 * Swing action to display the JMRI context for the user
 *
 * @author	    Bob Jacobsen    Copyright (C) 2007
 * @version         $Revision: 1.1 $
 */
public class ReportContextAction extends AbstractAction {

    public ReportContextAction() { super();}

    public void actionPerformed(ActionEvent ev) {

		JFrame frame = new JFrame();
		
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        frame.getContentPane().add(new JLabel("JMRI Version: "
                            +jmri.Version.name()+"  "));	 

        String prefs = jmri.jmrit.XmlFile.prefsDir();
        frame.getContentPane().add(new JLabel("Preferences directory: "+prefs+"  "));
        
        String prog = System.getProperty("user.dir");
        frame.getContentPane().add(new JLabel("Program directory: "+prog+"  "));

        addProperty("java.version", frame);
        addProperty("java.vendor", frame);
        addProperty("java.home", frame);

        addProperty("java.vm.version", frame);
        addProperty("java.vm.vendor", frame);
        addProperty("java.vm.name", frame);

        addProperty("java.specification.version", frame);
        addProperty("java.specification.vendor", frame);
        addProperty("java.specification.name", frame);

        addProperty("java.class.version", frame);
        addProperty("java.class.path", frame);
        addProperty("java.library.path", frame);

        addProperty("java.compiler", frame);
        addProperty("java.ext.dirs", frame);
        		
        addProperty("os.name", frame);
        addProperty("os.arch", frame);
        addProperty("os.version", frame);

        addProperty("biff", frame);

        addProperty("user.name", frame);
        addProperty("user.home", frame);
        addProperty("user.dir", frame);


		frame.pack();
		frame.setVisible(true);
	
	}
		
	void addProperty(String prop, JFrame frame) {
        frame.getContentPane().add(new JLabel(prop+": "
                            +System.getProperty(prop)+"  "));	    
    }
}

/* @(#)ReportContextAction.java */
