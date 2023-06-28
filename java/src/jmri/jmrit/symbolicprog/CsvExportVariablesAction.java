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
 * @author Bob Jacobsen Copyright (C) 2023
 */
public class CsvExportVariablesAction extends AbstractAction {

    public CsvExportVariablesAction(String actionName, VariableTableModel pModel, JFrame pParent) {
        super(actionName);
        mModel = pModel;
        mParent = pParent;
    }

    JFileChooser fileChooser;
    JFrame mParent;

    /**
     * VariableTableModel to load
     */
    VariableTableModel mModel;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(mParent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to CSV file {}", file);
            }

            try (CSVPrinter str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
                str.printRecord("Variable", "value");
                for (int i = 0; i < mModel.getRowCount(); i++) {
                    VariableValue var = mModel.getVariable(i);
                    if (isWritable(var)) {
                        var name = var.label();
                        var value = var.getValueString();
                        str.printRecord(name, value);
                    }
                }
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    /**
     * Decide whether a given Variable should be written out.
     * @param var Variable to be checked
     * @return true if Variable should be included in output file.
     */
    protected boolean isWritable(VariableValue var) {
        return true;
    }


    private final static Logger log = LoggerFactory.getLogger(CsvExportVariablesAction.class);
}
