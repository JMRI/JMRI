// PrintAction.java
package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to print the information in a VariableTable.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @author Dennis Miller Copyright (C) 2005
 * @version $Revision$
 */
public class PrintAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -633211100496026121L;

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

    private final static Logger log = LoggerFactory.getLogger(PrintAction.class.getName());
}
