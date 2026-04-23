package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action to export the Function labels to a Comma Separated Variable (CSV) data file.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2026
 */
public class CsvFunctionExportAction extends AbstractAction {

    public CsvFunctionExportAction(String actionName, PaneProgFrame parent) {
        super(actionName);
        this.parent = parent;
    }

    JFileChooser fileChooser;
    PaneProgFrame parent;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (fileChooser == null) {
            fileChooser = new jmri.util.swing.JmriJFileChooser();
        }

        int retVal = fileChooser.showSaveDialog(parent);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (log.isDebugEnabled()) {
                log.debug("start to export to CSV file {}", file);
            }

            try (CSVPrinter str = new CSVPrinter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8), CSVFormat.DEFAULT)) {
                str.printRecord("Number", "Label");
                var labels = parent.getFnLabelPane().getLabels();
                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    str.printRecord(i, label);
                }
                str.flush();
            } catch (IOException ex) {
                log.error("Error writing file", ex);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CsvFunctionExportAction.class);
}
