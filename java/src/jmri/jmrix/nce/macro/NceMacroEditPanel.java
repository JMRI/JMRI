// NceMacroEditPanel.java
package jmri.jmrix.nce.macro;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrix.nce.NceBinaryCommand;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemorySerial;
import jmri.jmrix.nce.NceCmdStationMemory.CabMemoryUsb;
import jmri.jmrix.nce.NceMessage;
import jmri.jmrix.nce.NceReply;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;
import jmri.jmrix.nce.swing.NcePanelInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of NCE macros
 *
 * NCE macros are stored in Command Station (CS) memory starting at address
 * xC800. Each macro consists of 20 bytes. The last macro 255 is at address
 * xDBEC.
 *
 * Macro addr 0	xC800 1	xC814 2	xC828 3	xC83C . . . . 255	xDBEC
 *
 * Each macro can close or throw up to ten accessories. Macros can also be
 * linked together. Two bytes (16 bit word) define an accessory address and
 * command, or the address of the next macro to be executed. If the upper byte
 * of the macro data word is xFF, then the next byte contains the address of the
 * next macro to be executed by the NCE CS. For example, xFF08 means link to
 * macro 8. NCE uses the NMRA DCC accessory decoder packet format for the word
 * defination of their macros.
 *
 * Macro data byte:
 *
 * bit	15 14 13 12 11 10 9 8 7 6 5 4 3 2 1 0 _ _ _ _ 1 0 A A A A A A 1 A A A C D
 * D D addr bit 7 6 5 4 3 2 10 9 8 1 0 turnout	T
 *
 * By convention, MSB address bits 10 - 8 are one's complement. NCE macros
 * always set the C bit to 1. The LSB "D" (0) determines if the accessory is to
 * be thrown (0) or closed (1). The next two bits "D D" are the LSBs of the
 * accessory address. Note that NCE display addresses are 1 greater than NMRA
 * DCC. Note that address bit 2 isn't supposed to be inverted, but it is the way
 * NCE implemented their macros.
 *
 * Examples:
 *
 * 81F8 = accessory 1 thrown 9FFC = accessory 123 thrown B5FD = accessory 211
 * close BF8F = accessory 2044 close
 *
 * FF10 = link macro 16
 *
 * Updated for including the USB 7.* for 1.65 command station
 *
 * Variables found on cab context page 14 (Cab address 14)
 *
 * ;macro table MACRO_TBL ;table of macros, 16 entries of 16 bytes organized as:
 * ; macro 0, high byte, low byte - 7 more times (8 accy commands total) ; macro
 * 1, high byte, low byte - 7 more times (8 accy commands total) ; ; macro 16,
 * high byte, low byte - 7 more times (8 accy commands total)
 *
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @author Ken Cameron Copyright (C) 2013
 * @version $Revision$
 */
public class NceMacroEditPanel extends jmri.jmrix.nce.swing.NcePanel implements NcePanelInterface, jmri.jmrix.nce.NceListener {

    /**
     *
     */
    private static final long serialVersionUID = 3192077168997079793L;

    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.nce.macro.NceMacroBundle");

    private int macroNum = 0;						// macro being worked
    private int replyLen = 0;						// expected byte length
    private int waiting = 0;						// to catch responses not intended for this module
    private static final int firstTimeSleep = 3000;		// delay first operation to let panel build
    private boolean firstTime = true; // wait for panel to display

    private static final String QUESTION = rb.getString("Add");// The three possible states for a turnout
    private static final String CLOSED = InstanceManager.turnoutManagerInstance().getClosedText();
    private static final String THROWN = InstanceManager.turnoutManagerInstance().getThrownText();
    private static final String CLOSED_NCE = rb.getString("Normal");
    private static final String THROWN_NCE = rb.getString("Reverse");

    private static final String DELETE = rb.getString("Delete");

    private static final String EMPTY = rb.getString("empty");	// One of two accessory states
    private static final String ACCESSORY = rb.getString("accessory");

    private static final String LINK = rb.getString("LinkMacro");// Line 10 alternative to Delete

    Thread NceMemoryThread;
    private boolean readRequested = false;
    private boolean writeRequested = false;

    private boolean macroSearchInc = false;		// next search
    private boolean macroSearchDec = false;		// previous search
    private boolean macroValid = false;		// when true, NCE CS has responded to macro read
    private boolean macroModified = false;		// when true, macro has been modified by user

    // member declarations
    JLabel textMacro = new JLabel(rb.getString("Macro"));
    JLabel textReply = new JLabel(rb.getString("Reply"));
    JLabel macroReply = new JLabel();

    // major buttons
    JButton previousButton = new JButton(rb.getString("Previous"));
    JButton nextButton = new JButton(rb.getString("Next"));
    JButton getButton = new JButton(rb.getString("Get"));
    JButton saveButton = new JButton(rb.getString("Save"));
    JButton backUpButton = new JButton(rb.getString("Backup"));
    JButton restoreButton = new JButton(rb.getString("Restore"));

    // check boxes
    JCheckBox checkBoxEmpty = new JCheckBox(rb.getString("EmptyMacro"));
    JCheckBox checkBoxNce = new JCheckBox(rb.getString("NCETurnout"));

    // macro text field
    JTextField macroTextField = new JTextField(4);

    // for padding out panel
    JLabel space1 = new JLabel("                          ");
    JLabel space2 = new JLabel("                          ");
    JLabel space3 = new JLabel("                          ");
    JLabel space4 = new JLabel("                          ");
    JLabel space5 = new JLabel("                          ");
    JLabel space15 = new JLabel(" ");

    // accessory row 1
    JLabel num1 = new JLabel();
    JLabel textAccy1 = new JLabel();
    JTextField accyTextField1 = new JTextField(4);
    JButton cmdButton1 = new JButton();
    JButton deleteButton1 = new JButton();

