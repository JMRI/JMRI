// PrintRosterAction.java

package jmri.jmrit.roster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.davidflanagan.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import java.io.IOException;

import java.util.List;
import jmri.beans.Beans;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @author  Dennis Miller  Copyright (C) 2005
 * @version     $Revision$
 */
public class PrintRosterAction  extends jmri.util.swing.JmriAbstractAction {

    public PrintRosterAction(String s, jmri.util.swing.WindowInterface wi) {
    	super(s, wi);
        isPreview = true;
    }
     
 	public PrintRosterAction(String s, javax.swing.Icon i, jmri.util.swing.WindowInterface wi) {
    	super(s, i, wi);
        isPreview = true;
    }
    
    public PrintRosterAction(String actionName, Frame frame, boolean preview) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }
    
    public void setPreview(boolean preview){
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame = new Frame();
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;
    

    public void actionPerformed(ActionEvent e) {
        // obtain a HardcopyWriter to do this
        Roster r = Roster.instance();
        String title = "DecoderPro Roster";
        String rosterGroup = r.getDefaultRosterGroup();
        // rosterGroup may legitimately be null
        // but getProperty returns null if the property cannot be found, so
        // we test that the property exists before attempting to get its value
        if (Beans.hasProperty(wi, "selectedRosterGroup")) {
            rosterGroup = (String) Beans.getProperty(wi, "selectedRosterGroup");
        }
        if(rosterGroup==null){
            title = title + " All Entries";
        } else {
            title = title + " Group " + rosterGroup + " Entires";
        }
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, title, 10, .5, .5, .5, .5, isPreview);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));
        //Add a number of blank lines, so that the roster entry starts below the decoderpro logo
        int height = icon.getImage().getHeight(null);
        int blanks = (height-writer.getLineAscent())/writer.getLineHeight();
        
        try{
            for(int i = 0; i<blanks; i++){
                String s = "\n";
                writer.write(s,0,s.length());
            }
        } catch (IOException ex) { log.warn("error during printing: "+ex);
        }

        // Loop through the Roster, printing as needed
        List<RosterEntry> l = r.matchingList(null, null, null, null, null, null, null); // take all
        log.debug("Roster list size: "+l.size());
        for (RosterEntry re : l) {
            if (rosterGroup != null) {
                if (re.getAttribute(Roster.getRosterGroupProperty(rosterGroup)) != null
                        && re.getAttribute(Roster.getRosterGroupProperty(rosterGroup)).equals("yes")) {
                    re.printEntry(writer);
                }
            } else {
                re.printEntry(writer);
            }
        }

        // and force completion of the printing
        writer.close();
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
    
    public void setParameter(String parameter, String value){
        parameter = parameter.toLowerCase();
        value = value.toLowerCase();
        if(parameter.equals("ispreview")){
            if(value.equals("true"))
                isPreview=true;
            else
                isPreview=false;
        }
    }

    static Logger log = LoggerFactory.getLogger(PrintRosterAction.class.getName());
}
