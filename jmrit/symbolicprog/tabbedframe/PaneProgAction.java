/** 
 * PaneProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import java.awt.event.ActionEvent;

import javax.swing.*;
import java.awt.*;

public class PaneProgAction 			extends AbstractAction {

	public PaneProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create a dummy paned frame
		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
				
		JTabbedPane t = new JTabbedPane();
		
				JPanel p1 = new JPanel();
				p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
					JPanel p11 = new JPanel();
						p11.setLayout(new GridLayout(5,2));
						p11.add(new JLabel("Address:"));
						p11.add(new JTextField("      "));
						p11.add(new JLabel("Acceleration:"));
						p11.add(new JTextField("      "));
						p11.add(new JLabel("Deceleration:"));
						p11.add(new JTextField("      "));
						p11.add(new JLabel("Max Voltage:"));
						p11.add(new JTextField("      "));
						p11.add(new JLabel("Mid Voltage:"));
						p11.add(new JTextField("      "));
					p1.add(p11);
					p1.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));
					JPanel p12 = new JPanel();
						p12.setLayout(new GridLayout(6,1));
						p12.add(new JCheckBox("Reverse direction"));
						p12.add(new JCheckBox("28/128 speed steps"));
						p12.add(new JCheckBox("Analog mode"));
						p12.add(new JCheckBox("Use speed table"));
						p12.add(new JCheckBox("Long Address"));
						p12.add(new JCheckBox("Long Address"));
					p1.add(p12);				
				t.addTab("Basics", p1);
				
				JPanel p2 = new JPanel();
					p2.setLayout(new GridLayout(3,2));
				t.addTab("CVs", p2);
				
				JPanel p3 = new JPanel();
					p3.setLayout(new GridLayout(3,2));
				t.addTab("variables", p3);
				
		f.getContentPane().add(t);
		
		f.getContentPane().add(new JSeparator());
		
		// f.getContentPane().add(new jmri.ProgModePane(BoxLayout.X_AXIS));
		JPanel r1 = new JPanel();
			r1.setLayout(new FlowLayout());
			r1.add(new JRadioButton("Paged Mode"));
			r1.add(new JRadioButton("Direct Mode"));
		f.getContentPane().add(r1);
					
		f.pack();	
		f.show();	
		
	}
}


/* @(#)PanecProgAction.java */
