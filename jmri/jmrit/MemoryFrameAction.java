// MemoryFrameAction.java

package jmri.jmrit;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import jmri.jmrit.roster.*;
import jmri.jmrit.decoderdefn.*;

/**
 * Display memory usage on request
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Id: MemoryFrameAction.java,v 1.3 2003-05-11 02:00:30 jacobsen Exp $
 */
public class MemoryFrameAction extends AbstractAction {

    public MemoryFrameAction(String s) {
        super(s);
    }
    public MemoryFrameAction() {
        this("Memory monitor");
    }

	JTextField used1 = new JTextField(15);
	JTextField used2 = new JTextField(15);
	JTextField used3 = new JTextField(15);

	JTextField free1 = new JTextField(15);
	JTextField free2 = new JTextField(15);
	JTextField free3 = new JTextField(15);

	JTextField total1 = new JTextField(15);
	JTextField total2 = new JTextField(15);
	JTextField total3 = new JTextField(15);

	JButton updateButton = new JButton("Update");
	JButton gcButton = new JButton("Collect memory");
	JButton testButton = new JButton("Test");

    public void actionPerformed(ActionEvent e) {
		JFrame f = new JFrame("Memory usage");

		Container p = f.getContentPane();
		p.setLayout(new GridLayout(5,3));

		p.add(new JLabel("used (kB)"));
		p.add(new JLabel("free (kB)"));
		p.add(new JLabel("total (kB)"));

		p.add(used3);
		p.add(free3);
		p.add(total3);

		p.add(used2);
		p.add(free2);
		p.add(total2);

		p.add(used1);
		p.add(free1);
		p.add(total1);

		p.add(updateButton);
		p.add(gcButton);
		p.add(testButton);

		updateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				updateDisplay();
			}
		});
		gcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Runtime.getRuntime().gc();
				updateDisplay();
			}
		});
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Roster.instance();
				DecoderIndexFile.instance();
				updateDisplay();
			}
		});

		f.pack();
		f.show();
	}

	void updateDisplay() {
		used3.setText(used2.getText());
		free3.setText(free2.getText());
		total3.setText(total2.getText());

		used2.setText(used1.getText());
		free2.setText(free1.getText());
		total2.setText(total1.getText());

		long free  = Runtime.getRuntime().freeMemory()/1024;
		long total = Runtime.getRuntime().totalMemory()/1024;
		used1.setText(Long.toString(total-free));
		free1.setText(Long.toString(free));
		total1.setText(Long.toString(total));
	}

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MemoryFrameAction.class.getName());

}
