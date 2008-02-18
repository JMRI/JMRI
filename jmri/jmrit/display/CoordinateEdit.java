// CoordinateEdit.java

package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.*;

import jmri.util.JmriJFrame;

import java.io.*;

/**
 * Displays and allows user to modify x & y coordinates of
 * positionable labels
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision: 1.2 $
 */

public class CoordinateEdit extends JmriJFrame {

	PositionableLabel pl; 			// positional label tracked by this frame
	MouseListener ml = new ml(); 	// mouse listerner so we know if the label moves
	static final int INIT = -999;
	int oldX = INIT;
	int oldY = INIT;

	// member declarations
	javax.swing.JLabel lableName = new javax.swing.JLabel();
	javax.swing.JLabel nameText = new javax.swing.JLabel();
	javax.swing.JLabel textX = new javax.swing.JLabel();
	javax.swing.JLabel textY = new javax.swing.JLabel();

	// buttons
	javax.swing.JButton okButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();

	// text field
	javax.swing.JTextField xTextField = new javax.swing.JTextField(4);
	javax.swing.JTextField yTextField = new javax.swing.JTextField(4);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();

	public CoordinateEdit() {
		super();
	}

	public void windowClosed(java.awt.event.WindowEvent e) {
		pl.removeMouseListener(ml);
		super.windowClosed(e);
	}

	public void initComponents(PositionableLabel l, String name) throws Exception {
		pl = l;
		// the following code sets the frame's initial state
		
		lableName.setText("Name: ");
		lableName.setVisible(true);
		
		nameText.setText(name);
		nameText.setVisible(true);

		textX.setText("x= " + pl.getX());
		textX.setVisible(true);
		textY.setText("y= " + pl.getY());
		textY.setVisible(true);

		xTextField.setText("");
		xTextField.setToolTipText("Enter x coordinate");
		xTextField.setMaximumSize(new Dimension(
				xTextField.getMaximumSize().width, xTextField
						.getPreferredSize().height));

		yTextField.setText("");
		yTextField.setToolTipText("Enter y coordinate");
		yTextField.setMaximumSize(new Dimension(
				yTextField.getMaximumSize().width, yTextField
						.getPreferredSize().height));

		okButton.setText("  Set  ");
		okButton.setVisible(true);
// 		tool tips get in the way of using this panel		
//		okButton.setToolTipText("press to change x and y coordinates");

		cancelButton.setText(" Cancel ");
		cancelButton.setVisible(true);
//		tool tips get in the way of using this panel	
//		cancelButton.setToolTipText("press to cancel changes");

		setTitle("Set x & y");
		getContentPane().setLayout(new GridBagLayout());
		
		//setSize(150, 120);

		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(xTextField, 1, 1);
		addItem(textY, 0, 2);
		addItem(yTextField, 1, 2);
		addItem(cancelButton, 0, 3);
		addItem(okButton, 1, 3);

		// setup buttons
		addButtonAction(okButton);
		addButtonAction(cancelButton);
		pack();

		// Add listener so we know if the label moves

		pl.addMouseListener(ml);
	}

	private void addItem(JComponent c, int x, int y) {
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = x;
		gc.gridy = y;
		gc.weightx = 100.0;
		gc.weighty = 100.0;
		getContentPane().add(c, gc);
	}

	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

		if (ae.getSource() == okButton) {
			// save current coordinates in case user cancels
			if (oldX == INIT) {
				oldX = pl.getX();
				oldY = pl.getY();
			}
			int x = validXCoordinate(xTextField.getText());
			int y = validYCoordinate(yTextField.getText());
			pl.setLocation(x, y);
			textX.setText("x= " + pl.getX());
			textY.setText("y= " + pl.getY());
		}
		if (ae.getSource() == cancelButton) {
			if (oldX != INIT)
				pl.setLocation(oldX, oldY);
			dispose();
		}
	}

	// determines x movement absolute or relative
	private int validXCoordinate(String s) {
		int x = pl.getX();
		try {
			x = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			if (s.length() > 0) {
				if (s.charAt(0) == '+')
					if (s.length() > 1) {
						try {
							x = x + Integer.parseInt(s.substring(1));
						} catch (NumberFormatException e2) {
						}
					} else {
						x = x + 1;
					}
				if (s.charAt(0) == '-')
					x = x - 1;
			}
		}
		// neg delta?
		if (x < 0) {
			x = pl.getX() + x;
			if (x < 0)
				x = 0;
		}
		return x;
	}

	// determines y movement absolute or relative
	private int validYCoordinate(String s) {
		int y = pl.getY();
		try {
			y = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			if (s.length() > 0) {
				if (s.charAt(0) == '+')
					if (s.length() > 1) {
						try {
							y = y + Integer.parseInt(s.substring(1));
						} catch (NumberFormatException e2) {
						}
					} else {
						y = y + 1;
					}
				if (s.charAt(0) == '-')
					y = y - 1;
			}
		}
		// neg delta?
		if (y < 0) {
			y = pl.getY() + y;
			if (y < 0)
				y = 0;
		}
		return y;
	}

	class ml implements MouseListener {

		public void mousePressed(MouseEvent e) {
			// log.debug("Pressed: ");
		}

		public void mouseReleased(MouseEvent e) {
			log.debug("Mouse released");
			textX.setText("x= " + pl.getX());
			textY.setText("y= " + pl.getY());
		}

		public void mouseClicked(MouseEvent e) {
			// log.debug("Clicked: ");
		}

		public void mouseEntered(MouseEvent e) {
			// log.debug("Entered: ");
		}

		public void mouseExited(MouseEvent e) {
			// log.debug("Exited:  ");
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(CoordinateEdit.class.getName());
}
