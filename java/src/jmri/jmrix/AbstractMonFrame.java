package jmri.jmrix;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.TextAreaFIFO;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Abstract base class for Frames displaying communications monitor information.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2014
 */
public abstract class AbstractMonFrame extends JmriJFrame {

    // template functions to fill in
    protected abstract String title();    // provide the title for the frame

    /**
     * Initialize the data source.
     * <p>
     * This is invoked at the end of the GUI initialization phase. Subclass
     * implementations should connect to their data source here.
     */
    protected abstract void init();

    // the subclass also needs a dispose() method to close any specific communications; call super.dispose()
    @OverridingMethodsMustInvokeSuper
    @Override
    public void dispose() {
        if (userPrefs!=null) {
           userPrefs.setSimplePreferenceState(timeStampCheck, timeCheckBox.isSelected());
           userPrefs.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
           userPrefs.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
           userPrefs.setSimplePreferenceState(autoScrollCheck, !autoScrollCheckBox.isSelected());
        }
        stopLogButtonActionPerformed(null);
        monTextPane.dispose();
        super.dispose();
    }
    // you'll also have to add the message(Foo) members to handle info to be logged.
    // these should call nextLine(String line, String raw) with their updates

    // member declarations
    protected JButton clearButton = new JButton(Bundle.getMessage("ButtonClearScreen"));
    protected JToggleButton freezeButton = new JToggleButton(Bundle.getMessage("ButtonFreezeScreen"));
    protected JScrollPane jScrollPane1 = new JScrollPane();
    protected TextAreaFIFO monTextPane = new TextAreaFIFO(MAX_LINES);
    protected JButton startLogButton = new JButton(Bundle.getMessage("ButtonStartLogging"));
    protected JButton stopLogButton = new JButton(Bundle.getMessage("ButtonStopLogging"));

    protected JCheckBox rawCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowRaw"));
    protected JCheckBox timeCheckBox = new JCheckBox(Bundle.getMessage("ButtonShowTimestamps"));
    protected JCheckBox alwaysOnTopCheckBox = new JCheckBox(Bundle.getMessage("ButtonWindowOnTop"));
    protected JCheckBox autoScrollCheckBox = new JCheckBox(Bundle.getMessage("ButtonAutoScroll"));
    protected JButton openFileChooserButton = new JButton(Bundle.getMessage("ButtonChooseLogFile"));
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton(Bundle.getMessage("ButtonAddMessage"));
    private final String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    private final String timeStampCheck = this.getClass().getName() + ".TimeStamp"; // NOI18N
    private final String alwaysOnTopCheck = this.getClass().getName() + ".alwaysOnTop"; // NOI18N
    private final String autoScrollCheck = this.getClass().getName() + ".AutoScroll"; // NOI18N
    public jmri.UserPreferencesManager userPrefs;

    // for locking
    final AbstractMonFrame self;

    // to find and remember the log file
    public final javax.swing.JFileChooser logFileChooser = new jmri.util.swing.JmriJFileChooser(FileUtil.getUserFilesPath());

    public AbstractMonFrame() {
        super();
        self = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

        userPrefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        // the following code sets the frame's initial state

        monTextPane.setVisible(true);
        monTextPane.setToolTipText(Bundle.getMessage("TooltipMonTextPane")); // NOI18N
        monTextPane.setEditable(false);

        // fix a width for current character set
        JTextField t = new JTextField(200);
        int x = jScrollPane1.getPreferredSize().width + t.getPreferredSize().width;
        int y = jScrollPane1.getPreferredSize().height + 10 * t.getPreferredSize().height;

        jScrollPane1.getViewport().add(monTextPane);
        jScrollPane1.setPreferredSize(new Dimension(x, y));
        jScrollPane1.setVisible(true);

        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        getContentPane().add(jScrollPane1);

        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel topActions = new JPanel();
        topActions.add(getActionButtonsPanel());
        topActions.add(getCheckBoxPanel());

        paneA.add(topActions);
        paneA.add(getLogToFilePanel());

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        enterButton.setVisible(true);
        enterButton.setToolTipText(Bundle.getMessage("TooltipAddMessage")); // NOI18N
        enterButton.addActionListener(this::enterButtonActionPerformed);
        entryField.setToolTipText(Bundle.getMessage("TooltipEntryPane", Bundle.getMessage("ButtonAddMessage"))); // NOI18N
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);

        getContentPane().add(paneA);

        // connect to data source
        init();

        // add help menu to window
        setHelp();

