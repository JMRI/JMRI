package jmri.jmrit.logixng.tools.swing;


import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.logixng.Base.PrintTreeSettings;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * Browse window for LogixNG
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class LogixNGBrowseWindow extends JmriJFrame {

    public interface GetText {
        public String getText(PrintTreeSettings printTreeSettings);
    }


    // Browser Options
    private static final String PRINT_LINE_NUMBERS_OPTION = "jmri.jmrit.logixng.PrintLineNumbers";
    private static final String PRINT_ERROR_HANDLING_OPTION = "jmri.jmrit.logixng.ErrorHandling";
    private static final String PRINT_NOT_CONNECTED_OPTION = "jmri.jmrit.logixng.NotConnectedSockets";
    private static final String PRINT_LOCAL_VARIABLES_OPTION = "jmri.jmrit.logixng.LocalVariables";
    private static final String PRINT_SYSTEM_NAMES_OPTION = "jmri.jmrit.logixng.SystemNames";
    private static final String PRINT_DISABLED_OPTION = "jmri.jmrit.logixng.Disabled";

    private PrintTreeSettings _printTreeSettings = new PrintTreeSettings();
    private JTextArea _textContent;
    private JFileChooser userFileChooser =
            new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());
    

    public LogixNGBrowseWindow(String title) {
        super(title, false, true);
    }
    
    public void getPrintTreeSettings() {
        // Set options
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            _printTreeSettings._printLineNumbers = prefMgr.getSimplePreferenceState(PRINT_LINE_NUMBERS_OPTION);
            _printTreeSettings._printErrorHandling = prefMgr.getSimplePreferenceState(PRINT_ERROR_HANDLING_OPTION);
            _printTreeSettings._printNotConnectedSockets = prefMgr.getSimplePreferenceState(PRINT_NOT_CONNECTED_OPTION);
            _printTreeSettings._printLocalVariables = prefMgr.getSimplePreferenceState(PRINT_LOCAL_VARIABLES_OPTION);
            _printTreeSettings._printSystemNames = prefMgr.getSimplePreferenceState(PRINT_SYSTEM_NAMES_OPTION);
            _printTreeSettings._printDisabled = prefMgr.getSimplePreferenceState(PRINT_DISABLED_OPTION);
            _printTreeSettings._printStartup = true;
        });
    }

     /**
     * Update text in the browser window.
     * @param getText the text
     */
    public void updateBrowserText(GetText getText) {
        if (_textContent != null) {
            _textContent.setText(getText.getText(_printTreeSettings));
        }
    }

    /**
     * Create and initialize the browser window.
     * @param browseMonoSpace use monospace font?
     * @param showSettingsPanel show settings panel?
     * @param header the header
     * @param systemName the system name of the bean or the header
     * @param getText the text
     */
    public void makeBrowserWindow(
            boolean browseMonoSpace, boolean showSettingsPanel,
            String header, String systemName, GetText getText) {

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                _textContent = null;
            }
        });

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        // bean header information
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel(header));
        contentPane.add(topPanel, BorderLayout.NORTH);

        // Build the conditionalNGs listing
        _textContent = new JTextArea(getText.getText(_printTreeSettings));

        if (browseMonoSpace) {
            _textContent.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        }
        JScrollPane scrollPane = new JScrollPane(_textContent);
        contentPane.add(scrollPane);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JButton helpBrowse = new JButton(Bundle.getMessage("MenuHelp"));   // NOI18N
        bottomPanel.add(helpBrowse, BorderLayout.WEST);
        helpBrowse.addActionListener((ActionEvent e) -> {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("LogixNG_Browse_HelpText"),   // NOI18N
                    Bundle.getMessage("BrowserHelpTitle"),  // NOI18N
                    JmriJOptionPane.INFORMATION_MESSAGE);
        });

        if (showSettingsPanel) {
            JPanel settingsPanel = getSettingsPanel(getText);
            bottomPanel.add(settingsPanel, BorderLayout.CENTER);
        }
        JButton saveBrowse = new JButton(Bundle.getMessage("BrowserSaveButton"));   // NOI18N
        saveBrowse.setToolTipText(Bundle.getMessage("BrowserSaveButtonHint"));      // NOI18N
        bottomPanel.add(saveBrowse, BorderLayout.EAST);
        saveBrowse.addActionListener((ActionEvent e) -> {
            saveBrowserPressed(header, systemName);
        });
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }  // makeBrowserWindow

    /**
     * Save the bean browser window content to a text file.
     * @param header the header
     * @param systemName the system name of the bean or the header
     */
    void saveBrowserPressed(String header, String systemName) {
        userFileChooser.setApproveButtonText(Bundle.getMessage("BrowserSaveDialogApprove"));  // NOI18N
        userFileChooser.setDialogTitle(Bundle.getMessage("BrowserSaveDialogTitle"));  // NOI18N
        userFileChooser.rescanCurrentDirectory();
        // Default to logixNG system name.txt
        String suggestedFileName = systemName.replace(':', '_') + ".txt";
        userFileChooser.setSelectedFile(new File(suggestedFileName));  // NOI18N
        int retVal = userFileChooser.showSaveDialog(null);
        if (retVal != JFileChooser.APPROVE_OPTION) {
            log.debug("Save browser content stopped, no file selected");  // NOI18N
            return;  // give up if no file selected or cancel pressed
        }
        File file = userFileChooser.getSelectedFile();
        log.debug("Save browser content to '{}'", file);  // NOI18N

        if (file.exists()) {
            Object[] options = {Bundle.getMessage("BrowserSaveDuplicateReplace"),  // NOI18N
                    Bundle.getMessage("BrowserSaveDuplicateAppend"),  // NOI18N
                    Bundle.getMessage("ButtonCancel")};               // NOI18N
            int selectedOption = JmriJOptionPane.showOptionDialog(null,
                    Bundle.getMessage("BrowserSaveDuplicatePrompt", file.getName()), // NOI18N
                    Bundle.getMessage("BrowserSaveDuplicateTitle"),   // NOI18N
                    JmriJOptionPane.DEFAULT_OPTION,
                    JmriJOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            if (selectedOption == 2 || selectedOption == -1) {
                log.debug("Save browser content stopped, file replace/append cancelled");  // NOI18N
                return;  // Cancel selected or dialog box closed
            }
            if (selectedOption == 0) {
                FileUtil.delete(file);  // Replace selected
            }
        }

        // Create the file content
        try {
            // Add bean Header inforation first
            FileUtil.appendTextToFile(file, header);
            FileUtil.appendTextToFile(file, "-".repeat(header.length()));
            FileUtil.appendTextToFile(file, "");
            FileUtil.appendTextToFile(file, _textContent.getText());
        } catch (IOException e) {
            log.error("Unable to write browser content to '{}'", file, e);  // NOI18N
        }
    }

    protected JPanel getSettingsPanel(GetText getText) {
        JPanel checkBoxPanel = new JPanel();

        JCheckBox printLineNumbers = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintLineNumbers"));
        printLineNumbers.setSelected(_printTreeSettings._printLineNumbers);
        printLineNumbers.addChangeListener((event) -> {
            if (_printTreeSettings._printLineNumbers != printLineNumbers.isSelected()) {
                _printTreeSettings._printLineNumbers = printLineNumbers.isSelected();
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_LINE_NUMBERS_OPTION, printLineNumbers.isSelected());
                });
                updateBrowserText(getText);
            }
        });

        JCheckBox printErrorHandling = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintErrorHandling"));
        printErrorHandling.setSelected(_printTreeSettings._printErrorHandling);
        printErrorHandling.addChangeListener((event) -> {
            if (_printTreeSettings._printErrorHandling != printErrorHandling.isSelected()) {
                _printTreeSettings._printErrorHandling = printErrorHandling.isSelected();
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_ERROR_HANDLING_OPTION, printErrorHandling.isSelected());
                });
                updateBrowserText(getText);
            }
        });

        JCheckBox printNotConnectedSockets = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintNotConnectedSocket"));
        printNotConnectedSockets.setSelected(_printTreeSettings._printNotConnectedSockets);
        printNotConnectedSockets.addChangeListener((event) -> {
            if (_printTreeSettings._printNotConnectedSockets != printNotConnectedSockets.isSelected()) {
                _printTreeSettings._printNotConnectedSockets = printNotConnectedSockets.isSelected();
                updateBrowserText(getText);
                InstanceManager.getOptionalDefault(jmri.UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_NOT_CONNECTED_OPTION, printNotConnectedSockets.isSelected());
                });
            }
        });

        JCheckBox printLocalVariables = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintLocalVariables"));
        printLocalVariables.setSelected(_printTreeSettings._printLocalVariables);
        printLocalVariables.addChangeListener((event) -> {
            if (_printTreeSettings._printLocalVariables != printLocalVariables.isSelected()) {
                _printTreeSettings._printLocalVariables = printLocalVariables.isSelected();
                updateBrowserText(getText);
                InstanceManager.getOptionalDefault(jmri.UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_LOCAL_VARIABLES_OPTION, printLocalVariables.isSelected());
                });
            }
        });

        JCheckBox printSystemNames = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintSystemNames"));
        printSystemNames.setSelected(_printTreeSettings._printSystemNames);
        printSystemNames.addChangeListener((event) -> {
            if (_printTreeSettings._printSystemNames != printSystemNames.isSelected()) {
                _printTreeSettings._printSystemNames = printSystemNames.isSelected();
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_SYSTEM_NAMES_OPTION, printSystemNames.isSelected());
                });
                updateBrowserText(getText);
            }
        });

        JCheckBox printDisabled = new JCheckBox(Bundle.getMessage("LogixNG_Browse_PrintDisabled"));
        printDisabled.setSelected(_printTreeSettings._printDisabled);
        printDisabled.addChangeListener((event) -> {
            if (_printTreeSettings._printDisabled != printDisabled.isSelected()) {
                _printTreeSettings._printDisabled = printDisabled.isSelected();
                InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
                    prefMgr.setSimplePreferenceState(PRINT_DISABLED_OPTION, printDisabled.isSelected());
                });
                updateBrowserText(getText);
            }
        });

        checkBoxPanel.add(printLineNumbers);
        checkBoxPanel.add(printErrorHandling);
        checkBoxPanel.add(printNotConnectedSockets);
        checkBoxPanel.add(printLocalVariables);
        checkBoxPanel.add(printSystemNames);
        checkBoxPanel.add(printDisabled);

        return checkBoxPanel;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGBrowseWindow.class);
}
