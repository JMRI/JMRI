package jmri.jmrix.nce.cab;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.Calendar;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceCmdStationMemory;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemorySerial;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemoryUsb;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to display NCE cabs
 * <p>
 * Note, NCE bit layout MSB = bit 7, LSB = bit 0.
 * <p>
 * From Jim Scorse at NCE:
 * <p>
 * Each cab has a 256 byte "context page" in system RAM These pages start at
 * 0x8000 in system RAM with Cab 0 at 0x8800, cab 1 at 0x8900, Cab 2 at 0x8a00,
 * etc.
 * <p>
 * Below is a list of offsets (in decimal) into the cab context page for useful
 * memory locations.
 * <p>
 * For example if you want to know the current speed of cab address 2's
 * currently selected loco look at memory location 0x8a00 + 32 dec (0x20). This
 * will be address 0x8a20.
 * <p>
 * <br>
 * To determine if a cab is active (plugged in at any point this session) you
 * will need to look at the byte "FLAGS1" (offset 101)
 * <p>
 * If bit 1 of FLAGS1 = 1 then the cab has been talked to at least once by the
 * command station.
 * <p>
 * Bits 0 and 7 indicate the type of cab being use this session at this cab
 * address
 * <p>
 * Bit 7,0 = 0,0 Procab or other cab with an LCD display (type A) Bit 7,0 = 0,1
 * Cab04 other cab without an LCD (type B) Bit 7,0 = 1,0 USB or similar device
 * (type C) Bit 7,0 = 1,1 AIU or similar device (type D)
 * <p>
 * <br>
 * CAB_BASE EQU 0 ; LCD_TOP_LINE EQU 0 ;16 chars (in ASCII) for top line of LCD
 * LCD_BOT_LINE EQU 16 ;16 chars (in ASCII) for bottom line of LCD
 * <p>
 * CURR_SPEED EQU 32 ;this cab's current speed ADDR_H EQU 33 ;loco address, high
 * byte ADDR_L EQU 34 ;loco address, low byte FLAGS EQU 35 ;bit 0 - Do not use
 * ;bit 1 - 1=128 speed mode, 0=28 speed mode ;bit 2 - 1=forward, 0=reverse ;bit
 * 3 - Do not use ;bit 4 - Do not use ;bit 5 - Do not use ;bit 6 - Do not use
 * ;bit 7 - 1=rear loco of consist is active address use reverse speeds
 * <p>
 * FUNCTION_L EQU 36 ;bit 0 = function 1, 1=on, 0=off ;bit 1 = function 2, 1=on,
 * 0=off ;bit 2 = function 3, 1=on, 0=off ;bit 3 = function 4, 1=on, 0=off ;bit
 * 4 = headlight, 1=on, 0=off
 * <p>
 * FUNCTION_H EQU 37 ;bit 0 = function 5, 1=on, 0=off ;bit 1 = function 6, 1=on,
 * 0=off ;bit 2 = function 7, 1=on, 0=off ;bit 3 = function 8, 1=on, 0=off ;bit
 * 4 = function 9, 1=on, 0=off ;bit 5 = function 10, 1=on, 0=off ;bit 6 =
 * function 11, 1=on, 0=off ;bit 7 = function 12, 1=on, 0=off
 * <p>
 * ALIAS EQU 38 ;If loco is in consist this is the consist address
 * <p>
 * <br>
 * FUNC13_20 EQU 82 ;bit map of current functions (bit 0=F13) FUNC21_28 EQU 83
 * ;bit map of current functions (bit 0=F21)
 * <p>
 * ACC_AD_H EQU 90 ;current accessory address high byte ACC_AD_L EQU 91 ;current
 * accessory address low byte
 * <p>
 * ;lower nibble bit 0 =1 if setup advanced consist in process
 * <p>
 * FLAGS2 EQU 93 ;bit 0 = \ ;bit 1 = {@literal >}Number of recalls for this cab
 * ;bit 2 = / 1-6 valid ;bit 3 = 1=refresh LCD on ProCab ;bit 4 = Do not use
 * ;bit 5 = Do not use ;bit 6 = Do not use ;bit 7 = Do not use
 * <p>
 * FLAGS1 EQU 101 ;bit0 - 0 = type a or type C cab, 1 = type b or type d ;bit1 -
 * 0 = cab type not determined, 1 = it has ;bit2 - 0 = Do not use ;bit3 - 0 = Do
 * not use ;bit4 - 0 = Do not use ;bit5 - 0 = Do not use ;bit6 - 0 = Do not use
 * ;bit7 - 0 = type a or type b cab, 1=type c or d Writing zero to FLAGS1 will
 * remove the cab from the 'active' list
 *
 * @author Dan Boudreau Copyright (C) 2009, 2010
 * @author Ken Cameron Copyright (C) 2012, 2013
 */
public class NceShowCabPanel extends jmri.jmrix.nce.swing.NcePanel implements jmri.jmrix.nce.NceListener {

    private int replyLen = 0; // expected byte length
    private int waiting = 0; // to catch responses not
    // intended for this module
    private int minCabNum = -1; // either the USB or serial size depending on what we connect to
    private int maxCabNum = -1; // either the USB or serial size depending on what we connect to

    private static final int FIRST_TIME_SLEEP = 3000; // delay first operation to let panel build

    private static final int CAB_MIN_USB = 2; // USB cabs start at 2
    private static final int CAB_MIN_PRO = 1; // Serial cabs start at 1
    private static final int CAB_MAX_USB = 10; // There are up to 10 cabs
    private static final int CAB_MAX_PRO = 65; // There are up to 64 cabs plus the serial computer cab
    private static final int CAB_LINE_LEN = 16; // display line length of 16 bytes
    private static final int CAB_MAX_CABDATA = 66; // Size for arrays. One more than highest cab number

    Thread nceCabUpdateThread;
    Thread autoRefreshThread;

    private final int[] cabFlag1Array = new int[CAB_MAX_CABDATA];
    private final Calendar[] cabLastChangeArray = new Calendar[CAB_MAX_CABDATA];
    private final int[] cabSpeedArray = new int[CAB_MAX_CABDATA];
    private final int[] cabFlagsArray = new int[CAB_MAX_CABDATA];
    private final int[] cabLocoArray = new int[CAB_MAX_CABDATA];
    private final boolean[] cabLongShortArray = new boolean[CAB_MAX_CABDATA];
    private final int[] cabConsistArray = new int[CAB_MAX_CABDATA];
    private final int[] cabF0Array = new int[CAB_MAX_CABDATA];
    private final int[] cabF5Array = new int[CAB_MAX_CABDATA];
    private final int[] cabF13Array = new int[CAB_MAX_CABDATA];
    private final int[] cabF21Array = new int[CAB_MAX_CABDATA];
    private final int[][] cabLine1Array = new int[CAB_MAX_CABDATA][CAB_LINE_LEN];
    private final int[][] cabLine2Array = new int[CAB_MAX_CABDATA][CAB_LINE_LEN];

    private boolean purgeRequested = false;
    private boolean updateRequested = false;
    private int purgeCabId = -1;

