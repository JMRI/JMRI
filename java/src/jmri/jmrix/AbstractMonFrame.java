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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.TextAreaFIFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        if(p!=null) {
           p.setSimplePreferenceState(timeStampCheck, timeCheckBox.isSelected());
           p.setSimplePreferenceState(rawDataCheck, rawCheckBox.isSelected());
           p.setSimplePreferenceState(alwaysOnTopCheck, alwaysOnTopCheckBox.isSelected());
           p.setSimplePreferenceState(autoScrollCheck, !autoScrollCheckBox.isSelected());
        }
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
    protected JButton openFileChooserButton = new JButton();
    protected JTextField entryField = new JTextField();
    protected JButton enterButton = new JButton();
    String rawDataCheck = this.getClass().getName() + ".RawData"; // NOI18N
    String timeStampCheck = this.getClass().getName() + ".TimeStamp"; // NOI18N
    String alwaysOnTopCheck = this.getClass().getName() + ".alwaysOnTop"; // NOI18N
    String autoScrollCheck = this.getClass().getName() + ".AutoScroll"; // NOI18N
    jmri.UserPreferencesManager p;

    // for locking
    AbstractMonFrame self;

    // to find and remember the log file
    final javax.swing.JFileChooser logFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    public AbstractMonFrame() {
        super();
        self = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {

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

        monTextPane.setVisible(true);
        monTextPane.setToolTipText(Bundle.getMessage("TooltipMonTextPane")); // NOI18N
        monTextPane.setEditable(false);

        entryField.setToolTipText(Bundle.getMessage("TooltipEntryPane", Bundle.getMessage("ButtonAddMessage"))); // NOI18N

        // fix a width for current character set
        JTextField t = new JTextField(200);
        int x = jScrollPane1.getPreferredSize().width + t.getPreferredSize().width;
        int y = jScrollPane1.getPreferredSize().height + 10 * t.getPreferredSize().height;

        jScrollPane1.getViewport().add(monTextPane);
        jScrollPane1.setPreferredSize(new Dimension(x, y));
        jScrollPane1.setVisible(true);

        startLogButton.setText(Bundle.getMessage("ButtonStartLogging")); // NOI18N
        startLogButton.setVisible(true);
        startLogButton.setToolTipText(Bundle.getMessage("TooltipStartLogging")); // NOI18N

        stopLogButton.setText(Bundle.getMessage("ButtonStopLogging")); // NOI18N
        stopLogButton.setVisible(true);
        stopLogButton.setToolTipText(Bundle.getMessage("TooltipStopLogging")); // NOI18N

        rawCheckBox.setText(Bundle.getMessage("ButtonShowRaw")); // NOI18N
        rawCheckBox.setVisible(true);
        rawCheckBox.setToolTipText(Bundle.getMessage("TooltipShowRaw")); // NOI18N
        rawCheckBox.setSelected(p.getSimplePreferenceState(rawDataCheck));

        timeCheckBox.setText(Bundle.getMessage("ButtonShowTimestamps")); // NOI18N
        timeCheckBox.setVisible(true);
        timeCheckBox.setToolTipText(Bundle.getMessage("TooltipShowTimestamps")); // NOI18N
        timeCheckBox.setSelected(p.getSimplePreferenceState(timeStampCheck));

        alwaysOnTopCheckBox.setText(Bundle.getMessage("ButtonWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setVisible(true);
        alwaysOnTopCheckBox.setToolTipText(Bundle.getMessage("TooltipWindowOnTop")); // NOI18N
        alwaysOnTopCheckBox.setSelected(p.getSimplePreferenceState(alwaysOnTopCheck));
        setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());

        autoScrollCheckBox.setText(Bundle.getMessage("ButtonAutoScroll")); // NOI18N
        autoScrollCheckBox.setVisible(true);
        autoScrollCheckBox.setToolTipText(Bundle.getMessage("TooltipAutoScroll")); // NOI18N
        autoScrollCheckBox.setSelected(!p.getSimplePreferenceState(autoScrollCheck));

        openFileChooserButton.setText(Bundle.getMessage("ButtonChooseLogFile")); // NOI18N
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setToolTipText(Bundle.getMessage("TooltipChooseLogFile")); // NOI18N

        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // add items to GUI
        getContentPane().add(jScrollPane1);

        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.X_AXIS));
        pane1.add(clearButton);
        pane1.add(freezeButton);
        pane1.add(rawCheckBox);
        pane1.add(timeCheckBox);
        pane1.add(alwaysOnTopCheckBox);
        paneA.add(pane1);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
        pane2.add(openFileChooserButton);
        pane2.add(startLogButton);
        pane2.add(stopLogButton);
        paneA.add(pane2);

        JPanel pane3 = new JPanel();
        pane3.setLayout(new BoxLayout(pane3, BoxLayout.X_AXIS));
        pane3.add(enterButton);
        pane3.add(entryField);
        paneA.add(pane3);

        getContentPane().add(paneA);

        // connect actions to buttons
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearButtonActionPerformed(e);
            }
        });
        startLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startLogButtonActionPerformed(e);
            }
        });
        stopLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopLogButtonActionPerformed(e);
            }
        });
        openFileChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFileChooserButtonActionPerformed(e);
            }
        });

        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enterButtonActionPerformed(e);
            }
        });

        alwaysOnTopCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAlwaysOnTop(alwaysOnTopCheckBox.isSelected());
            }
        });

        autoScrollCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                monTextPane.setAutoScroll(autoScrollCheckBox.isSelected());
            }
        });

        // set file chooser to a default
        logFileChooser.setSelectedFile(new File("monitorLog.txt")); // NOI18N

        // connect to data source
        init();

        // add help menu to window
        setHelp();

        // prevent button areas from expanding
        pack();
        paneA.setMaximumSize(paneA.getSize());
        pack();
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

    public void nextLine(String line, String raw) {
        // handle display of traffic
        // line is the traffic in 'normal form', raw is the "raw form"
        // Both should be one or more well-formed lines, e.g. end with \n
        StringBuffer sb = new StringBuffer(120);

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
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    synchronized (self) {
                        monTextPane.append(linesBuffer.toString());
                        linesBuffer.setLength(0);
                    }
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
                    int i = 0;
                    int lim = sb.length();
                    StringBuffer out = new StringBuffer(sb.length() + 10);  // arbitrary guess at space
                    for (i = 0; i < lim; i++) {
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
        nextLine(entryField.getText() + "\n", ""); // NOI18N
    }

    public synchronized String getFrameText() {
        return linesBuffer.toString();
    }

    /** 
     * Get access to the main text area. This is intended
     * for use in e.g. scripting to extend the behavior of the window.
     */
    public final synchronized JTextArea getTextArea() {
        return monTextPane;
    }

    volatile PrintStream logStream = null;

    // to get a time string
    DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");

    StringBuffer linesBuffer = new StringBuffer();
    static private int MAX_LINES = 500;

    private static final Logger log = LoggerFactory.getLogger(AbstractMonFrame.class);

}
