package jmri.jmrix;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import jmri.util.FileUtil;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for JPanels displaying communications monitor information
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2003, 2010
 */
public abstract class AbstractMonPane extends JmriPanel {

    // template functions to fill in
    @Override
    public abstract String getTitle();    // provide the title for the frame

    /**
     * Initialize the data source.
     * <P>
     * This is invoked at the end of the GUI initialization phase. Subclass
     * implementations should connect to their data source here.
     */
    protected abstract void init();

    // the subclass also needs a dispose() method to close any specific communications; call super.dispose()
    @Override
    public void dispose() {
        p.setSimplePreferenceState(timeStampCheck, timeCheckBox.isSelected());
        p.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
        p.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
        p.setSimplePreferenceState(autoScrollCheck, !autoScrollCheckBox.isSelected());
        p.setProperty(filterFieldCheck, filterFieldCheck, filterField.getText());
        super.dispose();
    }
    // you'll also have to add the message(Foo) members to handle info to be logged.
    // these should call nextLine(String line, String raw) with their updates

    // member declarations
    protected JButton clearButton = new JButton();
    protected JToggleButton freezeButton = new JToggleButton();
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected JTextArea monTextPane = new JTextArea();
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
    jmri.UserPreferencesManager p;

    // for locking
    AbstractMonPane self;

    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    @SuppressWarnings("LeakingThisInConstructor") // NOI18N
    public AbstractMonPane() {
        super();
        self = this;
    }

    /**
     * By default, creates just one place (one data pane) to put trace data
     */
    protected void createDataPanes() {
        configureDataPane(monTextPane);
    }
    
