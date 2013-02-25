// PaneOpsProgFrame.java

package jmri.jmrit.symbolicprog.tabbedframe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import javax.swing.JPanel;

/**
 * Extend the PaneProgFrame to handle ops mode operations
 *
 * @author			Bob Jacobsen   Copyright (C) 2002, 2008
 * @version			$Revision$
 */
public class PaneOpsProgFrame extends PaneProgFrame
                 implements java.beans.PropertyChangeListener  {

    JPanel modePane;

    /**
     * Provide programming-mode panel to the parent class.
     * <p>
     * In this case, provide just an empty JPanel; we
     * presently don't want a selection GUI to be
     * present when in ops mode.
     */
    protected JPanel getModePane() { 
        if (modePane == null) 
            modePane = new JPanel();
        return modePane;
    }

    /**
     * This invokes the parent ctor to do the real work. That will
     * call back to get the programming mode panel (none) and to
     * hear if there is read mode (no)
     *
     * @param decoderFile XML file defining the decoder contents
     * @param r RosterEntry for information on this locomotive
     * @param name
     * @param file
     */
    public PaneOpsProgFrame(DecoderFile decoderFile, RosterEntry r,
                            String name, String file, Programmer p) {
        super(decoderFile, r, name, file, p, true);

        if (log.isDebugEnabled()) log.debug("PaneOpsProgFrame \""+name
                                            +"\" constructed");
    }

    void addHelp() {
        addHelpMenu("package.jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame", true);
    }

    /**
     * local dispose, which also invokes parent. Note that
     * we remove the components (removeAll) before taking those
     * apart.
     */
    public void dispose() {

        if (log.isDebugEnabled()) log.debug("dispose local");

        super.dispose();

    }

    static Logger log = LoggerFactory.getLogger(PaneOpsProgFrame.class.getName());

}

