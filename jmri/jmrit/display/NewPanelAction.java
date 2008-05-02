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
 * @version	$Revision: 1.3 $
 */
public class NewPanelAction extends AbstractAction {

    static final ResourceBundle rbx = ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle");

    public NewPanelAction(String s) {
        super(s);
    }

    public NewPanelAction() {
        this(
            java.util.ResourceBundle.getBundle("jmri.jmrit.display.DisplayBundle")
                .getString("MenuItemNew"));
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
			String name = "My Layout";
			for (int i = 2; i < 100; i++){
				if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
					name = "My Layout " +i;
				}
			}
			LayoutEditor panel = new LayoutEditor(name);
			panel.pack();
			panel.show();
			panel.setEditMode(true);
			panel.setCurrentPositionAndSize();
		}
		else if (response == 2) {
			PanelEditor panel = new PanelEditor();
			String name = "Panel";
			for (int i = 2; i < 100; i++){
				if(jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)){
					name = "Panel " +i;
				}
			}
			JmriJFrame targetFrame = panel.makeFrame(name);
			jmri.jmrit.display.PanelMenu.instance().addPanelEditorPanel(panel);
			targetFrame.setLocation(20,20);
        
			panel.setTitle();

			targetFrame.pack();
			targetFrame.setVisible(true);

			panel.pack();
			panel.setVisible(true);
		}
	}
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NewPanelAction.class.getName());

}
