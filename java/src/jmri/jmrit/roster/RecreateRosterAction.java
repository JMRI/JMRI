package jmri.jmrit.roster;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Recreate the roster index file if it's been damaged or lost.
 * <p>
 * Scans the roster directory for xml files, including any that are found.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class RecreateRosterAction extends JmriAbstractAction {

    public RecreateRosterAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public RecreateRosterAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public RecreateRosterAction() {
        this("Rebuild Roster");
    }

    public RecreateRosterAction(String s) {
        super(s);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Roster.getDefault().reindex();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
