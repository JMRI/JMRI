// PrintAction.java

package jmri.jmrit.symbolicprog;

import jmri.util.davidflanagan.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Action to print the information in a VariableTable and CV table.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version             $Revision: 1.2 $
 */
public class PrintAction  extends AbstractAction {

    public PrintAction(String actionName, PaneProgFrame frame) {
        super(actionName);
        mFrame = frame;
    }

    /**
     * Frame hosting the printing
     */
    PaneProgFrame mFrame;

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, mFrame.getRosterEntry().getId(), 10, .5, .5, .5, .5);
        } catch (HardcopyWriter.PrintCanceledException ex) {
            log.debug("Print cancelled");
            return;
        }

        // ask the frame to print it's content panes
        mFrame.printPanes(writer);

        writer.close();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintAction.class.getName());
}