    //  accessory row 2
    JLabel num2 = new JLabel();
    JLabel textAccy2 = new JLabel();
    JTextField accyTextField2 = new JTextField(4);
    JButton cmdButton2 = new JButton();
    JButton deleteButton2 = new JButton();

    //  accessory row 3
    JLabel num3 = new JLabel();
    JLabel textAccy3 = new JLabel();
    JTextField accyTextField3 = new JTextField(4);
    JButton cmdButton3 = new JButton();
    JButton deleteButton3 = new JButton();

    //  accessory row 4
    JLabel num4 = new JLabel();
    JLabel textAccy4 = new JLabel();
    JTextField accyTextField4 = new JTextField(4);
    JButton cmdButton4 = new JButton();
    JButton deleteButton4 = new JButton();

    //  accessory row 5
    JLabel num5 = new JLabel();
    JLabel textAccy5 = new JLabel();
    JTextField accyTextField5 = new JTextField(4);
    JButton cmdButton5 = new JButton();
    JButton deleteButton5 = new JButton();

    //  accessory row 6
    JLabel num6 = new JLabel();
    JLabel textAccy6 = new JLabel();
    JTextField accyTextField6 = new JTextField(4);
    JButton cmdButton6 = new JButton();
    JButton deleteButton6 = new JButton();

    //  accessory row 7
    JLabel num7 = new JLabel();
    JLabel textAccy7 = new JLabel();
    JTextField accyTextField7 = new JTextField(4);
    JButton cmdButton7 = new JButton();
    JButton deleteButton7 = new JButton();

    //  accessory row 8
    JLabel num8 = new JLabel();
    JLabel textAccy8 = new JLabel();
    JTextField accyTextField8 = new JTextField(4);
    JButton cmdButton8 = new JButton();
    JButton deleteButton8 = new JButton();

    //  accessory row 9
    JLabel num9 = new JLabel();
    JLabel textAccy9 = new JLabel();
    JTextField accyTextField9 = new JTextField(4);
    JButton cmdButton9 = new JButton();
    JButton deleteButton9 = new JButton();

    //  accessory row 10
    JLabel num10 = new JLabel();
    JLabel textAccy10 = new JLabel();
    JTextField accyTextField10 = new JTextField(4);
    JButton cmdButton10 = new JButton();
    JButton deleteButton10 = new JButton();

    private NceTrafficController tc = null;
    private int maxNumMacros = 0;
    private int macroSize = 0;
    private int memBase = -1;
    private boolean isUsb = false;

    public NceMacroEditPanel() {
        super();
    }

    public void initContext(Object context) throws Exception {
        if (context instanceof NceSystemConnectionMemo) {
            initComponents((NceSystemConnectionMemo) context);
        }
    }

    public String getHelpTarget() {
        return "package.jmri.jmrix.nce.macro.NceMacroEditFrame";
    }

    public String getTitle() {
        StringBuilder x = new StringBuilder();
        if (memo != null) {
            x.append(memo.getUserName());
        } else {
            x.append("NCE_");
        }
        x.append(": ");
        x.append(rb.getString("TitleEditNCEMacro"));
        return x.toString();
    }

