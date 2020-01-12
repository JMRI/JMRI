package jmri.jmrix.nce.consist;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.util.davidflanagan.HardcopyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print a summary of the Roster contents
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class PrintNceConsistRosterAction extends AbstractAction {

    public PrintNceConsistRosterAction(String actionName, Frame frame, boolean preview) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    @Override
    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, Bundle.getMessage("NcePrintRosterTitle"), 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(FileUtil.findURL("resources/decoderpro.gif", FileUtil.Location.INSTALLED));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the Roster, printing as needed
        NceConsistRoster r = InstanceManager.getDefault(NceConsistRoster.class);
        List<NceConsistRosterEntry> list = r.matchingList(null, null, null, null, null, null, null, null, null, null); // take all

        log.debug("Roster list size: " + list.size());
        for (NceConsistRosterEntry entry : list) {
            entry.printEntry(writer);
        }

        // and force completion of the printing
        writer.close();
    }

    private final static Logger log = LoggerFactory.getLogger(PrintNceConsistRosterAction.class);
}
