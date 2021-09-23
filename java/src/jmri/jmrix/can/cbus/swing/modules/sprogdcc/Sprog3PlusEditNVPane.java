package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.*;
import java.awt.event.ActionEvent;

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
public class Sprog3PlusEditNVPane extends AbstractEditNVPane {
    
    private static final int USER_FLAGS = 0;
    private static final int OPS_FLAGS = 1;
    private static final int DEBUG_FLAGS = 2;
    private CbusModulesCommon.CmdStaFlags [] csFlags = new CbusModulesCommon.CmdStaFlags[3];
    
    private final UpdateNV cmdStaNoUpdateFn = new UpdateCmdStaNo();
    private final UpdateNV canIdUpdateFn = new UpdateCanId();
    private final UpdateNV nodeNumberUpdateFn = new UpdateNodeNumber();
    private final UpdateNV currentLimitUpdateFn = new UpdateCurrentLimit();
    private final UpdateNV accyPktUpdateFn = new UpdateAccyCount();
    private final UpdateNV nnMapUpdateFn = new UpdateNnMap();
    private final UpdateNV preambleUpdateFn = new UpdatePreamble();
    private final UpdateNV powerModeUpdateFn = new UpdatePowerMode();
    private final UpdateNV meterUpdateFn = new UpdateMeter();
    private final UpdateNV flagUpdateFn = new UpdateFlags();
    
    private CbusModulesCommon.TitledSpinner cmdStaNoSpinner;
    private JComboBox<String> powerModeList ;
    private CbusModulesCommon.TitledSpinner mainSpinner;
    private CbusModulesCommon.TitledSpinner progSpinner;
    private CbusModulesCommon.TitledSpinner accyPktSpinner;
    private JRadioButton meter;
    private CbusModulesCommon.TitledSpinner nnMapDccSpinner;
    private JRadioButton setup;
    private CbusModulesCommon.TitledSpinner canIdSpinner;
    private CbusModulesCommon.TitledSpinner nodeNumberSpinner;
    private CbusModulesCommon.TitledSpinner preambleSpinner;
    private JRadioButton disable ;
            
    protected String flagTitleStrings[] = new String[] {
        Bundle.getMessage("UserFlags"),
        Bundle.getMessage("OperationsFlags"),
        Bundle.getMessage("DebugFlags")
    };

    protected String flagStrings[][] = new String[][] {
        // User
        {Bundle.getMessage("Reserved"),
            Bundle.getMessage("PermitSteal"),
            Bundle.getMessage("PermitShare"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("MapEvents"),
            Bundle.getMessage("StopOnTimeout"),
            Bundle.getMessage("StartOfDay"),
            Bundle.getMessage("AutoPower")},
        // Ops
        {Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("ZtcMode"),
            Bundle.getMessage("AllStopTrackOff"),
            Bundle.getMessage("BluelineMode"),
            Bundle.getMessage("AckSensitivity"),
            Bundle.getMessage("Reserved")},
        // Debug
        {Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved"),
            Bundle.getMessage("Reserved")
        }};

    protected String flagTtStrings[][] = new String[][] {
        // User
        {Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("PermitStealTt"),
            Bundle.getMessage("PermitShareTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("MapEventsTt"),
            Bundle.getMessage("StopOnTimeoutTt"),
            Bundle.getMessage("StartOfDayTt"),
            Bundle.getMessage("AutoPowerTt")},
        // Ops
        {Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ZtcModeTt"),
            Bundle.getMessage("AllStopTrackOffTt"),
            Bundle.getMessage("BluelineModeTt"),
            Bundle.getMessage("AckSensitivityTt"),
            Bundle.getMessage("ReservedTt")},
        // Debug
        {Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt"),
            Bundle.getMessage("ReservedTt")
        }};

    protected Sprog3PlusEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
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
        tabbedPane.addTab("CBUS Diagnostics", cbusPane);
        
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
                case Sprog3PlusPaneProvider.CMD_STATION_NUMBER:
                    cmdStaNoSpinner.getModel().setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.USER_FLAGS:
                    csFlags[0].setFlags(value);
                    break;
                    
                case Sprog3PlusPaneProvider.OPERATIONS_FLAGS:
                    csFlags[1].setFlags(value);
                    break;
                    
                case Sprog3PlusPaneProvider.DEBUG_FLAGS:
                    csFlags[2].setFlags(value);
                    break;
                            
                case Sprog3PlusPaneProvider.PROG_TRACK_POWER_MODE:
                    powerModeList.setSelectedIndex(value);
                    break;
                    
                case Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT:
                    double progLimit = (double)value/100;
                    progSpinner.getModel().setValue(progLimit);
                    break;
                
                case Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT:
                    double mainLimit = (double)value/100;
                    mainSpinner.getModel().setValue(mainLimit);
                    break;
                    
                case Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT:
                    accyPktSpinner.getModel().setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.MULTIMETER_MODE:
                    meter.setSelected(value != 0);
                    break;
                    
                case Sprog3PlusPaneProvider.NN_MAP_DCC_HI:
                case Sprog3PlusPaneProvider.NN_MAP_DCC_LO:
                    nnMapDccSpinner.getModel().setValue(_nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_HI]*256 + _nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_LO]);
                    break;
                    
