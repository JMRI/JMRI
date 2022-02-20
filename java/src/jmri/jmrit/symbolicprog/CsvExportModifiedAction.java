package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

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
