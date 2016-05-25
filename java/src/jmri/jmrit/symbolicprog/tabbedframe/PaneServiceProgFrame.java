package jmri.jmrit.symbolicprog.tabbedframe;

import javax.swing.JPanel;
import jmri.GlobalProgrammerManager;
import jmri.Programmer;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend the PaneProgFrame to handle service mode operations.
 * <p>
 * If no programmer is provided, the programmer parts of the GUI are suppressed.
 *
 * @author	Bob Jacobsen Copyright (C) 2002, 2008
 */
public class PaneServiceProgFrame extends PaneProgFrame
        implements java.beans.PropertyChangeListener {

    jmri.jmrit.progsupport.ProgModeSelector modePane;

    /**
     * Provide the programming mode selection pane for inclusion
     */
    protected JPanel getModePane() {
        // ensure initialization, even if invoked in ctor
        if (modePane == null) {
            modePane = new jmri.jmrit.progsupport.ProgServiceModeComboBox() {
                protected java.util.List<GlobalProgrammerManager> getMgrList() {
                    return new java.util.ArrayList<GlobalProgrammerManager>();
                }

                public Programmer getProgrammer() {
                    return mProgrammer;
                }
            };
        }
        log.debug("invoked getModePane");
        return modePane;
    }

    /**
     * This invokes the parent ctor to do the real work. That will call back to
     * get the programming mode panel (provided) and to hear if there is read
     * mode (depends). Then, this sets the programming mode for the service
     * programmer based on what's in the decoder file.
     *
     * @param decoderFile XML file defining the decoder contents
     * @param r           RosterEntry for information on this locomotive
     * @param name
     * @param file
     */
    public PaneServiceProgFrame(DecoderFile decoderFile, RosterEntry r,
            String name, String file, Programmer pProg) {
        super(decoderFile, r, name, file, pProg, false);

        pack();

        if (log.isDebugEnabled()) {
            log.debug("PaneServiceProgFrame \"" + name
                    + "\" constructed");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PaneServiceProgFrame.class.getName());

}
