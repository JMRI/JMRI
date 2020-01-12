package jmri.jmrix.sprog.update;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import jmri.jmrix.sprog.SprogConstants.SprogState;
import jmri.jmrix.sprog.SprogListener;
import jmri.jmrix.sprog.SprogMessage;
import jmri.jmrix.sprog.SprogReply;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for SPROG firmware update utility.
 *
 * Refactored
 *
 * @author Andrew Crosland Copyright (C) 2004
 * @author Andrew Berridge - Feb 2010 - removed implementation of SprogListener - wasn't
 * being used.
 */
abstract public class SprogUpdateFrame
        extends jmri.util.JmriJFrame
        implements SprogListener {

// member declarations
    protected JButton programButton = new JButton();
    protected JButton openFileChooserButton = new JButton();
    protected JButton setSprogModeButton = new JButton();

    protected SprogVersion sv;

    // to find and remember the hex file
    final javax.swing.JFileChooser hexFileChooser = new JFileChooser(FileUtil.getUserFilesPath());

    JLabel statusBar = new JLabel();

    // File to hold name of hex file
    transient SprogHexFile hexFile = null;

    SprogMessage msg;

    // members for handling the bootloader interface
    protected enum BootState {

        IDLE,
        CRSENT, // awaiting reply to " "
        QUERYSENT, // awaiting reply to "?"
        SETBOOTSENT, // awaiting reply from bootloader
        VERREQSENT, // awaiting reply to version request
        WRITESENT, // write flash command sent, waiting reply
        NULLWRITE, // no write sent
        ERASESENT, // erase sent
        SPROGMODESENT, // enable sprog mode sent
        RESETSENT, // reset sent
        EOFSENT, // v4 end of file sent
        V4RESET,          // wait for v4 to reset
    }
    protected BootState bootState = BootState.IDLE;
    protected int eraseAddress;

    static final boolean UNKNOWN = false;
    static final boolean KNOWN = true;

    protected SprogReply reply;
    protected String replyString;
    int blockLen = 0;

    protected SprogTrafficController tc = null;
    protected SprogSystemConnectionMemo _memo = null;

    public SprogUpdateFrame(SprogSystemConnectionMemo memo) {
        super();
        _memo = memo;
    }

    protected String title() {
        return Bundle.getMessage("SprogXFirmwareUpdate");
    }

    protected void init() {
        // connect to the TrafficManager
        tc = _memo.getSprogTrafficController();
        tc.setSprogState(SprogState.NORMAL);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tc = null;
        _memo = null;
        super.dispose();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        // the following code sets the frame's initial state
        programButton.setText(Bundle.getMessage("ButtonProgram"));
        programButton.setVisible(true);
        programButton.setEnabled(false);
        programButton.setToolTipText(Bundle.getMessage("ButtonProgramTooltip"));

        openFileChooserButton.setText(Bundle.getMessage("ButtonSelectHexFile"));
        openFileChooserButton.setVisible(true);
        openFileChooserButton.setEnabled(false);
        openFileChooserButton.setToolTipText(Bundle.getMessage("ButtonSelectHexFileTooltip"));

        setSprogModeButton.setText(Bundle.getMessage("ButtonSetSPROGMode"));
        setSprogModeButton.setVisible(true);
        setSprogModeButton.setEnabled(false);
        setSprogModeButton.setToolTipText(Bundle.getMessage("ButtonSetSPROGModeTooltip"));

        statusBar.setVisible(true);
        statusBar.setText(" ");
        statusBar.setHorizontalTextPosition(SwingConstants.LEFT);

        setTitle(title());
        getContentPane().setLayout(new BoxLayout(getContentPane(),
                BoxLayout.Y_AXIS));

        JPanel paneA = new JPanel();
        paneA.setLayout(new BoxLayout(paneA, BoxLayout.Y_AXIS));

        JPanel buttons1 = new JPanel();
        buttons1.setLayout(new BoxLayout(buttons1, BoxLayout.X_AXIS));
        buttons1.add(openFileChooserButton);
        buttons1.add(programButton);

        JPanel buttons2 = new JPanel();
        buttons2.setLayout(new BoxLayout(buttons2, BoxLayout.X_AXIS));
        buttons2.add(setSprogModeButton);

        JPanel status = new JPanel();
        status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
        status.add(statusBar);

        paneA.add(buttons1);
        paneA.add(buttons2);
        paneA.add(status);

        getContentPane().add(paneA);

        openFileChooserButton.addActionListener((java.awt.event.ActionEvent e) -> {
            openFileChooserButtonActionPerformed(e);
        });

        programButton.addActionListener((java.awt.event.ActionEvent e) -> {
            programButtonActionPerformed(e);
        });

        setSprogModeButton.addActionListener((java.awt.event.ActionEvent e) -> {
            setSprogModeButtonActionPerformed(e);
        });

        // connect to data source
        init();

        // Don't connect to help here, let the subclasses do it
        // prevent button areas from expanding
        pack();
        paneA.setMaximumSize(paneA.getSize());
//        pack();
    }

    @Override
    public void notifyMessage(SprogMessage m) {
    }

    /**
     * State machine to catch replies that calls functions to handle each state.
     * <p>
     * These functions can be overridden for each SPROG type.
     *
     * @param m the SprogReply received from the SPROG
     */
    @Override
    synchronized public void notifyReply(SprogReply m) {
        reply = m;
        frameCheck();
        replyString = m.toString();
        switch (bootState) {
            case IDLE:
                stateIdle();
                break;
            case SETBOOTSENT:           // awaiting reply from bootloader
                stateSetBootSent();
                break;
            case VERREQSENT:            // awaiting reply to version request
                stateBootVerReqSent();
                break;
            case WRITESENT:             // write flash command sent, waiting reply
                stateWriteSent();
                break;
            case ERASESENT:             // erase sent
                stateEraseSent();
                break;
            case SPROGMODESENT:         // enable sprog mode sent
                stateSprogModeSent();
                break;
            case RESETSENT:             // reset sent
                stateResetSent();
                break;
            case EOFSENT:               // v4 end of file sent
                stateEofSent();
                break;
            case V4RESET:               // wait for v4 to reset
                stateV4Reset();
                break;
            default:
                stateDefault();
                break;
        }
    }

    protected void frameCheck() {
    }

    protected void stateIdle() {
        if (log.isDebugEnabled()) {
            log.debug("reply in IDLE state");
        }
    }

    protected void stateSetBootSent() {
    }

    protected void stateBootVerReqSent() {
    }

    protected void stateWriteSent() {
    }

    protected void stateEraseSent() {
    }

    protected void stateSprogModeSent() {
    }

    protected void stateResetSent() {
    }

    protected void stateEofSent() {
    }

    protected void stateV4Reset() {
    }

    synchronized protected void stateDefault() {
        // Houston, we have a problem
        if (log.isDebugEnabled()) {
            log.debug("Reply in unknown state");
        }
        bootState = BootState.IDLE;
        tc.setSprogState(SprogState.NORMAL);
    }

    // Normally this happens well before the transfer thread
    // is kicked off, but it's synchronized anyway to control
    // access to shared hexFile variable.
    synchronized public void openFileChooserButtonActionPerformed(java.awt.event.ActionEvent e) {
        // start at current file, show dialog
        int retVal = hexFileChooser.showOpenDialog(this);

        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            hexFile = new SprogHexFile(hexFileChooser.getSelectedFile().getPath());
            if (log.isDebugEnabled()) {
                log.debug("hex file chosen: " + hexFile.getName());
            }
            if ((!hexFile.getName().contains("sprog"))) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("HexFileSelectDialogString"),
                        Bundle.getMessage("HexFileSelectTitle"), JOptionPane.ERROR_MESSAGE);
                hexFile = null;
            } else {
                hexFile.openRd();
                programButton.setEnabled(true);
            }
        }
    }

    public synchronized void programButtonActionPerformed(java.awt.event.ActionEvent e) {
    }

    public void setSprogModeButtonActionPerformed(java.awt.event.ActionEvent e) {
    }

    abstract protected void requestBoot();

    abstract protected void sendWrite();

    abstract protected void doneWriting();

    /**
     * Internal routine to handle a timeout.
     */
    synchronized protected void timeout() {
        if ((bootState == BootState.CRSENT) || (bootState == BootState.SETBOOTSENT)) {
            log.debug("timeout in CRSENT - assuming boot mode");
            // Either:
            // 1) We were looking for a SPROG in normal mode but have had no reply
            // so maybe it was already in boot mode.
            // 2) We sent the b command and had an extected timeout
            // In both cases, try looking for bootloader version
            requestBoot();
        } else if (bootState == BootState.VERREQSENT) {
            log.error("timeout in VERREQSENT!");
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorConnectingDialogString"),
                    Bundle.getMessage("FatalErrorTitle"), JOptionPane.ERROR_MESSAGE);
            statusBar.setText(Bundle.getMessage("ErrorConnectingStatus"));
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        } else if (bootState == BootState.WRITESENT) {
            log.error("timeout in WRITESENT!");
            // This is fatal!
            JOptionPane.showMessageDialog(this, Bundle.getMessage("ErrorTimeoutDialogString"),
                    Bundle.getMessage("FatalErrorTitle"), JOptionPane.ERROR_MESSAGE);
            statusBar.setText(Bundle.getMessage("ErrorTimeoutStatus"));
            bootState = BootState.IDLE;
            tc.setSprogState(SprogState.NORMAL);
        } else if (bootState == BootState.NULLWRITE) {
            if (hexFile.read() > 0) {
                // More data to write
                sendWrite();
            } else {
                doneWriting();
            }
        }
    }

    protected int V_SHORT_TIMEOUT = 5;
    protected int SHORT_TIMEOUT = 500;
    protected int LONG_TIMEOUT = 4000;

    javax.swing.Timer timer = null;

    /**
     * Internal routine to start very short timer for null writes.
     */
    protected void startVShortTimer() {
        restartTimer(V_SHORT_TIMEOUT);
    }

    /**
     * Internal routine to start timer to protect the mode-change.
     */
    protected void startShortTimer() {
        restartTimer(SHORT_TIMEOUT);
    }

    /**
     * Internal routine to restart timer with a long delay.
     */
    synchronized protected void startLongTimer() {
        restartTimer(LONG_TIMEOUT);
    }

    /**
     * Internal routine to stop timer, as all is well.
     */
    synchronized protected void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /**
     * Internal routine to handle timer starts {@literal &} restarts.
     * 
     * @param delay milliseconds until action
     */
    synchronized protected void restartTimer(int delay) {
        if (timer == null) {
            timer = new javax.swing.Timer(delay, (java.awt.event.ActionEvent e) -> {
                timeout();
            });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(false);
        timer.start();
    }

    private final static Logger log = LoggerFactory
            .getLogger(SprogUpdateFrame.class);
}
