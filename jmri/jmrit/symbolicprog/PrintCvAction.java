// PrintCvAction.java

package jmri.jmrit.symbolicprog;

import jmri.util.davidflanagan.*;
import jmri.jmrit.symbolicprog.tabbedframe.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Action to print the information in the CV table.
 * <P>
 * This uses the older style printing, for compatibility with Java 1.1.8 in
 * Macintosh MRJ
 *
 * @author			Bob Jacobsen   Copyright (C) 2003
 * @version             $Revision: 1.2 $
 */
public class PrintCvAction  extends AbstractAction {

    public PrintCvAction(String actionName, CvTableModel pModel, PaneProgFrame pParent) {
        super(actionName);
        mModel = pModel;
        mFrame = pParent;
    }

    /**
     * Frame hosting the printing
     */
    PaneProgFrame mFrame;
    CvTableModel mModel;

    public void actionPerformed(ActionEvent e) {

        // obtain a HardcopyWriter to do this
        HardcopyWriter writer = null;
        try {
            writer = new HardcopyWriter(mFrame, mFrame.getRosterEntry().getId(), 10, .5, .5, .5, .5);

            // print the decoder info section, etc
            mFrame.printInfoSection(writer);

            // print a simple heading
            String s = "\nCV\t value\n";
            writer.write(s, 0, s.length());
            s = "\n";
            writer.write(s, 0, s.length());

            // print each CV value
            for (int i=0; i<mModel.getRowCount(); i++) {
                CvValue cv = mModel.getCvByRow(i);
                int num = cv.number();
                int value = cv.getValue();
                s = ""+num+"\t "+value+"\n";
                writer.write(s, 0, s.length());
            }
        }
        catch (java.io.IOException ex1) {
            log.error("IO exception while printing");
            return;
        }
        catch (HardcopyWriter.PrintCanceledException ex2) {
            log.debug("Print cancelled");
            return;
        }

        writer.close();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PrintCvAction.class.getName());
}
