package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;

import java.util.Date;
import jmri.util.FileUtil;

class OpSessionLog {
	
	BufferedWriter _outBuff;
	
    private static OpSessionLog _instance;
    
    private OpSessionLog() {   	
    }
    
    public static OpSessionLog getInstance() {
    	if (_instance==null) {
    		_instance = new OpSessionLog();
    	}
    	return _instance;
    }
    
    public synchronized boolean showFileChooser(java.awt.Component parent) {
    	JFileChooser fileChooser = new JFileChooser(FileUtil.getUserFilesPath());
    	fileChooser.setDialogTitle(Bundle.getMessage("logSession"));
    	fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt", "TXT"));
    	int retVal = fileChooser.showDialog(parent, Bundle.getMessage("logFile"));
        if (retVal != JFileChooser.APPROVE_OPTION) {
        	return false;
        }
        
        File file = fileChooser.getSelectedFile();
        String fileName = file.getAbsolutePath();
        String fileNameLC = fileName.toLowerCase();
        if (!fileNameLC.endsWith(".txt")){
        	fileName = fileName+".txt";
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
    
    void writeHeader(String fileName) {
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
            log.error("Op session log error "+ioe.getMessage());        	
    	}
    }
    
    synchronized public void writeLn(String text) {
    	try {
        	SimpleDateFormat dateFormatter = new SimpleDateFormat("  hh:mm:ss a   ");
        	_outBuff.append(dateFormatter.format(new Date()));
        	_outBuff.append(text);
        	_outBuff.newLine();
    	} catch (IOException ioe) {
            log.error("Op session log error "+ioe.getMessage());        	
    	}
    }
    
    synchronized public void close() {
    	try {
        	writeLn(Bundle.getMessage("stopLog"));
        	_outBuff.flush();
        	_outBuff.close();
    	} catch (IOException ioe) {
            log.error("Op session log error "+ioe.getMessage());        	
    	}
    }
	
    static Logger log = LoggerFactory.getLogger(OpSessionLog.class.getName());
}