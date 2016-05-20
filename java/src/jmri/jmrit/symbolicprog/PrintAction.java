// PrintAction.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Action to print the information in a VariableTable.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author		Bob Jacobsen   Copyright (C) 2003
 * @author      Dennis Miller  Copyright (C) 2005
 * @version             $Revision$
 */
public class PrintAction  extends AbstractAction {

    public PrintAction(String actionName, PaneProgFrame frame, boolean preview) {
        super(actionName);
        mFrame = frame;
        isPreview = preview;
    }

    /**
     * Frame hosting the printing
     */
    PaneProgFrame mFrame;
    /**
     * Variable to set whether this is to be printed or previewed
     */
    boolean isPreview;

    public void actionPerformed(ActionEvent e) {
        mFrame.printPanes(isPreview);
    }

    static Logger log = LoggerFactory.getLogger(PrintAction.class.getName());
}
