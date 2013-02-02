// InputWindow.java

package jmri.jmrit.jython;

import org.apache.log4j.Logger;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import jmri.util.PythonInterp;
import javax.swing.event.*;
import jmri.util.FileUtil;

/**
 * This Action runs creates a JFrame for sending input to the
 * global jython interpreter
 *
 * @author      Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision$
 */
public class InputWindow extends JPanel {

    JTextArea area;
    JButton button;
    JButton loadButton;
    JButton storeButton;
    JLabel status;
    JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");

    public InputWindow() {
    
        //setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        
        area = new JTextArea(12, 50);

        // from: http://stackoverflow.com/questions/5139995/java-column-number-and-line-number-of-cursors-current-position
        area.addCaretListener(new CaretListener() {
            // Each time the caret is moved, it will trigger the listener and its method caretUpdate.
            // It will then pass the event to the update method including the source of the event (which is our textarea control)
            public void caretUpdate(CaretEvent e) {
                JTextArea editArea = (JTextArea)e.getSource();

                // Lets start with some default values for the line and column.
                int linenum = 1;
                int columnnum = 1;

                // We create a try catch to catch any exceptions. We will simply ignore such an error for our demonstration.
                try {
                    // First we find the position of the caret. This is the number of where the caret is in relation to the start of the JTextArea
                    // in the upper left corner. We use this position to find offset values (eg what line we are on for the given position as well as
                    // what position that line starts on.
                    int caretpos = editArea.getCaretPosition();
                    linenum = editArea.getLineOfOffset(caretpos);

                    // We subtract the offset of where our line starts from the overall caret position.
                    // So lets say that we are on line 5 and that line starts at caret position 100, if our caret position is currently 106
                    // we know that we must be on column 6 of line 5.
                    columnnum = caretpos - editArea.getLineStartOffset(linenum);

                    // We have to add one here because line numbers start at 0 for getLineOfOffset and we want it to start at 1 for display.
                    linenum += 1;
                }
                catch(Exception ex) { }

                // Once we know the position of the line and the column, pass it to a helper function for updating the status bar.
                updateStatus(linenum, columnnum);
            }
        });

        JScrollPane js = new JScrollPane(area);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(js, BorderLayout.CENTER);
        
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(loadButton = new JButton(rb.getString("ButtonLoad")));
        p.add(storeButton = new JButton(rb.getString("ButtonStore")));
        p.add(button = new JButton(rb.getString("ButtonExecute")));
        
        alwaysOnTopCheckBox.setText("Window always on Top");
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText("If checked, this window be always be displayed in front of any other window");
        p.add(alwaysOnTopCheckBox);

        status = new JLabel("         ");   // create some space for the counters
        p.add(status);
        updateStatus(1,0);

        add(p, BorderLayout.SOUTH);
        
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonPressed();
            }
        });

        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                loadButtonPressed();
            }
        });

        storeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                storeButtonPressed();
            }
        });
        
        alwaysOnTopCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (getTopLevelAncestor()!=null){
                    ((jmri.util.JmriJFrame)getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                }
            }
        });

        // set a monospaced font
        int size = area.getFont().getSize();
        area.setFont(new Font("Monospaced", Font.PLAIN, size));

    }
    

    // This helper function updates the status bar with the line number and column number.
    private void updateStatus(int linenumber, int columnnumber) {
        status.setText("    " + linenumber + ":" + columnnumber);
    }
    /**
     *
     * @param fileChooser
     * @return true if successful
     */
    protected boolean loadFile(JFileChooser fileChooser) {
        boolean results = false;
        java.io.File file = getFile(fileChooser);
        if (file != null) {
            try {
                StringBuilder fileData = new StringBuilder(1024);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                char[] buf = new char[1024];
                int numRead = 0;
                while ((numRead = reader.read(buf)) != -1) {
                    String readData = String.valueOf(buf, 0, numRead);
                    fileData.append(readData);
                    buf = new char[1024];
                }
                reader.close();

                area.setText(fileData.toString());

            } catch (Exception e) {
                log.error("Unhandled problem in loadFile: " + e);
            }
        } else {
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        }
        return results;
    }

        /**
     *
     * @param fileChooser
     * @return true if successful
     */
    protected boolean storeFile(JFileChooser fileChooser) {
        boolean results = false;
        java.io.File file = getFile(fileChooser);
        if (file != null) {
            try {
                // check for possible overwrite
                if (file.exists()) {
                    int selectedValue = JOptionPane.showConfirmDialog(null,
                            "File " + file.getName() + " already exists, overwrite it?",
                            "Overwrite file?",
                            JOptionPane.OK_CANCEL_OPTION);
                    if (selectedValue != JOptionPane.OK_OPTION) {
                        results = false; // user clicked no to override
                        return results;
                    }
                }

                StringBuilder fileData = new StringBuilder(area.getText());
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.append(fileData);
                writer.close();

            } catch (Exception e) {
                log.error("Unhandled problem in storeFile: " + e);
            }
        } else {
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        }
        return results;
    }

    static public java.io.File getFile(JFileChooser fileChooser) {
        fileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        return getFileCustom(fileChooser);
    }

    static public java.io.File getFileCustom(JFileChooser fileChooser) {
        fileChooser.rescanCurrentDirectory();
        int retVal = fileChooser.showDialog(null, null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            return null;  // give up if no file selected
        }
        if (log.isDebugEnabled()) {
            log.debug("Open file: " + fileChooser.getSelectedFile().getPath());
        }
        return fileChooser.getSelectedFile();
    }


    void loadButtonPressed() {
        JFileChooser userFileChooser = new JFileChooser(FileUtil.getScriptsPath());

        userFileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(rb.getString("MenuItemLoad"));
        userFileChooser.setDialogTitle(rb.getString("MenuItemLoad"));
        jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");
        filt.addExtension("py");
        userFileChooser.setFileFilter(filt);

        boolean results = loadFile(userFileChooser);
        log.debug(results ? "load was successful" : "load failed");
        if (!results) {
            log.debug("Not loading file: " + userFileChooser.getSelectedFile().getPath());
        }
    }

    void storeButtonPressed() {
        JFileChooser userFileChooser = new JFileChooser(FileUtil.getScriptsPath());

        userFileChooser.setDialogType(javax.swing.JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(rb.getString("MenuItemStore"));
        userFileChooser.setDialogTitle(rb.getString("MenuItemStore"));
        jmri.util.FileChooserFilter filt = new jmri.util.FileChooserFilter("Python script files");
        filt.addExtension("py");
        userFileChooser.setFileFilter(filt);

        boolean results = storeFile(userFileChooser);
        log.debug(results ? "store was successful" : "store failed");
        if (!results) {
            log.debug("Not storing file: " + userFileChooser.getSelectedFile().getPath());
        }
    }

    void buttonPressed() {
        PythonInterp.getPythonInterpreter();

        String cmd = area.getText() + "\n";

        // The command must end with exactly one \n
        while ((cmd.length() > 1) && cmd.charAt(cmd.length() - 2) == '\n') {
            cmd = cmd.substring(0, cmd.length() - 1);
        }

        // add the text to the output frame
        String echo = ">>> " + cmd;
        // intermediate \n characters need to be prefixed
        echo = echo.replaceAll("\n", "\n... ");
        echo = echo.substring(0, echo.length() - 4);
        PythonInterp.getOutputArea().append(echo);

        // and execute
        PythonInterp.execCommand(cmd);
    }
    // initialize logging
    static Logger log = Logger.getLogger(InputWindow.class.getName());
}

/* @(#)InputWindow.java */
