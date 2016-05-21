// RecreateRosterAction.java

package jmri.jmrit.roster;

import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import javax.swing.Icon;

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
        String[] list = Roster.getAllFileNames();
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
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // initialize logging
    static private Logger log = LoggerFactory.getLogger(RecreateRosterAction.class.getName());
}
