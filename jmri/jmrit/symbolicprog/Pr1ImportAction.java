// Pr1ImportAction.java

package jmri.jmrit.symbolicprog;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to import the CV values from a PR1WIN/PR1DOS data file.
 *
 * @author	Bob Jacobsen   Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class Pr1ImportAction  extends AbstractAction {

    public Pr1ImportAction(String actionName, CvTableModel pModel) {
        super(actionName);
        mModel = pModel;
    }

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    public void actionPerformed(ActionEvent e) {

        log.debug("start to import PR1 file");

        // Sample of what needs to happen in the import. In this case,
        // set CV29 to 21.
        int num = 29;
        int value = 21;
        mModel.getCvByNumber(num).setValue(value);

        // If mModel.getCvByNumber() returns null, the CV doesn't exist
        // yet.  In that case, you have to add a new CV object:

        num = 123;
        mModel.addCV(""+123);
        mModel.getCvByNumber(num).setValue(value);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(Pr1ImportAction.class.getName());
}
