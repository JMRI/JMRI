package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Start a Panel Editor or a Layout Editor for a new Panel.
 * <P>
 * Uses the individual LayoutEditorAction or PanelEditorAction to start the
 * editors, to ensure consistent operation.
 *
 * @author	Dave Duchamp Copyright (C) 2007
 * @author	Bob Jacobsen Copyright (C) 2008
 */
public class NewPanelAction extends AbstractAction {

    public NewPanelAction(String s) {
        super(s);
    }

    public NewPanelAction() {
        this(Bundle.getMessage("MenuItemNew"));
    }

    public void actionPerformed(ActionEvent e) {
        // allow user to choose a panel editor
        int response = JOptionPane.showOptionDialog(null,
                Bundle.getMessage("ChoiceText1") + "\n" + Bundle.getMessage("ChoiceText2") + "\n"
                + Bundle.getMessage("ChoiceText3"), Bundle.getMessage("ChooseEditor"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("LayoutEditor"),
                    Bundle.getMessage("PanelEditor")},
                Bundle.getMessage("PanelEditor"));
        if (response == 1) {
            new jmri.jmrit.display.layoutEditor.LayoutEditorAction().actionPerformed(null);
        } else if (response == 2) {
            new jmri.jmrit.display.panelEditor.PanelEditorAction().actionPerformed(null);
        }
    }

}
