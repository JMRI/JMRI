// RecreateRosterAction.java

package jmri.jmrit.roster;

import jmri.jmrit.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.jdom.*;

/**
 * Recreate the roster index file if it's been damaged or lost
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision: 1.3 $
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
                lroot = lf.rootFromName(LocoFile.fileLocation+fullFromFilename);
            } catch (Exception ex) {
                log.error("Exception while loading loco XML file: "+fullFromFilename+" exception: "+ex);
                continue;
            }

            // create a new entry from XML info - find the element
            Element loco = lroot.getChild("locomotive");
            RosterEntry toEntry = new RosterEntry(loco);
            toEntry.setFileName(fullFromFilename);

            // add to roster
            roster.addEntry(toEntry);

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
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);

        // create an array of file names from roster dir in preferences, count entries
        int i;
        int np = 0;
        String[] sp = null;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir()+LocoFile.fileLocation);
        File fp = new File(XmlFile.prefsDir()+LocoFile.fileLocation);
        if (fp.exists()) {
            sp = fp.list();
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML"))
                    np++;
            }
        } else {
            log.warn(XmlFile.prefsDir()+"roster directory was missing, though tried to create it");
        }
        // create an array of file names from xml/roster, count entries
        String[] sx = (new File(XmlFile.xmlDir()+LocoFile.fileLocation)).list();
        if (sx == null) sx = new String[0];

        int nx = 0;
        for (i=0; i<sx.length; i++) {
            if (sx[i].endsWith(".xml") || sx[i].endsWith(".XML")) {
                nx++;
            }
        }
        // copy the entries to the final array
        // note: this results in duplicate entries if the same name is also local.
        // But for now I can live with that.
        String sbox[] = new String[np+nx];
        int n=0;
        if (sp != null && np> 0)
            for (i=0; i<sp.length; i++) {
                if (sp[i].endsWith(".xml") || sp[i].endsWith(".XML"))
                    sbox[n++] = sp[i];
            }
        for (i=0; i<sx.length; i++) {
            if (sx[i].endsWith(".xml") || sx[i].endsWith(".XML"))
                sbox[n++] = sx[i];
        }
        //the resulting array is now sorted on file-name to make it easier
        // for humans to read
        java.util.Arrays.sort(sbox);

        return sbox;
    }

    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RecreateRosterAction.class.getName());
}
