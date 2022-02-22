package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Sprog3PlusEditNVPane extends AbstractEditNVPane {
    
    private static final int USER_FLAGS = 0;
    private static final int OPS_FLAGS = 1;
    private static final int DEBUG_FLAGS = 2;
    private CmdStaFlags [] csFlags = new CmdStaFlags[3];
    
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
    
    private TitledSpinner cmdStaNoSpinner;
    private JComboBox<String> powerModeList ;
    private TitledSpinner mainSpinner;
    private TitledSpinner progSpinner;
    private TitledSpinner accyPktSpinner;
    private JRadioButton meter;
    private TitledSpinner nnMapDccSpinner;
    private JRadioButton setup;
    private TitledSpinner canIdSpinner;
    private TitledSpinner nodeNumberSpinner;
    private TitledSpinner preambleSpinner;
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
    public AbstractEditNVPane getContent() {
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel cmdStaPane = new CmdStaPane();
        JPanel dccPane = new DccPane();
        JPanel cbusPane = new CbusPane();
        tabbedPane.addTab(Bundle.getMessage("CmdSta"), cmdStaPane);
        tabbedPane.addTab("DCC", dccPane);
        tabbedPane.addTab("CBUS Diagnostics", cbusPane);
        
        JScrollPane scroll = new JScrollPane(tabbedPane);
        add(scroll);
        
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int nv = row + 1;
            int value = getSelectValue(nv);
            log.debug("sprog3plus gui table changed NV: {} Value: {}", nv, value);
            
            switch (nv) {
                case Sprog3PlusPaneProvider.CMD_STATION_NUMBER:
                    cmdStaNoSpinner.setValue(value);
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
                    // Do nothing if nothing has changed, else causes an endless
                    // round of gui update, table update, ...
                    if (value != powerModeList.getSelectedIndex()) {
                        powerModeList.setSelectedIndex(value);
                    }
                    break;
                    
                case Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT:
                    double progLimit = (double)value/100;
                    progSpinner.setValue(progLimit);
                    break;
                
                case Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT:
                    double mainLimit = (double)value/100;
                    mainSpinner.setValue(mainLimit);
                    break;
                    
                case Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT:
                    accyPktSpinner.setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.MULTIMETER_MODE:
                    meter.setSelected(value != 0);
                    break;
                    
                case Sprog3PlusPaneProvider.NN_MAP_DCC_HI:
                case Sprog3PlusPaneProvider.NN_MAP_DCC_LO:
                    nnMapDccSpinner.setValue(getSelectValue(Sprog3PlusPaneProvider.NN_MAP_DCC_HI,
                            Sprog3PlusPaneProvider.NN_MAP_DCC_LO, 0));
                    break;
                    
                case Sprog3PlusPaneProvider.SETUP:
                    setup.setSelected(value != 0);
                    break;
                    
                case Sprog3PlusPaneProvider.CANID:
                    canIdSpinner.setValue(value);
                    break;
                    
                case Sprog3PlusPaneProvider.NN_HI:
                case Sprog3PlusPaneProvider.NN_LO:
                    nodeNumberSpinner.setValue(getSelectValue(Sprog3PlusPaneProvider.NN_HI,
                            Sprog3PlusPaneProvider.NN_LO, 0));
                    break;
                    
                case Sprog3PlusPaneProvider.DCC_PREAMBLE:
                    preambleSpinner.setValue(value);
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
                    // Not used, or row was -1
//                    log.debug("Update unknown NV {}", nv);
                    
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
            int cmdStaNo = cmdStaNoSpinner.getIntegerValue();
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
            int canId = canIdSpinner.getIntegerValue();
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
            int nn = nodeNumberSpinner.getIntegerValue();
            int nnHi = nn/256;
            int nnLo = nn%256;
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
            double fLimit;
            if (index == Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT) {
                fLimit = mainSpinner.getDoubleValue();
            } else {
                fLimit = progSpinner.getDoubleValue();
            }
            // Limit to 10mA precision
            limit = (int)(fLimit*100 + 0.5);
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
            int cnt = accyPktSpinner.getIntegerValue();
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
            int mapNn = nnMapDccSpinner.getIntegerValue();
            int mapNnHi = mapNn/256;
            int mapNnLo = mapNn%256;
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
            int pre = preambleSpinner.getIntegerValue();
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
            log.debug("UpdatePowerMode.setNewVal()");
            int mode = powerModeList.getSelectedIndex();
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
                csFlags[i] = new CmdStaFlags(i, flagTitleStrings[i], flagStrings[i], flagTtStrings[i], flagUpdateFn);
                flagPane[i] = csFlags[i].getContents();
            }
            csFlags[0].setFlags(getSelectValue(Sprog3PlusPaneProvider.USER_FLAGS));
            csFlags[1].setFlags(getSelectValue(Sprog3PlusPaneProvider.OPERATIONS_FLAGS));
            csFlags[2].setFlags(getSelectValue(Sprog3PlusPaneProvider.DEBUG_FLAGS));
            
            String powerModeStrings [] = new String[] {Bundle.getMessage("ProgOffMode"),
                Bundle.getMessage("ProgOnMode"),
                Bundle.getMessage("ProgArMode")
            };
            
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            // x = 1
            cmdStaNoSpinner = new TitledSpinner(Bundle.getMessage("CmdStaNo"), Sprog3PlusPaneProvider.CMD_STATION_NUMBER, cmdStaNoUpdateFn);
            cmdStaNoSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            cmdStaNoSpinner.init(0, 0, 255, 1);
            gridPane.add(cmdStaNoSpinner, c);
            c.gridy++;
            
            c.gridwidth = 3;
            powerModeList = new JComboBox<>(powerModeStrings);
            powerModeList.setSelectedIndex(getSelectValue(Sprog3PlusPaneProvider.PROG_TRACK_POWER_MODE));
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
            
            mainSpinner = new TitledSpinner(Bundle.getMessage("MainLimit"), Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            mainSpinner.setToolTip(Bundle.getMessage("MainLimitTt"));
            mainSpinner.init(getSelectValue(Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT, 100)/100.0, 1.0, 2.5, 0.01);
            gridPane.add(mainSpinner, c);
            c.gridy++;
            
            gridPane.add(flagPane[USER_FLAGS], c);
            c.gridx++;

            // x = 2
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            progSpinner = new TitledSpinner(Bundle.getMessage("ProgLimit"), Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT, currentLimitUpdateFn);
            progSpinner.setToolTip(Bundle.getMessage("ProgLimitTt"));
            progSpinner.init(getSelectValue(Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT, 100)/100.0, 1.0, 2.5, 0.01);
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
            log.debug("pwrModeActionListener()");
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
            
            accyPktSpinner = new TitledSpinner(Bundle.getMessage("AccyPktCnt"), Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT, accyPktUpdateFn);
            accyPktSpinner.setToolTip(Bundle.getMessage("AccyPktCntTt"));
            accyPktSpinner.init(getSelectValue(Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT, 1), 1, 7, 1);
            gridPane.add(accyPktSpinner, c);
            c.gridy++;
            
            nnMapDccSpinner = new TitledSpinner(Bundle.getMessage("NnMapDcc"), Sprog3PlusPaneProvider.NN_MAP_DCC_HI, nnMapUpdateFn);
            nnMapDccSpinner.setToolTip(Bundle.getMessage("NnMapDccTt"));
            int nn = getSelectValue(Sprog3PlusPaneProvider.NN_MAP_DCC_HI, Sprog3PlusPaneProvider.NN_MAP_DCC_LO, 0);
            nnMapDccSpinner.init(nn, 0, 65535, 1);
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;
            
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;

            preambleSpinner = new TitledSpinner(Bundle.getMessage("DccPreambles"), Sprog3PlusPaneProvider.DCC_PREAMBLE, preambleUpdateFn);
            preambleSpinner.setToolTip(Bundle.getMessage("DccPreamblesTt"));
            preambleSpinner.init(getSelectValue(Sprog3PlusPaneProvider.DCC_PREAMBLE, 14), 14, 32, 1);
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
            canIdSpinner.init(getSelectValue(Sprog3PlusPaneProvider.CANID, 100), 100, 127, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), Sprog3PlusPaneProvider.NN_HI, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("NodeNumberTt"));
            int nn = getSelectValue(Sprog3PlusPaneProvider.NN_HI, Sprog3PlusPaneProvider.NN_LO, 65520);
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
    
    private final static Logger log = LoggerFactory.getLogger(Sprog3PlusEditNVPane.class);

}
