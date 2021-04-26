package jmri.jmrix.can.cbus.swing.console;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.*;
import jmri.util.FileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pane for CBUS Console Logging to File Options
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Steve Young Copyright (C) 2018
 */
public class CbusConsoleLoggingPane extends javax.swing.JPanel {
    
    private final CbusConsolePane _mainPane;
    private final JFileChooser logFileChooser;
    private final JToggleButton startStopLogButton;
    private final JButton openLogFileButton;
    private final JButton openFileChooserButton;
    private final JTextField entryField;
    private final JButton logenterButton;
    
    // @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "separately interlocked")
    private PrintStream logStream;
    
    public CbusConsoleLoggingPane(CbusConsolePane mainPane){
        super();
        _mainPane = mainPane;
        // set file chooser to a default
        logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());
        logFileChooser.setSelectedFile(new File(FileUtil.getUserFilesPath()+"monitorLog.txt"));
        
        startStopLogButton = new JToggleButton();
        openLogFileButton = new JButton();
        openFileChooserButton = new JButton();
        entryField = new JTextField();
        logenterButton = new JButton();
        setupPane();

    }
    
    private void setupPane() {
    
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("Logging"))); 
        
        setButtonToolTips();
        
        add(startStopLogButton);
        add(openFileChooserButton);
        add(openLogFileButton);
        add(logenterButton);
        add(entryField);
        
        addListeners();
    
    }
    
    private void setSelectFileToolTip() {
    
        openFileChooserButton.setToolTipText(logFileChooser.getSelectedFile().getPath());
    
        
    }
    
    private void addListeners() {
    
        startStopLogButton.addActionListener((java.awt.event.ActionEvent e) -> {
        
            openFileChooserButton.setEnabled(!startStopLogButton.isSelected());
            
            if (startStopLogButton.isSelected()){
                startLogButtonActionPerformed(e);
            }
            else {
                stopLogButtonActionPerformed(e);
            }
            
            updateStartStopButtonText();
            
        });
        
        openFileChooserButton.addActionListener(this::openFileChooserButtonActionPerformed);
        
        openLogFileButton.addActionListener((java.awt.event.ActionEvent e) -> {
            try {
                openLogFileActionPerformed(e);
            } catch (IOException ex) {
                log.error("log file open exception {}", ex);
            }
        });
        
        logenterButton.addActionListener(this::textToLogButtonActionPerformed);
        entryField.addActionListener(this::textToLogButtonActionPerformed);
        
        updateStartStopButtonText();
    
    }
    
    private void setButtonToolTips() {
    
        logenterButton.setText(Bundle.getMessage("ButtonAddMessage"));
        logenterButton.setToolTipText(Bundle.getMessage("TooltipAddMessage"));
        
        openFileChooserButton.setText(Bundle.getMessage("ButtonChooseLogFile"));
        setSelectFileToolTip();
        
        openLogFileButton.setText(Bundle.getMessage("OpenLogFile"));
        openLogFileButton.setToolTipText(Bundle.getMessage("OpenLogFileTip"));
        
        entryField.setToolTipText(Bundle.getMessage("EntryAddtoLogTip"));
    
    }
    
    private void updateStartStopButtonText(){
    
        if (startStopLogButton.isSelected()){
            
            startStopLogButton.setText(Bundle.getMessage("ButtonStopLogging"));
            startStopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging"));
            startStopLogButton.setForeground(Color.red);
            
        }
        else {
        
            startStopLogButton.setText(Bundle.getMessage("ButtonStartLogging"));
            startStopLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging") + " " +
                Bundle.getMessage("ButtonStartLogTipExtra"));
            startStopLogButton.setForeground(new JToggleButton().getForeground());
            
        }
    
    }
    
    private void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if (logStream == null) {  // successive clicks don't restart the file
            // start logging
            try {
                logStream = new PrintStream(new FileOutputStream(logFileChooser.getSelectedFile()));
            } catch (FileNotFoundException ex) {
                log.error("exception {}", ex);
            }
        }
    }

    private void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream != null) {
            logStream.flush();
            logStream.close();
        }
        logStream = null;
    }

    private void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = logFileChooser.showSaveDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            boolean loggingNow = (logStream != null);
            stopLogButtonActionPerformed(e);  // stop before changing file
            //File file = logFileChooser.getSelectedFile();
            // if we were currently logging, start the new file
            if (loggingNow) {
                startLogButtonActionPerformed(e);
            }
        }
        setSelectFileToolTip();
    }
    
    private void openLogFileActionPerformed(java.awt.event.ActionEvent e) throws IOException {
        // start at current file, show dialog
        Desktop desktop = Desktop.getDesktop();
        File dirToOpen;
        
        try {
            dirToOpen = logFileChooser.getSelectedFile();
            desktop.open(dirToOpen);
        } catch (IllegalArgumentException iae) {
            // log.info("Merg Cbus Console Log File Not Found");
            JOptionPane.showMessageDialog(_mainPane, 
                (Bundle.getMessage("NoOpenLogFile")), Bundle.getMessage("WarningTitle"),
                JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    private void textToLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        _mainPane.nextLine(entryField.getText() + "\n", entryField.getText() + "\n", -1);
    }
    
    private final String newline = System.getProperty("line.separator");
    
    protected void sendLogToFile( String sbCbus ){
        if (logStream != null) {
            String logLine = sbCbus;
            if (!newline.equals("\n")) {
                // have to massage the line-ends
                StringBuilder out = new StringBuilder(sbCbus.length() + 10);  // arbitrary guess at space
                for (int j = 0; j < sbCbus.length(); j++) {
                    if (sbCbus.charAt(j) == '\n') {
                        out.append(newline);
                    } else {
                        out.append(sbCbus.charAt(j));
                    }
                }
                logLine = new String(out);
            }
            logStream.print(logLine);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CbusConsoleLoggingPane.class);
}