    // member declarations
    JLabel textNumber = new JLabel(Bundle.getMessage("Number"));
    JLabel textCab = new JLabel(Bundle.getMessage("Type"));
    JLabel textAddrType = new JLabel(Bundle.getMessage("AddrType"));
    JLabel textAddress = new JLabel(Bundle.getMessage("Loco"));
    JLabel textSpeed = new JLabel(Bundle.getMessage("Speed"));
    JLabel textConsist = new JLabel(Bundle.getMessage("Consist"));
    JLabel textConsistPos = new JLabel(Bundle.getMessage("ConsistPos"));
    JLabel textFunctions = new JLabel(Bundle.getMessage("Functions"));
    JLabel textDisplay1 = new JLabel(Bundle.getMessage("Display1"));
    JLabel textDisplay2 = new JLabel(Bundle.getMessage("Display2"));
    JLabel textReply = new JLabel(Bundle.getMessage("Reply"));
    JLabel textStatus = new JLabel("");
    JLabel textLastUsed = new JLabel(Bundle.getMessage("LastUsed"));

    // major buttons
    JButton refreshButton = new JButton(Bundle.getMessage("Refresh"));

    // check boxes
    JCheckBox checkBoxShowAllCabs = new JCheckBox(Bundle.getMessage("CheckBoxLabelShowAllCabs"));
    //    JCheckBox checkBoxShowDisplayText = new JCheckBox(Bundle.getMessage("CheckBoxLabelShowDisplayText"));
    //    JCheckBox checkBoxShowAllFunctions = new JCheckBox(Bundle.getMessage("CheckBoxLabelShowAllFunctions"));
    JCheckBox checkBoxAutoRefresh = new JCheckBox(Bundle.getMessage("CheckBoxLabelAutoRefresh"));

    static class DataRow {

        int cabNumber;
        String cabType;
        String longShort;
        int locoAddress;
        int locoSpeed;
        String locoDir;
        String mode;
        int consist;
        String consistPos;
        boolean F0;
        boolean F1;
        boolean F2;
        boolean F3;
        boolean F4;
        boolean F5;
        boolean F6;
        boolean F7;
        boolean F8;
        boolean F9;
        boolean F10;
        boolean F11;
        boolean F12;
        boolean F13;
        boolean F14;
        boolean F15;
        boolean F16;
        boolean F17;
        boolean F18;
        boolean F19;
        boolean F20;
        boolean F21;
        boolean F22;
        boolean F23;
        boolean F24;
        boolean F25;
        boolean F26;
        boolean F27;
        boolean F28;
        String text1;
        String text2;
        String lastChange;
    }

    DataRow[] cabData = new DataRow[CAB_MAX_CABDATA];

    NceCabTableModel cabModel = new NceCabTableModel(cabData);
    JTable cabTable = new JTable(cabModel);

    private NceTrafficController tc = null;

    public NceShowCabPanel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initContext(Object context) {
        if (context instanceof NceSystemConnectionMemo) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.cab.NceShowCabFrame";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(Bundle.getMessage("Title"));
        return x.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initComponents(NceSystemConnectionMemo m) {
        this.memo = m;
        this.tc = m.getNceTrafficController();

        // fill in cab array
        minCabNum = CAB_MIN_PRO;
        maxCabNum = CAB_MAX_PRO;
        if ((tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)
                && (tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
            minCabNum = CAB_MIN_USB;
            maxCabNum = CAB_MAX_USB;
        }
        for (int i = minCabNum; i <= maxCabNum; i++) {
            cabData[i] = new DataRow();
        }
        // the following code sets the frame's initial state

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setPreferredSize(new Dimension(1000, 40));
        // row 1
        refreshButton.setToolTipText(Bundle.getMessage("RefreshToolTip"));
        addButtonAction(refreshButton);

        checkBoxShowAllCabs.setToolTipText(Bundle.getMessage("CheckBoxAllCabsToolTip"));
        checkBoxShowAllCabs.setSelected(false);
        addCheckBoxAction(checkBoxShowAllCabs);

        //        checkBoxShowAllFunctions.setToolTipText(Bundle.getMessage("CheckBoxShowAllFunctionsToolTip"));
        //        checkBoxShowAllFunctions.setSelected(true);
        //        checkBoxShowAllFunctions.setEnabled(false);
        //        checkBoxShowDisplayText.setToolTipText(Bundle.getMessage("CheckBoxShowDisplayToolTip"));
        //        checkBoxShowDisplayText.setSelected(true);
        //        checkBoxShowDisplayText.setEnabled(false);
        checkBoxAutoRefresh.setToolTipText(Bundle.getMessage("CheckBoxAutoRefreshToolTip"));
        checkBoxAutoRefresh.setSelected(false);
        addCheckBoxAction(checkBoxAutoRefresh);

        addItem(p1, refreshButton, 2, 1);
        addItem(p1, checkBoxAutoRefresh, 3, 1);
        addItem(p1, checkBoxShowAllCabs, 4, 1);
        //        addItem(p1, checkBoxShowAllFunctions, 6, 1);
        addItem(p1, textStatus, 2, 2);
        //        addItem(p1, checkBoxShowDisplayText, 6, 2);

        JScrollPane cabScrollPane = new JScrollPane(cabTable);
        cabTable.setFillsViewportHeight(true);
        cabTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        cabModel.setShowAllCabs(false);
        cabModel.setShowAllFunctions(true);
        cabModel.setShowCabDisplay(true);
        for (int col = 0; col < cabTable.getColumnCount(); col++) {
            int width = cabModel.getPreferredWidth(col);
            TableColumn c = cabTable.getColumnModel().getColumn(col);
            c.setPreferredWidth(width);
        }
        cabTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cabScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        cabScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        setColumnPurgeButton(cabTable, 2);
        add(p1);
        add(cabScrollPane);

        // pad out panel
        cabScrollPane.setVisible(true);

        refreshPanel();

    }

    // button actions
    public void buttonActionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == refreshButton) {
            refreshPanel();
        } else {
            log.error("unknown action performed: " + src);
        }
    }