    /**
     * Do default configuration of a data pane
     */
    protected void configureDataPane(JTextArea textPane) {
        textPane.setVisible(true);
        textPane.setToolTipText(Bundle.getMessage("TooltipMonTextPane")); // NOI18N
        textPane.setEditable(false);

        // Add document listener to scroll to end when modified if required
        textPane.getDocument().addDocumentListener(new DocumentListener() {

            // References to the JTextArea and JCheckBox
            // of this instantiation
            JTextArea ta = textPane;
            JCheckBox chk = autoScrollCheckBox;

            @Override
            public void insertUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doAutoScroll(ta, chk.isSelected());
            }
        });
    }
    
    /**
     * Provide initial preferred line length.
     * Used to size the initial GUI
     */
    protected int getInitialPreferredLineLength() { return 80; }
    /**
     * Provide initial number of lines to display
     * Used to size the initial GUI
     */
    protected int getInitialPreferredLineCount() { return 10; }
    
    
    /**
     * Put data pane(s) in the GUI
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
    
    @Override
    public void initComponents() throws Exception {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
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
            filterField.setText(p.getProperty(filterFieldCheck, filterFieldCheck).toString());  //restore prev values
        } catch (Exception e1) {  //leave blank if previous value not retrieved
        }
        //automatically uppercase input in filterField, and only accept spaces and valid hex characters
        ((AbstractDocument) filterField.getDocument()).setDocumentFilter(new DocumentFilter() {
            final static String pattern = "[0-9a-fA-F ]*+"; // typing inserts individual characters
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
                    AttributeSet attrs) throws BadLocationException {
                if (text.matches(pattern)) { // NOI18N
                    fb.insertString(offset, text.toUpperCase(), attrs);
                } else {
                    fb.insertString(offset, "", attrs);
                }
            }

            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
                    AttributeSet attrs) throws BadLocationException {
                if (text.matches(pattern)) { // NOI18N
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
        if (p!=null) rawCheckBox.setSelected(p.getSimplePreferenceState(rawDataCheck));

        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamps")); // NOI18N
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps")); // NOI18N
        if (p!=null) timeCheckBox.setSelected(p.getSimplePreferenceState(timeStampCheck));

        alwaysOnTopCheckBox.setText(Bundle.getMessage("ButtonWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText(Bundle.getMessage("TooltipWindowOnTop")); // NOI18N
        if (p!=null) alwaysOnTopCheckBox.setSelected(p.getSimplePreferenceState(alwaysOnTopCheck));
        if (getTopLevelAncestor() != null) {
            ((jmri.util.JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        } else {
            // this pane isn't yet part of a frame,
            // which can be normal, but 
            if (alwaysOnTopCheckBox.isSelected()) {
                // in this case we want to access the enclosing frame to setAlwaysOnTop.  So defer for a bit....
                log.debug("Cannot set Always On Top from preferences due to no Top Level Ancestor");
                timerCount = 0;
                timer = new javax.swing.Timer(20, (java.awt.event.ActionEvent evt)->{
                    if (getTopLevelAncestor() != null && timerCount> 3) {
                        timer.stop();
                        ((jmri.util.JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                        log.debug("set Always On Top");
                    } else {
                        log.debug("Have to repeat attempt to set Always on Top");
                        timerCount++;
                        if (timerCount > 50) {
                            log.debug("Set Always on Top failed");
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
        if (p!=null) autoScrollCheckBox.setSelected(!p.getSimplePreferenceState(autoScrollCheck));

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
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        startLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                startLogButtonActionPerformed(e);
            }
        });
        stopLogButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });

        enterButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });

        alwaysOnTopCheckBox.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (getTopLevelAncestor() != null) {
                    ((jmri.util.JmriJFrame) getTopLevelAncestor()).setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
                }
            }
        });

        autoScrollCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAutoScroll(monTextPane, autoScrollCheckBox.isSelected());
            }
        });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));

        // connect to data source
        init();

    }

    private int timerCount = 0;
    private javax.swing.Timer timer;
    /**
     * Sets the display window to fixed width font, so that e.g. columns line up
     */
    public void setFixedWidthFont() {
        monTextPane.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, monTextPane.getFont().getSize()));
    }

    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page that covers general features.
     * Specific implementations can override this to show their own help page if
     * desired.
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.AbstractMonFrame"; // NOI18N
    }

    public void nextLine(String line, String raw) {
        nextLineWithTime(new Date(), line, raw);
    }

    /**
     * Handle display of traffic.
     *
     * @param timestamp
     * @param line The traffic in normal parsed form, ending with \n
     * @param raw The traffic in raw form, ending with \n
     */
    public void nextLineWithTime(Date timestamp, String line, String raw) {

        StringBuffer sb = new StringBuffer(120);

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
        synchronized (self) {
            linesBuffer.append(sb.toString());
        }

        // if requested, log to a file.
        if (logStream != null) {
            synchronized (logStream) {
                String logLine = sb.toString();
                if (!newline.equals("\n")) { // NOI18N
                    // have to massage the line-ends
                    int i = 0;
                    int lim = sb.length();
                    StringBuffer out = new StringBuffer(sb.length() + 10);  // arbitrary guess at space
                    for (i = 0; i < lim; i++) {
                        if (sb.charAt(i) == '\n') // NOI18N
                        {
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
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                synchronized (self) {
                    monTextPane.append(linesBuffer.toString());
                    int LineCount = monTextPane.getLineCount();
                    if (LineCount > MAX_LINES) {
                        LineCount -= MAX_LINES;
                        try {
                            int offset = monTextPane.getLineStartOffset(LineCount);
                            monTextPane.getDocument().remove(0, offset);
                        } catch (BadLocationException ex) {
                        }
                    }
                    linesBuffer.setLength(0);
                }
            }
        };
        javax.swing.SwingUtilities.invokeLater(r);
    }

    /**
     * Default filtering implementation, more of an example
     * than anything else, not clear it really works
     * for any system.  Override this in system-specific subclasses to do something 
     * useful.
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
     * Get hex opcode for filtering
     */
    protected String getOpCodeForFilter(String raw) {
        //note: Generic raw is formatted like "Tx - BB 01 00 45", so extract the correct bytes from it (BB) for comparison
        if (raw != null && raw.length() >= 7) {
            return raw.substring(5, 7);
        } else return null;
    }
    
    String newline = System.getProperty("line.separator"); // NOI18N

    public synchronized void clearButtonActionPerformed(java.awt.event.ActionEvent e) {
        // clear the monitoring history
        synchronized (linesBuffer) {
            linesBuffer.setLength(0);
            monTextPane.setText("");
        }
    }

    public synchronized void startLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start logging by creating the stream
        if (logStream == null) {  // successive clicks don't restart the file
            // start logging
            try {
                logStream = new PrintStream(new FileOutputStream(logFileChooser.getSelectedFile()));
            } catch (Exception ex) {
                log.error("exception " + ex);
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

    public synchronized String getFilterText() {
        return filterField.getText();
    }

    public synchronized void setFilterText(String text) {
        filterField.setText(text);
    }

    /**
     * Method to position caret at end of JTextArea ta when scroll true.
     *
     * @param ta     Reference to JTextArea
     * @param scroll True to move to end
     */
    private void doAutoScroll(final JTextArea ta, final boolean scroll) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int len = ta.getText().length();
                if (scroll) {
                    ta.setCaretPosition(len);
                } else if (ta.getCaretPosition() == len && len > 0) {
                    ta.setCaretPosition(len - 1);
                }
            }
        });
    }

    volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    protected StringBuffer linesBuffer = new StringBuffer();
    static private int MAX_LINES = 500;

    private static final Logger log = LoggerFactory.getLogger(AbstractMonFrame.class.getName());
}
