/*
 *
 */

package jmri.jmrix.sprog.update;

import java.awt.*;
import java.awt.event.*;

public class SprogAlertDialog extends Dialog implements ActionListener {

    public SprogAlertDialog(Frame parent,
		       String title,
		       String text) {
	super(parent, title, true);

	Panel labelPanel = new Panel();
	labelPanel.setLayout(new GridLayout(3, 1));
	labelPanel.add(new Label(text, Label.CENTER));
	add(labelPanel, "Center");

	Panel buttonPanel = new Panel();
	Button okButton = new Button("OK");
	okButton.addActionListener(this);
	buttonPanel.add(okButton);
	add(buttonPanel, "South");

	FontMetrics fm = getFontMetrics(getFont());
	int width = fm.stringWidth(text);

	setSize(width + 40, 150);
	setLocation(parent.getLocationOnScreen().x + 30,
		    parent.getLocationOnScreen().y + 30);
	setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
	setVisible(false);
	dispose();
    }
}
