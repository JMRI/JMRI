// TextBorderSizeEdit.java

package jmri.jmrit.display;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import jmri.util.JmriJFrame;

import java.util.ResourceBundle;

/**
 * Displays and allows user to modify x & y coordinates of
 * positionable labels
 *
 * This is a modification of CoordinateEdit.java by Dan Boudreau for use with LayoutEditor
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @author Dave Duchamp (LayoutEditor version);
 * @version $Revision: 1.1 $
 */

public class TextBorderSizeEdit extends JmriJFrame 
//								implements MouseListener 
{

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

	PositionableLabel pl; 	// layout positional label tracked by this frame
	MouseListener ml = null; 	// mouse listerner so we know if non-background label moves
	static final int INIT = -999;
	int oldSize = INIT;
	
	// member declarations
	javax.swing.JLabel lableName = new javax.swing.JLabel();
	javax.swing.JLabel nameText = new javax.swing.JLabel();
	javax.swing.JLabel textX = new javax.swing.JLabel();
	//javax.swing.JLabel textY = new javax.swing.JLabel();

	// buttons
	javax.swing.JButton okButton = new javax.swing.JButton();
	javax.swing.JButton cancelButton = new javax.swing.JButton();

	// text field
	javax.swing.JTextField xTextField = new javax.swing.JTextField(4);
	//javax.swing.JTextField yTextField = new javax.swing.JTextField(4);

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();

	public TextBorderSizeEdit() {
		super();
	}

	public void windowClosed(java.awt.event.WindowEvent e) {
		if (ml != null) {
			pl.removeMouseListener(ml);
		}
		super.windowClosed(e);
	}

	public void initComponents(PositionableLabel l, String name) throws Exception {
		pl = l;
		if (!pl.isBackground()) {
			ml = new ml();
		}
		// the following code sets the frame's initial state
		
		lableName.setText(rb.getString("Name")+": ");
		lableName.setVisible(true);
		
		nameText.setText(name);
		nameText.setVisible(true);

		textX.setText("Size= " + pl.getBorderSize());
		textX.setVisible(true);
		//textY.setText("Size= " + pl.getFixedSize());
		//textY.setVisible(true);

		xTextField.setText("");
		xTextField.setToolTipText(rb.getString("EnterXTooltip"));
		/*xTextField.setMaximumSize(new Dimension(
				xTextField.getMaximumSize().size, xTextField
						.getPreferredSize().height));*/

		/*yTextField.setText("");
		yTextField.setToolTipText(rb.getString("EnterYTooltip"));
		yTextField.setMaximumSize(new Dimension(
				yTextField.getMaximumSize().size, yTextField
						.getPreferredSize().height));*/

		okButton.setText(rb.getString("Set"));
		okButton.setVisible(true);
		okButton.setToolTipText(rb.getString("SetButtonToolTip"));

		cancelButton.setText(rb.getString("Cancel"));
		cancelButton.setVisible(true);
		cancelButton.setToolTipText(rb.getString("CancelButtonToolTip"));

		setTitle(rb.getString("SetXY"));
		getContentPane().setLayout(new GridBagLayout());
		
		//setSize(150, 120);

		addItem(lableName, 0, 0);
		addItem(nameText, 1, 0);
		addItem(textX, 0, 1);
		addItem(xTextField, 1, 1);
//		addItem(textY, 0, 2);
//		addItem(yTextField, 1, 2);
		addItem(cancelButton, 0, 3);
		addItem(okButton, 1, 3);

		// setup buttons
		addButtonAction(okButton);
		addButtonAction(cancelButton);
		pack();

		if (!pl.isBackground()) {
			// Add listener so we know if the label moves
			pl.addMouseListener(ml);
		}
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
			if (oldSize == INIT) {
				oldSize = pl.getBorderSize();
				//oldHeight = pl.getBorderSize();
			}
			int size = validXFixedSize(xTextField.getText());
			//int height = validYFixedSize(yTextField.getText());
			pl.setBorderSize(size);
			textX.setText("Size= " + pl.getBorderSize());
			//textY.setText("y= " + pl.getBorderSize());
		}
		if (ae.getSource() == cancelButton) {
			if (oldSize != INIT)
				pl.setBorderSize(oldSize);
			if (ml != null) 
				pl.removeMouseListener(ml);
			dispose();
		}
	}

	// determines x movement absolute or relative
	private int validXFixedSize(String s) {
		int x = pl.getBorderSize();
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
			x = pl.getBorderSize() + x;
			if (x < 0)
				x = 0;
		}
		return x;
	}

	// determines y movement absolute or relative
/*	private int validYFixedSize(String s) {
		int y = pl.getFixedSize();
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
			y = pl.getFixedSize() + y;
			if (y < 0)
				y = 0;
		}
		return y;
	}*/

	class ml implements MouseListener {

		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			textX.setText("x= " + pl.getBorderSize());
		//	textY.setText("y= " + pl.getY());
		}

		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TextBorderSizeEdit.class.getName());
}
