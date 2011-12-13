// jmri.jmrit.display.NewPanelAction.java

package jmri.jmrit.display;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import java.util.ResourceBundle;

/**
 * Start a Panel Editor or a Layout Editor for a new Panel.
 * <P>
 * Uses the individual LayoutEditorAction or PanelEditorAction to 
 * start the editors, to ensure consistent operation.
 *
 * @author	Dave Duchamp   Copyright (C) 2007
 * @author	Bob Jacobsen   Copyright (C) 2008 
 * @version	$Revision$
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
            new jmri.jmrit.display.layoutEditor.LayoutEditorAction().actionPerformed(null);
		}
		else if (response == 2) {
		    new jmri.jmrit.display.panelEditor.PanelEditorAction().actionPerformed(null);
		}
	}
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NewPanelAction.class.getName());

}
