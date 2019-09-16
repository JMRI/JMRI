package jmri.jmrix.loconet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Display and modify an Digitrax board configuration.
 * <p>
 * Supports boards which can be read and write using LocoNet opcode
 * OPC_MULTI_SENSE, such as PM4x, DS64, SE8c, BDL16x.
 * <p>
 * The read and write require a sequence of operations, which we handle with a
 * state variable.
 * <p>
 * Each read or write OpSw access requires a response from the addressed board.
 * If a response is not received within a fixed time, then the process will
 * repeat the read or write OpSw access up to MAX_OPSW_ACCESS_RETRIES additional
 * times to try to get a response from the addressed board. If the board does
 * not respond, the access sequence is aborted and a failure message is
 * populated in the "status" variable.
 * <p>
 * Programming of the board is done via configuration messages, so the board
 * should not be put into programming mode via the built-in pushbutton while
 * this tool is in use.
 * <p>
 * Throughout, the terminology is "closed" == true, "thrown" == false. Variables
 * are named for their closed state.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 * @author B. Milhaupt  Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016, 2017
 */
abstract public class AbstractBoardProgPanel extends jmri.jmrix.loconet.swing.LnPanel
        implements LocoNetListener {

    JPanel contents = new JPanel();

    public JToggleButton readAllButton = null;
    public JToggleButton writeAllButton = null;
    public JTextField addrField = new JTextField(4);
    JLabel status = new JLabel();

    public boolean read = false;
    public int state = 0;
    boolean awaitingReply = false;
    int replyTryCount = 0;

    /* The responseTimer provides a timeout mechanism for OpSw read and write
     * requests.
     */
    public javax.swing.Timer responseTimer = null;

    /* The pacing timer is used to reduce the speed of this tool's requests to
     * LocoNet.
     */
    public javax.swing.Timer pacingTimer = null;

    /* The boolean field onlyOneOperation is intended to allow accesses to
     * a single OpSw value at a time.  This is un-tested functionality.
     */
    public boolean onlyOneOperation = false;
    int address = 0;

    /* typeWord provides the encoded device type number, and is used within the
     * LocoNet OpSw Read and Write request messages.  Different Digitrax boards
     *  respond to different encoded device type values, as shown here:
     *      PM4/PM42                0x70
     *      BDL16/BDL162/BDL168     0x71
     *      SE8C                    0x72
     *      DS64                    0x73
     */
    int typeWord;

    boolean readOnInit;

    /**
     * True is "closed", false is "thrown". This matches how we do the check
     * boxes also, where we use the terminology for the "closed" option. Note
     * that opsw[0] is not a legal OpSwitch.
     */
    protected boolean[] opsw = new boolean[65];
    private final static int HALF_A_SECOND = 500;
    private final static int FIFTIETH_OF_A_SECOND = 20; // 20 milliseconds = 1/50th of a second

    private String boardTypeName;

    /**
     * Constructor which accepts a "board type" string.
     * The board number defaults to 1, and the board will not
     * be automatically read.
     *
     * @param boardTypeName  device type name, to be included in read and write GUI buttons
     */
    protected AbstractBoardProgPanel(String boardTypeName) {
        this(1, false, boardTypeName);
    }

    /**
     * Constructor which accepts a boolean which specifies whether
     * to automatically read the board, plus a string defining
     * the "board type".  The board number defaults to 1.
     *
     * @param readOnInit  true to read OpSw values of board 1 upon panel creation
     * @param boardTypeName  device type name, to be included in read and write GUI buttons
     */
    protected AbstractBoardProgPanel(boolean readOnInit, String boardTypeName) {
        this(1, readOnInit, boardTypeName);
    }

    /**
     * Constructor which accepts parameters for the initial board number, whether
     * to automatically read the board, and a "board type" string.
     *
     * @param boardNum  default board ID number upon panel creation
     * @param readOnInit  true to read OpSw values of board 1 upon panel creation
     * @param boardTypeName  device type name, to be included in read and write GUI buttons
     */
    protected AbstractBoardProgPanel(int boardNum, boolean readOnInit, String boardTypeName) {
        super();
        this.boardTypeName = boardTypeName;

        // basic formatting: Create pane to hold contents
        // within a scroll box
        contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));

        // and prep for display
        addrField.setText(Integer.toString(boardNum));
        this.readOnInit = readOnInit;
    }

    /**
     * Constructor which allows the caller to pass in the board ID number
     * and board type name
     *
     * @param boardNum  default board ID number upon panel creation
     * @param boardTypeName  device type name, to be included in read and write GUI buttons
     */
    protected AbstractBoardProgPanel(int boardNum, String boardTypeName) {
        this(boardNum, false, boardTypeName);
    }

    /**
     * In order to get the scrollpanel on the screen it must be added at the end when
     * all components and sub panels have been added to the one panel.
     * This must be called as the last thing in the initComponents.
     */
    protected void panelToScroll() {
        JScrollPane scroll = new JScrollPane(contents);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setLayout(new BorderLayout()); //!! added
        add(scroll,BorderLayout.CENTER);
        setVisible(true);
    }

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);

        // listen for message traffic
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().addLocoNetListener(~0, this);
            if (readOnInit == true) {
                readAllButton.setSelected(true);
                readAllButton.updateUI();
                readAll();
            }
        } else {
            log.error("No LocoNet connection available, this tool cannot function"); // NOI18N
        }
    }

    @Override
    public void initComponents() {
        initializeResponseTimer();
        initializePacingTimer();
    }

    /**
     * Set the Board ID number (also known as board address number)
     *
     * @param boardId  board ID number to be accessed
     */
    public void setBoardIdValue(Integer boardId) {
        /*
        * For device types where the valid range of Board ID numbers is different
        * than implemented here (1 to 256, inclusive), this method should be
        * overridden with appropriate range limits.
        */

        if (boardId < 1) {
            return;
        }
        if (boardId > 256) {
            return;
        }
        addrField.setText(Integer.toString(boardId));
        address = boardId - 1;
    }

    public Integer getBoardIdValue() {
        return Integer.parseInt(addrField.getText());
    }

    /**
     * Creates a JPanel to allow the user to specify a board address.  Includes
     * a previously-defined board type name within the panel, or, if none has
     * been previously provided, a default board-type name.
     *
     * @return a JPanel with address entry
     */
    protected JPanel provideAddressing() {
        return this.provideAddressing(boardTypeName);
    }

    /**
     * Creates a JPanel to allow the user to specify a board address and to
     * read and write the device.  The "read" and "write" buttons have text which
     * uses the specified "board type name" from the method parameter.
     *
     * @param boardTypeName  device type name, to be included in read and write GUI buttons
     * @return JPanel containing a JTextField and read and write JButtons
     */
    protected JPanel provideAddressing(String boardTypeName) {
        JPanel pane0 = new JPanel();
        pane0.setLayout(new FlowLayout());
        pane0.add(new JLabel(Bundle.getMessage("LABEL_UNIT_ADDRESS") + " "));
        pane0.add(addrField);
        readAllButton = new JToggleButton(Bundle.getMessage("AbstractBoardProgPanel_ReadFrom", boardTypeName));
        writeAllButton = new JToggleButton(Bundle.getMessage("AbstractBoardProgPanel_WriteTo", boardTypeName));

        // make both buttons a little bit bigger, with identical (preferred) sizes
        // (width increased because some computers/displays trim the button text)
        java.awt.Dimension d = writeAllButton.getPreferredSize();
        int w = d.width;
        d = readAllButton.getPreferredSize();
        if (d.width > w) {
            w = d.width;
        }
        writeAllButton.setPreferredSize(new java.awt.Dimension((int) (w * 1.1), d.height));
        readAllButton.setPreferredSize(new java.awt.Dimension((int) (w * 1.1), d.height));

        pane0.add(readAllButton);
        pane0.add(writeAllButton);

        // install read all, write all button handlers
        readAllButton.addActionListener((ActionEvent a) -> {
            if (readAllButton.isSelected()) {
                readAll();
            }
        });
        writeAllButton.addActionListener((ActionEvent a) -> {
            if (writeAllButton.isSelected()) {
                writeAll();
            }
        });
        return pane0;
    }

    /**
     * Create the status line for the GUI.
     *
     * @return JComponent which will display status updates
     */
    protected JComponent provideStatusLine() {
        status.setFont(status.getFont().deriveFont(0.9f * addrField.getFont().getSize())); // a bit smaller
        status.setForeground(Color.gray);
        return status;
    }

    /**
     * Update the status line.
     *
     * @param msg  to be displayed on the status line
     */
    protected void setStatus(String msg) {
        status.setText(msg);
    }

    /**
     * Handle GUI layout details during construction.
     * Adds items as lines onto JPanel.
     *
     * @param c component to put on a single line
     */
    protected void appendLine(JComponent c) {
        c.setAlignmentX(0.f);
        contents.add(c);
    }

    /**
     * Provides a mechanism to read several OpSw values in a sequence. The
     * sequence is defined by the {@link #nextState(int)} method.
     */
    public void readAll() {
        // check the address
        try {
            setAddress(256);
        } catch (Exception e) {
            log.debug(Bundle.getMessage("ERROR_READALL_INVALID_ADDRESS"));
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText(" ");
            return;
        }
        if (responseTimer == null) {
            initializeResponseTimer();
        }
        if (pacingTimer == null) {
            initializePacingTimer();
        }
        // Start the first operation
        read = true;
        state = 1;
        nextRequest();
    }

    /**
     * Configure the type word in the LocoNet messages.
     * <p>
     * Known values:
     * <ul>
     *   <li>0x70 - PM4
     *   <li>0x71 - BDL16
     *   <li>0x72 - SE8
     *   <li>0x73 - DS64
     * </ul>
     *
     * @param type board type number, per list above
     */
    protected void setTypeWord(int type) {
        typeWord = type;
    }

    /**
     * Triggers the next read or write request. Is executed by the "pacing"
     * delay timer, which allows time between any two OpSw accesses.
     */
    private final void delayedNextRequest() {
        pacingTimer.stop();
        if (read) {
            // read op
            status.setText(Bundle.getMessage("STATUS_READING_OPSW") + " " + state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(LnConstants.OPC_MULTI_SENSE);
            int element = 0x62;
            if ((address & 0x80) != 0) {
                element |= 1;
            }
            l.setElement(1, element);
            l.setElement(2, address & 0x7F);
            l.setElement(3, typeWord);
            int loc = (state - 1) / 8;
            int bit = (state - 1) - loc * 8;
            l.setElement(4, loc * 16 + bit * 2);
            memo.getLnTrafficController().sendLocoNetMessage(l);
            awaitingReply = true;
            responseTimer.stop();
            responseTimer.restart();
        } else {
            //write op
            status.setText(Bundle.getMessage("STATUS_WRITING_OPSW") + " " + state);
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(LnConstants.OPC_MULTI_SENSE);
            int element = 0x72;
            if ((address & 0x80) != 0) {
                element |= 1;
            }
            l.setElement(1, element);
            l.setElement(2, address & 0x7F);
            l.setElement(3, typeWord);
            int loc = (state - 1) / 8;
            int bit = (state - 1) - loc * 8;
            l.setElement(4, loc * 16 + bit * 2 + (opsw[state] ? 1 : 0));
            memo.getLnTrafficController().sendLocoNetMessage(l);
            awaitingReply = true;
            responseTimer.stop();
            responseTimer.restart();
        }
    }

    /**
     * Start the pacing timer, which, at timeout, will begin the next OpSw
     * access request.
     */
    private final void nextRequest() {
        pacingTimer.stop();
        pacingTimer.restart();
        replyTryCount = 0;
    }

    /**
     * Convert the GUI text field containing the address into a valid integer
     * address, and handles user-input errors as needed.
     *
     * @param maxValid highest Board ID number allowed for the given device type
     * @throws jmri.JmriException when the board address is invalid
     */
    void setAddress(int maxValid) throws jmri.JmriException {
        try {
            address = (Integer.parseInt(addrField.getText()) - 1);
        } catch (NumberFormatException e) {
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText(Bundle.getMessage("STATUS_INPUT_BAD"));
            JOptionPane.showMessageDialog(this, Bundle.getMessage("STATUS_INVALID_ADDRESS"),
                    Bundle.getMessage("STATUS_TYPE_ERROR"), JOptionPane.ERROR_MESSAGE);
            log.error(Bundle.getMessage("ERROR_PARSING_ADDRESS") + " " + e);
            throw e;
        }
        // parsed OK, check range
        if (address > (maxValid - 1) || address < 0) {
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText(Bundle.getMessage("STATUS_INPUT_BAD"));
            String message = Bundle.getMessage("AbstractBoardProgPanel_ErrorAddressRange", 1, maxValid);
            JOptionPane.showMessageDialog(this, message,
                    "Error", JOptionPane.ERROR_MESSAGE); // NOI18N
            log.error("Invalid board ID number: {}", Integer.toString(address)); // NOI18N
            throw new jmri.JmriException(Bundle.getMessage("ERROR_INVALID_ADDRESS") + " " + address);
        }
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before a write operation is started.
     */
    abstract protected void copyToOpsw();

    /**
     * Update the GUI based on the contents of opsw[].
     * <p>
     * This method is executed after completion of a read operation sequence.
     */
    abstract protected void updateDisplay();

    /**
     * Compute the next OpSw number to be accessed, based on the current OpSw number.
     *
     * @param state current OpSw number
     * @return computed next OpSw nubmer
     */
    abstract protected int nextState(int state);

    /**
     * Provide a mechanism to write several OpSw values in a sequence. The
     * sequence is defined by the {@link #nextState(int)} method.
     */
    public void writeAll() {
        // check the address
        try {
            setAddress(256);
        } catch (Exception e) {
            log.debug(Bundle.getMessage("ERROR_WRITEALL_ABORTED") + " " + e);
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText(" "); // NOI18N
            return;
        }

        if (responseTimer == null) {
            initializeResponseTimer();
        }
        if (pacingTimer == null) {
            initializePacingTimer();
        }

        // copy over the display
        copyToOpsw();

        // start the first operation
        read = false;
        state = 1;
        // specify as single request, not multiple
        onlyOneOperation = false;
        nextRequest();
    }

    /**
     * writeOne() is intended to provide a mechanism to write a single OpSw
     * value, rather than a sequence of OpSws as done by writeAll().  The value
     * to be written is taken from the appropriate entry in booleans[].
     *
     * @see jmri.jmrix.loconet.AbstractBoardProgPanel#writeAll()
     * @param opswIndex  OpSw number
     */
    public void writeOne(int opswIndex) {
        // check the address
        try {
            setAddress(256);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(Bundle.getMessage("ERROR_WRITEONE_ABORTED") + " " + e);
            }
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            status.setText(" ");
            return;
        }

        // copy over the displayed value
        copyToOpsw();

        // Start the first operation
        read = false;
        state = opswIndex;

        // specify as single request, not multiple
        onlyOneOperation = true;
        nextRequest();
    }

    /**
     * Processes incoming LocoNet message m for OpSw responses to read and write
     * operation messages, and automatically advances to the next OpSw operation
     * as directed by {@link #nextState(int)}.
     *
     *@param m  incoming LocoNet message
     */
    @Override
    public void message(LocoNetMessage m) {
        if (log.isDebugEnabled()) {
            log.debug(Bundle.getMessage("DEBUG_PARSING_LOCONET_MESSAGE") + " " + m);
        }
        // are we reading? If not, ignore
        if (state == 0) {
            return;
        }
        // check for right type, unit
        if ((m.getOpCode() != LnConstants.OPC_LONG_ACK)
                || ((m.getElement(1) != 0x00) && (m.getElement(1) != 0x50))) {
            return;
        }

        // LACK with 0 in opcode; assume its to us.  Note that there
        // should be a 0x50 in the opcode, not zero, but this is what we
        // see...
        if (awaitingReply == true) {
            if (responseTimer != null) {
                if (responseTimer.isRunning()) {
                    // stop the response timer!
                    responseTimer.stop();
                }
            }
        }

        boolean value = false;
        if ((m.getElement(2) & 0x20) != 0) {
            value = true;
        }

        // update opsw array if LACK return status is not 0x7F
        if ((m.getElement(2) != 0x7f)) {
            // record this bit
            opsw[state] = value;
        }

        // show what we've got so far
        if (read) {
            updateDisplay();
        }

        // and continue through next state, if any
        doTheNextThing();
    }

    /**
     * Helps continue sequences of OpSw accesses.
     * <p>
     * Handles aborting a sequence of reads or writes when the GUI Read button
     * or the GUI Write button (as appropriate for the current operation) is
     * de-selected.
     */
    public void doTheNextThing() {
        int origState;
        origState = state;
        if (origState != 0) {
            state = nextState(origState);
        }
        if ((origState == 0) || (state == 0)) {
            // done with sequence
            readAllButton.setSelected(false);
            writeAllButton.setSelected(false);
            if (origState != 0) {
                status.setText(Bundle.getMessage("AbstractBoardProgPanel_Done_Message"));
            } else {
                status.setText(Bundle.getMessage("ERROR_ABORTED_DUE_TO_TIMEOUT"));
            }
            // nothing more to do
        } else {
            // are not yet done, so create and send the next OpSw request message
            nextRequest();
        }
    }

    private ActionListener responseTimerListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (responseTimer.isRunning()) {
                // odd case - not sure why would get an event if the timer is not running.
            } else {
                if (awaitingReply == true) {
                    // Have a case where are awaiting a reply from the device,
                    // but the response timer has expired without a reply.

                    if (replyTryCount < MAX_OPSW_ACCESS_RETRIES) {
                        // have not reached maximum number of retries, so try
                        // the access again
                        replyTryCount++;
                        log.debug("retrying({}) access to OpSw{}", replyTryCount, state); // NOI18N
                        responseTimer.stop();
                        delayedNextRequest();
                        return;
                    }

                    // Have reached the maximum number of retries for accessing
                    // a given OpSw.
                    // Cancel the ongoing process and update the status line.
                    log.warn("Reached OpSw access retry limit of {} when accessing OpSw{}", MAX_OPSW_ACCESS_RETRIES, state); // NOI18N
                    awaitingReply = false;
                    responseTimer.stop();
                    state = 0;
                    replyTryCount = 0;
                    doTheNextThing();
                }
            }
        }
    };

    private void initializeResponseTimer() {
        if (responseTimer == null) {
            responseTimer = new javax.swing.Timer(HALF_A_SECOND, responseTimerListener);
            responseTimer.setRepeats(false);
            responseTimer.stop();
            responseTimer.setInitialDelay(HALF_A_SECOND);
            responseTimer.setDelay(HALF_A_SECOND);
        }
    }

    private ActionListener pacingTimerListener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (pacingTimer.isRunning()) {
                // odd case - not sure why would get an event if the timer is not running.
                log.warn("Unexpected pacing timer event while OpSw access timer is running."); // NOI18N
            } else {
                pacingTimer.stop();
                delayedNextRequest();
            }
        }
    };

    private void initializePacingTimer() {
        if (pacingTimer == null) {
            pacingTimer = new javax.swing.Timer(FIFTIETH_OF_A_SECOND, pacingTimerListener);
            pacingTimer.setRepeats(false);
            pacingTimer.stop();
            pacingTimer.setInitialDelay(FIFTIETH_OF_A_SECOND);
            pacingTimer.setDelay(FIFTIETH_OF_A_SECOND);
        }
    }

    @Override
    public void dispose() {
        // Drop LocoNet connection
        if (memo.getLnTrafficController() != null) {
            memo.getLnTrafficController().removeLocoNetListener(~0, this);
        }
        super.dispose();

        // stop all timers (if necessary) before disposing of this class
        if (responseTimer != null) {
            responseTimer.stop();
        }
        if (pacingTimer != null) {
            pacingTimer.stop();
        }
    }

    // maximum number of additional retries after board does not respond to
    // first attempt to access a given OpSw
    private final int MAX_OPSW_ACCESS_RETRIES = 2;

    private final static Logger log = LoggerFactory.getLogger(AbstractBoardProgPanel.class);

}