    public void initComponents(NceSystemConnectionMemo memo) throws Exception {
        this.memo = memo;
        this.tc = memo.getNceTrafficController();
        maxNumMacros = CabMemorySerial.CS_MAX_MACRO;
        isUsb = false;
        macroSize = CabMemorySerial.CS_MACRO_SIZE;
        memBase = CabMemorySerial.CS_MACRO_MEM;
        if ((tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)
                && (tc.getCmdGroups() & NceTrafficController.CMDS_MEM) != 0) {
            maxNumMacros = CabMemoryUsb.CS_MAX_MACRO;
            isUsb = true;
            macroSize = CabMemoryUsb.CS_MACRO_SIZE;
            memBase = -1;
        }

        // the following code sets the frame's initial state
        // default at startup
        macroReply.setText(rb.getString("unknown"));
        macroTextField.setText("");
        saveButton.setEnabled(false);

        // load tool tips
        previousButton.setToolTipText(rb.getString("toolTipSearchDecrementing"));
        nextButton.setToolTipText(rb.getString("toolTipSearchIncrementing"));
        getButton.setToolTipText(rb.getString("toolTipReadMacro"));
        if (isUsb) {
            macroTextField.setToolTipText(rb.getString("toolTipEnterMacroUsb"));
        } else {
            macroTextField.setToolTipText(rb.getString("toolTipEnterMacroSerial"));
        }
        saveButton.setToolTipText(rb.getString("toolTipUpdateMacro"));
        backUpButton.setToolTipText(rb.getString("toolTipBackUp"));
        restoreButton.setToolTipText(rb.getString("toolTipRestore"));
        checkBoxEmpty.setToolTipText(rb.getString("toolTipSearchEmpty"));
        checkBoxNce.setToolTipText(rb.getString("toolTipUseNce"));

        initAccyFields();

        setLayout(new GridBagLayout());

        // Layout the panel by rows
        // row 0
        addItem(textMacro, 2, 0);

        // row 1
        addItem(previousButton, 1, 1);
        addItem(macroTextField, 2, 1);
        addItem(nextButton, 3, 1);
        addItem(checkBoxEmpty, 4, 1);

        // row 2
        addItem(textReply, 0, 2);
        addItem(macroReply, 1, 2);
        addItem(getButton, 2, 2);
        addItem(checkBoxNce, 4, 2);

        // row 3 padding for looks
        //addItem(space1, 0,3);
        addItem(space2, 1, 3);
        addItem(space3, 2, 3);
        addItem(space4, 3, 3);
		//addItem(space5, 4,3);

        // row 4 RFU
        int rNum = 5;
        // row 5 accessory 1
        addAccyRow(num1, textAccy1, accyTextField1, cmdButton1, deleteButton1, rNum);
        rNum++;

        // row 6 accessory 2
        addAccyRow(num2, textAccy2, accyTextField2, cmdButton2, deleteButton2, rNum);
        rNum++;

        // row 7 accessory 3
        addAccyRow(num3, textAccy3, accyTextField3, cmdButton3, deleteButton3, rNum);
        rNum++;

        // row 8 accessory 4
        addAccyRow(num4, textAccy4, accyTextField4, cmdButton4, deleteButton4, rNum);
        rNum++;

        // row 9 accessory 5
        addAccyRow(num5, textAccy5, accyTextField5, cmdButton5, deleteButton5, rNum);
        rNum++;

        // row 10 accessory 6
        addAccyRow(num6, textAccy6, accyTextField6, cmdButton6, deleteButton6, rNum);
        rNum++;

        // row 11 accessory 7
        addAccyRow(num7, textAccy7, accyTextField7, cmdButton7, deleteButton7, rNum);
        rNum++;

        if (!isUsb) {
            // row 12 accessory 8
            addAccyRow(num8, textAccy8, accyTextField8, cmdButton8, deleteButton8, rNum);
            rNum++;

            // row 13 accessory 9
            addAccyRow(num9, textAccy9, accyTextField9, cmdButton9, deleteButton9, rNum);
            rNum++;
        }

        // row 14 accessory 10
        if (isUsb) {
            addAccyRow(num8, textAccy10, accyTextField10, cmdButton10, deleteButton10, rNum);
        } else {
            addAccyRow(num10, textAccy10, accyTextField10, cmdButton10, deleteButton10, rNum);
        }
        rNum++;

        // row 15 padding for looks
        addItem(space15, 2, rNum);
        rNum++;

        // row 16
        addItem(saveButton, 2, rNum);
        if (isUsb) {
            backUpButton.setEnabled(false);
            restoreButton.setEnabled(false);
        }
        addItem(backUpButton, 3, rNum);
        addItem(restoreButton, 4, rNum);
        rNum++;

        // setup buttons
        addButtonAction(previousButton);
        addButtonAction(nextButton);
        addButtonAction(getButton);
        addButtonAction(saveButton);
        addButtonAction(backUpButton);
        addButtonAction(restoreButton);

        // accessory command buttons
        addButtonCmdAction(cmdButton1);
        addButtonCmdAction(cmdButton2);
        addButtonCmdAction(cmdButton3);
        addButtonCmdAction(cmdButton4);
        addButtonCmdAction(cmdButton5);
        addButtonCmdAction(cmdButton6);
        addButtonCmdAction(cmdButton7);
        if (!isUsb) {
            addButtonCmdAction(cmdButton8);
            addButtonCmdAction(cmdButton9);
        }
        addButtonCmdAction(cmdButton10);

        // accessory delete buttons
        addButtonDelAction(deleteButton1);
        addButtonDelAction(deleteButton2);
        addButtonDelAction(deleteButton3);
        addButtonDelAction(deleteButton4);
        addButtonDelAction(deleteButton5);
        addButtonDelAction(deleteButton6);
        addButtonDelAction(deleteButton7);
        if (!isUsb) {
            addButtonDelAction(deleteButton8);
            addButtonDelAction(deleteButton9);
        }
        addButtonDelAction(deleteButton10);

        // NCE checkbox
        addCheckBoxAction(checkBoxNce);

    }

    // Previous, Next, Get, Save, Restore & Backup buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {

        // if we're searching ignore user 
        if (macroSearchInc || macroSearchDec) {
            return;
        }

        if (ae.getSource() == saveButton) {
            boolean status = saveMacro();
            if (status) // was save successful?
            {
                setSaveButton(false); // yes, clear save button
            }
            return;
        }

