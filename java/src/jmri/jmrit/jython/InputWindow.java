// InputWindow.java

package jmri.jmrit.jython;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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

/**
 * This Action runs creates a JFrame for sending input to the
 * global jython interpreter
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision$
 */
public class InputWindow extends JPanel {

    JTextArea area;
    JButton button;
    JButton loadButton;
    JButton storeButton;
    JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    static java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");

    public InputWindow() {
    
        //setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());
        
        area = new JTextArea(12, 50);
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
        JFileChooser userFileChooser = new JFileChooser(jmri.jmrit.XmlFile.scriptsDir());

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
        JFileChooser userFileChooser = new JFileChooser(jmri.jmrit.XmlFile.scriptsDir());

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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(InputWindow.class.getName());
}

/* @(#)InputWindow.java */
