// LocoSelTreePane.java
package jmri.jmrit.symbolicprog;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import jmri.jmrit.progsupport.ProgModeSelector;

/**
 * Provide GUI controls to select a new decoder.
 * <P>
 * This is an extension of the CombinedLocoSelPane class to use a JTree instead
 * of a JComboBox for the decoder selection. The loco selection (Roster
 * manipulation) parts are unchanged.
 * <P>
 * The JComboBox implementation always had to have selected entries, so we added
 * dummy "select from .." items at the top & used those to indicate that there
 * was no selection in that box. Here, the lack of a selection indicates there's
 * no selection.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2008, 2015
 * @version	$Revision$
 */
public class LocoSelTreePane extends CombinedLocoSelTreePane {

    /**
     *
     */
    private static final long serialVersionUID = -4048446656319711656L;

    public LocoSelTreePane(JLabel s, ProgModeSelector selector) {
        super(s, selector);
    }

    // don't show the select-roster-entry box
    protected JPanel layoutRosterSelection() {
        return null;
    }

    protected JPanel layoutDecoderSelection() {
        JPanel pan = super.layoutDecoderSelection();
        viewButtons.setVisible(false);
        return pan;
    }

    // don't show the Ident button
    JToggleButton addDecoderIdentButton() {
        return null;
    }

}
