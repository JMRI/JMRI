// ImportRollingStock.java
package jmri.jmrit.operations.rollingstock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.jmrit.operations.setup.Control;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides common routes for importing cars and locomotives
 *
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 24463 $
 */
public class ImportRollingStock extends Thread {

    protected static final String NEW_LINE = "\n"; // NOI18N

    protected JLabel lineNumber = new JLabel();
    protected JLabel importLine = new JLabel();

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
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // NOI18N
        } catch (FileNotFoundException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            log.error("UTF-8 encoding not supported");
            return null;
        }
        return in;
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
        if (line.contains("\"")) { // NOI18N
            // log.debug("line number "+lineNum+" has escape char \"");
            String[] parseLine = line.split(",");
            int j = 0;
            for (int i = 0; i < parseLine.length; i++) {
                if (parseLine[i].contains("\"")) { // NOI18N
                    StringBuilder sb = new StringBuilder(parseLine[i++]);
                    sb.deleteCharAt(0); // delete the "
                    outLine[j] = sb.toString();
                    while (i < parseLine.length) {
                        if (parseLine[i].contains("\"")) { // NOI18N
                            sb = new StringBuilder(parseLine[i]);
                            sb.deleteCharAt(sb.length() - 1); // delete the "
                            outLine[j] = outLine[j] + "," + sb.toString();
                            // log.debug("generated string: "+outLine[j]);
                            j++;
                            break; // done!
                        } else {
                            outLine[j] = outLine[j] + "," + parseLine[i++];
                        }
                    }

                } else {
                    // log.debug("outLine: "+parseLine[i]);
                    outLine[j++] = parseLine[i];
                }
                if (j > arraySize - 1) {
                    break;
                }
            }
        } else {
            outLine = line.split(",");
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
            if (name.matches(".*\\.txt")) // NOI18N
            {
                return true;
            }
            if (name.matches(".*\\.csv")) // NOI18N
            {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getDescription() {
            return Bundle.getMessage("Text&CSV");
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ImportRollingStock.class.getName());
}
