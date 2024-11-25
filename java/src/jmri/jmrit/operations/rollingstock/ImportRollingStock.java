package jmri.jmrit.operations.rollingstock;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.setup.Control;

/**
 * Provides common routes for importing cars and locomotives
 *
 * @author Dan Boudreau Copyright (C) 2013
 *
 */
public abstract class ImportRollingStock extends Thread {

    protected static final String NEW_LINE = "\n"; // NOI18N

    protected JLabel lineNumber = new JLabel();
    protected JLabel importLine = new JLabel();

    protected static final String LOCATION_TRACK_SEPARATOR = "-";

    protected jmri.util.JmriJFrame fstatus;

    // Get file to read from
    protected File getFile() {
        JFileChooser fc = new jmri.util.swing.JmriJFileChooser(jmri.jmrit.operations.OperationsXml.getFileLocation());
        fc.setFileFilter(new FileNameExtensionFilter(Bundle.getMessage("Text&CSV"), "txt", "csv")); // NOI18N
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null; // canceled
        }
        log.info("Importing from file: {}", fc.getSelectedFile());
        return fc.getSelectedFile();
    }

    protected BufferedReader getBufferedReader(File file) {
        try {
            return new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    // create a status frame showing line number and imported text
    protected void createStatusFrame(String title) {
        JPanel ps = new JPanel();
        ps.setLayout(new BoxLayout(ps, BoxLayout.Y_AXIS));
        fstatus = new jmri.util.JmriJFrame(title);
        fstatus.setLocation(10, 10);
        fstatus.setSize(Control.panelWidth1025, 100);

        ps.add(lineNumber);
        ps.add(importLine);

        fstatus.getContentPane().add(ps);
        fstatus.setVisible(true);
    }

    /*
     * Needs to handle empty lines
     */
    protected String[] parseCommaLine(String line) {
        String[] outLine = new String[0];
        try {
            CSVRecord record = CSVParser.parse(line, CSVFormat.DEFAULT).getRecords().get(0);
            outLine = new String[record.size()];
            // load output array to prevent NPE
            for (int i = 0; i < outLine.length; i++) {
                outLine[i] = record.get(i);
            }
        } catch (IndexOutOfBoundsException e) {
            // do nothing blank line
        } catch (IOException ex) {
            log.error("Error parsing CSV: {}, {}", line, ex.getLocalizedMessage());
            Arrays.fill(outLine, ""); // NOI18N
        }
        return outLine;
    }

    private final static Logger log = LoggerFactory.getLogger(ImportRollingStock.class);
}
