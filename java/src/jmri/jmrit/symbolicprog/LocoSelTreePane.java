package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import jmri.jmrit.progsupport.ProgModeSelector;

/**
 * Provide GUI controls to select a new decoder.
 * <p>
 * This is an extension of the CombinedLocoSelPane class to use a JTree instead
 * of a JComboBox for the decoder selection. The loco selection (Roster
 * manipulation) parts are unchanged.
 * <p>
 * The JComboBox implementation always had to have selected entries, so we added
 * dummy "select from .." items at the top {@literal &} used those to indicate
 * that there was no selection in that box. Here, the lack of a selection
 * indicates there's no selection.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2008, 2015
 */
public class LocoSelTreePane extends CombinedLocoSelTreePane {

    public LocoSelTreePane(JLabel s, ProgModeSelector selector) {
        super(s, selector);
    }

    // don't show the select-roster-entry box
    @Override
    protected JPanel layoutRosterSelection() {
        return null;
    }

    @Override
    protected JPanel layoutDecoderSelection() {
        JPanel pan = super.layoutDecoderSelection();
        viewButtons.setVisible(false);
        return pan;
    }

    // don't show the Ident button
    @Override
    JToggleButton addDecoderIdentButton() {
        return null;
    }

}
