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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the CV values to a Comma Separated Variable (CSV) data file.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class CsvExportAction extends AbstractAction {

    public CsvExportAction(String actionName, CvTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;
    }

    JFileChooser fileChooser;
    JFrame mParent;

    /**
     * CvTableModel to load
     */
    CvTableModel mModel;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new JFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to CSV file {}", file);
            }

            try (CSVPrinter str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
                str.printRecord("CV, value");
                for (int i = 0; i < mModel.getRowCount(); i++) {
                    CvValue cv = mModel.getCvByRow(i);
                    if (isWritable(cv)) {
                        String num = cv.number();
                        int value = cv.getValue();
                        str.printRecord(num, value);
                    }
                }
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Decide whether a given CV should be written out.
     * @param cv CV to be checked
     * @return true if CV should be included in output file.
     */
    protected boolean isWritable(CvValue cv) {
        return true;
    }


    private final static Logger log = LoggerFactory.getLogger(CsvExportAction.class);
}
