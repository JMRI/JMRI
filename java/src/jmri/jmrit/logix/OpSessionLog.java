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

import jmri.util.FileUtil;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.TextFilter;

class OpSessionLog {

    private static final String OP_SESSION_LOG_ERROR = "Op session log error {}";
    static BufferedWriter _outBuff;

    private OpSessionLog() {
    }

    public static synchronized boolean makeLogFile(java.awt.Component parent) {

        JFileChooser fileChooser = new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());
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
            if (JmriJOptionPane.showConfirmDialog(parent,
                    Bundle.getMessage("overWritefile", fileName), Bundle.getMessage("QuestionTitle"),
                    JmriJOptionPane.OK_CANCEL_OPTION, JmriJOptionPane.QUESTION_MESSAGE) != JmriJOptionPane.OK_OPTION) {
                return false;
            }
        }

        try {
            _outBuff = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writeHeader(fileName);
        } catch (FileNotFoundException fnfe) {
            JmriJOptionPane.showMessageDialog(parent, fnfe.getMessage(),
                    Bundle.getMessage("WarningTitle"), JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private static void writeHeader(String fileName) {
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
            log.error(OP_SESSION_LOG_ERROR,ioe.getMessage());
        }
    }

    public static synchronized void writeLn(String text) {
        if (_outBuff==null) {
            return;
        }
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("  hh:mm:ss a   ");
            _outBuff.append(dateFormatter.format(new Date()));
            _outBuff.append(text);
            _outBuff.newLine();
        } catch (IOException ioe) {
            log.error(OP_SESSION_LOG_ERROR,ioe.getMessage());
        }
    }

    public static synchronized void flush() {
        if (_outBuff==null) {
            return;
        }
        try {
            _outBuff.flush();
        } catch (IOException ioe) {
            log.error(OP_SESSION_LOG_ERROR,ioe.getMessage());
        }
    }

    public static synchronized void close() {
        if (_outBuff==null) {
            return;
        }
        try {
            writeLn(Bundle.getMessage("stopLog"));
            _outBuff.flush();
            _outBuff.close();
        } catch (IOException ioe) {
            log.error(OP_SESSION_LOG_ERROR,ioe.getMessage());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OpSessionLog.class);

}
