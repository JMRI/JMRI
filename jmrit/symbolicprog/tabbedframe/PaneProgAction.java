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
				
		System.out.println("red           "+Color.red);
		System.out.println("bright red    "+Color.red.brighter());
		System.out.println("dark red      "+Color.red.darker());

		System.out.println("green         "+Color.green);
		System.out.println("bright green  "+Color.green.brighter());
		System.out.println("dark green    "+Color.green.darker());
		
		System.out.println("yellow        "+Color.yellow);
		System.out.println("bright yellow "+Color.yellow.brighter());
		System.out.println("dark yellow   "+Color.yellow.darker());

		System.out.println("black         "+Color.black);
		System.out.println("gray          "+Color.gray);
		System.out.println("white         "+Color.white);
		
		Color myred = new Color(1.0f, 0.5f, 0.5f);
		Color mygreen = new Color(0.5f, 1.0f, 0.5f);
		Color myyellow = new Color(1.0f, 1.0f, 0.5f);
		
		Color c1 = Color.white;
		Color c2 = Color.red;
		
		Color mixred = new Color((c1.getRed()+c2.getRed())/2.f, (c1.getGreen()+c2.getGreen())/2.f,(c1.getBlue()+c2.getBlue())/2.f );
		
		JTabbedPane t = new JTabbedPane();
		
				JPanel p1 = new JPanel();
				JComponent temp;
				p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
					JPanel p11 = new JPanel();
						p11.setLayout(new GridLayout(6,2));
						p11.add(new JLabel("Address:"));
						p11.add(temp = new JTextField("mixed red"));
						temp.setBackground(myred);
						
						p11.add(new JLabel("Acceleration:"));
						p11.add(temp = new JTextField("mixed yellow"));
						temp.setBackground(myyellow);
						
						p11.add(new JLabel("Deceleration:"));
						p11.add(temp = new JTextField("mixed green"));
						temp.setBackground(mygreen);

						p11.add(new JLabel("Max Voltage:"));
						p11.add(temp = new JTextField("green"));
						temp.setBackground(Color.green);

						p11.add(new JLabel("Mid Voltage:"));
						p11.add(temp = new JTextField("yellow"));
						temp.setBackground(Color.yellow);

						p11.add(new JLabel("Start Voltage:"));
						p11.add(temp = new JTextField("red"));
						temp.setBackground(Color.red);

					p1.add(p11);
					p1.add(new JSeparator(javax.swing.SwingConstants.VERTICAL));
					JPanel p12 = new JPanel();
						p12.setLayout(new GridLayout(6,1));
						p12.add(temp = new JCheckBox("Reverse direction"));
						temp.setBackground(Color.red);

						p12.add(temp = new JCheckBox("28/128 speed steps"));
						temp.setBackground(Color.red.darker());

						p12.add(temp = new JCheckBox("Analog mode"));
						temp.setBackground(Color.green);

						p12.add(temp = new JCheckBox("Use speed table"));
						temp.setBackground(Color.green.darker());

						p12.add(temp = new JCheckBox("Long Address"));
						temp.setBackground(Color.yellow);

						p12.add(temp = new JCheckBox("Long Address"));
						temp.setBackground(Color.yellow.darker());
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
