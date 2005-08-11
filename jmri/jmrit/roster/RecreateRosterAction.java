// RecreateRosterAction.java

package jmri.jmrit.roster;

import jmri.jmrit.XmlFile;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import org.jdom.Element;

/**
 * Recreate the roster index file if it's been damaged or lost.
 * <P>
 * Scans the roster directory for xml files, including any that are found.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.10 $
 */
public class RecreateRosterAction extends AbstractAction {

    public RecreateRosterAction(String s) {
        super(s, null);
    }

    public void actionPerformed(ActionEvent e) {
        Roster roster = new Roster();
        String[] list = getFileNames();
        for (int i=0; i<list.length; i++) {
            // get next filename
            String fullFromFilename = list[i];

            // read it
            LocoFile lf = new LocoFile();  // used as a temporary
            Element lroot = null;
            try {
                lroot = lf.rootFromName(LocoFile.getFileLocation()+fullFromFilename);
            } catch (Exception ex) {
                log.error("Exception while loading loco XML file: "+fullFromFilename+" exception: "+ex);
                continue;
            }

            // create a new entry from XML info - find the element
            Element loco = lroot.getChild("locomotive");
            if (loco != null) {
                RosterEntry toEntry = new RosterEntry(loco);
                toEntry.setFileName(fullFromFilename);

                // add to roster
                roster.addEntry(toEntry);
            }
        }

        // write updated roster
        Roster.instance().makeBackupFile(Roster.defaultRosterFilename());
        try {
            roster.writeFile(Roster.defaultRosterFilename());
        } catch (Exception ex) {
            log.error("Exception while writing the new roster file, may not be complete: "+ex);
        }
        // use the new one
        Roster.resetInstance();
        Roster.instance();

    }

    String[] getFileNames() {
        // ensure preferences will be found for read
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(LocoFile.getFileLocation());

        // create an array of file names from roster dir in preferences, count entries
        int i;
        int np = 0;
        String[] sp = null;
        XmlFile.ensurePrefsPresent(LocoFile.getFileLocation());
        if (log.isDebugEnabled()) log.debug("search directory "+LocoFile.getFileLocation());
        File fp = new File(LocoFile.getFileLocation());
        if (fp.exists()) {
            sp = fp.list();
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                    np++;
                }
            }
        } else {
            log.warn(XmlFile.prefsDir()+"roster directory was missing, though tried to create it");
        }

        // Copy the entries to the final array
        String sbox[] = new String[np];
        int n=0;
        if (sp != null && np> 0)
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML")) {
                    sbox[n++] = sp[i];
                }
            }
        // The resulting array is now sorted on file-name to make it easier
        // for humans to read
        jmri.util.StringUtil.sort(sbox);

        if (log.isDebugEnabled()) {
            log.debug("filename list:");
            for (i=0; i<sbox.length; i++)
                log.debug("      "+sbox[i]);
        }
        return sbox;
    }

    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RecreateRosterAction.class.getName());
}
