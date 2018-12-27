package jmri.jmrit.logix;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.util.FileUtil;
import jmri.util.swing.TextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OpSessionLog {

    static BufferedWriter _outBuff;

    private OpSessionLog() {
    }

    public static synchronized boolean makeLogFile(java.awt.Component parent) {

        JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());
        fileChooser.setDialogTitle(Bundle.getMessage("logSession"));
        fileChooser.setFileFilter(new TextFilter());
        int retVal = fileChooser.showDialog(parent, Bundle.getMessage("logFile"));
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File file = fileChooser.getSelectedFile();
        String fileName = file.getAbsolutePath();
        String fileNameLC = fileName.toLowerCase();
        if (!fileNameLC.endsWith(".txt")) {
            fileName = fileName + ".txt";
            file = new File(fileName);
        }
        // check for possible overwrite
        if (file.exists()) {
            if (JOptionPane.showConfirmDialog(parent,
                    Bundle.getMessage("overWritefile", fileName), Bundle.getMessage("QuestionTitle"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
                return false;
            }
        }

        try {
            _outBuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writeHeader(fileName);
        } catch (FileNotFoundException fnfe) {
            JOptionPane.showMessageDialog(parent, fnfe.getMessage(),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    static private void writeHeader(String fileName) {
        if (_outBuff==null) {
            return;
        }
        try {
            _outBuff.newLine();
            _outBuff.append("\t\t\t");
            _outBuff.append(fileName);
            _outBuff.newLine();
            _outBuff.append("\t\t\t");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy");
            _outBuff.append(dateFormatter.format(new Date()));
            _outBuff.newLine();
            _outBuff.newLine();
            writeLn(Bundle.getMessage("startLog"));
        } catch (IOException ioe) {
            log.error("Op session log error " + ioe.getMessage());
        }
    }

    static synchronized public void writeLn(String text) {
        if (_outBuff==null) {
            return;
        }
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("  hh:mm:ss a   ");
            _outBuff.append(dateFormatter.format(new Date()));
            _outBuff.append(text);
            _outBuff.newLine();
        } catch (IOException ioe) {
            log.error("Op session log error " + ioe.getMessage());
        }
    }

    static synchronized public void flush() {
        if (_outBuff==null) {
            return;
        }
        try {
            _outBuff.flush();
        } catch (IOException ioe) {
            log.error("Op session log error " + ioe.getMessage());
        }
    }

    static synchronized public void close() {
        if (_outBuff==null) {
            return;
        }
        try {
            writeLn(Bundle.getMessage("stopLog"));
            _outBuff.flush();
            _outBuff.close();
        } catch (IOException ioe) {
            log.error("Op session log error " + ioe.getMessage());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OpSessionLog.class);
}
