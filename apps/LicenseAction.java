// LicenseAction.java

package apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

import java.awt.*;
import javax.swing.*;
import java.io.*;

/**
 * Swing action to display the JMRI license
 *
 * @author	    Bob Jacobsen    Copyright (C) 2004
 * @version         $Revision: 1.1 $
 */
public class LicenseAction extends AbstractAction {

    public LicenseAction() { super();}

    public void actionPerformed(ActionEvent ev) {

		JFrame frame = new JFrame();
		
   		JScrollPane jScrollPane = new JScrollPane();
		JTextPane textPane = new JTextPane();

		// get the file
		
		File file = new File("resources"+File.separator+"COPYING");
		
		String t="";
		try {
			BufferedReader r = new BufferedReader(new FileReader(file));
			while (r.ready())
				t = t+r.readLine()+"\n";
			
			textPane.setText(t);
			r.close();
		} catch (IOException ex) {
			t = "For license information, see the JMRI website jttp://jmri.sourceforge.net";
		}
		
		// set up display
        textPane.setEditable(false);
        jScrollPane.getViewport().add(textPane);
		frame.getContentPane().add(jScrollPane);
		
		frame.pack();
		frame.setSize(new Dimension(660, 550));
		frame.show();
		
		// start scrolled to top
		JScrollBar b = jScrollPane.getVerticalScrollBar();
		b.setValue(b.getMinimum());
    }
}

/* @(#)LicenseAction.java */
