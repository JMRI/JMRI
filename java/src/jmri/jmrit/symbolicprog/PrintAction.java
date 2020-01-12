package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;

/**
 * Action to print the information in a VariableTable.
 * <p>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 */
public class PrintAction extends AbstractAction {

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

    @Override
    public void actionPerformed(ActionEvent e) {
        mFrame.printPanes(isPreview);
    }
}
