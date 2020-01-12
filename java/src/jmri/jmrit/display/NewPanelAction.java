package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * Start a Panel Editor or a Layout Editor for a new Panel.
 * <p>
 * Uses the individual LayoutEditorAction or PanelEditorAction to start the
 * editors, to ensure consistent operation.
 *
 * @author Dave Duchamp Copyright (C) 2007
 * @author Bob Jacobsen Copyright (C) 2008
 * @author Egbert Broerse Copyright (C) 2017
 */
public class NewPanelAction extends AbstractAction {

    public NewPanelAction(String s) {
        super(s);
    }

    public NewPanelAction() {
        this(Bundle.getMessage("MenuItemNew"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // allow user to choose a panel editor
        int response = JOptionPane.showOptionDialog(null,
                Bundle.getMessage("ChoiceText1") + "\n" + Bundle.getMessage("ChoiceText2") + "\n"
                + Bundle.getMessage("ChoiceText3") + "\n" + Bundle.getMessage("ChoiceText4") + "\n"
                        + Bundle.getMessage("ChoiceText5"),
                Bundle.getMessage("ChooseEditor"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                new Object[]{Bundle.getMessage("SwitchboardEditor"), Bundle.getMessage("LayoutEditor"),
                        Bundle.getMessage("ControlPanelEditor"), Bundle.getMessage("PanelEditor"),
                        Bundle.getMessage("ButtonCancel")},
                Bundle.getMessage("PanelEditor")); // title
        if (response == 3) {
            new jmri.jmrit.display.panelEditor.PanelEditorAction().actionPerformed(null);
        } else if (response == 2) {
            new jmri.jmrit.display.controlPanelEditor.ControlPanelEditorAction().actionPerformed(null);
        } else if (response == 1) {
            new jmri.jmrit.display.layoutEditor.LayoutEditorAction().actionPerformed(null);
        } else if (response == 0) {
            new jmri.jmrit.display.switchboardEditor.SwitchboardEditorAction().actionPerformed(null);
        }
    }

}
