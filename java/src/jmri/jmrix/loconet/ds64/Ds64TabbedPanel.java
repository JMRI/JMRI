package jmri.jmrix.loconet.ds64;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.jmrix.loconet.AbstractBoardProgPanel;
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.swing.ValidatedTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A "tabbed" swing panel to display and modify Digitrax DS64 board
 * configuration.
 * <p>
 * The read and write operations require a sequence of operations, which are
 * handled with a state variable.
 * <p>
 * Programming of the DS64 is done via LocoNet configuration messages, so the
 * DS64 should not be manually put into its programming mode via the DS64
 * built-in pushbutton while this tool is in use.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 * <p>
 * Extensions to include read/write of turnout output addresses and routes are
 * based on reverse-engineering of DS64 operating characteristics by B.
 * Milhaupt. As such, this tool may not be compatible with all DS64 devices.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * Based on Revision 1.1 of DS64Panel.java by Bob Jacobsen
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2004, 2005, 2007, 2010
 * @author B. Milhaupt Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016, 2017
 */
public class Ds64TabbedPanel extends AbstractBoardProgPanel {

    /**
     * Ds64TabbedPanel constructor when the boardNum is already known. Allows
     * the instantiating method to specify whether the basic feature
     * configuration information will be read upon instantiation.
     *
     * @param boardNum   initial BoardID number
     * @param readOnInit true to automatically read the basic configuration
     *                   info.
     */
    public Ds64TabbedPanel(int boardNum, boolean readOnInit) {
        super(boardNum, readOnInit, "DS64");
        origAccessBoardNum = boardNum;
        boardNumsEntryValue.add(boardNum);
    }

    /**
     * Ds64TabbedPanel constructor when the boardNum is not already known.
     * <p>
     * At instantiation, the object will automatically assume BoardID 1 and will
     * not pre-read the basic board configuration information.
     */
    public Ds64TabbedPanel() {
        // this is a constructor which is in-place to support legacy applications.
        this(1, false);
    }

    /**
     * Ds64TabbedPanel constructor when the boardNum is already known.
     * <p>
     * When instantiated, the object will not automatically read the basic
     * configuration information.
     *
     * @param boardNum initial BoardID number
     */
    public Ds64TabbedPanel(int boardNum) {
        this(boardNum, false);
        origAccessBoardNum = boardNum;
    }
    int[] boardNumbers;
    int origAccessBoardNum = 0;
    ArrayList<Integer> boardNumsEntryValue = new ArrayList<>();

    /**
     * Ds64TabbedPanel constructor which may be used when the instantiating
     * method already has an array of DS64 BoardID numbers; this array is used
     * to pre-populate the GUI combobox showing BoardID numbers. The first
     * BoardID number in the array will automatically be selected upon
     * instantiation.
     * <p>
     * When instantiated, the object will automatically read the basic
     * configuration information if readOnInit is true.
     *
     * @param readOnInit true to automatically read the basic configuration info
     *                   from the DS64 with BoardID equal to the first value in
     *                   the boardNums array
     * @param boardNums  Array of known DS64 BoardID numbers
     */
    public Ds64TabbedPanel(boolean readOnInit, Integer[] boardNums) {
        this(boardNums[0], readOnInit);
        log.debug("into DS64 tabbed panel with list of boards of length {}", boardNums.length); // NOI18N
        log.debug("boardNums[0] = {}", boardNums[0]); // NOI18N
        origAccessBoardNum = boardNums[0];
        boardNumsEntryValue.remove(0);  // remove the entry  added by Ds64TabbedPanel(int boardNum, boolean readOnInit)
        for (int boardNum : boardNums) {
            log.debug("board {}", boardNum); // NOI18N
            boardNumsEntryValue.add(boardNum);
        }
        Collections.sort(boardNumsEntryValue);
    }

    /**
     * Ds64TabbedPanel constructor when the boardNum is not known; BoardID 1 is
     * assumed.
     * <p>
     * Allows the instantiating method to specify whether the basic feature
     * configuration information will be read upon instantiation.
     *
     * @param readBoardOnInit true to automatically read the basic configuration
     *                        info.
     */
    public Ds64TabbedPanel(boolean readBoardOnInit) {
        this(1, readBoardOnInit);
    }

    JPanel generalPanel = null;
    JPanel opswsPanel = null;
    JScrollPane opswsScrollPane = null;
    JTabbedPane generalTabbedPane = null;
    JTabbedPane routesTabbedPane;
    JPanel opswsValues = null;
    JPanel outputAddrsPanel = null;
    ValidatedTextField outAddr1 = null;
    JLabel outState1 = null;
    ValidatedTextField outAddr2 = null;
    JLabel outState2 = null;
    ValidatedTextField outAddr3 = null;
    JLabel outState3 = null;
    ValidatedTextField outAddr4 = null;
    JLabel outState4 = null;
    JPanel[] routePanel;
    SimpleTurnoutStateEntry[] routeTop;
    SimpleTurnoutStateEntry[] routeA2;
    SimpleTurnoutStateEntry[] routeA3;
    SimpleTurnoutStateEntry[] routeA4;
    SimpleTurnoutStateEntry[] routeA5;
    SimpleTurnoutStateEntry[] routeA6;
    SimpleTurnoutStateEntry[] routeA7;
    SimpleTurnoutStateEntry[] routeA8;
    JToggleButton resetRouteButton = null;

    JComboBox<Integer> addressComboBox;

    // output controls
    JLabel outputTypeLabel;
    JComboBox<String> outputType;

    JLabel delayTimeLabel;
    JComboBox<String> delayTime;

    JLabel outputStatesLabel;
    JComboBox<String> outputStates;

    JLabel startupDelayLabel;
    JComboBox<String> startupDelay;

    JLabel staticOutputShutoffLabel;
    JComboBox<String> staticOutputShutoff;

    // command sources
    JLabel commandTypeLabel;
    JComboBox<String> commandType;

    JLabel commandSourceLabel;
    JComboBox<String> commandSource;

    // Crossbuck Flasher controls
    JCheckBox output1CrossbuckFlasherCheckBox;
    JCheckBox output2CrossbuckFlasherCheckBox;
    JCheckBox output3CrossbuckFlasherCheckBox;
    JCheckBox output4CrossbuckFlasherCheckBox;

    // DS64 routes
    JLabel routesControlLabel;
    JComboBox<String> routesControl;

    // local input controls
    JLabel localControlOfOutputsStyleLabel;
    JComboBox<String> localControlOfOutputsStyle;

    JLabel sensorMessageTriggerLabel;
    JComboBox<String> sensorMessageTrigger;

    JComboBox<String> localSensorType;

    JToggleButton factoryResetButton;

    JRadioButtonWithInteger[] opswThrown = new JRadioButtonWithInteger[21];
    JRadioButtonWithInteger[] opswClosed = new JRadioButtonWithInteger[21];

