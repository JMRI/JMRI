package jmri.jmrit.operations.rollingstock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.operations.setup.Control;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        JFileChooser fc = new JFileChooser(jmri.jmrit.operations.OperationsXml.getFileLocation());
        fc.addChoosableFileFilter(new ImportFilter());
        int retVal = fc.showOpenDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null; // canceled
        }
        if (fc.getSelectedFile() == null) {
            return null; // canceled
        }
        File file = fc.getSelectedFile();
        return file;
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
        fstatus.setSize(Control.panelWidth700, 100);

        ps.add(lineNumber);
        ps.add(importLine);

        fstatus.getContentPane().add(ps);
        fstatus.setVisible(true);
    }

    protected String[] parseCommaLine(String line, int arraySize) {
        String[] outLine = new String[arraySize];
        try {
            CSVRecord record = CSVParser.parse(line, CSVFormat.DEFAULT).getRecords().get(0);
            // load output array to prevent NPE
            for (int i = 0; i < outLine.length; i++) {
                outLine[i] = record.get(i);
            }
        } catch (IOException ex) {
            log.error("Error parsing CSV: {}", line, ex);
            Arrays.fill(outLine, ""); // NOI18N
        }
        return outLine;
    }

    public static class ImportFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            return (name.matches(".*\\.txt") || name.matches(".*\\.csv")); // NOI18N
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("Text&CSV");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImportRollingStock.class);
}
