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
 * Node Variable edit frame for a SPROG DCC Pi-SPROG 3 module
 *
 * @author Andrew Crosland Copyright (C) 2022
 */
public class PiSprog3EditNVPane extends AbstractEditNVPane {
    
    private static final int USER_FLAGS = 0;
    private static final int OPS_FLAGS = 1;
    private CmdStaFlags [] csFlags = new CmdStaFlags[2];
    
    private final UpdateNV currentLimitUpdateFn = new UpdateCurrentLimit();
    private final UpdateNV accyPktUpdateFn = new UpdateAccyCount();
    private final UpdateNV preambleUpdateFn = new UpdatePreamble();
    private final UpdateNV modeUpdateFn = new UpdatePowerMode();
    private final UpdateNV meterUpdateFn = new UpdateMeter();
    private final UpdateNV flagUpdateFn = new UpdateFlags();
    
    private JComboBox<String> modeList ;
    private TitledSpinner mainSpinner;
    private TitledSpinner accyPktSpinner;
    private JRadioButton meter;
    private JRadioButton setup;
    private TitledSpinner preambleSpinner;
            
    protected String flagTitleStrings[] = new String[] {
        Bundle.getMessage("UserFlags"),
        Bundle.getMessage("OperationsFlags")
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
            Bundle.getMessage("ReservedTt")
        }};

    protected PiSprog3EditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
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
            int value = getSelectValue8(nv);
            log.debug("pisprog3 gui table changed NV: {} Value: {}", nv, value);
            
            switch (nv) {
                case PiSprog3PaneProvider.USER_FLAGS:
                    csFlags[0].setFlags(value);
                    break;
                    
                case PiSprog3PaneProvider.OPERATIONS_FLAGS:
                    csFlags[1].setFlags(value);
                    break;
                    
                case PiSprog3PaneProvider.CMD_STATION_MODE:
                    // Do nothing if nothing has changed, else causes an endless
                    // round of gui update, table update, ...
                    if (value != modeList.getSelectedIndex()) {
                        modeList.setSelectedIndex(value);
                    }
                    break;
                    
                case PiSprog3PaneProvider.CURRENT_LIMIT:
                    double mainLimit = (double)value/100;
                    mainSpinner.setValue(mainLimit);
                    break;
                    
                case PiSprog3PaneProvider.ACCY_PACKET_REPEAT_COUNT:
                    accyPktSpinner.setValue(value);
                    break;
                    
                case PiSprog3PaneProvider.MULTIMETER_MODE:
                    meter.setSelected(value != 0);
                    break;
                    
                case PiSprog3PaneProvider.SETUP:
                    setup.setSelected(value != 0);
                    break;
                    
                case PiSprog3PaneProvider.DCC_PREAMBLE:
                    preambleSpinner.setValue(value);
                    break;
                    
                case PiSprog3PaneProvider.INPUT_VOLTAGE:
                case PiSprog3PaneProvider.TRACK_CURRENT:
                    // These read-only NVs are not preented in the edit GUI as they can be displayed on meters
                    break;
                    
                default:
                    // Not used, or row was -1
//                    log.debug("Update unknown NV {}", nv);
                    
            }
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
            _dataModel.setValueAt(flags, csFlags[index].getNv() - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
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
            fLimit = mainSpinner.getDoubleValue();
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
            int mode = modeList.getSelectedIndex();
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
            JPanel [] flagPane = new JPanel[2];
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            for (int i = 0; i < 2; i++) {
                csFlags[i] = new CmdStaFlags(i, PiSprog3PaneProvider.USER_FLAGS + i, flagTitleStrings[i], flagStrings[i], flagTtStrings[i], flagUpdateFn);
                csFlags[i].setFlags(getSelectValue8(PiSprog3PaneProvider.USER_FLAGS + i));
                flagPane[i] = csFlags[i].getContents();
            }
            
            String modeStrings [] = new String[] {Bundle.getMessage("ProgMode"),
                Bundle.getMessage("CmdMode")
            };
            
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            // x = 1
            c.gridwidth = 3;
            modeList = new JComboBox<>(modeStrings);
            modeList.setSelectedIndex(getSelectValue8(PiSprog3PaneProvider.CMD_STATION_MODE));
            modeList.addActionListener((ActionEvent e) -> {
                modeActionListener(e);
            });
            gridPane.add(modeList, c);
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
            
            mainSpinner = new TitledSpinner(Bundle.getMessage("MainLimit"), PiSprog3PaneProvider.CURRENT_LIMIT, currentLimitUpdateFn);
            mainSpinner.setToolTip(Bundle.getMessage("MainLimitTt"));
            mainSpinner.init(getSelectValue8(PiSprog3PaneProvider.CURRENT_LIMIT, 100)/100.0, 1.0, 2.5, 0.01);
            gridPane.add(mainSpinner, c);
            c.gridy++;
            c.gridx = 0;
            
            gridPane.add(flagPane[USER_FLAGS], c);
            c.gridx++;

            gridPane.add(flagPane[OPS_FLAGS], c);
            c.gridx++;

            add(gridPane);
        }
        
        /**
         * Action listener for the command station power mode
         * 
         * @param e the action event
         */
        protected void modeActionListener(ActionEvent e) {
            log.debug("modeActionListener()");
            modeUpdateFn.setNewVal(PiSprog3PaneProvider.CMD_STATION_MODE);
        }
        
        /**
         * Action listener for multimeter nmode selection
         * 
         * @param e the action event
         */
        protected void meterActionListener(ActionEvent e) {
            meterUpdateFn.setNewVal(PiSprog3PaneProvider.MULTIMETER_MODE);
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
            
            accyPktSpinner = new TitledSpinner(Bundle.getMessage("AccyPktCnt"), PiSprog3PaneProvider.ACCY_PACKET_REPEAT_COUNT, accyPktUpdateFn);
            accyPktSpinner.setToolTip(Bundle.getMessage("AccyPktCntTt"));
            accyPktSpinner.init(getSelectValue8(PiSprog3PaneProvider.ACCY_PACKET_REPEAT_COUNT, 1), 1, 7, 1);
            gridPane.add(accyPktSpinner, c);
            c.gridy++;
            
            preambleSpinner = new TitledSpinner(Bundle.getMessage("DccPreambles"), PiSprog3PaneProvider.DCC_PREAMBLE, preambleUpdateFn);
            preambleSpinner.setToolTip(Bundle.getMessage("DccPreamblesTt"));
            preambleSpinner.init(getSelectValue8(PiSprog3PaneProvider.DCC_PREAMBLE, 14), 14, 32, 1);
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

            setup = new JRadioButton("SetupMode");
            setup.setSelected(false);
            setup.setToolTipText(Bundle.getMessage("SetupModeTt"));
            gridPane.add(setup, c);
            c.gridy++;
            
            add(gridPane);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(PiSprog3EditNVPane.class);

}