    JPanel sensorMessageTriggerPanel;
    JPanel localInputControlsPanel;

    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.loconet.ds64.DS64TabbedPanel"; // NOI18N
    }

    @Override
    public String getTitle() {
        return getTitle(Bundle.getMessage("MenuItemDS64Programmer"));
    }

    public javax.swing.Timer boardResetResponseTimer = null;

    public void updateGuiBasicOpSw(int index) {
        if ((index < 1) || (index == 7) || (index > 21)) {
            return;
        }
        if (opsw[index]) {
            opswThrown[index].setSelected(false);
            opswClosed[index].setSelected(true);
        } else {
            opswThrown[index].setSelected(true);
            opswClosed[index].setSelected(false);
        }
        opswThrown[index].updateUI();
        opswClosed[index].updateUI();
    }

    /**
     * Copy from the GUI to the opsw array.
     * <p>
     * Used before write operations start.
     */
    @Override
    protected void copyToOpsw() {
        // copy over the display
        opsw[1] = (outputType.getSelectedIndex() == 1);
        updateGuiBasicOpSw(1);
        int selection = delayTime.getSelectedIndex();
        opsw[2] = ((selection & 0x1) == 1);
        opsw[3] = ((selection & 0x2) == 2);
        opsw[4] = ((selection & 0x4) == 4);
        opsw[5] = ((selection & 0x8) == 8);
        updateGuiBasicOpSw(2);
        updateGuiBasicOpSw(3);
        updateGuiBasicOpSw(4);
        updateGuiBasicOpSw(5);
        opsw[6] = (outputStates.getSelectedIndex() == 1);
        updateGuiBasicOpSw(6);
        opsw[7] = (isWritingResetOpSw ? resetOpSwVal : false);
        updateGuiBasicOpSw(7);
        opsw[8] = startupDelay.getSelectedIndex() == 1;
        updateGuiBasicOpSw(8);
        opsw[9] = staticOutputShutoff.getSelectedIndex() == 1;
        updateGuiBasicOpSw(9);
        opsw[10] = commandType.getSelectedIndex() == 1;
        updateGuiBasicOpSw(10);
        opsw[11] = (routesControl.getSelectedIndex() == 1) || (routesControl.getSelectedIndex() == 3);
        updateGuiBasicOpSw(11);
        opsw[12] = (localControlOfOutputsStyle.getSelectedIndex() & 1) == 1;  //2 -> OpSw12="c"
        updateGuiBasicOpSw(12);
        opsw[13] = (sensorMessageTrigger.getSelectedIndex() == 1);
        updateGuiBasicOpSw(13);
        opsw[14] = commandSource.getSelectedIndex() == 1;
        updateGuiBasicOpSw(14);
        opsw[15] = (localControlOfOutputsStyle.getSelectedIndex() >= 2);  //0 -> OpSw15="c"
        updateGuiBasicOpSw(15);
        opsw[16] = routesControl.getSelectedIndex() >= 2;
        updateGuiBasicOpSw(16);
        opsw[17] = output1CrossbuckFlasherCheckBox.isSelected();
        updateGuiBasicOpSw(17);
        opsw[18] = output2CrossbuckFlasherCheckBox.isSelected();
        updateGuiBasicOpSw(18);
        opsw[19] = output3CrossbuckFlasherCheckBox.isSelected();
        updateGuiBasicOpSw(19);
        opsw[20] = output4CrossbuckFlasherCheckBox.isSelected();
        updateGuiBasicOpSw(20);
        opsw[21] = localSensorType.getSelectedIndex() == 1;
        updateGuiBasicOpSw(21);
    }
    java.awt.Component colorizedObject;

    @Override
    protected void updateDisplay() {
        switch (state) {
            case 1:
                outputType.setSelectedIndex((opsw[1] == true) ? 1 : 0);
                break;
            case 2:
            case 3:
            case 4:
            case 5:
                delayTime.setSelectedIndex(
                        ((opsw[2] == true) ? 1 : 0)
                        + ((opsw[3] == true) ? 2 : 0)
                        + ((opsw[4] == true) ? 4 : 0)
                        + ((opsw[5] == true) ? 8 : 0));
                break;
            case 6:
                outputStates.setSelectedIndex((opsw[6] == true) ? 1 : 0);
                break;
            case 8:
                startupDelay.setSelectedIndex((opsw[8] == true) ? 1 : 0);
                break;
            case 9:
                staticOutputShutoff.setSelectedIndex((opsw[9] == true) ? 1 : 0);
                break;
            case 10:
                commandType.setSelectedIndex((opsw[10] == true) ? 1 : 0);
                break;
            case 11:
            case 16:
                routesControl.setSelectedIndex((((opsw[16] == true) ? 2 : 0) + (opsw[11] ? 1 : 0)));
                break;
            case 15:
            case 12:
                localControlOfOutputsStyle.setSelectedIndex(((opsw[15] == true) ? 2 : 0) + (opsw[12] ? 1 : 0));
                break;
            case 13:
                sensorMessageTrigger.setSelectedIndex((opsw[13] == true) ? 1 : 0);   // selection 0 - only for A inputs; 1 - both A and S inputs
                break;
            case 14:
                commandSource.setSelectedIndex((opsw[14] == true) ? 1 : 0);
                break;
            case 17:
                output1CrossbuckFlasherCheckBox.setSelected(opsw[17]);
                break;
            case 18:
                output2CrossbuckFlasherCheckBox.setSelected(opsw[18]);
                break;
            case 19:
                output3CrossbuckFlasherCheckBox.setSelected(opsw[19]);
                break;
            case 20:
                output4CrossbuckFlasherCheckBox.setSelected(opsw[20]);

                break;
            case 21:
                localSensorType.setSelectedIndex(opsw[21] ? 1 : 0);
                break;
            default:
                // we are only interested in the states above. Ignore the rest
                log.debug("Unhandled state code: {}", state);
                break;
        }
        updateUI();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "Cannot catch an exception without grabbing the exception, but we don't do anything with the exception details.")
    protected int nextState(int state) {
        if (isWritingResetOpSw) {
            if ((state == 7) && (opsw[7] == true)) {
                opsw[7] = false;
                return 7;
            } else if (state == 7) {
                return 0;
            }
        }

        if (onlyOneOperation == true) {
            onlyOneOperation = false;
            return 0;
        }
        if ((state > 1)
                && (((isRead == true) && (readAllButton.isSelected() == false))
                || ((isRead == false)
                && ((writeAllButton.isSelected() == false)
                && (resetRouteButton.isSelected() == false))))) {
            // handle case where a button is de-selected by the user during the operation

            Color noAccessColor = ValidatedTextField.COLOR_BG_UNEDITED;
            if ((operationType == OpSwOpType.BasicsRead)
                    || (operationType == OpSwOpType.BasicsWrite)) {
                unhighlightAllBasicOpSws();
                unhighlightAllOutputEntryFields();
                unhighlightAllRouteEntryFields();
                return 0;
            } else if ((operationType == OpSwOpType.OutputsRead)
                    || (operationType == OpSwOpType.OutputsWrite)
                    || (operationType == OpSwOpType.Route1Read)
                    || (operationType == OpSwOpType.Route1Write)
                    || (operationType == OpSwOpType.Route2Read)
                    || (operationType == OpSwOpType.Route2Write)
                    || (operationType == OpSwOpType.Route3Read)
                    || (operationType == OpSwOpType.Route3Write)
                    || (operationType == OpSwOpType.Route4Read)
                    || (operationType == OpSwOpType.Route4Write)
                    || (operationType == OpSwOpType.Route5Read)
                    || (operationType == OpSwOpType.Route5Write)
                    || (operationType == OpSwOpType.Route6Read)
                    || (operationType == OpSwOpType.Route6Write)
                    || (operationType == OpSwOpType.Route7Read)
                    || (operationType == OpSwOpType.Route7Write)
                    || (operationType == OpSwOpType.Route8Read)
                    || (operationType == OpSwOpType.Route8Write)) {
                // handle stopping of indirect access operations
                if (state == 48) {
                    // for DS64, indirect operations for output addresses or route entries can be
                    // aborted after the first 16 indirect bits are accessed
                    changeComponentBgColor(whichComponent(33, indexToRead), noAccessColor);
                    log.debug("Decided to stop read/write after OpSw 48 because no read/write button selected."); // NOI18N
                    return 0;
                } else if (state == 64) {
                    // for DS64, indirect operations for output addresses or route entries can be
                    // aborted after the second 16 indirect bits are accessed
                    changeComponentBgColor(whichComponent(49, indexToRead), noAccessColor);
                    log.debug("Decided to stop read/write after OpSw 64 because no read/write button selected."); // NOI18N
                    return 0;
                }
            }
        }

        switch (state) {
            case 1: {
                if (colorizedObject == null) {
                    colorizedObject = outputType;
                }
                colorizedObject.setBackground(null);
                isRead = read;
                indexToRead = 0;
                if ((operationType == null)
                        || (operationType == OpSwOpType.BasicsRead)
                        || (operationType == OpSwOpType.BasicsWrite)) {
                    colorizedObject = delayTime;
                    colorizedObject.setBackground(Color.blue.brighter());
                    return 2;
                } else if ((operationType == OpSwOpType.OutputsRead)
                        || (operationType == OpSwOpType.OutputsWrite)) {
                    indexToRead = 0;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route1Read) || (operationType == OpSwOpType.Route1Write)) {
                    indexToRead = 16;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route2Read) || (operationType == OpSwOpType.Route2Write)) {
                    indexToRead = 20;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route3Read) || (operationType == OpSwOpType.Route3Write)) {
                    indexToRead = 24;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route4Read) || (operationType == OpSwOpType.Route4Write)) {
                    indexToRead = 28;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route5Read) || (operationType == OpSwOpType.Route5Write)) {
                    indexToRead = 32;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route6Read) || (operationType == OpSwOpType.Route6Write)) {
                    indexToRead = 36;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route7Read) || (operationType == OpSwOpType.Route7Write)) {
                    indexToRead = 40;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                } else if ((operationType == OpSwOpType.Route8Read) || (operationType == OpSwOpType.Route8Write)) {
                    indexToRead = 44;
                    read = false;               // want to write opSw 25 thru 32
                    setOpSwIndex(indexToRead);  //set values for opSw25 thru 32 to point to correct index
                    return 25;
                }
                return 0;
            }
            case 2: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = delayTime;
                colorizedObject.setBackground(Color.blue.brighter());
                return 3;
            }
            case 3: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = delayTime;
                colorizedObject.setBackground(Color.blue.brighter());
                return 4;
            }
            case 4: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = delayTime;
                colorizedObject.setBackground(Color.blue.brighter());
                return 5;
            }
            case 5: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = outputStates;
                colorizedObject.setBackground(Color.blue.brighter());
                return 6;
            }
            case 6: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = startupDelay;
                colorizedObject.setBackground(Color.blue.brighter());
                return 8;// 7 has to be done last, as it's reset
            }
            case 8: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = staticOutputShutoff;
                colorizedObject.setBackground(Color.blue.brighter());
                return 9;
            }
            case 9: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = commandType;
                colorizedObject.setBackground(Color.blue.brighter());
                return 10;
            }
            case 10: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = routesControl;
                colorizedObject.setBackground(Color.blue.brighter());
                return 11;
            }
            case 11: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = localControlOfOutputsStyle;
                colorizedObject.setBackground(Color.blue.brighter());
                return 12;
            }
            case 12: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = sensorMessageTrigger;
                colorizedObject.setBackground(Color.blue.brighter());
                return 13;
            }
            case 13: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = commandSource;
                colorizedObject.setBackground(Color.blue.brighter());
                return 14;
            }
            case 14: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = localControlOfOutputsStyle;
                colorizedObject.setBackground(Color.blue.brighter());
                return 15;
            }
            case 15: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = routesControl;
                colorizedObject.setBackground(Color.blue.brighter());
                return 16;
            }
            case 16: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = output1CrossbuckFlasherCheckBox;
                colorizedObject.setBackground(Color.blue.brighter());
                return 17;
            }
            case 17: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = output2CrossbuckFlasherCheckBox;
                colorizedObject.setBackground(Color.blue.brighter());
                return 18;
            }
            case 18: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = output3CrossbuckFlasherCheckBox;
                colorizedObject.setBackground(Color.blue.brighter());
                return 19;
            }
            case 19: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = output4CrossbuckFlasherCheckBox;
                colorizedObject.setBackground(Color.blue.brighter());
                return 20;
            }
            case 20: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                colorizedObject = localSensorType;
                colorizedObject.setBackground(Color.blue.brighter());
                return 21;
            }
            case 21: {
                if (colorizedObject != null) {
                    colorizedObject.setBackground(null);
                }
                this.readAllButton.setEnabled(true);
                this.writeAllButton.setEnabled(true);
                return 0;
            }
            case 22: {
                return 0;
            }
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31: {
                // handle indirect index bits
                return state + 1;
            }
            case 32: {
                read = isRead;               // go back to original mode of operation
                log.debug("Dealing with index {}", indexToRead);
                changeGuiElementHighlight(33, indexToRead);
                if (isRead == true) {
                    return 46;  // want to read "out-of-turn" to speed up reads when
                    // a route entry is disabled
                } else {
                    // prepare values in opsw[] from appropriate write values
                    updateOpswForWrite(indexToRead);
                    return 33;
                }
            }
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 46:
            case 47:
                return state + 1;
            case 45:
                if (isRead) {
                    // Have already read OpSws 64-48 and determined that there is
                    // a valid value in bits 33-45.  Deal with that velue, then
                    // go to the validity bits for the other half.
                    int extractedDataValue = 0;
                    for (int i = 48; i >= 33; i--) {
                        extractedDataValue = (extractedDataValue << 1) + (opsw[i] ? 1 : 0);
                    }
                    log.debug("Read Index {} value (OpSws 33-48) = 0x{}", indexToRead + 1, Integer.toHexString(extractedDataValue));
                    updateGuiFromOpSws33_48();
                    changeGuiElementHighlight(48, indexToRead); // clear the highlighted GUI element

                    return 62;  // because OpSws 46, 47, and 48 were read "out-
                    // of-turn" for speediness, and also want to read OpSws
                    // 62-64 "out-of-turn" for speediness
                } else {
                    return 46;
                }
            case 48: {
                changeGuiElementHighlight(48, indexToRead);
                if (isRead == true) {
                    // For reads, check the upper bits of this "half" to determine
                    // whether or not to read the remaining bits in the "half"

                    if ((opsw[47] == false) && (opsw[48] == false)) {
                        // entry is valid, so need to read all opSw bits in [46:33]
                        log.debug("Read of low value in index {} is a valid entry.", indexToRead); // NOI18N
                        return 33;
                    } else {
                        log.debug("Read of low value in index {} is an invalid entry.", indexToRead); // NOI18N
                        // entry is not valid, so do not need to read opSw bits in [46:33]
                        // Do need to update internal opSw bits so that they imply an unused
                        // entry
                        changeGuiElementUnHighlight(48, indexToRead);
                        for (int i = 33; i < 46; ++i) {
                            opsw[i] = true;
                        }
                        opsw[40] = false;

                        // need to update the GUI
                        updateGuiFromOpSws33_48();
                        changeGuiElementHighlight(48, indexToRead); // clear the highlighted GUI element

                        return 62; // need to skip ahead to the validity bits of the
                        // next entry
                    }
                } else {
                    // handle the case for writes

                    int extractedDataValue = 0;
                    for (int i = 48; i >= 33; i--) {
                        extractedDataValue = (extractedDataValue << 1) + (opsw[i] ? 1 : 0);
                    }
                    log.debug("Wrote Index {} value (OpSws 33-48) = 0x{}", indexToRead + 1, Integer.toHexString(extractedDataValue));
                    updateGuiFromOpSws33_48();
                    switch (indexToRead) {
                        case 0: {
                            // have written a value for output1 - update GUI
                            outAddr1.setLastQueriedValue(outAddr1.getText());
                            break;
                        }
                        case 1: {
                            // have written a value for output3 - update GUI
                            outAddr3.setLastQueriedValue(outAddr3.getText());
                            break;
                        }
                        case 16:
                        case 20:
                        case 24:
                        case 28:
                        case 32:
                        case 36:
                        case 40:
                        case 44: {
                            // have written value for Route[n] Top entry - update GUI
                            Integer effectiveIndex = (indexToRead - 12) / 4;
                            routeTop[effectiveIndex].addressField.setLastQueriedValue(routeTop[effectiveIndex].addressField.getText());
                            try {
                                routeTop[effectiveIndex].setAddress(Integer.parseInt(routeTop[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeTop[effectiveIndex].setIsUnused();
                            }
                            break;
                        }
                        case 17:
                        case 21:
                        case 25:
                        case 29:
                        case 33:
                        case 37:
                        case 41:
                        case 45: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 13) / 4;
                            routeA3[effectiveIndex].addressField.setLastQueriedValue(routeA3[effectiveIndex].addressField.getText());
                            try {
                                routeA3[effectiveIndex].setAddress(Integer.parseInt(routeA3[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA3[effectiveIndex].setIsUnused();

                            }
                            break;
                        }
                        case 18:
                        case 22:
                        case 26:
                        case 30:
                        case 34:
                        case 38:
                        case 42:
                        case 46: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 14) / 4;
                            routeA5[effectiveIndex].addressField.setLastQueriedValue(routeA5[effectiveIndex].addressField.getText());
                            try {
                                routeA5[effectiveIndex].setAddress(Integer.parseInt(routeA5[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA5[effectiveIndex].setIsUnused();
                            }
                            break;
                        }
                        case 19:
                        case 23:
                        case 27:
                        case 31:
                        case 35:
                        case 39:
                        case 43:
                        case 47: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 15) / 4;
                            routeA7[effectiveIndex].addressField.setLastQueriedValue(routeA7[effectiveIndex].addressField.getText());
                            try {
                                routeA7[effectiveIndex].setAddress(Integer.parseInt(routeA7[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA7[effectiveIndex].setIsUnused();
                            }
                            break;
                        }
                        default:
                            log.error("invalid indirectIndex for write: {}", indexToRead); // NOI18N
                            return 0;
                    }
                    return 49;
                }
            }
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 62:
            case 63:
                return (state + 1);
            case 55:
                return 57; // skip apparantly-unused bit to see if this reduces amount of output state disruption during read
            case 61:
                if (isRead) {
                    int extractedDataValue = 0;
                    for (int i = 64; i >= 49; i--) {
                        extractedDataValue = (extractedDataValue << 1) + (opsw[i] ? 1 : 0);
                    }
                    log.debug("Read Index {} value (OpSws 49-64) = 0x{}", indexToRead + 1, Integer.toHexString(extractedDataValue));
                    updateGuiFromOpSws49_64();
                    changeGuiElementHighlight(61, indexToRead);
                    return determineNextStateForRead();
                } else {
                    return 62;
                }
            case 64: {
                if (isRead == true) {
                    // For reads, check the upper bits of this "half" to determine
                    // whether or not to read the remaining bits in the "half"

                    if ((opsw[63] == false) && (opsw[64] == false)) {
                        // entry is valid, so need to read all opSw bits in [62:49]
                        log.debug("Read of high value in index {} is a valid entry.", indexToRead); // NOI18N
                        changeGuiElementUnHighlight(64, indexToRead);
                        return 49;
                    } else {
                        // entry is not valid, so do not need to read opSw bits in [46:33]
                        // Do need to update internal opSw bits so that they imply an unused
                        // entry
                        log.debug("Read of high value in index {} is an invalid entry.", indexToRead); // NOI18N
                        for (int i = 49; i < 62; ++i) {
                            opsw[i] = true;
                        }
                        opsw[56] = false;

                        // need to update GUI to show unused value
                        updateGuiFromOpSws49_64();
                        changeGuiElementHighlight(64, indexToRead); // clear the highlighted GUI element

                        return determineNextStateForRead();
                    }
                } // end handling of read operation
                else {
                    //handle write operation
                    // skip to next index, or, if done with indexables,
                    // go to end.
                    changeGuiElementHighlight(64, indexToRead); // clear the highlighted GUI element
                    switch (indexToRead) {
                        case 0: {
                            // have written a value for output2 - update GUI
                            outAddr2.setLastQueriedValue(outAddr2.getText());
                            outAddr2.repaint();
                            indexToRead++;
                            read = false;
                            setOpSwIndex(indexToRead);
                            return 25;
                        }
                        case 1: {
                            // have written a value for output4 - update GUI
                            outAddr4.setLastQueriedValue(outAddr4.getText());
                            outAddr4.repaint();
                            this.readAllButton.setEnabled(true);
                            this.writeAllButton.setEnabled(true);
                            return 0;
                        }
                        case 16:
                        case 20:
                        case 24:
                        case 28:
                        case 32:
                        case 36:
                        case 40:
                        case 44: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 12) / 4;
                            routeA2[effectiveIndex].addressField.setLastQueriedValue(routeA2[effectiveIndex].addressField.getText());
                            try {
                                routeA2[effectiveIndex].setAddress(Integer.parseInt(routeA2[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA2[effectiveIndex].setIsUnused();
                            }
                            indexToRead++;
                            read = false;
                            setOpSwIndex(indexToRead);
                            return 25;
                        }
                        case 17:
                        case 21:
                        case 25:
                        case 29:
                        case 33:
                        case 37:
                        case 41:
                        case 45: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 13) / 4;
                            routeA4[effectiveIndex].addressField.setLastQueriedValue(routeA4[effectiveIndex].addressField.getText());
                            try {
                                routeA4[effectiveIndex].setAddress(Integer.parseInt(routeA4[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA4[effectiveIndex].setIsUnused();
                            }
                            indexToRead++;
                            read = false;
                            setOpSwIndex(indexToRead);
                            return 25;
                        }
                        case 18:
                        case 22:
                        case 26:
                        case 30:
                        case 34:
                        case 38:
                        case 42:
                        case 46: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 14) / 4;
                            routeA6[effectiveIndex].addressField.setLastQueriedValue(routeA6[effectiveIndex].addressField.getText());
                            try {
                                routeA6[effectiveIndex].setAddress(Integer.parseInt(routeA6[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA6[effectiveIndex].setIsUnused();
                            }
                            indexToRead++;
                            read = false;
                            setOpSwIndex(indexToRead);
                            return 25;
                        }
                        case 19:
                        case 23:
                        case 27:
                        case 31:
                        case 35:
                        case 39:
                        case 43:
                        case 47: {
                            // have written a value for Route n - update GUI
                            Integer effectiveIndex = (indexToRead - 15) / 4;
                            routeA8[effectiveIndex].addressField.setLastQueriedValue(routeA8[effectiveIndex].addressField.getText());
                            try {
                                routeA8[effectiveIndex].setAddress(Integer.parseInt(routeA8[effectiveIndex].addressField.getText()));
                            } catch (NumberFormatException e) {
                                routeA8[effectiveIndex].setIsUnused();
                            }
                            indexToRead++;
                            read = false;
                            setOpSwIndex(indexToRead);
                            return 0;
                        }
                        default: {
                            return 0;
                        }
                    }
                } // end handling for write operations
            }

            case 7: {
                this.readAllButton.setEnabled(true);
                this.writeAllButton.setEnabled(true);
                log.warn("Board has been reset.  The board will now respond at Address 1."); // NOI18N
                return 0;
            }       // done!
            default:
                log.error("unexpected state {}", state); // NOI18N
                this.readAllButton.setEnabled(true);
                this.writeAllButton.setEnabled(true);
                return 0;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", 
                justification = "False positive on the implied local variable in indexToRead++")
    private int determineNextStateForRead() {
        switch (indexToRead) {
            case 1: {
                // have read output addresses 1, 2, 3, and 4.  No more to
                // read (for this tab of the GUI).
                this.readAllButton.setEnabled(true);
                this.writeAllButton.setEnabled(true);
                return 0;
            }
            case 0:
            case 16:
            case 20:
            case 24:
            case 28:
            case 32:
            case 36:
            case 40:
            case 44:
            case 17:
            case 21:
            case 25:
            case 29:
            case 33:
            case 37:
            case 41:
            case 45:
            case 18:
            case 22:
            case 26:
            case 30:
            case 34:
            case 38:
            case 42:
            case 46: {
                // have read a value for Route n.  go to next value if necessary
                indexToRead++;
                read = false;
                setOpSwIndex(indexToRead);
                return 25;
            }
            case 19:
            case 23:
            case 27:
            case 31:
            case 35:
            case 39:
            case 43:
            case 47: {
                // have read last values for Route n.  No next value so stop.
                indexToRead++;
                read = false;
                setOpSwIndex(indexToRead);
                return 0;
            }
            default:
                return 0;
        }
    }

    private void changeComponentBgColor(JComponent comp, Color color) {
        comp.setBackground(color);
    }

    private JComponent whichComponent(Integer reportedState, Integer reportedIndexToRead) {
        if (reportedState == 33) {
            switch (reportedIndexToRead) {
                case 0:
                    return outAddr1;

                case 1:
                    return outAddr3;

                case 16:
                case 20:
                case 24:
                case 28:
                case 32:
                case 36:
                case 40:
                case 44:
                    return routeTop[(reportedIndexToRead - 12) / 4].addressField;

                case 17:
                case 21:
                case 25:
                case 29:
                case 33:
                case 37:
                case 41:
                case 45:
                    return routeA3[(reportedIndexToRead - 13) / 4].addressField;

                case 18:
                case 22:
                case 26:
                case 30:
                case 34:
                case 38:
                case 42:
                case 46:
                    return routeA5[(reportedIndexToRead - 14) / 4].addressField;

                case 19:
                case 23:
                case 27:
                case 31:
                case 35:
                case 39:
                case 43:
                case 47:
                    return routeA7[(reportedIndexToRead - 15) / 4].addressField;

                default:
                    return null;
            }
        } else if (reportedState == 49) {
            switch (reportedIndexToRead) {
                case 0:
                    return outAddr2;

                case 1:
                    return outAddr4;

                case 16:
                case 20:
                case 24:
                case 28:
                case 32:
                case 36:
                case 40:
                case 44:
                    return routeA2[(reportedIndexToRead - 12) / 4].addressField;

                case 17:
                case 21:
                case 25:
                case 29:
                case 33:
                case 37:
                case 41:
                case 45:
                    return routeA4[(reportedIndexToRead - 13) / 4].addressField;

                case 18:
                case 22:
                case 26:
                case 30:
                case 34:
                case 38:
                case 42:
                case 46:
                    return routeA6[(reportedIndexToRead - 14) / 4].addressField;

                case 19:
                case 23:
                case 27:
                case 31:
                case 35:
                case 39:
                case 43:
                case 47:
                    return routeA8[(reportedIndexToRead - 15) / 4].addressField;

                default:
                    return null;
            }
        }
        return null;
    }

    private void changeGuiElementUnHighlight(Integer reportedState, Integer reportedIndexToRead) {
        log.debug("changedGuiElementUnHiglight st={} index={}", reportedState, reportedIndexToRead);
        JComponent jc;
        switch (reportedState) {
            case 33:
                return;
            case 45:
            case 48:
                jc = whichComponent(33, reportedIndexToRead);
                if (jc != null) {
                    changeComponentBgColor(jc, null); // inherit from parent
                }
                return;
            case 61:
            case 64:
                jc = whichComponent(49, reportedIndexToRead);
                if (jc != null) {
                    changeComponentBgColor(jc, null); // inherit from parent
                }
                break;
            default:
                // nothing to do in this case
                break;
        }
    }

    private void changeGuiElementHighlight(Integer reportedState, Integer reportedIndexToRead) {
        log.debug("changedGuiElementHiglight st={} index={}", reportedState, reportedIndexToRead);
        Color accessColor = Color.blue.brighter();
        JComponent jc;
        if (reportedState == 33) {
            jc = whichComponent(reportedState, reportedIndexToRead);
            changeComponentBgColor(jc, accessColor);
        }
        if (reportedState == 48) {
            changeGuiElementUnHighlight(33, reportedIndexToRead);
            jc = whichComponent(49, reportedIndexToRead);
            if (jc != null) {
                changeComponentBgColor(jc, accessColor);
            }
        }
        if (reportedState == 64) {
            jc = whichComponent(49, reportedIndexToRead);
            changeComponentBgColor(jc, null); // inherit from parent component
        }
    }

    private boolean alreadyKnowThisBoardId(Integer id) {
        return (boardNumsEntryValue.contains(id));
    }

    private Integer addBoardIdToList(Integer id) {
        boardNumsEntryValue.add(boardNumsEntryValue.size(), id);
        addressComboBox.removeAllItems();
        Collections.sort(boardNumsEntryValue);
        Integer indexOfTargetBoardAddress = 0;
        for (Integer index = 0; index < boardNumsEntryValue.size(); ++index) {
            if (boardNumsEntryValue.get(index).equals(id)) {
                indexOfTargetBoardAddress = index;
            }
            addressComboBox.addItem(boardNumsEntryValue.get(index));
        }
        return indexOfTargetBoardAddress;
    }

    private void selectBoardIdByIndex(Integer index) {
        addressComboBox.setSelectedIndex(index);
    }

    @Override
    public void readAll() {
        addrField.setText(addressComboBox.getSelectedItem().toString());

        Integer curAddr = Integer.parseInt(addrField.getText());

        // If a new board address is specified, add it (and sort it) into the current list.
        if (!alreadyKnowThisBoardId(curAddr)) {
            Integer index = addBoardIdToList(curAddr);
            selectBoardIdByIndex(index);
        }
        if (generalTabbedPane.getSelectedComponent().getClass() == JPanel.class) {
            if (((JPanel) generalTabbedPane.getSelectedComponent()) == generalPanel) {
                operationType = OpSwOpType.BasicsRead;
            } else if (((JPanel) generalTabbedPane.getSelectedComponent()) == outputAddrsPanel) {
                operationType = OpSwOpType.OutputsRead;
            }
        } else if (generalTabbedPane.getSelectedComponent().getClass() == JScrollPane.class) {
            if (((JScrollPane) generalTabbedPane.getSelectedComponent()) == opswsScrollPane) {
                operationType = OpSwOpType.BasicsRead;
            }
        } else if (generalTabbedPane.getSelectedComponent().getClass() == JTabbedPane.class) {
            if (((JTabbedPane) generalTabbedPane.getSelectedComponent() == routesTabbedPane)) {
                if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[1]) {
                    operationType = OpSwOpType.Route1Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[2]) {
                    operationType = OpSwOpType.Route2Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[3]) {
                    operationType = OpSwOpType.Route3Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[4]) {
                    operationType = OpSwOpType.Route4Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[5]) {
                    operationType = OpSwOpType.Route5Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[6]) {
                    operationType = OpSwOpType.Route6Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[7]) {
                    operationType = OpSwOpType.Route7Read;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[8]) {
                    operationType = OpSwOpType.Route8Read;
                } else {
                    log.error("DS64 TabbedPanel into readAll(): no known Route[n] tab selected.");
                    return;
                }
            } else {
                log.error("DS64 TabbedPanel into ReadAll(): no selected tab group");
                return;
            }
        } else {
            return;
        }
        super.readAll();
    }

    @Override
    public void writeAll() {
        addrField.setText(addressComboBox.getSelectedItem().toString());

        Integer curAddr = Integer.parseInt(addrField.getText());

        // If a new board address is specified, add it (and sort it) into the current list.
        if (!boardNumsEntryValue.contains(curAddr)) {
            boardNumsEntryValue.add(boardNumsEntryValue.size(), curAddr);
            addressComboBox.removeAllItems();
            Collections.sort(boardNumsEntryValue);
            Integer indexOfTargetBoardAddress = 0;
            for (Integer index = 0; index < boardNumsEntryValue.size(); ++index) {
                if (boardNumsEntryValue.get(index).equals(curAddr)) {
                    indexOfTargetBoardAddress = index;
                }
                addressComboBox.addItem(boardNumsEntryValue.get(index));
            }
            addressComboBox.setSelectedIndex(indexOfTargetBoardAddress);
        }

        if (generalTabbedPane.getSelectedComponent().getClass() == JPanel.class) {
            if (((JPanel) generalTabbedPane.getSelectedComponent()) == generalPanel) {
                operationType = OpSwOpType.BasicsWrite;
            } else if (((JPanel) generalTabbedPane.getSelectedComponent()) == outputAddrsPanel) {
                operationType = OpSwOpType.OutputsWrite;
            }
        } else if (generalTabbedPane.getSelectedComponent().getClass() == JScrollPane.class) {
            if (((JScrollPane) generalTabbedPane.getSelectedComponent()) == opswsScrollPane) {
                operationType = OpSwOpType.BasicsWrite;
            }

        } else if (generalTabbedPane.getSelectedComponent().getClass() == JTabbedPane.class) {
            if (((JTabbedPane) generalTabbedPane.getSelectedComponent()) == routesTabbedPane) {

                if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[1]) {
                    operationType = OpSwOpType.Route1Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[2]) {
                    operationType = OpSwOpType.Route2Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[3]) {
                    operationType = OpSwOpType.Route3Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[4]) {
                    operationType = OpSwOpType.Route4Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[5]) {
                    operationType = OpSwOpType.Route5Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[6]) {
                    operationType = OpSwOpType.Route6Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[7]) {
                    operationType = OpSwOpType.Route7Write;
                } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[8]) {
                    operationType = OpSwOpType.Route8Write;
                } else {
                    return;
                }
            } else {
                return;
            }
        } else {
            return;
        }

        super.writeAll();
    }

    private enum OpSwOpType {
        OutputsRead, OutputsWrite,
        Route1Read, Route1Write,
        Route2Read, Route2Write,
        Route3Read, Route3Write,
        Route4Read, Route4Write,
        Route5Read, Route5Write,
        Route6Read, Route6Write,
        Route7Read, Route7Write,
        Route8Read, Route8Write,
        BasicsRead, BasicsWrite
    }

    private OpSwOpType operationType = null;
    Boolean isRead;
    Boolean isWritingResetOpSw = false;
    Integer indexToRead = 0;
    Boolean resetOpSwVal = false;

    /**
     * Set index into OpSw table
     *
     * @param index  the indirect address
     */
    protected void setOpSwIndex(int index) {
        opsw[25] = (index & 1) == 1;
        opsw[26] = (index & 2) == 2;
        opsw[27] = (index & 4) == 4;
        opsw[28] = (index & 8) == 8;
        opsw[29] = (index & 16) == 16;
        opsw[30] = (index & 32) == 32;
        opsw[31] = (index & 64) == 64;
        opsw[32] = (index & 128) == 128;
    }

    /**
     * Updates data register to reflect address, state, and enable for two
     * turnouts.
     *
     * @param address1   first turnout address
     * @param state1     first turnout's state
     * @param is1Unused  true if first turnout entry is to be "unused"
     * @param address2   second turnout address
     * @param state2     second turnout's state
     * @param is2Unused  true if second turnout entry is to be "unused"
     */
    protected void updateOpSwsOutAddr(int address1, boolean state1, boolean is1Unused, int address2, boolean state2, boolean is2Unused) {
        int addr1 = address1 - 1;
        int addr2 = address2 - 1;
        if ((address1 == 0) || (is1Unused)) {
            addr1 = 2047;
            is1Unused = true;
        }
        if ((address2 == 0) || (is2Unused)) {
            addr2 = 2047;
            is2Unused = true;
        }
        opsw[33] = ((addr1 & 1) == 1);
        opsw[34] = ((addr1 & 2) == 2);
        opsw[35] = ((addr1 & 4) == 4);
        opsw[36] = ((addr1 & 8) == 8);
        opsw[37] = ((addr1 & 16) == 16);
        opsw[38] = ((addr1 & 32) == 32);
        opsw[39] = ((addr1 & 64) == 64);
        opsw[40] = false;
        opsw[41] = ((addr1 & 128) == 128);
        opsw[42] = ((addr1 & 256) == 256);
        opsw[43] = ((addr1 & 512) == 512);
        opsw[44] = ((addr1 & 1024) == 1024);
        opsw[45] = true;
        opsw[46] = state1;
        if (!is1Unused) {
            opsw[47] = false;
            opsw[48] = false;
        } else {
            opsw[47] = true;
            opsw[48] = true;
        }

        opsw[49] = ((addr2 & 1) == 1);
        opsw[50] = ((addr2 & 2) == 2);
        opsw[51] = ((addr2 & 4) == 4);
        opsw[52] = ((addr2 & 8) == 8);
        opsw[53] = ((addr2 & 16) == 16);
        opsw[54] = ((addr2 & 32) == 32);
        opsw[55] = ((addr2 & 64) == 64);
        opsw[56] = false;
        opsw[57] = ((addr2 & 128) == 128);
        opsw[58] = ((addr2 & 256) == 256);
        opsw[59] = ((addr2 & 512) == 512);
        opsw[60] = ((addr2 & 1024) == 1024);
        opsw[61] = true;
        opsw[62] = state2;
        if (!is2Unused) {
            opsw[63] = false;
            opsw[64] = false;
        } else {
            opsw[63] = true;
            opsw[64] = true;
        }
    }

    /**
     * Updates OpSw values for a given index into the data array
     *
     * @param index  indirect address
     */
    protected void updateOpswForWrite(int index) {
        Integer value1Address;
        Integer value2Address;
        boolean value1IsUnused;
        boolean value2IsUnused;
        boolean value1DirectionIsClosed;
        boolean value2DirectionIsClosed;

        switch (index) {
            case 0: {
                try {
                    value1Address = Integer.parseInt(outAddr1.getText());
                } catch (NumberFormatException e) {
                    value1Address = 2048;
                }

                try {
                    value2Address = Integer.parseInt(outAddr2.getText());
                } catch (NumberFormatException e) {
                    value2Address = 2048;
                }

                updateOpSwsOutAddr(value1Address, false, false, value2Address, false, false);
                break;
            }
            case 1: {
                try {
                    value1Address = Integer.parseInt(outAddr3.getText());
                } catch (NumberFormatException e) {
                    value1Address = 2048;
                }

                try {
                    value2Address = Integer.parseInt(outAddr4.getText());
                } catch (NumberFormatException e) {
                    value2Address = 2048;
                }

                updateOpSwsOutAddr(value1Address, false, false, value2Address, false, false);
                break;
            }
            case 16:
            case 20:
            case 24:
            case 28:
            case 32:
            case 36:
            case 40:
            case 44: {
                Integer extractedIndex = (index - 12) / 4;

                opsw[47] = false;
                opsw[48] = false; // assume valid turnout address entry
                opsw[63] = false;
                opsw[64] = false; // assume valid turnout address entry

                if (routeTop[extractedIndex].getIsUnused()) {
                    log.warn("updateOpswForWrite - routetop[{}] is unused.", extractedIndex); // NOI18N
                    value1Address = 2048;
                    value1IsUnused = true;
                    value1DirectionIsClosed = true;
                } else {
                    value1DirectionIsClosed = routeTop[extractedIndex].closedRadioButton.isSelected();
                    value1IsUnused = false;
                    try {
                        value1Address = Integer.parseInt(routeTop[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value1Address = 2048;
                        value1IsUnused = true;
                        value1DirectionIsClosed = true;
                    }
                }

                if (routeA2[extractedIndex].getIsUnused()) {
                    log.warn("updateOpswForWrite - routeA2[{}] is unused.", extractedIndex); // NOI18N
                    value2Address = 2048;
                    value2IsUnused = true;
                    value2DirectionIsClosed = true;
                } else {
                    value2DirectionIsClosed = routeA2[extractedIndex].closedRadioButton.isSelected();
                    value2IsUnused = false;
                    try {
                        value2Address = Integer.parseInt(routeA2[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value2Address = 2048;
                        value2IsUnused = true;
                        value2DirectionIsClosed = true;
                    }
                }

                updateOpSwsOutAddr(value1Address, value1DirectionIsClosed, value1IsUnused,
                        value2Address, value2DirectionIsClosed, value2IsUnused);
                if (value1IsUnused) {
                    opsw[46] = true;
                    opsw[47] = true;
                    opsw[48] = true; // mark entry as invalid
                    routeTop[extractedIndex].unusedRadioButton.setSelected(true);
                    routeTop[extractedIndex].unusedRadioButton.repaint();
                    routeTop[extractedIndex].setAddress(2048);
                    routeTop[extractedIndex].addressField.setText("");
                }
                if (value2IsUnused) {
                    opsw[62] = true;
                    opsw[63] = true;
                    opsw[64] = true; // mark entry as invalid
                    routeA2[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA2[extractedIndex].unusedRadioButton.repaint();
                    routeA2[extractedIndex].setAddress(2048);
                    routeA2[extractedIndex].addressField.setText("");
                }
                break;
            }
            case 17:
            case 21:
            case 25:
            case 29:
            case 33:
            case 37:
            case 41:
            case 45: {
                Integer extractedIndex = (index - 13) / 4;

                opsw[47] = false;
                opsw[48] = false; // assume valid turnout address entry
                opsw[63] = false;
                opsw[64] = false; // assume valid turnout address entry

                if (routeA3[extractedIndex].getIsUnused()) {
                    value1Address = 2048;
                    value1IsUnused = true;
                    value1DirectionIsClosed = true;
                } else {
                    value1DirectionIsClosed = routeA3[extractedIndex].closedRadioButton.isSelected();
                    value1IsUnused = false;
                    try {
                        value1Address = Integer.parseInt(routeA3[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value1Address = 2048;
                        value1IsUnused = true;
                    }
                }

                if (routeA4[extractedIndex].getIsUnused()) {
                    value2Address = 2048;
                    value2IsUnused = true;
                    value2DirectionIsClosed = true;
                } else {
                    value2DirectionIsClosed = routeA4[extractedIndex].closedRadioButton.isSelected();
                    value2IsUnused = false;
                    try {
                        value2Address = Integer.parseInt(routeA4[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value2Address = 2048;
                        value2IsUnused = true;
                    }
                }

                updateOpSwsOutAddr(value1Address, value1DirectionIsClosed, value1IsUnused,
                        value2Address, value2DirectionIsClosed, value2IsUnused);
                if (value1IsUnused) {
                    opsw[46] = true;
                    opsw[47] = true;
                    opsw[48] = true; // mark entry as invalid
                    routeA3[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA3[extractedIndex].unusedRadioButton.repaint();
                    routeA3[extractedIndex].setAddress(2048);
                    routeA3[extractedIndex].addressField.setText("");
                }
                if (value2IsUnused) {
                    opsw[62] = true;
                    opsw[63] = true;
                    opsw[64] = true; // mark entry as invalid
                    routeA4[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA4[extractedIndex].unusedRadioButton.repaint();
                    routeA4[extractedIndex].setAddress(2048);
                    routeA4[extractedIndex].addressField.setText("");
                }
                break;
            }
            case 18:
            case 22:
            case 26:
            case 30:
            case 34:
            case 38:
            case 42:
            case 46: {
                Integer extractedIndex = (index - 14) / 4;
                opsw[47] = false;
                opsw[48] = false; // assume valid turnout address entry
                opsw[63] = false;
                opsw[64] = false; // assume valid turnout address entry

                if (routeA5[extractedIndex].getIsUnused()) {
                    value1Address = 2048;
                    value1IsUnused = true;
                    value1DirectionIsClosed = true;
                } else {
                    value1DirectionIsClosed = routeA5[extractedIndex].closedRadioButton.isSelected();
                    value1IsUnused = false;
                    try {
                        value1Address = Integer.parseInt(routeA5[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value1Address = 2048;
                        value1IsUnused = true;
                    }
                }

                if (routeA6[extractedIndex].getIsUnused()) {
                    value2Address = 2048;
                    value2IsUnused = true;
                    value2DirectionIsClosed = true;
                } else {
                    value2DirectionIsClosed = routeA6[extractedIndex].closedRadioButton.isSelected();
                    value2IsUnused = false;
                    try {
                        value2Address = Integer.parseInt(routeA6[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value2Address = 2048;
                        value2IsUnused = true;
                    }
                }

                updateOpSwsOutAddr(value1Address, value1DirectionIsClosed, value1IsUnused,
                        value2Address, value2DirectionIsClosed, value2IsUnused);
                if (value1IsUnused) {
                    opsw[46] = true;
                    opsw[47] = true;
                    opsw[48] = true; // mark entry as invalid
                    routeA5[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA5[extractedIndex].unusedRadioButton.repaint();
                    routeA5[extractedIndex].setAddress(2048);
                    routeA5[extractedIndex].addressField.setText("");
                }
                if (value2IsUnused) {
                    opsw[62] = true;
                    opsw[63] = true;
                    opsw[64] = true; // mark entry as invalid
                    routeA6[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA6[extractedIndex].unusedRadioButton.repaint();
                    routeA6[extractedIndex].setAddress(2048);
                    routeA6[extractedIndex].addressField.setText("");
                }
                break;
            }
            case 19:
            case 23:
            case 27:
            case 31:
            case 35:
            case 39:
            case 43:
            case 47: {
                Integer extractedIndex = (index - 15) / 4;

                opsw[47] = false;
                opsw[48] = false; // assume valid turnout address entry
                opsw[63] = false;
                opsw[64] = false; // assume valid turnout address entry

                if (routeA7[extractedIndex].getIsUnused()) {
                    value1Address = 2048;
                    value1IsUnused = true;
                    value1DirectionIsClosed = true;
                } else {
                    value1DirectionIsClosed = routeA7[extractedIndex].closedRadioButton.isSelected();
                    value1IsUnused = false;
                    try {
                        value1Address = Integer.parseInt(routeA7[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value1Address = 2048;
                        value1IsUnused = true;
                    }
                }

                if (routeA8[extractedIndex].getIsUnused()) {
                    value2Address = 2048;
                    value2IsUnused = true;
                    value2DirectionIsClosed = true;
                } else {
                    value2DirectionIsClosed = routeA8[extractedIndex].closedRadioButton.isSelected();
                    value2IsUnused = false;
                    try {
                        value2Address = Integer.parseInt(routeA8[extractedIndex].addressField.getText());
                    } catch (NumberFormatException e) {
                        value2Address = 2048;
                        value2IsUnused = true;
                    }
                }

                updateOpSwsOutAddr(value1Address, value1DirectionIsClosed, value1IsUnused,
                        value2Address, value2DirectionIsClosed, value2IsUnused);
                if (value1IsUnused) {
                    opsw[46] = true;
                    opsw[47] = true;
                    opsw[48] = true; // mark entry as invalid
                    routeA7[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA7[extractedIndex].unusedRadioButton.repaint();
                    routeA7[extractedIndex].setAddress(2048);
                    routeA7[extractedIndex].addressField.setText("");
                }
                if (value2IsUnused) {
                    opsw[62] = true;
                    opsw[63] = true;
                    opsw[64] = true; // mark entry as invalid
                    routeA8[extractedIndex].unusedRadioButton.setSelected(true);
                    routeA8[extractedIndex].unusedRadioButton.repaint();
                    routeA8[extractedIndex].setAddress(2048);
                    routeA8[extractedIndex].addressField.setText("");
                }
                break;
            }
            case 48: {
                break;
            }
            default: {
                break;
            }
        }
    }

    private void resetRouteOperation(Integer routeNumber) {
        if ((routeNumber < 1) || (routeNumber > 8)) {
            return;
        }
        routeTop[routeNumber].unusedRadioButton.setSelected(true);
        routeTop[routeNumber].setIsUnused();
        routeTop[routeNumber].addressField.setText("");

        routeA2[routeNumber].setIsUnused();
        routeA2[routeNumber].unusedRadioButton.setSelected(true);
        routeA2[routeNumber].addressField.setText("");

        routeA3[routeNumber].setIsUnused();
        routeA3[routeNumber].unusedRadioButton.setSelected(true);
        routeA3[routeNumber].addressField.setText("");

        routeA4[routeNumber].setIsUnused();
        routeA4[routeNumber].unusedRadioButton.setSelected(true);
        routeA4[routeNumber].addressField.setText("");

        routeA5[routeNumber].setIsUnused();
        routeA5[routeNumber].unusedRadioButton.setSelected(true);
        routeA5[routeNumber].addressField.setText("");

        routeA6[routeNumber].setIsUnused();
        routeA6[routeNumber].unusedRadioButton.setSelected(true);
        routeA6[routeNumber].addressField.setText("");

        routeA7[routeNumber].setIsUnused();
        routeA7[routeNumber].unusedRadioButton.setSelected(true);
        routeA7[routeNumber].addressField.setText("");

        routeA8[routeNumber].setIsUnused();
        routeA8[routeNumber].unusedRadioButton.setSelected(true);
        routeA8[routeNumber].addressField.setText("");

        writeAll();
    }

    private void unhighlightAllBasicOpSws() {

        outputType.setBackground(null);
        delayTime.setBackground(null);
        outputStates.setBackground(null);
        startupDelay.setBackground(null);
        staticOutputShutoff.setBackground(null);
        commandType.setBackground(null);
        routesControl.setBackground(null);
        localControlOfOutputsStyle.setBackground(null);
        sensorMessageTrigger.setBackground(null);
        commandSource.setBackground(null);
        output1CrossbuckFlasherCheckBox.setBackground(null);
        output2CrossbuckFlasherCheckBox.setBackground(null);
        output3CrossbuckFlasherCheckBox.setBackground(null);
        output4CrossbuckFlasherCheckBox.setBackground(null);
        localSensorType.setBackground(null);
    }

    private void unhighlightAllRouteEntryFields() {
        for (int i = 1; i < 9; ++i) {
            routeTop[i].addressField.setBackground(null);
            routeA2[i].addressField.setBackground(null);
            routeA3[i].addressField.setBackground(null);
            routeA4[i].addressField.setBackground(null);
            routeA5[i].addressField.setBackground(null);
            routeA6[i].addressField.setBackground(null);
            routeA7[i].addressField.setBackground(null);
            routeA8[i].addressField.setBackground(null);
            updateUI();
        }
    }

    private void unhighlightAllOutputEntryFields() {
        outAddr1.setBackground(null);
        outAddr2.setBackground(null);
        outAddr3.setBackground(null);
        outAddr4.setBackground(null);
    }

    @Override
    public void message(LocoNetMessage m) {
        super.message(m);

        if (m.getOpCode() == LnConstants.OPC_LONG_ACK) {
            if (((m.getElement(1) == LnConstants.RE_LACK_SPEC_CASE1)
                    || (m.getElement(1) == LnConstants.RE_LACK_SPEC_CASE2))
                    && (state == 0) && resetRouteButton.isSelected()) {
                // Handle DS64 confirmation of OpSw write when action is a DS64
                // Board Factory Reset
                resetRouteButton.setSelected(false);
                resetRouteButton.updateUI();
            }
        }
        if (m.getOpCode() == LnConstants.OPC_SW_REQ) {
            int swAddr = (((m.getElement(2) & 0x0f) * 128) + (m.getElement(1) & 0x7f)) + 1;
            boolean dir = ((m.getElement(2) & 0x20) == 0x20);
            if (swAddr == Integer.parseInt(outAddr1.getText())) {
                outState1.setText(dir ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                outState1.updateUI();
            }
            if (swAddr == Integer.parseInt(outAddr2.getText())) {
                outState2.setText(dir ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                outState2.updateUI();
            }
            if (swAddr == Integer.parseInt(outAddr3.getText())) {
                outState3.setText(dir ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                outState3.updateUI();
            }
            if (swAddr == Integer.parseInt(outAddr4.getText())) {
                outState4.setText(dir ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                outState4.updateUI();
            }
        } else if ((m.getOpCode() == LnConstants.OPC_MULTI_SENSE) && ((m.getElement(1) & 0x7E) == 0x62)) {
            // device identity report
            if (m.getElement(3) == 0x03) {
                Integer extractedBoardId = 1 + ((m.getElement(1) & 0x1) << 7)
                        + (m.getElement(2) & 0x7F);
                if (!alreadyKnowThisBoardId(extractedBoardId)) {
                    addBoardIdToList(extractedBoardId);
                }
            }
        }
    }

    /**
     * Reset the DS64 board
     */
    @SuppressFBWarnings(value = "UPM_UNCALLED_PRIVATE_METHOD", justification = "Functionality not yet confirmed with hardware; may be useful at a future date.")
    private void boardFactoryReset() {

        // before proceeding, make sure that the user really wants to go forward
        Object[] dialogBoxButtonOptions = {
            Bundle.getMessage("ButtonTextResetToFactoryDefault"),
            Bundle.getMessage("ButtonCancel")};
        int userReply = JOptionPane.showOptionDialog(this.getParent(),
                Bundle.getMessage("DialogTextBoardResetWarning"),
                Bundle.getMessage("WarningTitle"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, dialogBoxButtonOptions, dialogBoxButtonOptions[1]);
        if (userReply != 0) {
            return; // compare only to exactly the value for executing the reset!
        }
        readAllButton.setEnabled(false);
        writeAllButton.setEnabled(false);
        resetRouteButton.setEnabled(false);
        factoryResetButton.setEnabled(false);

        // send OpSw 7 = Closed to this boardId to reset the DS64
        //to its factory default settings
        // then want to read all OpSws to update the display.
        read = false;
        isWritingResetOpSw = true;
        resetOpSwVal = true;
        opsw[7] = true;
        writeOne(7);
        boardResetResponseTimer = new javax.swing.Timer(750,
                event -> {
                    factoryResetButton.setSelected(false);
                    factoryResetButton.setEnabled(true);
                    readAllButton.setEnabled(true);
                    writeAllButton.setEnabled(true);
                    resetRouteButton.setEnabled(true);
                    updateUI();
                }
        );
        boardResetResponseTimer.start();
    }

    private final ActionListener routeResetResponseTimerListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            log.debug("routeresetresponsetimerlistener state={}, indextoread{}", state, indexToRead);
            resetRouteButton.setSelected(false);
            readAllButton.setEnabled(true);
            writeAllButton.setEnabled(true);
            factoryResetButton.setEnabled(true);
        }
    };

    @Override
    public void initComponents(LocoNetSystemConnectionMemo memo) {
        super.initComponents(memo);
        LocoNetMessage m = new LocoNetMessage(6);
        m.setElement(0, LnConstants.OPC_MULTI_SENSE);
        m.setElement(1, 0x62);
        m.setElement(2, 0);
        m.setElement(3, 0x70);
        m.setElement(4, 0);
        memo.getLnTrafficController().sendLocoNetMessage(m);
    }

    @Override
    public void initComponents() {
        super.initComponents();
        // implements an AbstractBoardProgPanel with three tabs:
        //      Outputs tab - configure features most related to DS64 outputs
        //      Inputs tab - configure features most related to DS64 inputs
        //      Routes tab - configure features related to routes
        //          Routes tab has (left side, vertical) sub-tabs, one for each of the 8 routes

        addrField.setText(Bundle.getMessage("LabelBoardID"));

        String[] outputTypes = {Bundle.getMessage("ComboBoxOutputType0"),
            Bundle.getMessage("ComboBoxOutputType1")};
        outputTypeLabel = new JLabel(Bundle.getMessage("LabelOutputType"));
        outputType = new JComboBox<>(outputTypes); // opSw 1
        outputType.setToolTipText(Bundle.getMessage("ToolTipOutputType"));
        outputType.addActionListener(basicConfigChangeActionListener);
        outputType.setName("1"); // NOI18N

        String[] availableDelayTimes = new String[16];
        availableDelayTimes[0] = Bundle.getMessage("ComboBoxPulseTime0point1");
        availableDelayTimes[1] = Bundle.getMessage("ComboBoxPulseTime0point2");
        availableDelayTimes[2] = Bundle.getMessage("ComboBoxPulseTime0point4");
        availableDelayTimes[3] = Bundle.getMessage("ComboBoxPulseTime0point6");
        availableDelayTimes[4] = Bundle.getMessage("ComboBoxPulseTime0point8");
        availableDelayTimes[5] = Bundle.getMessage("ComboBoxPulseTime1point0");
        availableDelayTimes[6] = Bundle.getMessage("ComboBoxPulseTime1point2");
        availableDelayTimes[7] = Bundle.getMessage("ComboBoxPulseTime1point4");
        availableDelayTimes[8] = Bundle.getMessage("ComboBoxPulseTime1point6");
        availableDelayTimes[9] = Bundle.getMessage("ComboBoxPulseTime1point8");
        availableDelayTimes[10] = Bundle.getMessage("ComboBoxPulseTime2point0");
        availableDelayTimes[11] = Bundle.getMessage("ComboBoxPulseTime2point2");
        availableDelayTimes[12] = Bundle.getMessage("ComboBoxPulseTime2point4");
        availableDelayTimes[13] = Bundle.getMessage("ComboBoxPulseTime2point6");
        availableDelayTimes[14] = Bundle.getMessage("ComboBoxPulseTime2point8");
        availableDelayTimes[15] = Bundle.getMessage("ComboBoxPulseTime3point0");

        delayTimeLabel = new JLabel(Bundle.getMessage("LabelPulseTimeout")); // opSws 2-5
        delayTime = new JComboBox<>(availableDelayTimes);
        delayTime.setToolTipText(Bundle.getMessage("ToolTipPulseTimeout"));
        delayTime.setName("2345"); // NOI18N
        delayTime.addActionListener(basicConfigChangeActionListener);

        String[] initialOutputStates = {Bundle.getMessage("ComboBoxOutputPowerupType0"),
            Bundle.getMessage("ComboBoxOutputPowerupType1")};
        outputStatesLabel = new JLabel(Bundle.getMessage("LabelPowerUpOutputActivity"));
        outputStates = new JComboBox<>(initialOutputStates);    // opsw 6
        outputStates.setToolTipText(Bundle.getMessage("ToolTipOutputStates"));
        outputStates.setName("6"); // NOI18N
        outputStates.addActionListener(basicConfigChangeActionListener);

        String[] startupDelays = {Bundle.getMessage("ComboBoxOutputPowerupDelayType0"),
            Bundle.getMessage("ComboBoxOutputPowerupDelayType1")};
        startupDelayLabel = new JLabel(Bundle.getMessage("LabelInitialPowerUpDelay"));
        startupDelay = new JComboBox<>(startupDelays);  // opsw 8
        startupDelay.setToolTipText(Bundle.getMessage("ToolTipStartupDelay"));
        startupDelay.setName("8"); // NOI18N
        startupDelay.addActionListener(basicConfigChangeActionListener);

        String[] staticOutputShutoffs = {Bundle.getMessage("ComboBoxOutputPowerManagementType0"),
            Bundle.getMessage("ComboBoxOutputPowerManagementType1")};
        staticOutputShutoffLabel = new JLabel(Bundle.getMessage("LabelOutputPowerManagementStyle"));
        staticOutputShutoff = new JComboBox<>(staticOutputShutoffs); // opSw 9
        staticOutputShutoff.setToolTipText(Bundle.getMessage("ToolTipLabelOutputPowerManagementStyle"));
        staticOutputShutoff.setName("9"); // NOI18N
        staticOutputShutoff.addActionListener(basicConfigChangeActionListener);

        // command sources
        String[] commandTypes = {Bundle.getMessage("ComboBoxCommandsRecognizedFromType0"),
            Bundle.getMessage("ComboBoxCommandsRecognizedFromType1")};
        commandTypeLabel = new JLabel(Bundle.getMessage("LabelAcceptedSwitchCommandTypes"));
        commandType = new JComboBox<>(commandTypes); //opSw 10
        commandType.setToolTipText(Bundle.getMessage("ToolTipLabelAcceptedSwitchCommandTypes"));
        commandType.setName("10"); // NOI18N
        commandType.addActionListener(basicConfigChangeActionListener);

        String[] commandSources = {Bundle.getMessage("ComboBoxCommandSourceType0"),
            Bundle.getMessage("ComboBoxCommandSourceType1")};
        commandSourceLabel = new JLabel(Bundle.getMessage("LabelAcceptSwitchCommandsFrom"));
        commandSource = new JComboBox<>(commandSources); // opSw14
        commandSource.setToolTipText(Bundle.getMessage("ToolTipCommandSource"));
        commandSource.setName("14"); // NOI18N
        commandSource.addActionListener(basicConfigChangeActionListener);

        // Crossbuck Flasher controls
        output1CrossbuckFlasherCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxOutputXCrossbuck", 1));
        output1CrossbuckFlasherCheckBox.setToolTipText(Bundle.getMessage("ToolTipCheckBoxOutput1Crossbuck"));
        output1CrossbuckFlasherCheckBox.setName("17"); // NOI18N
        output1CrossbuckFlasherCheckBox.addActionListener(basicConfigChangeActionListener);
        // output 2
        output2CrossbuckFlasherCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxOutputXCrossbuck", 2));
        output2CrossbuckFlasherCheckBox.setToolTipText(Bundle.getMessage("ToolTipCheckBoxOutput2Crossbuck"));
        output2CrossbuckFlasherCheckBox.setName("18"); // NOI18N
        output2CrossbuckFlasherCheckBox.addActionListener(basicConfigChangeActionListener);
        // output 3
        output3CrossbuckFlasherCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxOutputXCrossbuck", 3));
        output3CrossbuckFlasherCheckBox.setToolTipText(Bundle.getMessage("ToolTipCheckBoxOutput3Crossbuck"));
        output3CrossbuckFlasherCheckBox.setName("19"); // NOI18N
        output3CrossbuckFlasherCheckBox.addActionListener(basicConfigChangeActionListener);
        // output 4
        output4CrossbuckFlasherCheckBox = new JCheckBox(Bundle.getMessage("CheckBoxOutputXCrossbuck", 4));
        output4CrossbuckFlasherCheckBox.setToolTipText(Bundle.getMessage("ToolTipCheckBoxOutput4Crossbuck"));
        output4CrossbuckFlasherCheckBox.setName("20"); // NOI18N
        output4CrossbuckFlasherCheckBox.addActionListener(basicConfigChangeActionListener);

        // DS64 routes
        String[] routesControls = {Bundle.getMessage("ComboBoxEntryRoutesOption0"),
            Bundle.getMessage("ComboBoxEntryRoutesOption1"),
            Bundle.getMessage("ComboBoxEntryRoutesOption2"),
            Bundle.getMessage("ComboBoxEntryRoutesOption3")};
        routesControlLabel = new JLabel(Bundle.getMessage("LabelTriggerDs64Routes"));
        routesControl = new JComboBox<>(routesControls);    // opSws 11, 16
        routesControl.setToolTipText(Bundle.getMessage("ToolTipLabelRouteControlOptions"));
        routesControl.setName("1116"); // NOI18N
        routesControl.addActionListener(basicConfigChangeActionListener);

        // local input controls
        String[] localControlOfOutputsStyles = {
            Bundle.getMessage("ComboBoxInputsControlOutputsType0"),
            Bundle.getMessage("ComboBoxInputsControlOutputsType1"),
            Bundle.getMessage("ComboBoxInputsControlOutputsType2"),
            Bundle.getMessage("comboboxInputsControlOutputsType3")};
        localControlOfOutputsStyleLabel = new JLabel(Bundle.getMessage("LabelLocalInputsControlOutputs"));
        localControlOfOutputsStyle = new JComboBox<>(localControlOfOutputsStyles); // opSw12
        localControlOfOutputsStyle.setToolTipText(Bundle.getMessage("ToolTipLocalInputsControl"));
        localControlOfOutputsStyle.setName("1215"); // NOI18N
        localControlOfOutputsStyle.addActionListener(basicConfigChangeActionListener);

        String[] sensorMessageTriggers = {Bundle.getMessage("ComboBoxInputsCauseMessagesType0"),
            Bundle.getMessage("ComboBoxInputsCauseMessagesType1")};
        sensorMessageTriggerLabel = new JLabel(Bundle.getMessage("LabelBetweenForMessageTypeSent"));
        sensorMessageTrigger = new JComboBox<>(sensorMessageTriggers); // opSw13
        sensorMessageTrigger.setToolTipText(Bundle.getMessage("ToolTipSensorMessageTrigger"));
        sensorMessageTrigger.setName("13"); // NOI18N
        sensorMessageTrigger.addActionListener(basicConfigChangeActionListener);

        String[] localSensorTypes = {Bundle.getMessage("ComboBoxSensorMessageTypeSentType0"),
            Bundle.getMessage("ComboBoxSensorMessageTypeSentType1")};
        localSensorType = new JComboBox<>(localSensorTypes); // opSw21
        localSensorType.setToolTipText(Bundle.getMessage("ToolTipLocalSensorsType"));
        localSensorType.setName("21"); // NOI18N
        localSensorType.addActionListener(basicConfigChangeActionListener);

        factoryResetButton = new JToggleButton(Bundle.getMessage("ButtonResetToFactoryDefault"));
        factoryResetButton.setToolTipText(Bundle.getMessage("ToolTipButtonResetToFactoryDefault"));
        factoryResetButton.addActionListener(
                event -> {
                    readAllButton.setEnabled(false);
                    writeAllButton.setEnabled(false);
                    resetRouteButton.setEnabled(false);
                    boardFactoryReset();
                }
        );
        routesTabbedPane = new JTabbedPane();

        routePanel = new JPanel[9];
        routeTop = new SimpleTurnoutStateEntry[9];
        routeA2 = new SimpleTurnoutStateEntry[9];
        routeA3 = new SimpleTurnoutStateEntry[9];
        routeA4 = new SimpleTurnoutStateEntry[9];
        routeA5 = new SimpleTurnoutStateEntry[9];
        routeA6 = new SimpleTurnoutStateEntry[9];
        routeA7 = new SimpleTurnoutStateEntry[9];
        routeA8 = new SimpleTurnoutStateEntry[9];

        resetRouteButton = new JToggleButton(Bundle.getMessage("ButtonResetRoute"));
        resetRouteButton.setToolTipText(Bundle.getMessage("ToolTipButtonResetRoute"));
        resetRouteButton.setEnabled(false);
        resetRouteButton.setVisible(false);

        JPanel addressingPanel = provideAddressing(" "); // create read/write buttons, address
        readAllButton.setPreferredSize(null);
        readAllButton.setText(Bundle.getMessage("ButtonReadFullSheet"));
        readAllButton.setToolTipText(Bundle.getMessage("ToolTipButtonReadFullSheet"));

        writeAllButton.setPreferredSize(null);
        writeAllButton.setText(Bundle.getMessage("ButtonWriteFullSheet"));
        writeAllButton.setToolTipText(Bundle.getMessage("ToolTipButtonWriteFullSheet"));

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

        addressingPanel.add(resetRouteButton);
        int indexOfTargetBoardAddress = 0;

        addressComboBox = new JComboBox<>();
        for (Integer index = 0; index < boardNumsEntryValue.size(); ++index) {
            if (boardNumsEntryValue.get(index) == origAccessBoardNum) {
                origAccessBoardNum = -1;
                indexOfTargetBoardAddress = index;
            }
            addressComboBox.addItem(boardNumsEntryValue.get(index));
        }

        addressComboBox.setSelectedIndex(indexOfTargetBoardAddress);
        addressingPanel.add(addressComboBox, 1);
        addressingPanel.getComponent(2).setVisible(false);
        addressComboBox.setEditable(true);

        appendLine(addressingPanel);  // add read/write buttons, address

        generalTabbedPane = new JTabbedPane();
        generalPanel = new JPanel();
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
        generalPanel.setName("Basic Settings"); // NOI18N

        JPanel allOutputControls = new JPanel();
        allOutputControls.setLayout(new BoxLayout(allOutputControls, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder allOutputControlsTitleBorder;
        javax.swing.border.Border blackline;
        blackline = javax.swing.BorderFactory.createLineBorder(java.awt.Color.black);
        allOutputControlsTitleBorder = javax.swing.BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelOutputControls"));
        allOutputControls.setBorder(allOutputControlsTitleBorder);

        JPanel outputTypePanel = new JPanel();
        outputTypePanel.setLayout(new FlowLayout());
        outputTypePanel.add(outputTypeLabel);
        outputTypePanel.add(outputType);
        allOutputControls.add(outputTypePanel);

        JPanel delayTimePanel = new JPanel();
        delayTimePanel.setLayout(new FlowLayout());
        delayTimePanel.add(delayTimeLabel);
        delayTimePanel.add(delayTime);
        allOutputControls.add(delayTimePanel);

        JPanel outputStatePanel = new JPanel();
        outputStatePanel.setLayout(new FlowLayout());
        outputStatePanel.add(outputStatesLabel);
        outputStatePanel.add(outputStates);
        allOutputControls.add(outputStatePanel);

        JPanel startupDelayPanel = new JPanel();
        startupDelayPanel.setLayout(new FlowLayout());
        startupDelayPanel.add(startupDelayLabel);
        startupDelayPanel.add(startupDelay);
        allOutputControls.add(startupDelayPanel);

        JPanel staticOutputShutoffPanel = new JPanel();
        staticOutputShutoffPanel.setLayout(new FlowLayout());
        staticOutputShutoffPanel.add(staticOutputShutoffLabel);
        staticOutputShutoffPanel.add(staticOutputShutoff);
        allOutputControls.add(staticOutputShutoffPanel);

        JPanel crossingGateControls = new JPanel(new java.awt.GridLayout(2, 2));
        crossingGateControls.add(output1CrossbuckFlasherCheckBox);
        crossingGateControls.add(output3CrossbuckFlasherCheckBox); // display output 3 box to the right of output 1 box
        crossingGateControls.add(output2CrossbuckFlasherCheckBox); // display output 2 box below output 1 box
        crossingGateControls.add(output4CrossbuckFlasherCheckBox);
        allOutputControls.add(crossingGateControls);

        generalPanel.add(allOutputControls);

        // command sources
        JPanel ds64CommandSourcesPanel = new JPanel();
        ds64CommandSourcesPanel.setLayout(new BoxLayout(ds64CommandSourcesPanel, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder ds64CommandSourcesTitleBorder;
        ds64CommandSourcesTitleBorder = javax.swing.BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelCommandSources"));
        ds64CommandSourcesPanel.setBorder(ds64CommandSourcesTitleBorder);

        JPanel commandTypePanel = new JPanel();
        commandTypePanel.setLayout(new FlowLayout());
        commandTypePanel.add(commandTypeLabel);
        commandTypePanel.add(commandType);
        ds64CommandSourcesPanel.add(commandTypePanel);

        JPanel commandSourcePanel = new JPanel();
        commandSourcePanel.setLayout(new FlowLayout());
        commandSourcePanel.add(commandSourceLabel);
        commandSourcePanel.add(commandSource);
        ds64CommandSourcesPanel.add(commandSourcePanel);

        generalPanel.add(ds64CommandSourcesPanel);

        // DS64 routes
        JPanel localRoutesPanel = new JPanel();
        localRoutesPanel.setLayout(new BoxLayout(localRoutesPanel, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder localRoutesTitleBorder;
        localRoutesTitleBorder = javax.swing.BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelRoutes"));
        localRoutesPanel.setBorder(localRoutesTitleBorder);

        JPanel routesControlPanel = new JPanel();
        routesControlPanel.setLayout(new FlowLayout());
        routesControlPanel.add(routesControlLabel);
        routesControlPanel.add(routesControl);
        localRoutesPanel.add(routesControlPanel);

        generalPanel.add(localRoutesPanel);

        // local input controls
        localInputControlsPanel = new JPanel();
        localInputControlsPanel.setLayout(new BoxLayout(localInputControlsPanel, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder localInputControlsTitleBorder;
        localInputControlsTitleBorder = javax.swing.BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelLocalInputControls"));
        localInputControlsPanel.setBorder(localInputControlsTitleBorder);

        JPanel localControlOfOutputsStylePanel = new JPanel(new FlowLayout());
        localControlOfOutputsStylePanel.add(localControlOfOutputsStyleLabel);
        localControlOfOutputsStylePanel.add(localControlOfOutputsStyle);
        localInputControlsPanel.add(localControlOfOutputsStylePanel);

        sensorMessageTriggerPanel = new JPanel(new FlowLayout());
        sensorMessageTriggerPanel.add(localSensorType);
        sensorMessageTriggerPanel.add(sensorMessageTriggerLabel);
        sensorMessageTriggerPanel.add(sensorMessageTrigger);
        localInputControlsPanel.add(sensorMessageTriggerPanel);

        generalPanel.add(localInputControlsPanel);

        generalPanel.add(new JSeparator());
        JPanel factoryResetButtonPanel = new JPanel();
        factoryResetButtonPanel.add(factoryResetButton);
        generalPanel.add(factoryResetButtonPanel);

        generalTabbedPane.addTab(Bundle.getMessage("TabTextBasicSettings"), null,
                generalPanel, Bundle.getMessage("TabToolTipBasicSettings"));

        // opsws panel
        opswsPanel = new JPanel();

        opswsValues = new JPanel();
        opswsValues.setLayout(new BoxLayout(opswsValues, BoxLayout.Y_AXIS));
        javax.swing.border.TitledBorder opswsValuesTitleBorder;
        opswsValuesTitleBorder = javax.swing.BorderFactory.createTitledBorder(blackline,
                Bundle.getMessage("TitledBorderLabelOpSws"));
        opswsValues.setBorder(opswsValuesTitleBorder);

        opswsPanel.setLayout(new BoxLayout(opswsPanel, BoxLayout.Y_AXIS));
        JPanel innerPanel;
        ButtonGroup[] g = new ButtonGroup[22];
        opswThrown = new JRadioButtonWithInteger[22];
        opswClosed = new JRadioButtonWithInteger[22];
        for (int i = 1; i <= 21; i++) {
            if (i != 7) {
                log.debug("Creating entry for OpSw {}", i);
                innerPanel = new JPanel(new FlowLayout());
                innerPanel.add(new JLabel("OpSw " + i)); // NOI18N
                opswThrown[i] = new JRadioButtonWithInteger(i, Bundle.getMessage("TurnoutStateThrown"));
                opswClosed[i] = new JRadioButtonWithInteger(i, Bundle.getMessage("TurnoutStateClosed"));
                g[i] = new ButtonGroup();
                g[i].add(opswThrown[i]);
                g[i].add(opswClosed[i]);
                innerPanel.add(opswThrown[i]);
                innerPanel.add(opswClosed[i]);
                opswsPanel.add(innerPanel);
                opswsPanel.add(new JSeparator());
                opswThrown[i].addItemListener(event -> {
                    if (event.getSource().getClass() == JRadioButtonWithInteger.class) {
                        JRadioButtonWithInteger source = ((JRadioButtonWithInteger) (event.getSource()));
                        int ind = source.index;
                        boolean st = (event.getStateChange() == ItemEvent.DESELECTED);
                        log.debug("ItemEventListener Opsw values: {} thrown radio button event: {} {}.", ind, st, st ? "Closed" : "Thrown"); // NOI18N
                        opsw[ind] = st;
                    }
                });
            }
        }
        opswsValues.add(opswsPanel);
        opswsScrollPane = new JScrollPane(opswsValues);
        opswsScrollPane.setPreferredSize(new java.awt.Dimension(180, 200));
        opswsScrollPane.setName("Simple OpSws"); // NOI18N

        generalTabbedPane.addTab(Bundle.getMessage("TabTextOpSwValues"), null,
                opswsScrollPane, Bundle.getMessage("TabToolTipOpSwValues"));

        outputAddrsPanel = new JPanel();
        outputAddrsPanel.setLayout(new BoxLayout(outputAddrsPanel, BoxLayout.Y_AXIS));

        JPanel p = new JPanel();

        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelCautionReadingWritingCanCauseOutputChanges")));
        outputAddrsPanel.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelTextOutputX", 1)));
        outAddr1 = new ValidatedTextField(5, false, 1, 2048, Bundle.getMessage("ErrorTextNonBlankAddressInvalid"));
        outState1 = new JLabel(Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateUnknown")));
        p.add(outAddr1);
        p.add(outState1);
        outputAddrsPanel.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelTextOutputX", 2)));
        outAddr2 = new ValidatedTextField(5, false, 1, 2048, Bundle.getMessage("ErrorTextNonBlankAddressInvalid"));
        outState2 = new JLabel(Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateUnknown")));
        p.add(outAddr2);
        p.add(outState2);
        outputAddrsPanel.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelTextOutputX", 3)));
        outAddr3 = new ValidatedTextField(5, false, 1, 2048, Bundle.getMessage("ErrorTextNonBlankAddressInvalid"));
        outState3 = new JLabel(Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateUnknown")));
        p.add(outAddr3);
        p.add(outState3);
        outputAddrsPanel.add(p);

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelTextOutputX", 4)));
        outAddr4 = new ValidatedTextField(5, false, 1, 2048, Bundle.getMessage("ErrorTextNonBlankAddressInvalid"));
        outState4 = new JLabel(Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateUnknown")));
        p.add(outAddr4);
        p.add(outState4);
        outputAddrsPanel.add(p);
        outputAddrsPanel.add(new JSeparator());

        p = new JPanel();
        p.setLayout(new FlowLayout());
        p.add(new JLabel(Bundle.getMessage("LabelOutputsTabSensorNotes")));
        outputAddrsPanel.add(p);

        generalTabbedPane.addTab(Bundle.getMessage("TabTextOutputAddrs"), null,
                outputAddrsPanel, Bundle.getMessage("TabToolTipOutputAddrs"));

        routePanel[0] = new JPanel();

        routesTabbedPane.setTabPlacement(JTabbedPane.LEFT);
        // create route panels (one tab each for each of 8 routes)
        for (int i = 1; i <= 8; ++i) {
            routePanel[i] = new JPanel();
            routePanel[i].setLayout(new BoxLayout(routePanel[i], BoxLayout.Y_AXIS));

            routePanel[i].add(new JLabel(Bundle.getMessage("TabTextSpecificRoute",
                    Integer.toString(i))));
            routePanel[i].add(new JSeparator());
            JPanel q = new JPanel(new FlowLayout());
            q.add(new JLabel(Bundle.getMessage("LabelCautionReadingWritingCanCauseOutputChanges")));
            routePanel[i].add(q);
            routePanel[i].add(new JSeparator());
            routeTop[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA2[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA3[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA4[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA5[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA6[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA7[i] = new SimpleTurnoutStateEntry(2048, false, true);
            routeA8[i] = new SimpleTurnoutStateEntry(2048, false, true);

            routePanel[i].add(routeTop[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout",
                    Bundle.getMessage("LabelTextRouteXTopTurnout"))));
            routePanel[i].add(routeA2[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 2)));
            routePanel[i].add(routeA3[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 3)));
            routePanel[i].add(routeA4[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 4)));
            routePanel[i].add(routeA5[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 5)));
            routePanel[i].add(routeA6[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 6)));
            routePanel[i].add(routeA7[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 7)));
            routePanel[i].add(routeA8[i].createEntryPanel(Bundle.getMessage("LabelTextRouteXSpecificTurnout", 8)));

            routesTabbedPane.addTab(
                    Bundle.getMessage("TabTextSpecificRoute", Integer.toString(i)),
                    null,
                    routePanel[i],
                    Bundle.getMessage("TabToolTipSpecificRoute", Integer.toString(i))
            );
        }

        generalTabbedPane.addTab(Bundle.getMessage("TabTextRoutes"),
                null, routesTabbedPane,
                Bundle.getMessage("ToolTipTabTextRoutes"));
        resetRouteButton.addActionListener(
                event -> {
                    readAllButton.setEnabled(false);
                    writeAllButton.setEnabled(false);
                    factoryResetButton.setEnabled(false);

                    Integer routeNumber = 0;
                    if (((JTabbedPane) generalTabbedPane.getSelectedComponent()) != routesTabbedPane) {
                        return;
                    }
                    if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[1]) {
                        routeNumber = 1;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[2]) {
                        routeNumber = 2;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[3]) {
                        routeNumber = 3;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[4]) {
                        routeNumber = 4;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[5]) {
                        routeNumber = 5;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[6]) {
                        routeNumber = 6;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[7]) {
                        routeNumber = 7;
                    } else if (((JPanel) routesTabbedPane.getSelectedComponent()) == routePanel[8]) {
                        routeNumber = 8;
                    }
                    if (routeNumber != 0) {
                        // before proceeding, make sure that the user really wants to go forward
                        Object[] dialogBoxButtonOptions = {
                            Bundle.getMessage("ButtonResetRouteN", routeNumber),
                            Bundle.getMessage("ButtonCancel")};
                        int userReply = JOptionPane.showOptionDialog(this.getParent(),
                                Bundle.getMessage("DialogTextClearRouteWarning", routeNumber),
                                Bundle.getMessage("WarningTitle"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, dialogBoxButtonOptions, dialogBoxButtonOptions[1]);
                        if (userReply != 0) {
                            resetRouteButton.setSelected(false);
                            return; // compare only to exactly the value for executing the "clear route" operation!
                        }

                        resetRouteOperation(routeNumber);
                    }
                    readAllButton.setEnabled(true);
                    writeAllButton.setEnabled(true);
                    factoryResetButton.setEnabled(true);
                    resetRouteButton.setSelected(false);
                }
        );

        appendLine(generalTabbedPane);
        JPanel statusPanel = new JPanel();
        setStatus(" ");
        statusPanel.add(new JSeparator());
        statusPanel.add(provideStatusLine());
        statusPanel.add(new JSeparator());
        appendLine(statusPanel);

        setTypeWord(0x73);  // configure DS64 message type
        opsw[7] = false;
        operationType = OpSwOpType.BasicsRead;

        routesTabbedPane.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes

            String route1TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(1));
            String route2TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(2));
            String route3TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(3));
            String route4TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(4));
            String route5TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(5));
            String route6TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(6));
            String route7TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(7));
            String route8TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(8));

            @Override
            public void stateChanged(ChangeEvent evt) {
                unhighlightAllBasicOpSws();
                unhighlightAllOutputEntryFields();
                unhighlightAllRouteEntryFields();

                String activeTabTitle = routesTabbedPane.getTitleAt(routesTabbedPane.getSelectedIndex());

                if ((activeTabTitle.equals(route1TabText))
                        || (activeTabTitle.equals(route2TabText))
                        || (activeTabTitle.equals(route3TabText))
                        || (activeTabTitle.equals(route4TabText))
                        || (activeTabTitle.equals(route5TabText))
                        || (activeTabTitle.equals(route6TabText))
                        || (activeTabTitle.equals(route7TabText))
                        || (activeTabTitle.equals(route8TabText))) {
                    resetRouteButton.setVisible(true);
                    resetRouteButton.setEnabled(true);
                    resetRouteButton.updateUI();
                    readAllButton.setSelected(false);
                    readAllButton.updateUI();
                    updateUI();
                }
            }

        });

        generalTabbedPane.addChangeListener(new ChangeListener() {
            // This method is called whenever the selected tab changes
            String route1TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(1));
            String route2TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(2));
            String route3TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(3));
            String route4TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(4));
            String route5TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(5));
            String route6TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(6));
            String route7TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(7));
            String route8TabText = Bundle.getMessage("TabTextSpecificRoute", Integer.toString(8));
            String outputsTabText = Bundle.getMessage("TabTextOutputAddrs");

            @Override
            public void stateChanged(ChangeEvent evt) {
                String activeTabTitle;
                unhighlightAllBasicOpSws();
                unhighlightAllOutputEntryFields();
                unhighlightAllRouteEntryFields();

                activeTabTitle = generalTabbedPane.getTitleAt(generalTabbedPane.getSelectedIndex());
                JTabbedPane pane = (JTabbedPane) evt.getSource();

                // Get current tab
                if ((activeTabTitle.equals(Bundle.getMessage("TabTextRoutes")))) {
                    activeTabTitle = routesTabbedPane.getTitleAt(routesTabbedPane.getSelectedIndex());
                    if ((activeTabTitle.equals(route1TabText))
                            || (activeTabTitle.equals(route2TabText))
                            || (activeTabTitle.equals(route3TabText))
                            || (activeTabTitle.equals(route4TabText))
                            || (activeTabTitle.equals(route5TabText))
                            || (activeTabTitle.equals(route6TabText))
                            || (activeTabTitle.equals(route7TabText))
                            || (activeTabTitle.equals(route8TabText))) {
                        resetRouteButton.setEnabled(true);
                        resetRouteButton.setVisible(true);
                        readAllButton.setEnabled(true);
                        writeAllButton.setSelected(false);
                        readAllButton.setSelected(false);
                        writeAllButton.setEnabled(true);
                        factoryResetButton.setEnabled(true);
                        routesTabbedPane.updateUI();
                        updateUI();
                    } else {
                        routesTabbedPane.setSelectedIndex(0);
                        routesTabbedPane.updateUI();
                    }
                } else if (activeTabTitle.equals(outputsTabText)) {
                    resetRouteButton.setEnabled(false);
                    resetRouteButton.setVisible(false);
                    readAllButton.setEnabled(true);
                    writeAllButton.setSelected(false);
                    readAllButton.setSelected(false);
                    writeAllButton.setEnabled(true);
                    readAllButton.updateUI();
                    updateUI();
                } else {
                    readAllButton.setEnabled(true);
                    writeAllButton.setEnabled(true);
                    writeAllButton.setSelected(false);
                    readAllButton.setSelected(false);
                    resetRouteButton.setVisible(false);
                    resetRouteButton.setEnabled(false);
                    readAllButton.updateUI();
                    updateUI();
                }
                Container c = pane.getRootPane().getParent();
                c.setPreferredSize(null);
                if (c instanceof Window) {
                    ((Window) c).pack();
                }
            }

        });

        responseTimer.addActionListener(routeResetResponseTimerListener);
        commandType.setToolTipText(Bundle.getMessage("ToolTipLabelAcceptedSwitchCommandTypes"));
        updateBasicOpSwTab();

        panelToScroll();

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "Cannot catch an exception without grabbing the exception, but we don't do anything with the exception details.")
    private void updateGuiFromOpSws49_64() {
        Integer readValue;
        boolean isUsed = true;

        readValue = 0;
        for (int i = 60; i >= 49; i--) {
            if (i != 56) {
                readValue = (readValue << 1) + (opsw[i] ? 1 : 0);
            }
        }
        readValue++; // account for physical/user numbering difference

        String readValueString = readValue.toString();
        if ((opsw[63] == true) && (opsw[64] == true)) {
            readValueString = "";
            isUsed = false;
        }
        boolean direction = opsw[62];

        switch (indexToRead) {
            case 0: {
                // have read value for output2 - update local storage
                outAddr2.setText(Integer.toString(readValue));
                outAddr2.setLastQueriedValue(outAddr2.getText());
                outState2.setText(direction
                        ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                break;
            }
            case 1: {
                // have read value for output4 - update local storage
                outAddr4.setText(Integer.toString(readValue));
                outAddr4.setLastQueriedValue(outAddr4.getText());
                outState4.setText(direction
                        ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                break;
            }
            case 16:
            case 20:
            case 24:
            case 28:
            case 32:
            case 36:
            case 40:
            case 44: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 12) / 4;

                if (isUsed == false) {
                    routeA2[effectiveIndex].setIsUnused();
                    routeA2[effectiveIndex].addressField.setText("");
                    routeA2[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA2[effectiveIndex].setAddress(readValue);
                    routeA2[effectiveIndex].addressField.setText(readValueString);
                    routeA2[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[62] == true) {
                        routeA2[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA2[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 17:
            case 21:
            case 25:
            case 29:
            case 33:
            case 37:
            case 41:
            case 45: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 13) / 4;
                if (isUsed == false) {
                    routeA4[effectiveIndex].setIsUnused();
                    routeA4[effectiveIndex].addressField.setText("");
                    routeA4[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA4[effectiveIndex].setAddress(readValue);
                    routeA4[effectiveIndex].addressField.setText(readValueString);
                    routeA4[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[62] == true) {
                        routeA4[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA4[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 18:
            case 22:
            case 26:
            case 30:
            case 34:
            case 38:
            case 42:
            case 46: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 14) / 4;
                if (isUsed == false) {
                    routeA6[effectiveIndex].setIsUnused();
                    routeA6[effectiveIndex].addressField.setText("");
                    routeA6[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA6[effectiveIndex].setAddress(readValue);
                    routeA6[effectiveIndex].addressField.setText(readValueString);
                    routeA6[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[62] == true) {
                        routeA6[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA6[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 19:
            case 23:
            case 27:
            case 31:
            case 35:
            case 39:
            case 43:
            case 47: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 15) / 4;
                if (isUsed == false) {
                    routeA8[effectiveIndex].setIsUnused();
                    routeA8[effectiveIndex].addressField.setText("");
                    routeA8[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA8[effectiveIndex].setAddress(readValue);
                    routeA8[effectiveIndex].addressField.setText(readValueString);
                    routeA8[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[62] == true) {
                        routeA8[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA8[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "Cannot catch an exception without grabbing the exception, but we don't do anything with the exception details.")
    void updateGuiFromOpSws33_48() {
        Integer readValue;
        boolean isUsed = true;

        readValue = 0;
        for (int i = 44; i >= 33; i--) {
            if (i != 40) {
                readValue = (readValue << 1) + (opsw[i] ? 1 : 0);
            }
        }
        readValue++; // account for physical/user numbering difference

        String readValueString = readValue.toString();
        if ((opsw[47] == true) && (opsw[48] == true)) {
            readValueString = "";
            isUsed = false;
        }
        boolean direction = opsw[46];

        switch (indexToRead) {
            case 0: {
                // have read value for output1 - update local storage
                outAddr1.setText(readValueString);
                outAddr1.setLastQueriedValue(readValueString);
                outAddr1.isValid();
                outState1.setText(direction
                        ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                break;
            }
            case 1: {
                // have read value for output3 - update local storage
                outAddr3.setText(readValueString);
                outAddr3.setLastQueriedValue(readValueString);
                outState3.setText(direction
                        ? Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateClosed"))
                        : Bundle.getMessage("LabelTurnoutCurrentStateX", Bundle.getMessage("BeanStateThrown")));
                break;
            }
            case 16:
            case 20:
            case 24:
            case 28:
            case 32:
            case 36:
            case 40:
            case 44: {
                // have read value for Route n Top - update local storage
                Integer effectiveIndex = (indexToRead - 12) / 4;
                if (isUsed == false) {
                    routeTop[effectiveIndex].setIsUnused();
                    routeTop[effectiveIndex].addressField.setText("");
                    routeTop[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeTop[effectiveIndex].setAddress(readValue);
                    routeTop[effectiveIndex].addressField.setText(readValueString);
                    routeTop[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[46] == true) {
                        routeTop[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeTop[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 17:
            case 21:
            case 25:
            case 29:
            case 33:
            case 37:
            case 41:
            case 45: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 13) / 4;
                if (isUsed == false) {
                    routeA3[effectiveIndex].setIsUnused();
                    routeA3[effectiveIndex].addressField.setText("");
                    routeA3[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA3[effectiveIndex].setAddress(readValue);
                    routeA3[effectiveIndex].addressField.setText(readValueString);
                    routeA3[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[46] == true) {
                        routeA3[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA3[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 18:
            case 22:
            case 26:
            case 30:
            case 34:
            case 38:
            case 42:
            case 46: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 14) / 4;
                if (isUsed == false) {
                    routeA5[effectiveIndex].setIsUnused();
                    routeA5[effectiveIndex].addressField.setText("");
                    routeA5[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA5[effectiveIndex].setAddress(readValue);
                    routeA5[effectiveIndex].addressField.setText(readValueString);
                    routeA5[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[46] == true) {
                        routeA5[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA5[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            case 19:
            case 23:
            case 27:
            case 31:
            case 35:
            case 39:
            case 43:
            case 47: {
                // have a read value for Route n - update local storage
                Integer effectiveIndex = (indexToRead - 15) / 4;
                if (isUsed == false) {
                    routeA7[effectiveIndex].setIsUnused();
                    routeA7[effectiveIndex].addressField.setText("");
                    routeA7[effectiveIndex].unusedRadioButton.setSelected(true);
                } else {
                    routeA7[effectiveIndex].setAddress(readValue);
                    routeA7[effectiveIndex].addressField.setText(readValueString);
                    routeA7[effectiveIndex].addressField.setLastQueriedValue(readValueString);
                    if (opsw[46] == true) {
                        routeA7[effectiveIndex].closedRadioButton.setSelected(true);
                    } else {
                        routeA7[effectiveIndex].thrownRadioButton.setSelected(true);
                    }
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    private final ActionListener basicConfigChangeActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().getClass() == JComboBox.class) {
                switch (((Component) e.getSource()).getName()) {
                    case "1": // NOI18N
                        opsw[1] = (outputType.getSelectedIndex() == 1);
                        updateGuiBasicOpSw(1);
                        break;
                    case "2345": // NOI18N
                        int selection = delayTime.getSelectedIndex();
                        opsw[2] = ((selection & 0x1) == 1);
                        opsw[3] = ((selection & 0x2) == 2);
                        opsw[4] = ((selection & 0x4) == 4);
                        opsw[5] = ((selection & 0x8) == 8);
                        updateGuiBasicOpSw(2);
                        updateGuiBasicOpSw(3);
                        updateGuiBasicOpSw(4);
                        updateGuiBasicOpSw(5);
                        break;
                    case "6": // NOI18N
                        opsw[6] = (outputStates.getSelectedIndex() == 1);
                        updateGuiBasicOpSw(6);
                        break;
                    case "8": // NOI18N
                        opsw[8] = startupDelay.getSelectedIndex() == 1;
                        updateGuiBasicOpSw(8);
                        break;
                    case "9": // NOI18N
                        opsw[9] = staticOutputShutoff.getSelectedIndex() == 1;
                        updateGuiBasicOpSw(9);
                        break;
                    case "10": // NOI18N
                        opsw[10] = commandType.getSelectedIndex() == 1;
                        updateGuiBasicOpSw(10);
                        break;
                    case "13": // NOI18N
                        opsw[13] = (sensorMessageTrigger.getSelectedIndex() == 1);
                        updateGuiBasicOpSw(13);
                        break;
                    case "14": // NOI18N
                        opsw[14] = commandSource.getSelectedIndex() == 1;
                        updateGuiBasicOpSw(14);
                        break;
                    case "21": // NOI18N
                        opsw[21] = localSensorType.getSelectedIndex() == 1;
                        updateGuiBasicOpSw(21);
                        break;

                    case "1116": // NOI18N
                        opsw[11] = (routesControl.getSelectedIndex() == 1) || (routesControl.getSelectedIndex() == 3);
                        opsw[16] = routesControl.getSelectedIndex() >= 2;
                        updateGuiBasicOpSw(11);
                        updateGuiBasicOpSw(16);
                        break;
                    case "1215": // NOI18N
                        opsw[12] = (localControlOfOutputsStyle.getSelectedIndex() & 1) == 1;  //2 -> OpSw12="c"
                        opsw[15] = (localControlOfOutputsStyle.getSelectedIndex() >= 2);  //0 -> OpSw15="c"
                        updateGuiBasicOpSw(12);
                        updateGuiBasicOpSw(15);
                        break;
                    default:
                }
            } else if (e.getSource().getClass() == JCheckBox.class) {
                switch (((Component) e.getSource()).getName()) {
                    case "17": // NOI18N
                        opsw[17] = output1CrossbuckFlasherCheckBox.isSelected();
                        updateGuiBasicOpSw(17);
                        break;
                    case "18": // NOI18N
                        opsw[18] = output2CrossbuckFlasherCheckBox.isSelected();
                        updateGuiBasicOpSw(18);
                        break;
                    case "19": // NOI18N
                        opsw[19] = output3CrossbuckFlasherCheckBox.isSelected();
                        updateGuiBasicOpSw(19);
                        break;
                    case "20": // NOI18N
                        opsw[20] = output4CrossbuckFlasherCheckBox.isSelected();
                        updateGuiBasicOpSw(20);
                        break;
                    default:
                        break;
                }

            }
        }
    };

    private void updateBasicOpSwTab() {
        for (int i = 1; i <= 21; ++i) {
            if (i != 7) {
                opswThrown[i].setSelected(!opsw[i]);
                opswClosed[i].setSelected(opsw[i]);
            }
        }
    }

    private class JRadioButtonWithInteger extends JRadioButton {

        public int index;

        JRadioButtonWithInteger(int i, String s) {
            super(s);
            index = i;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Ds64TabbedPanel.class); // NOI18N

}
