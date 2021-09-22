package jmri.jmrix.can.cbus.swing.modules.merg;


import java.awt.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon.*;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanCmdEditNVPane extends AbstractEditNVPane {
    
    private static final int USER_FLAGS = 0;
    private static final int OPS_FLAGS = 1;
    private static final int DEBUG_FLAGS = 2;
    private CmdStaFlags [] csFlags = new CmdStaFlags[3];
    
    private final UpdateNV cmdStaNoUpdateFn = new UpdateCmdStaNo();
    private final UpdateNV currentLimitUpdateFn = new UpdateCurrentLimit();
    private final UpdateNV nnMapUpdateFn = new UpdateNnMap();
    private final UpdateNV flagUpdateFn = new UpdateFlags();
    private final UpdateNV sodDelayUpdateFn = new UpdateSodDelay();
    private final UpdateNV intervalUpdateFn = new UpdateInterval();
    private final UpdateNV ackUpdateFn = new UpdateAck();
    private final UpdateNV multUpdateFn = new UpdateMult();
    private final UpdateNV walkaboutUpdateFn = new UpdateWalkabout();
    
    private CbusModulesCommon.TitledSpinner cmdStaNoSpinner;
    private CbusModulesCommon.TitledSpinner mainSpinner;
    private CbusModulesCommon.TitledSpinner progSpinner;
    private CbusModulesCommon.TitledSpinner nnMapDccSpinner;
    private CbusModulesCommon.TitledSpinner sodDelaySpinner;
    private CbusModulesCommon.TitledSpinner intervalSpinner;
    private CbusModulesCommon.TitledSpinner ackSpinner;
    private CbusModulesCommon.TitledSpinner multSpinner;
    private CbusModulesCommon.TitledSpinner walkaboutSpinner;
            
    protected String flagTitleStrings[] = new String[] {
        Bundle.getMessage("UserFlags"),
        Bundle.getMessage("OperationsFlags"),
        Bundle.getMessage("DebugFlags")
    };

    protected String flagStrings[][] = new String[][] {
        // User
        {Bundle.getMessage("Silent"),
            Bundle.getMessage("PermitSteal"),
            Bundle.getMessage("PermitShare"),
            Bundle.getMessage("PermitEvReset"),
            Bundle.getMessage("MapEvents"),
            Bundle.getMessage("StopOnTimeout"),
            Bundle.getMessage("StartOfDay"),
            Bundle.getMessage("Reserved")},
        // Ops
        {Bundle.getMessage("JumperControl"),
            Bundle.getMessage("MainOnBoard"),
            Bundle.getMessage("AnalogDetect"),
            Bundle.getMessage("ZtcMode"),
            Bundle.getMessage("AllStopTrackOff"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved")},
        // Debug
        {Bundle.getMessage("PriPkts"),
            Bundle.getMessage("SpdPkts"),
            Bundle.getMessage("FnPkts"),
            Bundle.getMessage("ServicePkts"),
            Bundle.getMessage("AccyPkts"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved")
        }};

    protected String flagTtStrings[][] = new String[][] {
        // User
        {Bundle.getMessage("SilentTt"),
            Bundle.getMessage("PermitStealTt"),
            Bundle.getMessage("PermitShareTt"),
            Bundle.getMessage("PermitEvResetTt"),
            Bundle.getMessage("MapEventsTt"),
            Bundle.getMessage("StopOnTimeoutTt"),
            Bundle.getMessage("StartOfDayTt"),
            Bundle.getMessage("ReservedTt")},
        // Ops
        {Bundle.getMessage("JumperControlTt"),
            Bundle.getMessage("MainOnBoardTt"),
            Bundle.getMessage("AnalogDetectTt"),
            Bundle.getMessage("ZtcModeTt"),
            Bundle.getMessage("AllStopTrackOffTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReervedTt"),
            Bundle.getMessage("ReservedTt")},
        // Debug
        {Bundle.getMessage("PriPktsTt"),
            Bundle.getMessage("SpdPktsTt"),
            Bundle.getMessage("FnPktsTt"),
            Bundle.getMessage("ServicePktsTt"),
            Bundle.getMessage("AccyPktsTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt")
        }};

    protected CanCmdEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getContent() {
        
        JPanel newPane = new JPanel(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel cmdStaPane = new CmdStaPane();
        JPanel dccPane = new DccPane();
        JPanel cbusPane = new CbusPane();
        tabbedPane.addTab(Bundle.getMessage("CmdSta"), cmdStaPane);
        tabbedPane.addTab("DCC", dccPane);
        tabbedPane.addTab("CBUS", cbusPane);
        
        JScrollPane scroll = new JScrollPane(tabbedPane);
        
        newPane.add(scroll, BorderLayout.CENTER);
        newPane.validate();
        newPane.repaint();
        
        return newPane;
    }

    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int nv = row + 1;
            CbusNodeNVTableDataModel model = (CbusNodeNVTableDataModel)e.getSource();
            int value = (int)model.getValueAt(row, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _nvArray[nv] = value;
            
            switch (nv) {
                case CanCmdPaneProvider.CMD_STATION_NUMBER:
                    cmdStaNoSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.USER_FLAGS:
                    csFlags[0].setFlags(value);
                    break;
                    
                case CanCmdPaneProvider.OPERATIONS_FLAGS:
                    csFlags[1].setFlags(value);
                    break;
                    
                case CanCmdPaneProvider.DEBUG_FLAGS:
                    csFlags[2].setFlags(value);
                    break;
                            
                case CanCmdPaneProvider.PROG_TRACK_CURRENT_LIMIT:
                    progSpinner.getModel().setValue(value);
                    break;
                
                case CanCmdPaneProvider.MAIN_TRACK_CURRENT_LIMIT:
                    mainSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.NN_MAP_DCC_HI:
                case CanCmdPaneProvider.NN_MAP_DCC_LO:
                    nnMapDccSpinner.getModel().setValue(_nvArray[CanCmdPaneProvider.NN_MAP_DCC_HI]*256 + _nvArray[CanCmdPaneProvider.NN_MAP_DCC_LO]);
                    break;
                    
                case CanCmdPaneProvider.WALKABOUT_TIMEOUT:
                    walkaboutSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.CURRENT_MULTIPLIER:
                    multSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.INC_CURRENT_FOR_ACK:
                    ackSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.SEND_CURRENT_INTERVAL:
                    intervalSpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.SOD_DELAY:
                    sodDelaySpinner.getModel().setValue(value);
                    break;
                    
                case CanCmdPaneProvider.UNUSED_NV10:
                case CanCmdPaneProvider.UNUSED_NV15:
                case CanCmdPaneProvider.UNUSED_NV16:
                    // Not currently used on CANCMD
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unexpected NV index");
                    
            }
        }
    }
    
    /**
     * Update the command station number
     */
    protected class UpdateCmdStaNo implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int cmdStaNo = ((SpinnerNumberModel)cmdStaNoSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = cmdStaNo;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(cmdStaNo, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
        
    /**
     * Update the Flags
     */
    protected class UpdateFlags implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int flags = csFlags[index].getFlags();
            _nvArray[CanCmdPaneProvider.USER_FLAGS + index] = flags;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(flags, CanCmdPaneProvider.USER_FLAGS + index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
        
    /**
     * Update a current Limit
     */
    protected class UpdateCurrentLimit implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int limit;
            float fLimit;
            if (index == CanCmdPaneProvider.MAIN_TRACK_CURRENT_LIMIT) {
                fLimit = ((SpinnerNumberModel)mainSpinner.getModel()).getNumber().floatValue();
            } else {
                fLimit = ((SpinnerNumberModel)progSpinner.getModel()).getNumber().floatValue();
            }
            // Limit to 10mA precision
            limit = (int)(fLimit*100 + 0.5);
            _nvArray[index] = limit;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(limit, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the Node Number of events to be mapped to DCC accessory commands
     */
    protected class UpdateNnMap implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int mapNn = ((SpinnerNumberModel)nnMapDccSpinner.getModel()).getNumber().intValue();
            int mapNnHi = mapNn/256;
            int mapNnLo = mapNn%256;
            _nvArray[index] = mapNnHi;
            _nvArray[index + 1] = mapNnLo;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(mapNnHi, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(mapNnLo, index, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }

    protected class UpdateSodDelay implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int sodDelay = ((SpinnerNumberModel)sodDelaySpinner.getModel()).getNumber().intValue();
            _nvArray[index] = sodDelay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(sodDelay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    protected class UpdateInterval implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int interval = ((SpinnerNumberModel)intervalSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = interval;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(interval, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    protected class UpdateAck implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int ack = ((SpinnerNumberModel)ackSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = ack;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(ack, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    protected class UpdateMult implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int mult = ((SpinnerNumberModel)multSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = mult;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(mult, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    protected class UpdateWalkabout implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int delay = ((SpinnerNumberModel)multSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = delay;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(delay, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    public class CmdStaPane extends JPanel {

        /**
         * Panel to display command station NVs
         */
        public CmdStaPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            JPanel [] flagPane = new JPanel[3];
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            for (int i = 0; i < 3; i++) {
                csFlags[i] = new CbusModulesCommon.CmdStaFlags(i, flagTitleStrings[i], flagStrings[i], flagTtStrings[i], flagUpdateFn);
                flagPane[i] = csFlags[i].getContents();
            }
            
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            // x = 1
            cmdStaNoSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("CmdStaNo"), CanCmdPaneProvider.CMD_STATION_NUMBER, cmdStaNoUpdateFn);
            cmdStaNoSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            cmdStaNoSpinner.init(0, 0, 255, 1);
            gridPane.add(cmdStaNoSpinner, c);
            c.gridy++;
            
            walkaboutSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("WalkaboutTimeout"), CanCmdPaneProvider.WALKABOUT_TIMEOUT, walkaboutUpdateFn);
            walkaboutSpinner.setToolTip(Bundle.getMessage("WalkaboutTimeoutTt"));
            walkaboutSpinner.init(_nvArray[CanCmdPaneProvider.WALKABOUT_TIMEOUT], 1, 60, 255);
            gridPane.add(walkaboutSpinner, c);
            c.gridy++;
            
            multSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("CurrentMultiplier"), CanCmdPaneProvider.CURRENT_MULTIPLIER, multUpdateFn);
            multSpinner.setToolTip(Bundle.getMessage("CurrentMultiplierTt"));
            multSpinner.init(_nvArray[CanCmdPaneProvider.CURRENT_MULTIPLIER], 1, 10, 255);
            gridPane.add(multSpinner, c);
            c.gridy++;
            
            ackSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("IncCurrentForAck"), CanCmdPaneProvider.INC_CURRENT_FOR_ACK, ackUpdateFn);
            ackSpinner.setToolTip(Bundle.getMessage("IncCurrentForAckTt"));
            ackSpinner.init(_nvArray[CanCmdPaneProvider.INC_CURRENT_FOR_ACK], 1, 3, 255);
            gridPane.add(ackSpinner, c);
            c.gridy++;
            
            intervalSpinner = new TitledSpinner(Bundle.getMessage("SendCurrentInterval"), CanCmdPaneProvider.SEND_CURRENT_INTERVAL, intervalUpdateFn);
            intervalSpinner.setToolTip(Bundle.getMessage("SendCurrentIntervalTt"));
            intervalSpinner.init(_nvArray[CanCmdPaneProvider.SEND_CURRENT_INTERVAL], 0, 255, 1);
            gridPane.add(intervalSpinner, c);
            c.gridy++;
            
            c.gridwidth = 3;
            
            mainSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("MainLimit"), CanCmdPaneProvider.MAIN_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            mainSpinner.setToolTip(Bundle.getMessage("MainLimitTt"));
            mainSpinner.init(_nvArray[CanCmdPaneProvider.MAIN_TRACK_CURRENT_LIMIT], 1, 96, 255);
            gridPane.add(mainSpinner, c);
            c.gridy++;
            
            gridPane.add(flagPane[USER_FLAGS], c);
            
            c.gridx++;

            // x = 2
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            progSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("ProgLimit"), CanCmdPaneProvider.PROG_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            progSpinner.setToolTip(Bundle.getMessage("ProgLimitTt"));
            progSpinner.init(_nvArray[CanCmdPaneProvider.PROG_TRACK_CURRENT_LIMIT], 1, 96, 255);
            gridPane.add(progSpinner, c);
            c.gridy++;
            
            gridPane.add(flagPane[OPS_FLAGS], c);
            
            c.gridx++;

            // x = 3
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            gridPane.add(flagPane[DEBUG_FLAGS], c);
            c.gridx++;

            add(gridPane);
        }
    }
    
    /**
     * Panel to display DCC related NVs
     */
    public class DccPane extends JPanel {
        public DccPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            
            c.gridy++;
            
            nnMapDccSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("NnMapDcc"), CanCmdPaneProvider.NN_MAP_DCC_HI, nnMapUpdateFn);
            nnMapDccSpinner.setToolTip(Bundle.getMessage("NnMapDccTt"));
            int nn = _nvArray[CanCmdPaneProvider.NN_MAP_DCC_HI]*256 + _nvArray[CanCmdPaneProvider.NN_MAP_DCC_LO];
            nnMapDccSpinner.init(nn, 0, 65535, 1);
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;
            
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;

            add(gridPane);
        }
    }
    
    /**
     * Panel to display CBUS operation related NVs
     */
    public class CbusPane extends JPanel {
        public CbusPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            sodDelaySpinner = new TitledSpinner(Bundle.getMessage("SodDelay"), CanCmdPaneProvider.SOD_DELAY, sodDelayUpdateFn);
            sodDelaySpinner.setToolTip(Bundle.getMessage("SodDelayTt"));
            sodDelaySpinner.init(_nvArray[CanCmdPaneProvider.SOD_DELAY], 0, 255, 1);
            gridPane.add(sodDelaySpinner, c);
            c.gridy++;

            add(gridPane);
        }
    }
    
    //private final static Logger log = LoggerFactory.getLogger(CanCmdEditNVPane.class);

}
