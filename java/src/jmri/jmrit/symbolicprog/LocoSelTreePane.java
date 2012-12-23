// LocoSelTreePane.java

package jmri.jmrit.symbolicprog;

import javax.swing.JToggleButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Provide GUI controls to select a new decoder.
 * <P>
 * This is an extension of the CombinedLocoSelPane class to use
 * a JTree instead of a JComboBox for the decoder selection.
 * The loco selection (Roster manipulation) parts are unchanged.
 * <P>
 * The JComboBox implementation always had to have selected entries, so
 * we added dummy "select from .." items at the top & used those to
 * indicate that there was no selection in that box.
 * Here, the lack of a selection indicates there's no selection.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001, 2002, 2008
 * @version			$Revision$
 */
public class LocoSelTreePane extends CombinedLocoSelTreePane  {

    public LocoSelTreePane(JLabel s) {
            super(s);
    }

    public LocoSelTreePane() {
            super();
    }

    // don't show the select-roster-entry box
    protected JPanel layoutRosterSelection() { return null; }
    
    protected JPanel layoutDecoderSelection() { 
        JPanel pan = super.layoutDecoderSelection();
        viewButtons.setVisible(false);
        return pan;
    }
    
    // don't show the Ident button
    JToggleButton addDecoderIdentButton() {
        return null;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoSelTreePane.class.getName());

}
