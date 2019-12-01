package jmri.jmrix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.TextAreaFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for JPanels displaying communications monitor
 * information.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
public abstract class AbstractMonPane extends JmriPanel {

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getTitle();    // provide the title for the frame

    /**
     * Initialize the data source.
     * <p>
     * This is invoked at the end of the GUI initialization phase. Subclass
     * implementations should connect to their data source here.
     */
    protected abstract void init();

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(timeStampCheck, timeCheckBox.isSelected());
        pm.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
        pm.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
        pm.setSimplePreferenceState(autoScrollCheck, !autoScrollCheckBox.isSelected());
        pm.setProperty(filterFieldCheck, filterFieldCheck, filterField.getText());
        monTextPane.dispose();
        super.dispose();
    }
    // you'll also have to add the message(Foo) members to handle info to be logged.
    // these should call nextLine(String line, String raw) with their updates

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected TextAreaFIFO monTextPane = new TextAreaFIFO(MAX_LINES);
    protected JButton startLogButton = new JButton();
    protected JButton stopLogButton = new JButton();
    protected JCheckBox rawCheckBox = new JCheckBox();
    protected JCheckBox timeCheckBox = new JCheckBox();
    protected JCheckBox alwaysOnTopCheckBox = new JCheckBox();
    protected JCheckBox autoScrollCheckBox = new JCheckBox();
    protected JTextField filterField = new JTextField();
    protected JLabel filterLabel = new JLabel(Bundle.getMessage("LabelFilterBytes"), JLabel.LEFT); // NOI18N
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    String timeStampCheck = this.getClass().getName() + ".TimeStamp"; // NOI18N
    String alwaysOnTopCheck = this.getClass().getName() + ".AlwaysOnTop"; // NOI18N
    String autoScrollCheck = this.getClass().getName() + ".AutoScroll"; // NOI18N
    String filterFieldCheck = this.getClass().getName() + ".FilterField"; // NOI18N

    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    public AbstractMonPane() {
        super();
    }

    /**
     * By default, create just one place (one data pane) to put trace data.
     */
    protected void createDataPanes() {
        configureDataPane(monTextPane);
    }

    /**
     * Do default configuration of a data pane.
     *
     * @param textPane a TextAreaFIFO into which the data pane will be placed
     */
    protected void configureDataPane(TextAreaFIFO textPane) {
        textPane.setVisible(true);
        textPane.setToolTipText(Bundle.getMessage("TooltipMonTextPane")); // NOI18N
        textPane.setEditable(false);
    }

    /**
     * Provide initial preferred line length. Used to size the initial GUI.
     *
     * @return preferred initial number of columns
     */
    protected int getInitialPreferredLineLength() {
        return 80;
    }

    /**
     * Provide initial number of lines to display Used to size the initial GUI.
     *
     * @return preferred initial number of rows
     */
    protected int getInitialPreferredLineCount() {
        return 10;
    }

    /**
     * Put data pane(s) in the GUI.
     */
    protected void addDataPanes() {

        // fix a width for current character set
        JTextField t = new JTextField(getInitialPreferredLineLength());
        int x = jScrollPane1.getPreferredSize().width + t.getPreferredSize().width;
        int y = jScrollPane1.getPreferredSize().height + getInitialPreferredLineCount() * t.getPreferredSize().height;

        jScrollPane1.getViewport().add(monTextPane);
        jScrollPane1.setPreferredSize(new Dimension(x, y));
        jScrollPane1.setVisible(true);

        // add in a JPanel that stays sized as the window changes size
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(jScrollPane1);
        add(p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);

        // the following code sets the frame's initial state
        clearButton.setText(Bundle.getMessage("ButtonClearScreen")); // NOI18N
        clearButton.setVisible(true);
        clearButton.setToolTipText(Bundle.getMessage("TooltipClearMonHistory")); // NOI18N

        freezeButton.setText(Bundle.getMessage("ButtonFreezeScreen")); // NOI18N
        freezeButton.setVisible(true);
        freezeButton.setToolTipText(Bundle.getMessage("TooltipStopScroll")); // NOI18N

        enterButton.setText(Bundle.getMessage("ButtonAddMessage")); // NOI18N
        enterButton.setVisible(true);
        enterButton.setToolTipText(Bundle.getMessage("TooltipAddMessage")); // NOI18N

        createDataPanes();

        entryField.setToolTipText(Bundle.getMessage("TooltipEntryPane")); // NOI18N
        // cap vertical size to avoid over-growth
        Dimension currentPreferredSize = entryField.getPreferredSize();
        Dimension currentMaximumSize = entryField.getMaximumSize();
        currentMaximumSize.height = currentPreferredSize.height;
        entryField.setMaximumSize(currentMaximumSize);

        //setup filterField
        filterField.setToolTipText(Bundle.getMessage("TooltipFilter")); // NOI18N
        filterField.setMaximumSize(currentMaximumSize);
        try {
            filterField.setText(pm.getProperty(filterFieldCheck, filterFieldCheck).toString());  //restore prev values
        } catch (NullPointerException e1) {
            // leave blank if previous value not retrieved
        }
        //automatically uppercase input in filterField, and only accept spaces and valid hex characters
        ((AbstractDocument) filterField.getDocument()).setDocumentFilter(new DocumentFilter() {
            final private static String PATTERN = "[0-9a-fA-F ]*+"; // typing inserts individual characters

            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
                    AttributeSet attrs) throws BadLocationException {
                if (text.matches(PATTERN)) { // NOI18N
                    fb.insertString(offset, text.toUpperCase(), attrs);
                } else {
                    fb.insertString(offset, "", attrs);
                }
            }

            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
                    AttributeSet attrs) throws BadLocationException {
                if (text.matches(PATTERN)) { // NOI18N
                    fb.replace(offset, length, text.toUpperCase(), attrs);
                } else {
                    fb.replace(offset, length, "", attrs);
                }
            }
        });

        startLogButton.setText(Bundle.getMessage("ButtonStartLogging")); // NOI18N
        startLogButton.setVisible(true);
        startLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging")); // NOI18N

        stopLogButton.setText(Bundle.getMessage("ButtonStopLogging")); // NOI18N
        stopLogButton.setVisible(true);
        stopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging")); // NOI18N

        rawCheckBox.setText(Bundle.getMessage("ButtonShowRaw")); // NOI18N
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw")); // NOI18N
        rawCheckBox.setSelected(pm.getSimplePreferenceState(rawDataCheck));

        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamps")); // NOI18N
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps")); // NOI18N
        timeCheckBox.setSelected(pm.getSimplePreferenceState(timeStampCheck));

        alwaysOnTopCheckBox.setText(Bundle.getMessage("ButtonWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText(Bundle.getMessage("TooltipWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setSelected(pm.getSimplePreferenceState(alwaysOnTopCheck));
        Component ancestor = getTopLevelAncestor();
        if (ancestor instanceof JmriJFrame) {
            ((JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        } else {
            // this pane isn't yet part of a frame,
            // which can be normal, but
            if (alwaysOnTopCheckBox.isSelected()) {
                // in this case we want to access the enclosing frame to setAlwaysOnTop.  So defer for a bit....
                log.debug("Cannot set Always On Top from preferences due to no Top Level Ancestor");
                timerCount = 0;
                timer = new javax.swing.Timer(20, (java.awt.event.ActionEvent evt) -> {
                    if ((getTopLevelAncestor() != null) && (timerCount > 3) && (getTopLevelAncestor() instanceof JmriJFrame)) {
                        timer.stop();
                        ((JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                        log.debug("set Always On Top");
                    } else {
                        log.debug("Have to repeat attempt to set Always on Top");
                        timerCount++;
                        if (timerCount > 50) {
                            log.warn("Took too long to \"Set Always on Top\", failed");
                            timer.stop();
                        }
                    }
                });
                timer.start();
            }
        }

        autoScrollCheckBox.setText(Bundle.getMessage("ButtonAutoScroll")); // NOI18N
        autoScrollCheckBox.setVisible(true);
        autoScrollCheckBox.setToolTipText(Bundle.getMessage("TooltipAutoScroll")); // NOI18N
        autoScrollCheckBox.setSelected(!pm.getSimplePreferenceState(autoScrollCheck));
        monTextPane.setAutoScroll(!pm.getSimplePreferenceState(autoScrollCheck));

        openFileChooserButton.setText(Bundle.getMessage("ButtonChooseLogFile")); // NOI18N
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText(Bundle.getMessage("TooltipChooseLogFile")); // NOI18N

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // add items to GUI
        addDataPanes();

        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(rawCheckBox);
        pane1.add(timeCheckBox);
        pane1.add(alwaysOnTopCheckBox);
        pane1.add(autoScrollCheckBox);
        paneA.add(pane1);
        addCustomControlPanes(paneA);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(filterLabel);
        pane2.add(filterField);
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
        paneA.add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);

        add(paneA);

        // connect actions to buttons
        clearButton.addActionListener((java.awt.event.ActionEvent e) -> {
            clearButtonActionPerformed(e);
        });
        startLogButton.addActionListener((java.awt.event.ActionEvent e) -> {
            startLogButtonActionPerformed(e);
        });
        stopLogButton.addActionListener((java.awt.event.ActionEvent e) -> {
            stopLogButtonActionPerformed(e);
        });
        openFileChooserButton.addActionListener((java.awt.event.ActionEvent e) -> {
            openFileChooserButtonActionPerformed(e);
        });

        enterButton.addActionListener((java.awt.event.ActionEvent e) -> {
            enterButtonActionPerformed(e);
        });

        alwaysOnTopCheckBox.addActionListener((java.awt.event.ActionEvent e) -> {
            if ((getTopLevelAncestor() != null) && (getTopLevelAncestor() instanceof JmriJFrame)) {
                ((JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
            }
        });

        autoScrollCheckBox.addActionListener((ActionEvent e) -> {
            monTextPane.setAutoScroll(autoScrollCheckBox.isSelected());
        });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));

        // connect to data source
        init();

    }

    /**
     * Expand the display with additional options specific to the hardware.
     * @param parent a Panel (with vertical BoxLayout); overrides should add a new Panel with horizontal BoxLayout to hold the additional options.
     */
    protected void addCustomControlPanes(JPanel parent) {
    }

    private int timerCount = 0;
    private javax.swing.Timer timer;

    /**
     * Set the display window to fixed width font, so that e.g. columns line up.
     */
    public void setFixedWidthFont() {
        monTextPane.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, monTextPane.getFont().getSize()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.AbstractMonFrame"; // NOI18N
    }
        
    /**
     *  Log an Message derived message.
     *
     *  @param message message object to log.
     */
    public void logMessage(Message message){
	    logMessage("","",message);
    }

    /**
     *  Log an Message derived message.
     *
     *  @param messagePrefix text to prefix the message with.
     *  @param message message object to log.
     */
    public void logMessage(String messagePrefix,Message message){
	    logMessage(messagePrefix,"",message);
    }

    /**
     *  Log an Message derived message with a prefixed label.
     *
     *  @param messagePrefix text to prefix the message with.
     *  @param rawPrefix label to add to the start of the message.
     *  @param message message object to log.
     */
    public void logMessage(String messagePrefix,String rawPrefix,Message message){
        // display the raw data if requested  
        StringBuilder raw = new StringBuilder(rawPrefix);
        if (rawCheckBox.isSelected()) {
            raw.append(message.toString());
        }

        // display the decoded data
        String text=message.toMonitorString();
        nextLine(messagePrefix + " " + text + "\n", raw.toString());
    }


    public void nextLine(String line, String raw) {
        nextLineWithTime(new Date(), line, raw);
    }

    /**
     * Handle display of traffic.
     *
     * @param timestamp timestamp to be pre-pended to the output line (if
     *                  timestamping is enabled)
     * @param line      The traffic in normal parsed form, ending with \n
     * @param raw       The traffic in raw form, ending with \n
     */
    public void nextLineWithTime(Date timestamp, String line, String raw) {

        StringBuilder sb = new StringBuilder(120);

        // display the timestamp if requested
        if (timeCheckBox.isSelected()) {
            sb.append(df.format(timestamp)).append(": ");
        }

        // display the raw data if available and requested
        if (raw != null && rawCheckBox.isSelected()) {
            sb.append('[').append(raw).append("]  "); // NOI18N
        }

        // display parsed data
        sb.append(line);
        synchronized (this) {
            linesBuffer.append(sb.toString());
        }

        // if requested, log to a file.
        if (logStream != null) {
            synchronized (logStream) {
                String logLine = sb.toString();
                if (!newline.equals("\n")) { // NOI18N
                    // have to massage the line-ends
                    int lim = sb.length();
                    StringBuilder out = new StringBuilder(sb.length() + 10);  // arbitrary guess at space
                    for (int i = 0; i < lim; i++) {
                        if (sb.charAt(i) == '\n') { // NOI18N
                            out.append(newline);
                        } else {
                            out.append(sb.charAt(i));
                        }
                    }
                    logLine = out.toString();
                }
                logStream.print(logLine);
            }
        }

        // if frozen, exit without adding to the Swing thread
        if (freezeButton.isSelected()) {
            return;
        }

        // if this message is filtered out, end
        if (isFiltered(raw)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            synchronized (AbstractMonPane.this) {
                monTextPane.append(linesBuffer.toString());
                linesBuffer.setLength(0);
            }
        });
    }

    /**
     * Default filtering implementation, more of an example than anything else,
     * not clear it really works for any system. Override this in
     * system-specific subclasses to do something useful.
     *
     * @param raw A string containing the raw message hex information, in ASCII
     *            encoding, with some "header" information pre-pended.
     * @return True if the opcode in the raw message matches one of the "filter"
     *         opcodes. False if the opcode does not match any of the "filter"
     *         opcodes.
     */
    protected boolean isFiltered(String raw) {
        String checkRaw = getOpCodeForFilter(raw);
        //don't bother to check filter if no raw value passed
        if (raw != null) {
            // if first bytes are in the skip list,  exit without adding to the Swing thread
            String[] filters = filterField.getText().toUpperCase().split(" ");

            for (String s : filters) {
                if (s.equals(checkRaw)) {
                    linesBuffer.setLength(0);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get hex opcode for filtering.
     * <p>
     * Reports the "opcode" byte from the string containing the ASCII string
     * representation of the message. Assumes that there is a generic header on
     * string, like "Tx - ", and ignores it.
     *
     * @param raw a String containing the generic raw hex information, with
     *            pre-pended header.
     *
     * @return a two character String containing only the hex representation of
     *         the opcode from the raw message.
     */
    protected String getOpCodeForFilter(String raw) {
        // note: Generic raw is formatted like "Tx - BB 01 00 45", so extract the correct bytes from it (BB) for comparison
        if (raw != null && raw.length() >= 7) {
            return raw.substring(5, 7);
        } else {
            return null;
        }
    }

    private static final String newline = System.getProperty("line.separator"); // NOI18N

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized (linesBuffer) {
            linesBuffer.setLength(0);
            monTextPane.setText("");
        }
    }

    public String getFilePathAndName() {
        String returnString = "";
        java.nio.file.Path p = logFileChooser.getSelectedFile().toPath();
        if (p.getParent() == null) {
            // This case is a file path with a "parent" of "null"
            //
            // Should instead use the profile directory, as "null" can default to
            // the JMRI program directory, which might not be user-writable.
            java.nio.file.Path fileName = p.getFileName();
            if (fileName != null) { // getUserFilesPath() never null
                returnString = FileUtil.getUserFilesPath() + fileName.toString();
            } else {
                log.error("User Files File Path not valid");
            }
            log.warn("File selection dialog box did not provide a path to the specified file. Log will be saved to {}",
                    returnString);
        } else {
            returnString = p.toString();
        }
        return returnString;
    }

    public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if (logStream == null) {  // successive clicks won't restart the file once running
            // start logging
            String filePathAndName = getFilePathAndName();
            log.warn("startLogButtonActionPerformed: getSelectedFile() returns {} {}",
                    logFileChooser.getSelectedFile().getPath(), logFileChooser.getSelectedFile().getName());
            log.warn("startLogButtonActionPerformed: is attempting to use returned file path and file name {}",
                    filePathAndName);
            File logFile = new File(filePathAndName);
            try {
                logStream = new PrintStream(new FileOutputStream(logFile));
            } catch (java.io.FileNotFoundException ex) {
                stopLogButtonActionPerformed(null);
                log.error("startLogButtonActionPerformed: FileOutputStream cannot open the file '{}'.  Exception: {}", logFileChooser.getSelectedFile().getName(), ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        (Bundle.getMessage("ErrorCannotOpenFileForWriting",
                                logFileChooser.getSelectedFile().getName(),
                                Bundle.getMessage("ErrorPossibleCauseCannotOpenForWrite"))),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream != null) {
            synchronized (logStream) {
                logStream.flush();
                logStream.close();
            }
            logStream = null;
        }
    }

    public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
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
    }

    public void enterButtonActionPerformed(java.awt.event.ActionEvent e) {
        nextLine(entryField.getText() + "\n", null); // NOI18N
    }

    public synchronized String getFrameText() {
        return monTextPane.getText();
    }

    /**
     * Get access to the main text area. This is intended for use in e.g.
     * scripting to extend the behavior of the window.
     *
     * @return the main text area
     */
    public final synchronized JTextArea getTextArea() {
        return monTextPane;
    }

    public synchronized String getFilterText() {
        return filterField.getText();
    }

    public synchronized void setFilterText(String text) {
        filterField.setText(text);
    }

    private volatile PrintStream logStream = null;

    // to get a time string
    private DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    protected StringBuffer linesBuffer = new StringBuffer();
    private static final int MAX_LINES = 500;

    private static final Logger log = LoggerFactory.getLogger(AbstractMonPane.class);

}
