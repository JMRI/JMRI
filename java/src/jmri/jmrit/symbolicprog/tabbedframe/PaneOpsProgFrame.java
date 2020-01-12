package jmri.jmrit.symbolicprog.tabbedframe;

import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.JPanel;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrit.decoderdefn.DecoderFile;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend the PaneProgFrame to handle ops mode operations
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 */
public class PaneOpsProgFrame extends PaneProgFrame {

    JPanel modePane;

    /**
     * Provide programming-mode panel to the parent class.
     * <p>
     * In this case, provide just an empty JPanel; we presently don't want a
     * selection GUI to be present when in ops mode.
     */
    @Override
    protected JPanel getModePane() {
        if (modePane == null) {
            modePane = new JPanel();
        }
        return modePane;
    }

    /**
     * This invokes the parent ctor to do the real work. That will call back to
     * get the programming mode panel (none) and to hear if there is read mode
     * (no)
     *
     * @param decoderFile XML file defining the decoder contents
     * @param r           RosterEntry for information on this locomotive
     */
    public PaneOpsProgFrame(DecoderFile decoderFile, RosterEntry r,
            String name, String file, Programmer p) {
        super(decoderFile, r, name, file, p, true);

        if (log.isDebugEnabled()) {
            log.debug("PaneOpsProgFrame \"" + name
                    + "\" constructed");
        }
    }

    @Override
    void addHelp() {
        addHelpMenu("package.jmri.jmrit.symbolicprog.tabbedframe.PaneOpsProgFrame", true);
    }

    @Override
    protected void pickProgrammerMode(@Nonnull Element programming) {
        // find an accepted mode to set it to
        List<ProgrammingMode> modes = mProgrammer.getSupportedModes();

        if (log.isDebugEnabled()) {
            log.debug("Programmer supports:");
            for (ProgrammingMode m : modes) {
                log.debug("   {} {}", m.getStandardName(), m.toString());
            }
        }
        
        // first try specified modes
        for (Element el1 : programming.getChildren("mode")) {
            String name = el1.getText();
            if (log.isDebugEnabled()) log.debug(" mode {} was specified", name);
            for (ProgrammingMode m : modes) {
                if (name.equals(m.getStandardName())) {
                    log.info("Programming mode selected: {} ({})", m.toString(), m.getStandardName());
                    mProgrammer.setMode(m);
                    return;
                }
            }
        }

        // else leave as it is
        log.debug("Leaving mode as is, supposed to be ops mode");
    }

    /**
     * local dispose, which also invokes parent. Note that we remove the
     * components (removeAll) before taking those apart.
     */
    @Override
    public void dispose() {

        if (log.isDebugEnabled()) {
            log.debug("dispose local");
        }

        super.dispose();

    }

    private final static Logger log = LoggerFactory.getLogger(PaneOpsProgFrame.class);

}
