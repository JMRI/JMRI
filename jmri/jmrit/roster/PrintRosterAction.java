// PrintRosterAction.java

package jmri.jmrit.roster;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import jmri.util.davidflanagan.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.sun.java.util.collections.List;


/**
 * Action to print a summary of the Roster contents
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class PrintRosterAction  extends AbstractAction {

    public PrintRosterAction(String actionName, Frame frame) {
        super(actionName);
        mFrame = frame;
    }

    /**
     * Frame hosting the printing
     */
    Frame mFrame;

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, "DecoderPro Roster", 10, .5, .5, .5, .5);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // add the image
        ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("resources/decoderpro.gif"));
        // we use an ImageIcon because it's guaranteed to have been loaded when ctor is complete
        writer.write(icon.getImage(), new JLabel(icon));

        // Loop through the Roster, printing as needed
        Roster r = Roster.instance();
        List l = r.matchingList(null, null, null, null, null, null, null); // take all
        int i=-1;
        log.debug("Roster list size: "+l.size());
        for (i = 0; i<l.size(); i++) {
            ((RosterEntry)l.get(i)).printEntry(writer);
        }

        // and force completion of the printing
        writer.close();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintRosterAction.class.getName());
}
