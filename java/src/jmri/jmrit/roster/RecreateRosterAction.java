// RecreateRosterAction.java

package jmri.jmrit.roster;

import java.awt.event.ActionEvent;
import java.io.File;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import jmri.util.FileUtil;

import org.jdom.Element;

/**
 * Recreate the roster index file if it's been damaged or lost.
 * <P>
 * Scans the roster directory for xml files, including any that are found.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001
 * @version	$Revision$
 */
public class RecreateRosterAction extends JmriAbstractAction {

    public RecreateRosterAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public RecreateRosterAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }

    public RecreateRosterAction(String s) {
        super(s);
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
                //See if the entry is assigned to any roster groups or not this will add the group if missing.
                String[] attributes = toEntry.getAttributeList();
                if (attributes!=null){
                    for(int x=0; x<attributes.length; x++){
                        if(attributes[x].startsWith(roster.getRosterGroupPrefix())){
                            //We don't bother checking to see if the group already exists as this is done by the addRosterGroupList.
                            roster.addRosterGroupList(attributes[x].substring(roster.getRosterGroupPrefix().length()));
                        }
                    }
                }
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
        FileUtil.createDirectory(LocoFile.getFileLocation());

        // create an array of file names from roster dir in preferences, count entries
        int i;
        int np = 0;
        String[] sp = null;
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
            log.warn(FileUtil.getUserFilesPath()+"roster directory was missing, though tried to create it");
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
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RecreateRosterAction.class.getName());
}
