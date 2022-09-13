package jmri.jmrit.symbolicprog;

import javax.swing.JFrame;

/**
 * Action to export modified CV values to a Comma Separated Variable (CSV) data file.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2022
 */
public class CsvExportModifiedAction extends CsvExportAction {

    public CsvExportModifiedAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName, pModel, pParent);
        mModel = pModel;
        mParent = pParent;
    }

    @Override
    protected boolean isWritable(CvValue cv) {
        return cv.getState() == AbstractValue.EDITED;
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CsvExportModifiedAction.class);
}
