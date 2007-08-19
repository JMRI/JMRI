// jmri.jmrit.display.NewPanelAction.java

package jmri.jmrit.display;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import jmri.util.JmriJFrame;

import java.util.ResourceBundle;

/**
 * Start a Panel Editor or a Layout Editor for a new Panel.
 * <P>
 *
 * @author	Dave Duchamp   Copyright (C) 2007
 * @version	$Revision: 1.1 $
 */
public class NewPanelAction extends AbstractAction {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public NewPanelAction(String s) {
        super(s);
    }

    public NewPanelAction() {
        this("New Panel");
    }

    public void actionPerformed(ActionEvent e) {
		// allow user to choose a panel editor
		int response = JOptionPane.showOptionDialog(null,
					rbx.getString("ChoiceText1")+"\n"+rbx.getString("ChoiceText2")+"\n"+
					rbx.getString("ChoiceText3"), rbx.getString("ChooseEditor"),
					JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,
					new Object[]{rbx.getString("Cancel"),rbx.getString("LayoutEditor"),
					rbx.getString("PanelEditor")},
					rbx.getString("PanelEditor"));
		if (response == 1) {
			LayoutEditor panel = new LayoutEditor();
			panel.pack();
			panel.show();
			panel.setEditMode(true);
			panel.setCurrentPositionAndSize();
		}
		else if (response == 2) {
			PanelEditor panel = new PanelEditor();
			JmriJFrame targetFrame = panel.makeFrame("Panel");
			jmri.jmrit.display.PanelMenu.instance().addPanelEditorPanel(panel);
			targetFrame.setLocation(20,20);
        
			panel.setTitle();

			targetFrame.pack();
			targetFrame.setVisible(true);

			panel.pack();
			panel.setVisible(true);
		}
	}
}