                case Sprog3PlusPaneProvider.SETUP:
                    setup.setSelected(value != 0);
                    break;
                    
                case Sprog3PlusPaneProvider.CANID:
                    canIdSpinner.getModel().setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.NN_HI:
                case Sprog3PlusPaneProvider.NN_LO:
                    nodeNumberSpinner.getModel().setValue(_nvArray[Sprog3PlusPaneProvider.NN_HI]*256 + _nvArray[Sprog3PlusPaneProvider.NN_LO]);
                    break;
                    
                case Sprog3PlusPaneProvider.DCC_PREAMBLE:
                    preambleSpinner.getModel().setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.CAN_DISABLE:
                    disable.setEnabled(value != 0);
                    break;

                case Sprog3PlusPaneProvider.INPUT_VOLTAGE:
                case Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT:
                case Sprog3PlusPaneProvider.PROG_TRACK_CURRENT:
                case Sprog3PlusPaneProvider.MAIN_HIGH_WATER_MARK:
                case Sprog3PlusPaneProvider.PROG_HIGH_WATER_MARK:
                    // These read-only NVs are not preented in the edit GUI as they can be displayed on meters
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
            _nvArray[Sprog3PlusPaneProvider.USER_FLAGS + index] = flags;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(flags, Sprog3PlusPaneProvider.USER_FLAGS + index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
        
    /**
     * Update the CAN ID
     * 
     * For debug only, CAN ID is not normally set this way
     */
    protected class UpdateCanId implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int canId = ((SpinnerNumberModel)canIdSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = canId;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(canId, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
        
    /**
     * Update the node number
     * 
     * For debug only, CAN ID is not normally set this way
     */
    protected class UpdateNodeNumber implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int nn = ((SpinnerNumberModel)nodeNumberSpinner.getModel()).getNumber().intValue();
            int nnHi = nn/256;
            int nnLo = nn%256;
            _nvArray[index] = nnHi;
            _nvArray[index + 1] = nnLo;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(nnHi, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(nnLo, index, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the multi meter events setting
     */
    protected class UpdateMeter implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int mode = meter.isSelected() ? 1 : 0;
            _nvArray[index] = mode;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(mode, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
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
            if (index == Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT) {
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
     * Update the number of times a DCC accessory packet is repeated
     */
    protected class UpdateAccyCount implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int cnt = ((SpinnerNumberModel)accyPktSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = cnt;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(cnt, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
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

    /**
     * Update the number of DCC packet preamble bits
     */
    protected class UpdatePreamble implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int pre = ((SpinnerNumberModel)preambleSpinner.getModel()).getNumber().intValue();
            _nvArray[index] = pre;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(pre, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }
    
    /**
     * Update the command station power mode
     */
    protected class UpdatePowerMode implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int mode = powerModeList.getSelectedIndex();
            _nvArray[index] = mode;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(mode, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
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
            
            String powerModeStrings [] = new String[] {Bundle.getMessage("ProgOffMode"),
                Bundle.getMessage("ProgOnMode"),
                Bundle.getMessage("ProgArMode")
            };
            
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            // x = 1
            cmdStaNoSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("CmdStaNo"), Sprog3PlusPaneProvider.CMD_STATION_NUMBER, cmdStaNoUpdateFn);
            cmdStaNoSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            cmdStaNoSpinner.init(0, 0, 255, 1);
            gridPane.add(cmdStaNoSpinner, c);
            c.gridy++;
            
            c.gridwidth = 3;
            powerModeList = new JComboBox<>(powerModeStrings);
            powerModeList.setSelectedIndex(_nvArray[Sprog3PlusPaneProvider.PROG_TRACK_POWER_MODE]);
            powerModeList.addActionListener((ActionEvent e) -> {
                pwrModeActionListener(e);
            });
            gridPane.add(powerModeList, c);
            c.gridwidth = 1;
            c.gridy++;
            
            meter = new JRadioButton(Bundle.getMessage("Multimeter"));
            meter.setSelected(false);
            meter.setToolTipText(Bundle.getMessage("MultimeterTt"));
            meter.addActionListener((ActionEvent e) -> {
                meterActionListener(e);
            });
            gridPane.add(meter, c);
            c.gridy++;
            
            mainSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("MainLimit"), Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            mainSpinner.setToolTip(Bundle.getMessage("MainLimitTt"));
            mainSpinner.init(_nvArray[Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT]/100.0, 1.0, 2.5, 0.01);
            gridPane.add(mainSpinner, c);
            c.gridy++;
            
            gridPane.add(flagPane[USER_FLAGS], c);
            c.gridx++;

            // x = 2
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            progSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("ProgLimit"), Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            progSpinner.setToolTip(Bundle.getMessage("ProgLimitTt"));
            progSpinner.init(_nvArray[Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT]/100.0, 1.0, 2.5, 0.01);
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
            
            gridPane.add(flagPane[DEBUG_FLAGS], c);
            c.gridx++;

            add(gridPane);
        }
        
        /**
         * Action listener for the command station power mode
         * 
         * @param e the action event
         */
        protected void pwrModeActionListener(ActionEvent e) {
            powerModeUpdateFn.setNewVal(Sprog3PlusPaneProvider.PROG_TRACK_POWER_MODE);
        }
        
        /**
         * Action listener for multimeter nmode selection
         * 
         * @param e the action event
         */
        protected void meterActionListener(ActionEvent e) {
            meterUpdateFn.setNewVal(Sprog3PlusPaneProvider.MULTIMETER_MODE);
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
            
            accyPktSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("AccyPktCnt"), Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT, accyPktUpdateFn);
            accyPktSpinner.setToolTip(Bundle.getMessage("AccyPktCntTt"));
            accyPktSpinner.init(_nvArray[Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT], 1, 7, 1);
            gridPane.add(accyPktSpinner, c);
            c.gridy++;
            
            nnMapDccSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("NnMapDcc"), Sprog3PlusPaneProvider.NN_MAP_DCC_HI, nnMapUpdateFn);
            nnMapDccSpinner.setToolTip(Bundle.getMessage("NnMapDccTt"));
            int nn = _nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_HI]*256 + _nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_LO];
            nnMapDccSpinner.init(nn, 0, 65535, 1);
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;
            
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;

            preambleSpinner = new TitledSpinner(Bundle.getMessage("DccPreambles"), Sprog3PlusPaneProvider.DCC_PREAMBLE, preambleUpdateFn);
            preambleSpinner.setToolTip(Bundle.getMessage("DccPreamblesTt"));
            preambleSpinner.init(_nvArray[Sprog3PlusPaneProvider.DCC_PREAMBLE], 14, 32, 1);
            gridPane.add(preambleSpinner, c);
                    
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

            canIdSpinner = new TitledSpinner(Bundle.getMessage("CanId"), Sprog3PlusPaneProvider.CANID, canIdUpdateFn);
            canIdSpinner.setToolTip(Bundle.getMessage("CanIdTt"));
            canIdSpinner.init(_nvArray[Sprog3PlusPaneProvider.CANID], 100, 127, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), Sprog3PlusPaneProvider.NN_HI, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("NodeNumberTt"));
            int nn = _nvArray[Sprog3PlusPaneProvider.NN_HI]*256 + _nvArray[Sprog3PlusPaneProvider.NN_LO];
            nodeNumberSpinner.init(nn, 65520, 65534, 1);
            gridPane.add(nodeNumberSpinner, c);
            c.gridy++;
            
            setup = new JRadioButton("SetupMode");
            setup.setSelected(false);
            setup.setToolTipText(Bundle.getMessage("SetupModeTt"));
            gridPane.add(setup, c);
            c.gridy++;
            
            disable = new JRadioButton("DisableCan");
            disable.setSelected(false);
            disable.setToolTipText(Bundle.getMessage("DisableCanTt"));
            gridPane.add(disable, c);
            c.gridy++;
            
            add(gridPane);
        }
    }
    
    //private final static Logger log = LoggerFactory.getLogger(Sprog3PlusEditNVPane.class);

}