        // prevent button areas from expanding
        pack();
        paneA.setMaximumSize(paneA.getSize());
        pack();
    }

    protected JPanel getCheckBoxPanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));

        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw")); // NOI18N
        rawCheckBox.setSelected(userPrefs.getSimplePreferenceState(rawDataCheck));

        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps")); // NOI18N
        timeCheckBox.setSelected(userPrefs.getSimplePreferenceState(timeStampCheck));

        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText(Bundle.getMessage("TooltipWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setSelected(userPrefs.getSimplePreferenceState(alwaysOnTopCheck));
        setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());

        alwaysOnTopCheckBox.addActionListener((ActionEvent e) -> {
            setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
        });

        autoScrollCheckBox.setVisible(true);
        autoScrollCheckBox.setToolTipText(Bundle.getMessage("TooltipAutoScroll")); // NOI18N
        autoScrollCheckBox.setSelected(!userPrefs.getSimplePreferenceState(autoScrollCheck));

        autoScrollCheckBox.addActionListener((ActionEvent e) -> {
            monTextPane.setAutoScroll(autoScrollCheckBox.isSelected());
        });

        pane1.add(rawCheckBox);
        pane1.add(timeCheckBox);
        pane1.add(alwaysOnTopCheckBox);
        pane1.add(autoScrollCheckBox);
        return pane1;
    }

    protected JPanel getActionButtonsPanel() {

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));

        clearButton.setVisible(true);
        clearButton.setToolTipText(Bundle.getMessage("TooltipClearMonHistory")); // NOI18N
        clearButton.addActionListener(this::clearButtonActionPerformed);

        freezeButton.setVisible(true);
        freezeButton.setToolTipText(Bundle.getMessage("TooltipStopScroll")); // NOI18N

        pane1.add(clearButton);
        pane1.add(freezeButton);
        return pane1;
    }

    protected JPanel getLogToFilePanel() {
        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));

        startLogButton.setVisible(true);
        startLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging"));

        stopLogButton.setVisible(false);
        stopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging"));

        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText(Bundle.getMessage("TooltipChooseLogFile"));

        startLogButton.addActionListener(this::startLogButtonActionPerformed);
        stopLogButton.addActionListener(this::stopLogButtonActionPerformed);

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt"));
        openFileChooserButton.addActionListener(this::openFileChooserButtonActionPerformed);

        pane1.add(openFileChooserButton);
        pane1.add(startLogButton);
        pane1.add(stopLogButton);
        return pane1;
    }

    /**
     * Define help menu for this window.
     * <p>
     * By default, provides a generic help page that covers general features.
     * Specific implementations can override this to show their own help page if
     * desired.
     */
    protected void setHelp() {
        addHelpMenu("package.jmri.jmrix.AbstractMonFrame", true); // NOI18N
    }

    /**
     * Handle display of traffic.
     * @param line is the traffic in 'normal form'. Should end with \n
     * @param raw is the "raw form" , should NOT end with \n
     */
    public void nextLine(String line, String raw) {
        StringBuilder sb = new StringBuilder(120);

        // display the timestamp if requested
        if (timeCheckBox.isSelected()) {
            sb.append(df.format(new Date())).append(": "); // NOI18N
        }

        // display the raw data if requested
        if (rawCheckBox.isSelected()) {
            sb.append('[').append(raw).append("]  "); // NOI18N
        }

        // display decoded data
        sb.append(line);
        synchronized (self) {
            linesBuffer.append(sb.toString());
        }

        // if not frozen, display it in the Swing thread
        if (!freezeButton.isSelected()) {
            Runnable r = () -> {
                synchronized (self) {
                    monTextPane.append(linesBuffer.toString());
                    linesBuffer.setLength(0);
                }
            };
            javax.swing.SwingUtilities.invokeLater(r);
        }

        // if requested, log to a file.
        if (logStream != null) {
            synchronized (logStream) {
                String logLine = sb.toString();
                if (!newline.equals("\n")) {
                    // have to massage the line-ends
                    int lim = sb.length();
                    StringBuilder out = new StringBuilder(sb.length() + 10);  // arbitrary guess at space
                    for (int i = 0; i < lim; i++) {
                        if (sb.charAt(i) == '\n') {
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
            } catch (java.io.FileNotFoundException ex) {
                log.error("exception", ex);
            }
        }
        updateLoggingButtons();
    }

    public synchronized void stopLogButtonActionPerformed(java.awt.event.ActionEvent e) {
        // stop logging by removing the stream
        if (logStream != null) {
            logStream.flush();
            logStream.close();
            logStream = null;
        }
        updateLoggingButtons();
    }

    private void updateLoggingButtons(){
        this.startLogButton.setVisible(logStream == null);
        this.stopLogButton.setVisible(logStream != null);
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
        nextLine(entryField.getText() + "\n", ""); // NOI18N
    }

    /**
     * Get access to the main text area.
     * This is intended for use in e.g. scripting
     * to extend the behaviour of the window.
     * @return the text area.
     */
    public final synchronized JTextArea getTextArea() {
        return monTextPane;
    }

    private volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    StringBuffer linesBuffer = new StringBuffer();
    static private int MAX_LINES = 500;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractMonFrame.class);

}