    // checkboxes
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        Object src = ae.getSource();
        if (src == checkBoxShowAllCabs) {
            cabModel.setShowAllCabs(checkBoxShowAllCabs.isSelected());
            refreshPanel();
        } else if (src == checkBoxAutoRefresh) {
            autoRefreshPanel();
        } else {
            log.error("unknown checkbox action performed: " + src);
        }
    }

    public void purgeCab(int cab) {
        if (cab < minCabNum || cab > maxCabNum) {
            log.error(Bundle.getMessage("ErrorValueRange") + cab);
            return;
        }
        // if id is active
        int act = cabFlag1Array[cab] & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE;
        if (act != NceCmdStationMemory.FLAGS1_CABISACTIVE) {
            log.error(Bundle.getMessage("ErrorCabNotActive") + cab);
        }
        // clear bit for active and cab type details
        cabFlag1Array[cab] = 0;
        processMemory(true, true, cab);
    }

    /**
     * Refresh cab display every 4 seconds.
     */
    private void autoRefreshPanel() {
        if (checkBoxAutoRefresh.isSelected()) {
            autoRefreshThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        refreshPanel();
                        synchronized (this) {
                            try {
                                wait(4000); // 4 seconds
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    }
                }
            });
            autoRefreshThread.setName("NCE Show Cabs Auto Refresh");
            autoRefreshThread.setPriority(Thread.MIN_PRIORITY);
            autoRefreshThread.start();
        } else {
            autoRefreshThread.interrupt();
        }
    }

    private void refreshPanel() {
        processMemory(false, true, -1);
    }

    private void processMemory(boolean doPurge, boolean doUpdate, int cabId) {
        if (doPurge) {
            purgeRequested = true;
            purgeCabId = cabId;
        }
        if (doUpdate) {
            updateRequested = true;
        }
        // Set up a separate thread to access CS memory
        if (nceCabUpdateThread != null && nceCabUpdateThread.isAlive()) {
            return; // thread is already running
        }
        textStatus.setText(Bundle.getMessage("StatusProcessingMemory"));
        nceCabUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_NONE) {
                    if (purgeRequested) {
                        cabPurgeSerial();
                    }
                    if (updateRequested) {
                        cabUpdateSerial();
                    }
                } else {
                    if (purgeRequested) {
                        cabPurgeUsb();
                    }
                    if (updateRequested) {
                        cabUpdateUsb();
                    }
                }
            }
        });
        nceCabUpdateThread.setName(Bundle.getMessage("ThreadTitle"));
        nceCabUpdateThread.setPriority(Thread.MIN_PRIORITY);
        nceCabUpdateThread.start();
    }

    private boolean firstTime = false; // wait for panel to display

    public void cabPurgeSerial() {
        if (purgeCabId <= minCabNum || purgeCabId >= maxCabNum) {
            log.error("purgeCabId out of range: " + purgeCabId);
        }
        if (firstTime) {
            try {
                Thread.sleep(FIRST_TIME_SLEEP); // wait for panel to display
            } catch (InterruptedException e) {
                log.error("Thread unexpectedly interrupted", e);
            }
        }

        firstTime = false;
        // clear bit for active and cab type details
        cabFlag1Array[purgeCabId] = 0;
        writeCabMemory1(purgeCabId, CabMemorySerial.CAB_FLAGS1, 0);
        if (!waitNce()) {
            return;
        }
        textStatus.setText(MessageFormat.format(Bundle.getMessage("StatusCabPurged"), purgeCabId));
    }

    public void cabPurgeUsb() {
        if (purgeCabId <= minCabNum || purgeCabId >= maxCabNum) {
            log.error("purgeCabId out of range: " + purgeCabId);
        }
        if (firstTime) {
            try {
                Thread.sleep(FIRST_TIME_SLEEP); // wait for panel to display
            } catch (InterruptedException e) {
                log.error("Thread unexpectedly interrupted", e);
            }
        }

        firstTime = false;
        // clear bit for active and cab type details
        cabFlag1Array[purgeCabId] = 0;
        setUsbCabMemoryPointer(purgeCabId, CabMemoryUsb.CAB_FLAGS1);
        if (!waitNce()) {
            return;
        }
        writeUsbCabMemory1(0);
        if (!waitNce()) {
            return;
        }
        textStatus.setText(MessageFormat.format(Bundle.getMessage("StatusCabPurged"), purgeCabId));
    }

    // Thread to update cab info, allows the use of sleep or wait, for serial connection
    private void cabUpdateSerial() {

        if (firstTime) {
            try {
                Thread.sleep(FIRST_TIME_SLEEP); // wait for panel to display
            } catch (InterruptedException e) {
                log.error("Thread unexpectedly interrupted", e);
            }
        }

        firstTime = false;
        int cabsFound = 0;
        // build table of cabs
        for (int currCabId = minCabNum; currCabId <= maxCabNum; currCabId++) {

            textStatus.setText(MessageFormat.format(Bundle.getMessage("StatusProcessingCabId"), currCabId));
            cabData[currCabId].cabNumber = currCabId;
            int foundChange = 0;
            recChar = -1;
            // create cab type by reading the FLAGS1 byte
            readCabMemory1(currCabId, CabMemorySerial.CAB_FLAGS1);
            if (!waitNce()) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("ID = " + currCabId + " Read flag1 character " + recChar);
            }
            // test it really changed
            if (recChar != -1) {
                // save value for purge
                if (recChar != cabFlag1Array[currCabId]) {
                    foundChange++;
                    if (log.isDebugEnabled()) {
                        log.debug(currCabId + ": Flag1 " + recChar + "<->" + cabFlag1Array[currCabId]);
                    }
                }
                cabFlag1Array[currCabId] = recChar;
                if ((recChar & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE) != NceCmdStationMemory.FLAGS1_CABISACTIVE) {
                    // not active slot
                    continue;
                }
                if (currCabId >= 1 || !checkBoxShowAllCabs.isSelected()) {
                    cabsFound++;
                }
                int cabType = recChar & NceCmdStationMemory.FLAGS1_MASK_CABTYPE; // mask off don't care bits
                if (currCabId == CAB_MAX_PRO) {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeSerial");
                } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_DISPLAY) {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeProCab");
                } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_NODISP) {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeCab04"); // Cab04 or Cab06
                } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_USB) {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeUSB"); // USB or Mini-Panel
                } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_AIU) {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeAIU");
                } else {
                    cabData[currCabId].cabType = Bundle.getMessage("TypeUnknownCab") + ": " + recChar;
                }

                cabData[currCabId].cabNumber = currCabId;
                if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_AIU) {
                    // get the AIU data and map it to the function bits
                    readAiuData(currCabId);
                    if (!waitNce()) {
                        return;
                    }
                    processAiuData(currCabId, recChars);
                    //                 } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_USB) {
                    // I don't have anything to do for the USB at this time
                } else {
                    // read 16 bytes of memory, we'll use 7 of the 16
                    readCabMemory16(currCabId, CabMemorySerial.CAB_CURR_SPEED);
                    if (!waitNce()) {
                        return;
                    }
                    // read the Speed byte
                    int readChar = recChars[0];
                    if (cabSpeedArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Speed " + readChar + "<->" + cabSpeedArray[currCabId]);
                        }
                    }
                    cabSpeedArray[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Read speed character " + Integer.toString(readChar));
                    }
                    cabData[currCabId].locoSpeed = readChar;

                    // read the FLAGS byte
                    readChar = recChars[CabMemorySerial.CAB_FLAGS - CabMemorySerial.CAB_CURR_SPEED];
                    if (cabFlagsArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Flags " + readChar + "<->" + cabFlagsArray[currCabId]);
                        }
                    }
                    cabFlagsArray[currCabId] = readChar;
                    int direction = readChar & 0x04;
                    if (direction > 0) {
                        cabData[currCabId].locoDir = Bundle.getMessage("DirForward");
                    } else {
                        cabData[currCabId].locoDir = Bundle.getMessage("DirReverse");
                    }
                    int mode = readChar & 0x02;
                    // USB doesn't use the 28/128 bit
                    cabData[currCabId].mode = "";
                    if (cabType != NceCmdStationMemory.FLAGS1_CABTYPE_USB) {
                        if (mode > 0) {
                            cabData[currCabId].mode = "128";
                        } else {
                            cabData[currCabId].mode = "28";
                        }
                    }

                    // create loco address, read the high address byte
                    readChar = recChars[CabMemorySerial.CAB_ADDR_H - CabMemorySerial.CAB_CURR_SPEED];
                    if (log.isDebugEnabled()) {
                        log.debug("Read address high character " + readChar);
                    }
                    int locoAddress = (readChar & 0x3F) * 256;
                    boolean aType = ((readChar & 0xC0) == 0xC0);
                    if (cabLongShortArray[currCabId] != aType) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Long " + aType + "<->" + cabLongShortArray[currCabId]);
                        }
                    }
                    cabLongShortArray[currCabId] = aType;
                    if (aType) {
                        cabData[currCabId].longShort = Bundle.getMessage("IsLongAddr");
                    } else {
                        cabData[currCabId].longShort = Bundle.getMessage("IsShortAddr");
                    }
                    // read the low address byte
                    readChar = recChars[CabMemorySerial.CAB_ADDR_L - CabMemorySerial.CAB_CURR_SPEED];
                    if (log.isDebugEnabled()) {
                        log.debug("Read address low character " + readChar);
                    }
                    locoAddress = locoAddress + (readChar & 0xFF);
                    if (cabLocoArray[currCabId] != locoAddress) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Loco " + locoAddress + "<->" + cabLocoArray[currCabId]);
                        }
                    }
                    cabLocoArray[currCabId] = locoAddress;
                    cabData[currCabId].locoAddress = locoAddress;

                    // create consist address
                    readChar = recChars[CabMemorySerial.CAB_ALIAS - CabMemorySerial.CAB_CURR_SPEED];
                    if (cabConsistArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Consist " + readChar + "<->" + cabConsistArray[currCabId]);
                        }
                    }
                    cabConsistArray[currCabId] = readChar;
                    cabData[currCabId].consist = readChar;

                    // show consist position if relevant
                    int pos = cabFlagsArray[currCabId] & NceCmdStationMemory.FLAGS_MASK_CONSIST_REAR;
                    cabData[currCabId].consistPos = "";
                    if (cabConsistArray[currCabId] != 0) {
                        if (pos > 0) {
                            cabData[currCabId].consistPos = Bundle.getMessage("IsRear");
                        } else {
                            cabData[currCabId].consistPos = Bundle.getMessage("IsLead");
                        }
                    }

                    // get the functions 0-4 values
                    readChar = recChars[CabMemorySerial.CAB_FUNC_L - CabMemorySerial.CAB_CURR_SPEED];
                    if (cabF0Array[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F0 " + readChar + "<->" + cabF0Array[currCabId]);
                        }
                    }
                    cabF0Array[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Function low character " + readChar);
                    }
                    procFunctions0_4(currCabId, readChar);

                    // get the functions 5-12 values
                    readChar = recChars[CabMemorySerial.CAB_FUNC_H - CabMemorySerial.CAB_CURR_SPEED];
                    if (cabF5Array[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F5 " + readChar + "<->" + cabF5Array[currCabId]);
                        }
                    }
                    cabF5Array[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Function high character " + readChar);
                    }
                    procFunctions5_12(currCabId, readChar);

                    // get the functions 13-20 values
                    readCabMemory1(currCabId, CabMemorySerial.CAB_FUNC_13_20);
                    if (!waitNce()) {
                        return;
                    }
                    if (cabF13Array[currCabId] != recChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F13 " + recChar + "<->" + cabF13Array[currCabId]);
                        }
                    }
                    cabF13Array[currCabId] = recChar;
                    procFunctions13_20(currCabId, recChar);

                    // get the functions 21-28 values
                    readCabMemory1(currCabId, CabMemorySerial.CAB_FUNC_21_28);
                    if (!waitNce()) {
                        return;
                    }
                    if (cabF21Array[currCabId] != recChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F21 " + recChar + "<->" + cabF21Array[currCabId]);
                        }
                    }
                    cabF21Array[currCabId] = recChar;
                    procFunctions21_28(currCabId, recChar);

                    // get the display values
                    readCabMemory16(currCabId, CabMemorySerial.CAB_LINE_1);
                    if (!waitNce()) {
                        return;
                    }
                    StringBuilder text1 = new StringBuilder();
                    StringBuilder debug1 = new StringBuilder();
                    for (int i = 0; i < CAB_LINE_LEN; i++) {
                        if (cabLine1Array[currCabId][i] != recChars[i]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine1["
                                        + i
                                        + "] "
                                        + recChars[i]
                                        + "<->"
                                        + cabLine1Array[currCabId][i]);
                            }
                        }
                        cabLine1Array[currCabId][i] = recChars[i];
                        if (recChars[i] >= 0x20 && recChars[i] <= 0x7F) {
                            text1.append((char) recChars[i]);
                        } else {
                            text1.append(" ");
                        }
                        debug1.append(" ").append(recChars[i]);
                    }
                    cabData[currCabId].text1 = text1.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("TextLine1Debug: " + debug1);
                    }

                    readCabMemory16(currCabId, CabMemorySerial.CAB_LINE_2);
                    if (!waitNce()) {
                        return;
                    }
                    StringBuilder text2 = new StringBuilder();
                    StringBuilder debug2 = new StringBuilder();
                    for (int i = 0; i < CAB_LINE_LEN; i++) {
                        if (cabLine2Array[currCabId][i] != recChars[i]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine2["
                                        + i
                                        + "] "
                                        + recChars[i]
                                        + "<->"
                                        + cabLine2Array[currCabId][i]);
                            }
                        }
                        cabLine2Array[currCabId][i] = recChars[i];
                        if (recChars[i] >= 0x20 && recChars[i] <= 0x7F) {
                            text2.append((char) recChars[i]);
                        } else {
                            text2.append(" ");
                        }
                        debug2.append(" ").append(recChars[i]);
                    }
                    cabData[currCabId].text2 = text2.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("TextLine2Debug: " + debug2);
                    }

                    Calendar now = Calendar.getInstance();
                    if (foundChange > 0 || cabLastChangeArray[currCabId] == null) {
                        cabLastChangeArray[currCabId] = now;
                        StringBuilder txt = new StringBuilder();
                        int h = cabLastChangeArray[currCabId].get(Calendar.HOUR_OF_DAY);
                        int m = cabLastChangeArray[currCabId].get(Calendar.MINUTE);
                        int s = cabLastChangeArray[currCabId].get(Calendar.SECOND);
                        if (h < 10) {
                            txt.append("0");
                        }
                        txt.append(h);
                        txt.append(":");
                        if (m < 10) {
                            txt.append("0");
                        }
                        txt.append(m);
                        txt.append(":");
                        if (s < 10) {
                            txt.append("0");
                        }
                        txt.append(s);
                        cabData[currCabId].lastChange = txt.toString();
                    }
                }
            }
        }

        textStatus.setText(Bundle.getMessage("StatusProcessingDone")
                + ". "
                + MessageFormat.format(Bundle.getMessage("StatusCabsFound"), cabsFound));
        cabModel.fireTableDataChanged();
        this.setVisible(true);
        this.repaint();
    }

    // Thread to update cab info, allows the use of sleep or wait, for NCE-USB connection
    private void cabUpdateUsb() {

        if (firstTime) {
            try {
                Thread.sleep(FIRST_TIME_SLEEP); // wait for panel to display
            } catch (InterruptedException e) {
                log.error("Thread unexpectedly interrupted", e);
            }
        }

        firstTime = false;
        int cabsFound = 0;
        // build table of cabs
        for (int currCabId = minCabNum; currCabId <= maxCabNum; currCabId++) {

            textStatus.setText(MessageFormat.format(Bundle.getMessage("StatusProcessingCabId"), currCabId));
            cabData[currCabId].cabNumber = currCabId;
            int foundChange = 0;
            recChar = -1;
            // create cab type by reading the FLAGS1 byte
            setUsbCabMemoryPointer(currCabId, CabMemoryUsb.CAB_FLAGS1);
            if (!waitNce()) {
                return;
            }
            readUsbCabMemoryN(1);
            if (!waitNce()) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("ID = " + currCabId + " Read flag1 character " + recChar);
            }
            // test it really changed
            if (recChar != -1) {
                // save value for purge
                if (recChar != cabFlag1Array[currCabId]) {
                    foundChange++;
                    if (log.isDebugEnabled()) {
                        log.debug(currCabId + ": Flag1 " + recChar + "<->" + cabFlag1Array[currCabId]);
                    }
                }
                cabFlag1Array[currCabId] = recChar;
                if ((recChar & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE) != NceCmdStationMemory.FLAGS1_CABISACTIVE) {
                    // not active slot
                    continue;
                }
                if (currCabId >= 1 || !checkBoxShowAllCabs.isSelected()) {
                    cabsFound++;
                }
                int cabType = recChar & NceCmdStationMemory.FLAGS1_MASK_CABTYPE; // mask off don't care bits
                switch (cabType) {
                    case NceCmdStationMemory.FLAGS1_CABTYPE_DISPLAY:
                        cabData[currCabId].cabType = Bundle.getMessage("TypeProCab");
                        break;
                    case NceCmdStationMemory.FLAGS1_CABTYPE_NODISP:
                        cabData[currCabId].cabType = Bundle.getMessage("TypeCab04"); // Cab04 or Cab06
                        break;
                    case NceCmdStationMemory.FLAGS1_CABTYPE_USB:
                        cabData[currCabId].cabType = Bundle.getMessage("TypeUSB"); // USB or Mini-Panel
                        break;
                    case NceCmdStationMemory.FLAGS1_CABTYPE_AIU:
                        cabData[currCabId].cabType = Bundle.getMessage("TypeAIU");
                        break;
                    default:
                        cabData[currCabId].cabType = Bundle.getMessage("TypeUnknownCab") + ": " + recChar;
                        break;
                }

                cabData[currCabId].cabNumber = currCabId;
                if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_AIU) {
                    // get the AIU data and map it to the function bits
                    readAiuData(currCabId);
                    if (!waitNce()) {
                        return;
                    }
                    processAiuData(currCabId, recChars);
                    //                 } else if (cabType == NceCmdStationMemory.FLAGS1_CABTYPE_USB) {
                    // I don't have anything to do for the USB at this time
                } else {
                    setUsbCabMemoryPointer(currCabId, CabMemoryUsb.CAB_CURR_SPEED);
                    if (!waitNce()) {
                        return;
                    }
                    // read the Speed byte
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    int readChar = recChar;
                    if (cabSpeedArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Speed " + readChar + "<->" + cabSpeedArray[currCabId]);
                        }
                    }
                    cabSpeedArray[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Read speed character " + Integer.toString(readChar));
                    }
                    cabData[currCabId].locoSpeed = readChar;

                    // create loco address, read the high address byte
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Read address high character " + readChar);
                    }
                    int locoAddress = (readChar & 0x3F) * 256;
                    boolean aType = ((readChar & 0xC0) == 0xC0);
                    if (cabLongShortArray[currCabId] != aType) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Long " + aType + "<->" + cabLongShortArray[currCabId]);
                        }
                    }
                    cabLongShortArray[currCabId] = aType;
                    if (aType) {
                        cabData[currCabId].longShort = Bundle.getMessage("IsLongAddr");
                    } else {
                        cabData[currCabId].longShort = Bundle.getMessage("IsShortAddr");
                    }
                    // read the low address byte
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Read address low character " + readChar);
                    }
                    locoAddress = locoAddress + (readChar & 0xFF);
                    if (cabLocoArray[currCabId] != locoAddress) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Loco " + locoAddress + "<->" + cabLocoArray[currCabId]);
                        }
                    }
                    cabLocoArray[currCabId] = locoAddress;
                    cabData[currCabId].locoAddress = locoAddress;

                    // read the FLAGS byte
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (cabFlagsArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Flags " + readChar + "<->" + cabFlagsArray[currCabId]);
                        }
                    }
                    cabFlagsArray[currCabId] = readChar;
                    int direction = readChar & 0x04;
                    if (direction > 0) {
                        cabData[currCabId].locoDir = Bundle.getMessage("DirForward");
                    } else {
                        cabData[currCabId].locoDir = Bundle.getMessage("DirReverse");
                    }
                    int mode = readChar & 0x02;
                    // USB doesn't use the 28/128 bit
                    cabData[currCabId].mode = "";
                    if (cabType != NceCmdStationMemory.FLAGS1_CABTYPE_USB) {
                        if (mode > 0) {
                            cabData[currCabId].mode = "128";
                        } else {
                            cabData[currCabId].mode = "28";
                        }
                    }

                    // get the functions 0-4 values
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (cabF0Array[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F0 " + readChar + "<->" + cabF0Array[currCabId]);
                        }
                    }
                    cabF0Array[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Function low character " + readChar);
                    }
                    procFunctions0_4(currCabId, readChar);

                    // get the functions 5-12 values
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (cabF5Array[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F5 " + readChar + "<->" + cabF5Array[currCabId]);
                        }
                    }
                    cabF5Array[currCabId] = readChar;
                    if (log.isDebugEnabled()) {
                        log.debug("Function high character " + readChar);
                    }
                    procFunctions5_12(currCabId, readChar);

                    // read consist address
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    readChar = recChar;
                    if (cabConsistArray[currCabId] != readChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": Consist " + readChar + "<->" + cabConsistArray[currCabId]);
                        }
                    }
                    cabConsistArray[currCabId] = readChar;
                    cabData[currCabId].consist = readChar;

                    // show consist position if relevant
                    int pos = cabFlagsArray[currCabId] & NceCmdStationMemory.FLAGS_MASK_CONSIST_REAR;
                    cabData[currCabId].consistPos = "";
                    if (cabConsistArray[currCabId] != 0) {
                        if (pos > 0) {
                            cabData[currCabId].consistPos = Bundle.getMessage("IsRear");
                        } else {
                            cabData[currCabId].consistPos = Bundle.getMessage("IsLead");
                        }
                    }

                    // get the functions 13-20 values
                    setUsbCabMemoryPointer(currCabId, CabMemoryUsb.CAB_FUNC_13_20);
                    if (!waitNce()) {
                        return;
                    }
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    if (cabF13Array[currCabId] != recChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F13 " + recChar + "<->" + cabF13Array[currCabId]);
                        }
                    }
                    cabF13Array[currCabId] = recChar;
                    procFunctions13_20(currCabId, recChar);

                    // get the functions 20-28 values
                    readUsbCabMemoryN(1);
                    if (!waitNce()) {
                        return;
                    }
                    if (cabF21Array[currCabId] != recChar) {
                        foundChange++;
                        if (log.isDebugEnabled()) {
                            log.debug(currCabId + ": F21 " + recChar + "<->" + cabF21Array[currCabId]);
                        }
                    }
                    cabF21Array[currCabId] = recChar;
                    procFunctions21_28(currCabId, recChar);

                    // get the display values
                    setUsbCabMemoryPointer(currCabId, CabMemoryUsb.CAB_LINE_1);
                    if (!waitNce()) {
                        return;
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    StringBuilder text1 = new StringBuilder();
                    StringBuilder debug1 = new StringBuilder();
                    int ptrData = 0;
                    int ptrCabLine = 0;
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine1Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine1["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine1Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine1Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text1.append((char) recChars[ptrData]);
                        } else {
                            text1.append(" ");
                        }
                        debug1.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine1Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine1["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine1Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine1Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text1.append((char) recChars[ptrData]);
                        } else {
                            text1.append(" ");
                        }
                        debug1.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine1Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine1["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine1Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine1Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text1.append((char) recChars[ptrData]);
                        } else {
                            text1.append(" ");
                        }
                        debug1.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine1Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine1["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine1Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine1Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text1.append((char) recChars[ptrData]);
                        } else {
                            text1.append(" ");
                        }
                        debug1.append(" ").append(recChars[ptrData]);
                    }
                    cabData[currCabId].text1 = text1.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("TextLine1Debug: " + debug1);
                    }

                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    StringBuilder text2 = new StringBuilder();
                    StringBuilder debug2 = new StringBuilder();
                    ptrCabLine = 0;
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine2Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine2["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine2Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine2Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text2.append((char) recChars[ptrData]);
                        } else {
                            text2.append(" ");
                        }
                        debug2.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine2Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine2["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine2Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine2Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text2.append((char) recChars[ptrData]);
                        } else {
                            text2.append(" ");
                        }
                        debug2.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine2Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine2["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine2Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine2Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text2.append((char) recChars[ptrData]);
                        } else {
                            text2.append(" ");
                        }
                        debug2.append(" ").append(recChars[ptrData]);
                    }
                    readUsbCabMemoryN(4);
                    if (!waitNce()) {
                        return;
                    }
                    for (ptrData = 0; ptrData < 4; ptrData++, ptrCabLine++) {
                        if (cabLine2Array[currCabId][ptrCabLine] != recChars[ptrData]) {
                            foundChange++;
                            if (log.isDebugEnabled()) {
                                log.debug(currCabId
                                        + ": CabLine2["
                                        + ptrCabLine
                                        + "] "
                                        + recChars[ptrData]
                                        + "<->"
                                        + cabLine2Array[currCabId][ptrCabLine]);
                            }
                        }
                        cabLine2Array[currCabId][ptrCabLine] = recChars[ptrData];
                        if (recChars[ptrData] >= 0x20 && recChars[ptrData] <= 0x7F) {
                            text2.append((char) recChars[ptrData]);
                        } else {
                            text2.append(" ");
                        }
                        debug2.append(" ").append(recChars[ptrData]);
                    }
                    cabData[currCabId].text2 = text2.toString();
                    if (log.isDebugEnabled()) {
                        log.debug("TextLine2Debug: " + debug2);
                    }

                    // add log time stamp
                    Calendar now = Calendar.getInstance();
                    if (foundChange > 0 || cabLastChangeArray[currCabId] == null) {
                        cabLastChangeArray[currCabId] = now;
                        StringBuilder txt = new StringBuilder();
                        int h = cabLastChangeArray[currCabId].get(Calendar.HOUR_OF_DAY);
                        int m = cabLastChangeArray[currCabId].get(Calendar.MINUTE);
                        int s = cabLastChangeArray[currCabId].get(Calendar.SECOND);
                        if (h < 10) {
                            txt.append("0");
                        }
                        txt.append(h);
                        txt.append(":");
                        if (m < 10) {
                            txt.append("0");
                        }
                        txt.append(m);
                        txt.append(":");
                        if (s < 10) {
                            txt.append("0");
                        }
                        txt.append(s);
                        cabData[currCabId].lastChange = txt.toString();
                    }
                }
            }
        }

        textStatus.setText(Bundle.getMessage("StatusProcessingDone")
                + ". "
                + MessageFormat.format(Bundle.getMessage("StatusCabsFound"), cabsFound));
        cabModel.fireTableDataChanged();
        this.setVisible(true);
        this.repaint();
    }

    /**
     * Process for functions F0-F4.
     * <p>
     */
    private void procFunctions0_4(int currCabId, int c) {
        cabData[currCabId].F0 = (c & NceCmdStationMemory.FUNC_L_F0) != 0;
        cabData[currCabId].F1 = (c & NceCmdStationMemory.FUNC_L_F1) != 0;
        cabData[currCabId].F2 = (c & NceCmdStationMemory.FUNC_L_F2) != 0;
        cabData[currCabId].F3 = (c & NceCmdStationMemory.FUNC_L_F3) != 0;
        cabData[currCabId].F4 = (c & NceCmdStationMemory.FUNC_L_F4) != 0;
    }

    /**
     * Process for functions 5 through 12.
     * <p>
     */
    private void procFunctions5_12(int currCabId, int c) {
        cabData[currCabId].F5 = (c & NceCmdStationMemory.FUNC_H_F5) != 0;
        cabData[currCabId].F6 = (c & NceCmdStationMemory.FUNC_H_F6) != 0;
        cabData[currCabId].F7 = (c & NceCmdStationMemory.FUNC_H_F7) != 0;
        cabData[currCabId].F8 = (c & NceCmdStationMemory.FUNC_H_F8) != 0;
        cabData[currCabId].F9 = (c & NceCmdStationMemory.FUNC_H_F9) != 0;
        cabData[currCabId].F10 = (c & NceCmdStationMemory.FUNC_H_F10) != 0;
        cabData[currCabId].F11 = (c & NceCmdStationMemory.FUNC_H_F11) != 0;
        cabData[currCabId].F12 = (c & NceCmdStationMemory.FUNC_H_F12) != 0;
    }

    /**
     * Process char for functions 13-20.
     * <p>
     */
    private void procFunctions13_20(int currCabId, int c) {
        cabData[currCabId].F13 = (c & NceCmdStationMemory.FUNC_H_F13) != 0;
        cabData[currCabId].F14 = (c & NceCmdStationMemory.FUNC_H_F14) != 0;
        cabData[currCabId].F15 = (c & NceCmdStationMemory.FUNC_H_F15) != 0;
        cabData[currCabId].F16 = (c & NceCmdStationMemory.FUNC_H_F16) != 0;
        cabData[currCabId].F17 = (c & NceCmdStationMemory.FUNC_H_F17) != 0;
        cabData[currCabId].F18 = (c & NceCmdStationMemory.FUNC_H_F18) != 0;
        cabData[currCabId].F19 = (c & NceCmdStationMemory.FUNC_H_F19) != 0;
        cabData[currCabId].F20 = (c & NceCmdStationMemory.FUNC_H_F20) != 0;
    }

    /**
     * Process char for functions 21-28.
     * <p>
     */
    private void procFunctions21_28(int currCabId, int c) {
        cabData[currCabId].F21 = (c & NceCmdStationMemory.FUNC_H_F21) != 0;
        cabData[currCabId].F22 = (c & NceCmdStationMemory.FUNC_H_F22) != 0;
        cabData[currCabId].F23 = (c & NceCmdStationMemory.FUNC_H_F23) != 0;
        cabData[currCabId].F24 = (c & NceCmdStationMemory.FUNC_H_F24) != 0;
        cabData[currCabId].F25 = (c & NceCmdStationMemory.FUNC_H_F25) != 0;
        cabData[currCabId].F26 = (c & NceCmdStationMemory.FUNC_H_F26) != 0;
        cabData[currCabId].F27 = (c & NceCmdStationMemory.FUNC_H_F27) != 0;
        cabData[currCabId].F28 = (c & NceCmdStationMemory.FUNC_H_F28) != 0;
    }

    private void processAiuData(int currCabId, int[] ptr) {
        cabData[currCabId].F1 = (ptr[1] & 0x01) == 0;
        cabData[currCabId].F2 = (ptr[1] & 0x02) == 0;
        cabData[currCabId].F3 = (ptr[1] & 0x04) == 0;
        cabData[currCabId].F4 = (ptr[1] & 0x08) == 0;
        cabData[currCabId].F5 = (ptr[1] & 0x10) == 0;
        cabData[currCabId].F6 = (ptr[1] & 0x20) == 0;
        cabData[currCabId].F7 = (ptr[1] & 0x40) == 0;
        cabData[currCabId].F8 = (ptr[1] & 0x80) == 0;
        cabData[currCabId].F9 = (ptr[0] & 0x01) == 0;
        cabData[currCabId].F10 = (ptr[0] & 0x02) == 0;
        cabData[currCabId].F11 = (ptr[0] & 0x04) == 0;
        cabData[currCabId].F12 = (ptr[0] & 0x08) == 0;
        cabData[currCabId].F13 = (ptr[0] & 0x10) == 0;
        cabData[currCabId].F14 = (ptr[0] & 0x20) == 0;
    }

    // puts the thread to sleep while we wait for the read CS memory to complete
    private boolean waitNce() {
        int count = 100;
        if (log.isDebugEnabled()) {
            log.debug("Going to sleep");
        }
        while (waiting > 0) {
            synchronized (this) {
                try {
                    wait(100);
                } catch (InterruptedException e) {
                    //nothing to see here, move along
                }
            }
            count--;
            if (count < 0) {
                textStatus.setText("Error");
                return false;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("awake!");
        }
        return true;
    }

    @Override
    public void message(NceMessage m) {
    } // ignore replies

    // response from read
    int recChar = 0;
    int[] recChars = new int[16];

    @SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification = "Thread wait from main transfer loop")
    @Override
    public void reply(NceReply r) {
        if (log.isDebugEnabled()) {
            log.debug("Receive character");
        }
        if (waiting <= 0) {
            log.error("unexpected response. Len: " + r.getNumDataElements() + " code: " + r.getElement(0));
            return;
        }
        waiting--;
        if (r.getNumDataElements() != replyLen) {
            textStatus.setText("error");
            return;
        }
        // Read one byte
        if (replyLen == NceMessage.REPLY_1) {
            // Looking for proper response
            recChar = r.getElement(0);
        }
        // Read two byte
        if (replyLen == NceMessage.REPLY_2) {
            // Looking for proper response
            for (int i = 0; i < NceMessage.REPLY_2; i++) {
                recChars[i] = r.getElement(i);
            }
        }
        // Read four byte
        if (replyLen == NceMessage.REPLY_4) {
            // Looking for proper response
            for (int i = 0; i < NceMessage.REPLY_4; i++) {
                recChars[i] = r.getElement(i);
            }
        }
        // Read 16 bytes
        if (replyLen == NceMessage.REPLY_16) {
            // Looking for proper response
            for (int i = 0; i < NceMessage.REPLY_16; i++) {
                recChars[i] = r.getElement(i);
            }
        }
        // wake up thread
        synchronized (this) {
            notify();
        }
    }

    // Write 1 byte of NCE cab memory
    private void writeCabMemory1(int cabNum, int offset, int value) {
        int nceCabAddr = getNceCabAddr(cabNum, offset);
        replyLen = NceMessage.REPLY_1; // Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryWrite1(nceCabAddr, (byte) value);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // Reads 1 byte of NCE cab memory
    private void readCabMemory1(int cabNum, int offset) {
        int nceCabAddr = getNceCabAddr(cabNum, offset);
        replyLen = NceMessage.REPLY_1; // Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryRead1(nceCabAddr);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // Reads 16 bytes of NCE cab memory
    private void readCabMemory16(int cabNum, int offset) {
        int nceCabAddr = getNceCabAddr(cabNum, offset);
        replyLen = NceMessage.REPLY_16; // Expect 16 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryRead(nceCabAddr);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_16);
        tc.sendNceMessage(m, this);
    }

    // Reads 16 bytes of NCE cab memory
    private int getNceCabAddr(int cabNum, int offset) {
        int nceCabAddr;
        if (cabNum < CAB_MAX_PRO) {
            nceCabAddr = (cabNum * CabMemorySerial.CAB_SIZE) + CabMemorySerial.CS_CAB_MEM_PRO + offset;
        } else {
            nceCabAddr = CabMemorySerial.CS_COMP_CAB_MEM_PRO + offset;
        }
        return nceCabAddr;
    }

    // USB set cab memory pointer
    private void setUsbCabMemoryPointer(int cab, int offset) {
        replyLen = NceMessage.REPLY_1; // Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.usbMemoryPointer(cab, offset);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // USB Read N bytes of NCE cab memory
    private void readUsbCabMemoryN(int num) {
        switch (num) {
            case 1:
                replyLen = NceMessage.REPLY_1; // Expect 1 byte response
                break;
            case 2:
                replyLen = NceMessage.REPLY_2; // Expect 2 byte response
                break;
            case 4:
                replyLen = NceMessage.REPLY_4; // Expect 4 byte response
                break;
            default:
                log.error("Invalid usb read byte count");
                return;
        }
        waiting++;
        byte[] bl = NceBinaryCommand.usbMemoryRead((byte) num);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, replyLen);
        tc.sendNceMessage(m, this);
    }

    // USB Write 1 byte of NCE cab memory
    private void writeUsbCabMemory1(int value) {
        replyLen = NceMessage.REPLY_1; // Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.usbMemoryWrite1((byte) value);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // USB Read AIU
    private void readAiuData(int cabId) {
        replyLen = NceMessage.REPLY_2; // Expect 2 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accAiu2Read(cabId);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, replyLen);
        tc.sendNceMessage(m, this);
    }

    protected void addItem(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        p.add(c, gc);
    }

    protected void addItemLeft(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = GridBagConstraints.WEST;
        p.add(c, gc);
    }

    protected void addItemTop(JPanel p, JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        gc.anchor = GridBagConstraints.NORTH;
        p.add(c, gc);
    }

    private void addButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
    }

    private void addCheckBoxAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkBoxActionPerformed(e);
            }
        });
    }

    void setColumnPurgeButton(JTable table, int column) {
        TableColumnModel tcm = table.getColumnModel();
        // install the button renderers & editors in this column
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(column).setCellRenderer(buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton(Bundle.getMessage("ButtonPurgeCab")));
        tcm.getColumn(column).setCellEditor(buttonEditor);
        // ensure the table rows, columns have enough room for buttons
        table.setRowHeight(new JButton("  " + cabModel.getValueAt(1, column)).getPreferredSize().height);
        table.getColumnModel().getColumn(column)
                .setPreferredWidth(new JButton(Bundle.getMessage("ButtonPurgeCab")).getPreferredSize().width + 1);
    }

    @Override
    public void dispose() {
        cabModel = null;
        cabData = null;
        if (autoRefreshThread != null) {
            autoRefreshThread.interrupt();
        }
        super.dispose();
    }

    class NceCabTableModel extends AbstractTableModel {

        DataRow[] cabData;

        NceCabTableModel(DataRow[] cabDataPtr) {
            this.cabData = cabDataPtr;
        }

        private final String[] columnNames1LineText = {
            Bundle.getMessage("ColHeaderCabId"),
            Bundle.getMessage("ColHeaderType"),
            Bundle.getMessage("ColHeaderPurge"),
            Bundle.getMessage("ColHeaderLongShort"),
            Bundle.getMessage("ColHeaderLoco"),
            Bundle.getMessage("ColHeaderSpeed"),
            Bundle.getMessage("ColHeaderDir"),
            Bundle.getMessage("ColHeaderMode"),
            Bundle.getMessage("ColHeaderConsist"),
            Bundle.getMessage("ColHeaderConsistPos"),
            Bundle.getMessage("ColHeaderF0"),
            Bundle.getMessage("ColHeaderF1"),
            Bundle.getMessage("ColHeaderF2"),
            Bundle.getMessage("ColHeaderF3"),
            Bundle.getMessage("ColHeaderF4"),
            Bundle.getMessage("ColHeaderF5"),
            Bundle.getMessage("ColHeaderF6"),
            Bundle.getMessage("ColHeaderF7"),
            Bundle.getMessage("ColHeaderF8"),
            Bundle.getMessage("ColHeaderF9"),
            Bundle.getMessage("ColHeaderF10"),
            Bundle.getMessage("ColHeaderF11"),
            Bundle.getMessage("ColHeaderF12"),
            Bundle.getMessage("ColHeaderF13"),
            Bundle.getMessage("ColHeaderF14"),
            Bundle.getMessage("ColHeaderF15"),
            Bundle.getMessage("ColHeaderF16"),
            Bundle.getMessage("ColHeaderF17"),
            Bundle.getMessage("ColHeaderF18"),
            Bundle.getMessage("ColHeaderF19"),
            Bundle.getMessage("ColHeaderF20"),
            Bundle.getMessage("ColHeaderF21"),
            Bundle.getMessage("ColHeaderF22"),
            Bundle.getMessage("ColHeaderF23"),
            Bundle.getMessage("ColHeaderF24"),
            Bundle.getMessage("ColHeaderF25"),
            Bundle.getMessage("ColHeaderF26"),
            Bundle.getMessage("ColHeaderF27"),
            Bundle.getMessage("ColHeaderF28"),
            Bundle.getMessage("ColHeaderText1"),
            Bundle.getMessage("ColHeaderText2"),
            Bundle.getMessage("ColHeaderLastUsed")
        };

        private boolean showAllCabs = false;
        private boolean showAllFunctions = false;
        private boolean showCabDisplay = false;

        @Override
        public int getColumnCount() {
            return columnNames1LineText.length;
        }

        @Override
        public int getRowCount() {
            int activeRows = 0;
            if (!getShowAllCabs()) {
                for (int i = minCabNum; i <= maxCabNum; i++) {
                    if ((cabFlag1Array[i]
                            & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE) == NceCmdStationMemory.FLAGS1_CABISACTIVE) {
                        activeRows++;
                    }
                }
            } else {
                activeRows = maxCabNum - minCabNum + 1;
            }
            return activeRows;
        }

        /**
         * Return cabId for row number.
         *
         * @param row row for cab information
         * @return cab id
         */
        protected int getCabIdForRow(int row) {
            int activeRows = -1;
            if (!getShowAllCabs()) {
                for (int i = minCabNum; i <= maxCabNum; i++) {
                    if ((cabFlag1Array[i]
                            & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE) == NceCmdStationMemory.FLAGS1_CABISACTIVE) {
                        activeRows++;
                        if (row == activeRows) {
                            return i;
                        }
                    }
                }
                return -1;
            } else {
                return row + minCabNum;
            }
        }

        @Override
        public String getColumnName(int col) {
            return columnNames1LineText[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            int cabId = getCabIdForRow(row);
            if (cabId == -1 && !getShowAllCabs()) {
                return null; // no active rows
            }
            if (cabId < minCabNum || cabId > maxCabNum) {
                log.error("getCabIdForRow(" + row + ") returned " + cabId);
                return null;
            }
            DataRow r = cabData[cabId];
            boolean activeCab = (cabFlag1Array[cabId]
                    & NceCmdStationMemory.FLAGS1_MASK_CABISACTIVE) == NceCmdStationMemory.FLAGS1_CABISACTIVE;
            if (r == null) {
                return null;
            }
            if (!activeCab && (col != 0)) {
                return null;
            }
            switch (col) {
                case 0:
                    return r.cabNumber;
                case 1:
                    return r.cabType;
                case 2:
                    return Bundle.getMessage("ButtonPurgeCab");
                case 3:
                    return r.longShort;
                case 4:
                    return r.locoAddress;
                case 5:
                    return r.locoSpeed;
                case 6:
                    return r.locoDir;
                case 7:
                    return r.mode;
                case 8:
                    return r.consist;
                case 9:
                    return r.consistPos;
                case 10:
                    return r.F0;
                case 11:
                    return r.F1;
                case 12:
                    return r.F2;
                case 13:
                    return r.F3;
                case 14:
                    return r.F4;
                case 15:
                    return r.F5;
                case 16:
                    return r.F6;
                case 17:
                    return r.F7;
                case 18:
                    return r.F8;
                case 19:
                    return r.F9;
                case 20:
                    return r.F10;
                case 21:
                    return r.F11;
                case 22:
                    return r.F12;
                case 23:
                    return r.F13;
                case 24:
                    return r.F14;
                case 25:
                    return r.F15;
                case 26:
                    return r.F16;
                case 27:
                    return r.F17;
                case 28:
                    return r.F18;
                case 29:
                    return r.F19;
                case 30:
                    return r.F20;
                case 31:
                    return r.F21;
                case 32:
                    return r.F22;
                case 33:
                    return r.F23;
                case 34:
                    return r.F24;
                case 35:
                    return r.F25;
                case 36:
                    return r.F26;
                case 37:
                    return r.F27;
                case 38:
                    return r.F28;
                case 39:
                    return r.text1;
                case 40:
                    return r.text2;
                case 41:
                    return r.lastChange;
                default:
                    log.error("Unhandled column number: {}", col);
                    break;
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            int cabId = getCabIdForRow(row);
            if (col == 2) {
                purgeCab(cabId);
            }
        }

        @Override
        public Class<?> getColumnClass(int c) {
            if (c == 0 || c == 4 || c == 5 || c == 8) {
                return Integer.class;
            } else if (c == 1 || c == 3 || c == 6 || c == 7 || c == 9 || (c >= 39 && c <= 41)) {
                return String.class;
            } else if (c >= 10 && c <= 38) {
                return Boolean.class;
            } else if (c == 2) {
                return JButton.class;
            } else {
                return null;
            }
        }

        public int getPreferredWidth(int col) {
            int width = new JLabel(columnNames1LineText[col]).getPreferredSize().width + 10;
            //            log.debug("Column {} width {} name {}", col, width, columnNames1LineText[col]);
            return width;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 2;
        }

        public boolean getShowAllCabs() {
            return this.showAllCabs;
        }

        public void setShowAllCabs(boolean b) {
            this.showAllCabs = b;
        }

        public boolean getShowAllFunctions() {
            return this.showAllFunctions;
        }

        public void setShowAllFunctions(boolean b) {
            this.showAllFunctions = b;
        }

        public boolean getShowCabDisplay() {
            return this.showCabDisplay;
        }

        public void setShowCabDisplay(boolean b) {
            this.showCabDisplay = b;
        }

    }

    /**
     * Nested class to create one of these using old-style defaults.
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {

        public Default() {
            super("Open NCE Cabs Monitor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NceShowCabPanel.class.getName(),
                    jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceShowCabPanel.class);
}
