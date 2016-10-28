package jmri.jmrit.jython;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.text.BadLocationException;
import jmri.script.JmriScriptEngineManager;
import jmri.script.ScriptFileChooser;
import jmri.script.ScriptOutput;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import org.python.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Action runs creates a JFrame for sending input to the global jython
 * interpreter
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class InputWindow extends JPanel {

    JTextArea area;
    JButton button;
    JButton loadButton;
    JButton storeButton;
    JLabel status;
    JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    JComboBox<String> languages = new JComboBox<>();

    JFileChooser userFileChooser = new ScriptFileChooser(FileUtil.getScriptsPath());

    public InputWindow() {

        //setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
        setLayout(new BorderLayout());

        area = new JTextArea(12, 50);

        // from: http://stackoverflow.com/questions/5139995/java-column-number-and-line-number-of-cursors-current-position
        area.addCaretListener((CaretEvent e) -> {
            // Each time the caret is moved, it will trigger the listener and its method caretUpdate.
            // It will then pass the event to the update method including the source of the event (which is our textarea control)
            JTextArea editArea = (JTextArea) e.getSource();

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
            } catch (BadLocationException ex) {
            }

            // Once we know the position of the line and the column, pass it to a helper function for updating the status bar.
            updateStatus(linenum, columnnum);
        });

        JScrollPane js = new JScrollPane(area);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(js, BorderLayout.CENTER);

        Vector<String> names = new Vector<>();
        JmriScriptEngineManager.getDefault().getManager().getEngineFactories().stream().forEach((ScriptEngineFactory factory) -> {
            names.add(factory.getLanguageName());
        });
        languages = new JComboBox<>(names);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(loadButton = new JButton(Bundle.getMessage("ButtonLoad")));
        p.add(storeButton = new JButton(Bundle.getMessage("ButtonStore")));
        p.add(this.languages);
        p.add(button = new JButton(Bundle.getMessage("ButtonExecute")));

        alwaysOnTopCheckBox.setText("Window always on Top");
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText("If checked, this window be always be displayed in front of any other window");
        p.add(alwaysOnTopCheckBox);

        status = new JLabel("         ");   // create some space for the counters
        p.add(status);
        updateStatus(1, 0);

        add(p, BorderLayout.SOUTH);

        button.addActionListener((ActionEvent e) -> {
            buttonPressed();
        });

        loadButton.addActionListener((ActionEvent e) -> {
            loadButtonPressed();
        });

        storeButton.addActionListener((ActionEvent e) -> {
            storeButtonPressed();
        });

        alwaysOnTopCheckBox.addActionListener((ActionEvent e) -> {
            if (getTopLevelAncestor() != null) {
                ((JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
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
     * @return true if successful
     */
    protected boolean loadFile(JFileChooser fileChooser) {
        boolean results = false;
        File file = getFile(fileChooser);
        if (file != null) {
            try {
                try {
                    languages.setSelectedItem(JmriScriptEngineManager.getDefault().getFactoryByExtension(Files.getFileExtension(file.getName())).getLanguageName());
                } catch (NullPointerException npe) {
                    log.error("Unable to identify script language for {}, assuming its Python.", file);
                    languages.setSelectedItem(JmriScriptEngineManager.getDefault().getFactory(JmriScriptEngineManager.PYTHON).getLanguageName());
                }
                StringBuilder fileData = new StringBuilder(1024);
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    char[] buf = new char[1024];
                    int numRead;
                    while ((numRead = reader.read(buf)) != -1) {
                        String readData = String.valueOf(buf, 0, numRead);
                        fileData.append(readData);
                        buf = new char[1024];
                    }
                }

                area.setText(fileData.toString());

                } catch (IOException e) {
                    log.error("Unhandled problem in loadFile: " + e);
                }
            }else {
            results = true;   // We assume that as the file is null then the user has clicked cancel.
        }
            return results;
        }
        /**
         *
         * @return true if successful
         */
    protected boolean storeFile(JFileChooser fileChooser) {
        boolean results = false;
        File file = getFile(fileChooser);
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
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.append(fileData);
                }

            } catch (HeadlessException | IOException e) {
                log.error("Unhandled problem in storeFile: " + e);
            }
        } else {
            results = true;   // If the file is null then the user has clicked cancel.
        }
        return results;
    }

    static public File getFile(JFileChooser fileChooser) {
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
        userFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        userFileChooser.setApproveButtonText(Bundle.getMessage("MenuItemLoad"));
        userFileChooser.setDialogTitle(Bundle.getMessage("MenuItemLoad"));

        boolean results = loadFile(userFileChooser);
        log.debug(results ? "load was successful" : "load failed");
        if (!results) {
            log.warn("Not loading file: " + userFileChooser.getSelectedFile().getPath());
        }
    }

    void storeButtonPressed() {
        userFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        userFileChooser.setApproveButtonText(Bundle.getMessage("MenuItemStore"));
        userFileChooser.setDialogTitle(Bundle.getMessage("MenuItemStore"));

        boolean results = storeFile(userFileChooser);
        log.debug(results ? "store was successful" : "store failed");
        if (!results) {
            log.warn("Not storing file: " + userFileChooser.getSelectedFile().getPath());
        }
    }

    void buttonPressed() {
        ScriptOutput.writeScript(area.getText());
        try {
            JmriScriptEngineManager.getDefault().eval(area.getText(), JmriScriptEngineManager.getDefault().getEngineByName((String) languages.getSelectedItem()));
        } catch (ScriptException ex) {
            log.error("Error executing script", ex);
        }
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(InputWindow.class.getName());
}
