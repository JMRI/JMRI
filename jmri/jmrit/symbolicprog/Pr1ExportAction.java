// Pr1ExportAction.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * Action to import the CV values from a PR1WIN/PR1DOS data file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class Pr1ExportAction  extends AbstractAction {

    public Pr1ExportAction(String actionName, CvTableModel pModel) {
        super(actionName);
        mModel = pModel;
    }

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    public void actionPerformed(ActionEvent e) {

        log.debug("start to export PR1 file");

        // Sample of how to access the existing CV values:
        for (int i=0; i<mModel.getRowCount(); i++) {
            CvValue cv = mModel.getCvByRow(i);
            int num = cv.number();
            int value = cv.getValue();
            log.debug("CV"+num+" = "+value);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1ExportAction.class.getName());
}