        if (macroModified) {
            // warn user that macro has been modified
            JOptionPane.showMessageDialog(this,
                    rb.getString("MacroModified"), rb.getString("NceMacro"),
                    JOptionPane.WARNING_MESSAGE);
            macroModified = false;		// only one warning!!!

        } else {

            setSaveButton(false);		// Turn off save button

            if (ae.getSource() == previousButton) {
                macroSearchDec = true;
                macroNum = getMacro();	// check for valid and kick off read process
                if (macroNum < 0) {	// Error user input incorrect
                    macroSearchDec = false;
                } else {
                    processMemory(true, false, macroNum, null);
                }
            }
            if (ae.getSource() == nextButton) {
                macroSearchInc = true;
                macroNum = getMacro();	// check for valid
                if (macroNum < 0) { 	// Error user input incorrect
                    macroSearchInc = false;
                } else {
                    processMemory(true, false, macroNum, null);
                }
            }

            if (ae.getSource() == getButton) {
                // Get Macro
                macroNum = getMacro();
                if (macroNum >= 0) {
                    processMemory(true, false, macroNum, null);
                }
            }

            if (!isUsb && (ae.getSource() == backUpButton)) {

                Thread mb = new NceMacroBackup(tc);
                mb.setName("Macro Backup");
                mb.start();
            }

            if (!isUsb && (ae.getSource() == restoreButton)) {
                Thread mr = new NceMacroRestore(tc);
                mr.setName("Macro Restore");
                mr.start();
            }
        }
    }

    // One of the ten accessory command buttons pressed
    public void buttonActionCmdPerformed(java.awt.event.ActionEvent ae) {

        // if we're searching ignore user 
        if (macroSearchInc || macroSearchDec) {
            return;
        }

        if (ae.getSource() == cmdButton1) {
            updateAccyCmdPerformed(accyTextField1, cmdButton1, textAccy1,
                    deleteButton1);
        }
        if (ae.getSource() == cmdButton2) {
            updateAccyCmdPerformed(accyTextField2, cmdButton2, textAccy2,
                    deleteButton2);
        }
        if (ae.getSource() == cmdButton3) {
            updateAccyCmdPerformed(accyTextField3, cmdButton3, textAccy3,
                    deleteButton3);
        }
        if (ae.getSource() == cmdButton4) {
            updateAccyCmdPerformed(accyTextField4, cmdButton4, textAccy4,
                    deleteButton4);
        }
        if (ae.getSource() == cmdButton5) {
            updateAccyCmdPerformed(accyTextField5, cmdButton5, textAccy5,
                    deleteButton5);
        }
        if (ae.getSource() == cmdButton6) {
            updateAccyCmdPerformed(accyTextField6, cmdButton6, textAccy6,
                    deleteButton6);
        }
        if (ae.getSource() == cmdButton7) {
            updateAccyCmdPerformed(accyTextField7, cmdButton7, textAccy7,
                    deleteButton7);
        }
        if (!isUsb) {
            if (ae.getSource() == cmdButton8) {
                updateAccyCmdPerformed(accyTextField8, cmdButton8, textAccy8,
                        deleteButton8);
            }
            if (ae.getSource() == cmdButton9) {
                updateAccyCmdPerformed(accyTextField9, cmdButton9, textAccy9,
                        deleteButton9);
            }
        }
        if (ae.getSource() == cmdButton10) {
            updateAccyCmdPerformed(accyTextField10, cmdButton10, textAccy10,
                    deleteButton10);
        }
    }

    // One of ten Delete buttons pressed
    public void buttonActionDeletePerformed(java.awt.event.ActionEvent ae) {

        // if we're searching ignore user 
        if (macroSearchInc || macroSearchDec) {
            return;
        }

        if (ae.getSource() == deleteButton1) {
            updateAccyDelPerformed(accyTextField1, cmdButton1, textAccy1,
                    deleteButton1);
        }
        if (ae.getSource() == deleteButton2) {
            updateAccyDelPerformed(accyTextField2, cmdButton2, textAccy2,
                    deleteButton2);
        }
        if (ae.getSource() == deleteButton3) {
            updateAccyDelPerformed(accyTextField3, cmdButton3, textAccy3,
                    deleteButton3);
        }
        if (ae.getSource() == deleteButton4) {
            updateAccyDelPerformed(accyTextField4, cmdButton4, textAccy4,
                    deleteButton4);
        }
        if (ae.getSource() == deleteButton5) {
            updateAccyDelPerformed(accyTextField5, cmdButton5, textAccy5,
                    deleteButton5);
        }
        if (ae.getSource() == deleteButton6) {
            updateAccyDelPerformed(accyTextField6, cmdButton6, textAccy6,
                    deleteButton6);
        }
        if (ae.getSource() == deleteButton7) {
            updateAccyDelPerformed(accyTextField7, cmdButton7, textAccy7,
                    deleteButton7);
        }
        if (!isUsb) {
            if (ae.getSource() == deleteButton8) {
                updateAccyDelPerformed(accyTextField8, cmdButton8, textAccy8,
                        deleteButton8);
            }
            if (ae.getSource() == deleteButton9) {
                updateAccyDelPerformed(accyTextField9, cmdButton9, textAccy9,
                        deleteButton9);
            }
        }
        // row ten delete button behaves differently
        // could be link button
        if (ae.getSource() == deleteButton10) {

            // is the user trying to link a macro?
            if (deleteButton10.getText() == LINK) {
                if (macroValid == false) { // Error user input incorrect
                    JOptionPane.showMessageDialog(this,
                            rb.getString("GetMacroNumber"), rb.getString("NceMacro"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int linkMacro = validMacro(accyTextField10.getText());
                if (linkMacro == -1) {
                    JOptionPane.showMessageDialog(this,
                            rb.getString("EnterMacroNumberLine10"), rb.getString("NceMacro"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // success, link a macro
                setSaveButton(true);
                textAccy10.setText(LINK);
                cmdButton10.setVisible(false);
                deleteButton10.setText(DELETE);
                deleteButton10.setToolTipText(rb.getString("toolTipRemoveMacroLink"));

                // user wants to delete a accessory address or a link	
            } else {
                updateAccyDelPerformed(accyTextField10, cmdButton10, textAccy10,
                        deleteButton10);
                initAccyRow10();
            }
        }
    }

    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        processMemory(true, false, macroNum, null);
    }

    // gets the user supplied macro number
    private int getMacro() {
        // Set all fields to default and build from there
        initAccyFields();
        if (firstTime) {
            try {
                Thread.sleep(firstTimeSleep);	// wait for panel to display 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        firstTime = false;
        String m = macroTextField.getText();
        if (m.isEmpty()) {
            m = "0";
        }
        int mN = validMacro(m);
        if (mN < 0) {
            macroReply.setText(rb.getString("error"));
            JOptionPane.showMessageDialog(this,
                    rb.getString("EnterMacroNumber"), rb.getString("NceMacro"),
                    JOptionPane.ERROR_MESSAGE);
            macroValid = false;
            return mN;
        }
        if (macroSearchInc || macroSearchDec) {
            macroReply.setText(rb.getString("searching"));
            if (macroSearchInc) {
                mN++;
                if (mN >= maxNumMacros + 1) {
                    mN = 0;
                }
            }
            if (macroSearchDec) {
                mN--;
                if (mN <= -1) {
                    mN = maxNumMacros;
                }
            }
        } else {
            macroReply.setText(rb.getString("waiting"));
        }

        return mN;
    }

    /**
     * Writes all bytes to NCE CS memory as long as there are no user input
     * errors
     *
     * @return
     */
    private boolean saveMacro() {
        if (firstTime) {
            try {
                Thread.sleep(firstTimeSleep);	// wait for panel to display 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        firstTime = false;
        byte[] macroAccy = new byte[macroSize];			// NCE Macro data
        int index = 0;
        int accyNum = 0;
        // test the inputs, convert from text
        accyNum = getAccyRow(macroAccy, index, textAccy1, accyTextField1, cmdButton1);
        if (accyNum < 0) //error
        {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy2, accyTextField2, cmdButton2);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy3, accyTextField3, cmdButton3);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy4, accyTextField4, cmdButton4);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy5, accyTextField5, cmdButton5);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy6, accyTextField6, cmdButton6);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        accyNum = getAccyRow(macroAccy, index, textAccy7, accyTextField7, cmdButton7);
        if (accyNum < 0) {
            return false;
        }
        if (accyNum > 0) {
            index += 2;
        }
        if (!isUsb) {
            accyNum = getAccyRow(macroAccy, index, textAccy8, accyTextField8, cmdButton8);
            if (accyNum < 0) {
                return false;
            }
            if (accyNum > 0) {
                index += 2;
            }
            accyNum = getAccyRow(macroAccy, index, textAccy9, accyTextField9, cmdButton9);
            if (accyNum < 0) {
                return false;
            }
            if (accyNum > 0) {
                index += 2;
            }
        }
        accyNum = getAccyRow(macroAccy, index, textAccy10, accyTextField10, cmdButton10);
        if (accyNum < 0) {
            JOptionPane.showMessageDialog(this,
                    rb.getString("EnterMacroNumberLine10"), rb.getString("NceMacro"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        processMemory(false, true, macroNum, macroAccy);
        return true;
    }

    private void processMemory(boolean doRead, boolean doWrite, int macroId, byte[] macroArray) {
        final byte[] macroData = new byte[macroSize];
        macroValid = false;
        if (doRead) {
            readRequested = true;
        }
        if (doWrite) {
            writeRequested = true;
            for (int i = 0; i < macroSize; i++) {
                macroData[i] = macroArray[i];
            }
        }
        // Set up a separate thread to access CS memory
        if (NceMemoryThread != null && NceMemoryThread.isAlive()) {
            return; // thread is already running
        }
        NceMemoryThread = new Thread(new Runnable() {
            public void run() {
                if (readRequested) {
                    macroNum = macroId;
                    int macroCount = 0;
                    while (true) {
                        int entriesRead = readMacroMemory(macroNum);
                        macroTextField.setText(Integer.toString(macroNum));
                        if (entriesRead == 0) {
                            // Macro is empty so init the accessory fields
                            initAccyFields();
                            macroReply.setText(rb.getString("macroEmpty"));
                            if (checkBoxEmpty.isSelected()) {
                                macroValid = true;
                                macroSearchInc = false;
                                macroSearchDec = false;
                                break;
                            }
                        } else if (entriesRead < 0) {
                            macroReply.setText(rb.getString("error"));
                            macroValid = false;
                            macroSearchInc = false;
                            macroSearchDec = false;
                            break;
                        } else {
                            macroReply.setText(rb.getString("macroFound"));
                            if (checkBoxEmpty.isSelected() == false) {
                                macroSearchInc = false;
                                macroSearchDec = false;
                                macroValid = true;
                                break;
                            }
                        }
                        if ((macroSearchInc || macroSearchDec) && !macroValid) {
                            macroCount++;
                            if (macroCount > maxNumMacros) {
                                macroSearchInc = false;
                                macroSearchDec = false;
                                break;
                            }
                            macroNum = getMacro();
                        }
                        if (!(macroSearchInc || macroSearchDec)) {
                            // we were doing a get, not a search
                            macroValid = true;
                            break;
                        }
                    }
                }
                if (writeRequested) {
                    writeMacroMemory(macroId, macroData);
                }
            }
        });
        NceMemoryThread.setName(rb.getString("ThreadTitle"));
        NceMemoryThread.setPriority(Thread.MIN_PRIORITY);
        NceMemoryThread.start();
    }

    // Reads 16/20 bytes of NCE macro memory
    private int readMacroMemory(int mN) {
        int entriesRead = 0;
        if (isUsb) {
            setUsbCabMemoryPointer(CabMemoryUsb.CAB_NUM_MACRO, (mN * macroSize));
            if (!waitNce()) {
                return -1;
            }
            // 1st word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            int accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy1, accyTextField1, cmdButton1,
                    deleteButton1);
            // 2nd word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy2, accyTextField2, cmdButton2,
                    deleteButton2);
            // 3rd word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy3, accyTextField3, cmdButton3,
                    deleteButton3);
            // 4th word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy4, accyTextField4, cmdButton4,
                    deleteButton4);
            // 5th word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy5, accyTextField5, cmdButton5,
                    deleteButton5);
            // 6th word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy6, accyTextField6, cmdButton6,
                    deleteButton6);
            // 7th word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy7, accyTextField7, cmdButton7,
                    deleteButton7);
            // 8th word of macro
            readUsbMemoryN(2);
            if (!waitNce()) {
                return -1;
            }
            accyAddr = getMacroAccyAdr(recChars);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(recChars), textAccy8, accyTextField8, cmdButton8,
                    deleteButton8);
            return entriesRead;
        } else {
            int memPtr = CabMemorySerial.CS_MACRO_MEM + (mN * macroSize);
            int readPtr = 0;
            int[] workBuf = new int[2];
            // 1st word of macro
            readSerialMemory16(memPtr);
            if (!waitNce()) {
                return -1;
            }
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            int accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy1, accyTextField1, cmdButton1,
                    deleteButton1);
            // 2nd word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy2, accyTextField2, cmdButton2,
                    deleteButton2);
            // 3rd word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy3, accyTextField3, cmdButton3,
                    deleteButton3);
            // 4th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy4, accyTextField4, cmdButton4,
                    deleteButton4);
            // 5th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy5, accyTextField5, cmdButton5,
                    deleteButton5);
            // 6th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy6, accyTextField6, cmdButton6,
                    deleteButton6);
            // 7th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy7, accyTextField7, cmdButton7,
                    deleteButton7);
            // 8th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy8, accyTextField8, cmdButton8,
                    deleteButton8);
            // 9th word of macro
            memPtr += 16;
            readPtr = 0;
            readSerialMemory16(memPtr);
            if (!waitNce()) {
                return -1;
            }
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy9, accyTextField9, cmdButton9,
                    deleteButton9);
            // 10th word of macro
            workBuf[0] = recChars[readPtr++];
            workBuf[1] = recChars[readPtr++];
            accyAddr = getMacroAccyAdr(workBuf);
            if (accyAddr <= 0) {
                return entriesRead;
            }
            entriesRead++;
            setAccy(accyAddr, getAccyCmd(workBuf), textAccy10, accyTextField10, cmdButton10,
                    deleteButton10);
            return entriesRead;
        }
    }

    // Updates the accessory line when the user hits the command button
    private void updateAccyCmdPerformed(JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton) {
        if (macroValid == false) { // Error user input incorrect
            JOptionPane.showMessageDialog(this,
                    rb.getString("GetMacroNumber"), rb.getString("NceMacro"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            String accyText = accyTextField.getText();
            int accyNum = 0;
            try {
                accyNum = Integer.parseInt(accyText);
            } catch (NumberFormatException e) {
                accyNum = -1;
            }

            if (accyNum < 1 | accyNum > 2044) {
                JOptionPane.showMessageDialog(this,
                        rb.getString("EnterAccessoryNumber"), rb.getString("NceMacroAddress"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String accyCmd = cmdButton.getText();

            // Use JMRI or NCE turnout terminology
            if (checkBoxNce.isSelected()) {

                if (accyCmd != THROWN_NCE) {
                    cmdButton.setText(THROWN_NCE);
                }
                if (accyCmd != CLOSED_NCE) {
                    cmdButton.setText(CLOSED_NCE);
                }

            } else {

                if (accyCmd != THROWN) {
                    cmdButton.setText(THROWN);
                }
                if (accyCmd != CLOSED) {
                    cmdButton.setText(CLOSED);
                }
            }

            setSaveButton(true);
            textAccy.setText(ACCESSORY);
            deleteButton.setText(DELETE);
            deleteButton.setToolTipText(rb.getString("toolTipRemoveAcessory"));
            deleteButton.setEnabled(true);
        }
    }

    // Delete an accessory from the macro
    private void updateAccyDelPerformed(JTextField accyTextField, JButton cmdButton, JLabel textAccy, JButton deleteButton) {
        setSaveButton(true);
        textAccy.setText(EMPTY);
        accyTextField.setText("");
        cmdButton.setText(QUESTION);
        deleteButton.setEnabled(false);
    }

    private int getAccyRow(byte[] b, int i, JLabel textAccy, JTextField accyTextField, JButton cmdButton) {
        int accyNum = 0;
        if (textAccy.getText() == ACCESSORY) {
            accyNum = getAccyNum(accyTextField.getText());
            if (accyNum < 0) {
                return accyNum;
            }
            accyNum = accyNum + 3;							// adjust for NCE's way of encoding
            int upperByte = (accyNum & 0xFF);
            upperByte = (upperByte >> 2) + 0x80;
            b[i] = (byte) upperByte;
            int lowerByteH = (((accyNum ^ 0x0700) & 0x0700) >> 4);// 3 MSB 1s complement
            int lowerByteL = ((accyNum & 0x3) << 1);       	// 2 LSB
            int lowerByte = (lowerByteH + lowerByteL + 0x88);
            if (cmdButton.getText() == CLOSED) // adjust for turnout command	
            {
                lowerByte++;
            }
            if (cmdButton.getText() == CLOSED_NCE) // adjust for turnout command	
            {
                lowerByte++;
            }
            b[i + 1] = (byte) (lowerByte);
        }
        if (textAccy.getText() == LINK) {
            int macroLink = validMacro(accyTextField.getText());
            if (macroLink < 0) {
                return macroLink;
            }
            b[i] = (byte) 0xFF;								// NCE macro link command
            b[i + 1] = (byte) macroLink;						// link macro number
        }
        return accyNum;
    }

    private int getAccyNum(String accyText) {
        int accyNum = 0;
        try {
            accyNum = Integer.parseInt(accyText);
        } catch (NumberFormatException e) {
            accyNum = -1;
        }
        if (accyNum < 1 | accyNum > 2044) {
            JOptionPane.showMessageDialog(this,
                    rb.getString("EnterAccessoryNumber"), rb.getString("NceMacroAddress"),
                    JOptionPane.ERROR_MESSAGE);
            accyNum = -1;
        }
        return accyNum;
    }

    // display save button
    private void setSaveButton(boolean display) {
        macroModified = display;
        saveButton.setEnabled(display);
        if (!isUsb) {
            backUpButton.setEnabled(!display);
            restoreButton.setEnabled(!display);
        }
    }

    // Convert NCE macro hex data to accessory address
    // returns 0 if macro address is empty
    // returns a negative address if link address
    // & loads accessory 10 with link macro
    private int getMacroAccyAdr(int[] b) {
        int accyAddrL = b[0];
        int accyAddr = 0;
        // check for null
        if ((accyAddrL == 0) && (b[1] == 0)) {
            return accyAddr;
        }
        // Check to see if link address
        if ((accyAddrL & 0xFF) == 0xFF) {
            // Link address
            accyAddr = b[1];
            linkAccessory10(accyAddr & 0xFF);
            accyAddr = -accyAddr;

            // must be an accessory address	
        } else {
            accyAddrL = (accyAddrL << 2) & 0xFC;			// accessory address bits 7 - 2
            int accyLSB = b[1];
            accyLSB = (accyLSB & 0x06) >> 1;				// accessory address bits 1 - 0
            int accyAddrH = b[1];
            accyAddrH = (0x70 - (accyAddrH & 0x70)) << 4; 	// One's completent of MSB of address 10 - 8
            // & multiply by 16
            accyAddr = accyAddrH + accyAddrL + accyLSB - 3; // adjust for the way NCE displays addresses
        }
        return accyAddr;
    }

    // whenever link macro is found, put it in the last location
    // this makes it easier for the user to edit the macro
    private void linkAccessory10(int accyAddr) {
        textAccy10.setText(LINK);
        accyTextField10.setText(Integer.toString(accyAddr));
        cmdButton10.setVisible(false);
        deleteButton10.setText(DELETE);
        deleteButton10.setToolTipText(rb.getString("toolTipRemoveMacroLink"));
    }

    // loads one row with a macro's accessory address and command
    private void setAccy(int accyAddr, String accyCmd, JLabel textAccy,
            JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
        textAccy.setText(ACCESSORY);
        accyTextField.setText(Integer.toString(accyAddr));
        deleteButton.setEnabled(true);
        cmdButton.setText(accyCmd);
    }

    // returns the accessory command
    private String getAccyCmd(int[] b) {
        int accyCmd = b[1];
        String s = THROWN;
        if (checkBoxNce.isSelected()) {
            s = THROWN_NCE;
        }
        accyCmd = accyCmd & 0x01;
        if (accyCmd == 0) {
            return s;
        } else {
            s = CLOSED;
            if (checkBoxNce.isSelected()) {
                s = CLOSED_NCE;
            }
        }
        return s;
    }

    /**
     * Check for valid macro, return number if valid, -1 if not.
     *
     * @param s - string of macro number
     * @return mN - int of macro number or -1 if invalid
     */
    private int validMacro(String s) {
        int mN;
        try {
            mN = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
        if (mN < 0 | mN > maxNumMacros) {
            return -1;
        } else {
            return mN;
        }
    }

    /**
     * writes bytes of NCE macro memory
     *
     * @param macroNum
     * @param b
     */
    private boolean writeMacroMemory(int macroNum, byte[] b) {
        if (isUsb) {
            setUsbCabMemoryPointer(CabMemoryUsb.CAB_NUM_MACRO, (macroNum * macroSize));
            if (!waitNce()) {
                return false;
            }
            for (int i = 0; i < macroSize; i++) {
                writeUsbMemory1(b[i]);
                if (!waitNce()) {
                    return false;
                }
            }
        } else {
            int nceMacroAddr = (macroNum * macroSize) + memBase;
            byte[] buf = new byte[macroSize];
            int i = 0;
            for (; i < 16; i++) {
                buf[i] = b[i];
            }
            writeSerialMemoryN(nceMacroAddr, buf, 16);
            if (!waitNce()) {
                return false;
            }
            for (; i < macroSize; i++) {
                buf[i] = b[i];
            }
            writeSerialMemory4(nceMacroAddr + 16, buf);
            if (!waitNce()) {
                return false;
            }
        }
        return true;
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
                macroReply.setText("Error");
                return false;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("awake!");
        }
        return true;
    }

    public void message(NceMessage m) {
    }  // ignore replies

    //	public void replyOrig(NceReply r) {
    //		// Macro command
    //		if (replyLen == NceMessage.REPLY_1) {
    //			// Looking for proper response
    //			int recChar = r.getElement(0);
    //			if (recChar == '!')
    //				macroReply.setText(rb.getString("okay"));
    //			if (recChar == '0')
    //				macroReply.setText(rb.getString("macroEmpty"));
    //		}
    //		// Macro memory read
    //		if (replyLen == NceMessage.REPLY_16) {
    //			// NCE macros consists of 20 bytes on serial, 16 on USB
    //			// so either 4 or 5 reads
    //			if (secondRead) {
    //				// Second memory read for accessories 9 and 10
    //				secondRead = false;
    //				loadAccy9and10(r);
    //
    //			} else {
    //				int recChar = r.getElement(0);
    //				recChar = recChar << 8;
    //				recChar = recChar + r.getElement(1);
    //				if (recChar == 0) {
    //					if (checkBoxEmpty.isSelected()) {
    //						if (macroCount > 0) {
    //							macroSearchInc = false;
    //							macroSearchDec = false;
    //						}
    //					}
    //					// Macro is empty so init the accessory fields
    //					macroReply.setText(rb.getString("macroEmpty"));
    //					initAccyFields();
    //					macroValid = true;
    //				} else {
    //					if (checkBoxEmpty.isSelected() == false) {
    //						if (macroCount > 0) {
    //							macroSearchInc = false;
    //							macroSearchDec = false;
    //						}
    //					}
    //					macroReply.setText(rb.getString("macroFound"));
    //					secondRead = loadAccy1to8(r);
    //					macroValid = true;
    //				}
    //				// if we're searching, don't bother with second read
    //				if (macroSearchInc || macroSearchDec)
    //					secondRead = false;
    //				// Do we need to read more CS memory?
    //				if (secondRead)
    //					// force second read of CS memory
    //					getMacro2ndHalf(macroNum);
    //				// when searching, have we read all of the possible
    //				// macros?
    //				macroCount++;
    //				if (macroCount > maxNumMacros) {
    //					macroSearchInc = false;
    //					macroSearchDec = false;
    //				}
    //				if (macroSearchInc) {
    //					macroNum++;
    //					if (macroNum == maxNumMacros + 1)
    //						macroNum = 0;
    //				}
    //				if (macroSearchDec) {
    //					macroNum--;
    //					if (macroNum == -1)
    //						macroNum = maxNumMacros;
    //				}
    //				if (macroSearchInc || macroSearchDec) {
    //					macroTextField.setText(Integer.toString(macroNum));
    //					macroNum = getMacro();
    //				}
    //			}
    //		}
    //	}
    /**
     * response from read
     *
     */
    int recChar = 0;
    int[] recChars = new int[16];

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "NN_NAKED_NOTIFY", justification = "Thread wait from main transfer loop")
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
            macroReply.setText("error");
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

    // USB set cab memory pointer
    private void setUsbCabMemoryPointer(int cab, int offset) {
        replyLen = NceMessage.REPLY_1;			// Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.usbMemoryPointer(cab, offset);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // USB Read N bytes of NCE cab memory 
    private void readUsbMemoryN(int num) {
        switch (num) {
            case 1:
                replyLen = NceMessage.REPLY_1;	// Expect 1 byte response
                break;
            case 2:
                replyLen = NceMessage.REPLY_2;	// Expect 2 byte response
                break;
            case 4:
                replyLen = NceMessage.REPLY_4;	// Expect 4 byte response
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

    /**
     * USB Write 1 byte of NCE memory
     *
     * @param value - byte being written
     */
    private void writeUsbMemory1(int value) {
        replyLen = NceMessage.REPLY_1;			// Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.usbMemoryWrite1((byte) value);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // Reads 16 bytes of NCE memory 
    private void readSerialMemory16(int nceCabAddr) {
        replyLen = NceMessage.REPLY_16;			// Expect 16 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryRead(nceCabAddr);
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_16);
        tc.sendNceMessage(m, this);
    }

    // Write N bytes of NCE memory 
    private void writeSerialMemoryN(int nceMacroAddr, byte[] x, int len) {
        replyLen = NceMessage.REPLY_1;			// Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryWriteN(nceMacroAddr, len);
        for (int i = 0; i < len; i++) {
            bl[3 + i] = x[i];
        }
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    // Write 4 bytes of NCE memory 
    private void writeSerialMemory4(int nceMacroAddr, byte[] x) {
        replyLen = NceMessage.REPLY_1;			// Expect 1 byte response
        waiting++;
        byte[] bl = NceBinaryCommand.accMemoryWrite4(nceMacroAddr);
        for (int i = 0; i < 4; i++) {
            bl[3 + i] = x[i];
        }
        NceMessage m = NceMessage.createBinaryMessage(tc, bl, NceMessage.REPLY_1);
        tc.sendNceMessage(m, this);
    }

    private void addAccyRow(JComponent col1, JComponent col2, JComponent col3, JComponent col4, JComponent col5, int row) {
        addItem(col1, 0, row);
        addItem(col2, 1, row);
        addItem(col3, 2, row);
        addItem(col4, 3, row);
        addItem(col5, 4, row);
    }

    private void addItem(JComponent c, int x, int y) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = x;
        gc.gridy = y;
        gc.weightx = 100.0;
        gc.weighty = 100.0;
        add(c, gc);
    }

    private void addButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionPerformed(e);
            }
        });
    }

    private void addButtonCmdAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionCmdPerformed(e);
            }
        });
    }

    private void addButtonDelAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonActionDeletePerformed(e);
            }
        });
    }

    private void addCheckBoxAction(JCheckBox cb) {
        cb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkBoxActionPerformed(e);
            }
        });
    }

    //  initialize accessories 1 to 10
    private void initAccyFields() {
        initAccyRow(1, num1, textAccy1, accyTextField1, cmdButton1, deleteButton1);
        initAccyRow(2, num2, textAccy2, accyTextField2, cmdButton2, deleteButton2);
        initAccyRow(3, num3, textAccy3, accyTextField3, cmdButton3, deleteButton3);
        initAccyRow(4, num4, textAccy4, accyTextField4, cmdButton4, deleteButton4);
        initAccyRow(5, num5, textAccy5, accyTextField5, cmdButton5, deleteButton5);
        initAccyRow(6, num6, textAccy6, accyTextField6, cmdButton6, deleteButton6);
        initAccyRow(7, num7, textAccy7, accyTextField7, cmdButton7, deleteButton7);
        initAccyRow(8, num8, textAccy8, accyTextField8, cmdButton8, deleteButton8);
        initAccyRow(9, num9, textAccy9, accyTextField9, cmdButton9, deleteButton9);
        initAccyRow(10, num10, textAccy10, accyTextField10, cmdButton10, deleteButton10);
    }

    private void initAccyRow(int row, JLabel num, JLabel textAccy, JTextField accyTextField, JButton cmdButton, JButton deleteButton) {
        num.setText(Integer.toString(row));
        num.setVisible(true);
        textAccy.setText(EMPTY);
        textAccy.setVisible(true);
        cmdButton.setText(QUESTION);
        cmdButton.setVisible(true);
        cmdButton.setToolTipText(rb.getString("toolTipSetCommand"));
        deleteButton.setText(DELETE);
        deleteButton.setVisible(true);
        deleteButton.setEnabled(false);
        deleteButton.setToolTipText(rb.getString("toolTipRemoveAcessory"));
        accyTextField.setText("");
        accyTextField.setToolTipText(rb.getString("EnterAccessoryNumber"));
        accyTextField.setMaximumSize(new Dimension(accyTextField
                .getMaximumSize().width,
                accyTextField.getPreferredSize().height));
        if (row == 10) {
            initAccyRow10();
        }
    }

    private void initAccyRow10() {
        cmdButton10.setVisible(true);
        deleteButton10.setText(LINK);
        deleteButton10.setEnabled(true);
        deleteButton10.setToolTipText(rb.getString("toolTipLink"));
        accyTextField10.setToolTipText(rb.getString("toolTip10"));
    }

    /**
     * Nested class to create one of these using old-style defaults
     */
    static public class Default extends jmri.jmrix.nce.swing.NceNamedPaneAction {

        /**
         *
         */
        private static final long serialVersionUID = 8301047661947770441L;

        public Default() {
            super("Open NCE Macro Editor",
                    new jmri.util.swing.sdi.JmriJFrameInterface(),
                    NceMacroEditPanel.class.getName(),
                    jmri.InstanceManager.getDefault(NceSystemConnectionMemo.class));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(NceMacroEditPanel.class.getName());
}
