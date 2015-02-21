// RecreateRosterAction.java
package jmri.jmrit.roster;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Recreate the roster index file if it's been damaged or lost.
 * <P>
 * Scans the roster directory for xml files, including any that are found.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version	$Revision$
 */
public class RecreateRosterAction extends JmriAbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 2421095427974812157L;

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

    public void actionPerformed(ActionEvent e) {
        Roster roster = new Roster();
        String[] list = Roster.getAllFileNames();
        for (int i = 0; i < list.length; i++) {
            // get next filename
            String fullFromFilename = list[i];

            // read it
            LocoFile lf = new LocoFile();  // used as a temporary
            Element lroot = null;
            try {
                lroot = lf.rootFromName(LocoFile.getFileLocation() + fullFromFilename);
            } catch (Exception ex) {
                log.error("Exception while loading loco XML file: " + fullFromFilename + " exception: " + ex);
                continue;
            }

            // create a new entry from XML info - find the element
            Element loco = lroot.getChild("locomotive");
            if (loco != null) {
                RosterEntry toEntry = new RosterEntry(loco);
                toEntry.setFileName(fullFromFilename);

                // add to roster
                roster.addEntry(toEntry);
                //See if the entry is assigned to any roster groups or not this will add the group if missing.
                toEntry.getGroups();
            }
        }

        // write updated roster
        Roster.instance().makeBackupFile(Roster.defaultRosterFilename());
        try {
            roster.writeFile(Roster.defaultRosterFilename());
        } catch (Exception ex) {
            log.error("Exception while writing the new roster file, may not be complete: " + ex);
        }
        // use the new one
        Roster.resetInstance();
        Roster.instance();
        log.info("Roster rebuilt, stored in " + Roster.defaultRosterFilename());

    }

    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static private Logger log = LoggerFactory.getLogger(RecreateRosterAction.class.getName());
}
