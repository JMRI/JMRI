package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon;
import jmri.jmrix.can.cbus.swing.modules.CbusModulesCommon.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Sprog3PlusEditNVPane extends AbstractEditNVPane {
    
    private static CmdStaFlags [] csFlags = new CmdStaFlags[3];
    
    private static final UpdateNV cmdStaNoUpdateFn = new UpdateCmdStaNo();
    private static final UpdateNV canIdUpdateFn = new UpdateCanId();
    private static final UpdateNV nodeNumberUpdateFn = new UpdateNodeNumber();
    private static final UpdateNV currentLimitUpdateFn = new UpdateCurrentLimit();
    private static final UpdateNV accyPktUpdateFn = new UpdateAccyCount();
    private static final UpdateNV nnMapUpdateFn = new UpdateNnMap();
    private static final UpdateNV preambleUpdateFn = new UpdatePreamble();
    private static final UpdateNV powerModeUpdateFn = new UpdatePowerMode();
    private static final UpdateNV meterUpdateFn = new UpdateMeter();
        
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
            if ((nv > 1) && (nv <= 4)) {
                log.debug("Update NV {} to {}", nv, value);
                csFlags[nv - 2].setButtons(value);
            }
//            } else if (nv == 9) {
//                log.debug("Update feedback delay to {}", value);
//                feedbackSpinner.tSpin.getModel().setValue(value*FEEDBACK_DELAY_STEP_SIZE);
//            } else if ((nv == 10) || (nv == 11)) {
//                log.debug("Update startup action", value);
//                for (int i = 1; i <= 8; i++) {
//                    out[i].action.setButtons();
//                }
//            } else if (nv == 12) {
//                // Not used
//                log.debug("Update unknow");
//                
//            } else {
//                throw new IllegalArgumentException("Unexpected NV index");
//            }
        }
    }
    
    /**
     * Update the command station number
     */
    protected static class UpdateCmdStaNo implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
        
    /**
     * Update the CAN ID
     * 
     * For debug only, CAN ID is not normally set this way
     */
    protected static class UpdateCanId implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
        
    /**
     * Update the node number
     * 
     * For debug only, CAN ID is not normally set this way
     */
    protected static class UpdateNodeNumber implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
    
    /**
     * Update the multi meter events setting
     */
    protected static class UpdateMeter implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int mode) {
        }
    }
    
    /**
     * Update a current Limit
     */
    protected static class UpdateCurrentLimit implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
    
    /**
     * Update the number of times a DCC accessory packet is repeated
     */
    protected static class UpdateAccyCount implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }

    /**
     * Update the Node Number of events to be mapped to DCC accessory commands
     */
    protected static class UpdateNnMap implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }

    /**
     * Update the number of DCC packet preamble bits
     */
    protected static class UpdatePreamble implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
    
    /**
     * Update the command station power mode
     */
    protected static class UpdatePowerMode implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int mode) {
        }
    }
    
    public static class CmdStaPane extends JPanel {

        /**
         * Panel to display command station NVs
         */
        public CmdStaPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            String userFlagStrings [] = new String[] {Bundle.getMessage("Reserved"),
                Bundle.getMessage("PermitSteal"),
                Bundle.getMessage("PermitShare"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("MapEvents"),
                Bundle.getMessage("StopOnTimeout"),
                Bundle.getMessage("StartOfDay"),
                Bundle.getMessage("AutoPower")
            };
            csFlags[0] = new CmdStaFlags(0, Bundle.getMessage("UserFlags"), userFlagStrings, _nvArray[Sprog3PlusPaneProvider.USER_FLAGS]);
            JPanel userFlags = csFlags[0].getContents();

            String opFlagStrings [] = new String[] {Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("ZtcMode"),
                Bundle.getMessage("AllStopTrackOff"),
                Bundle.getMessage("BluelineMode"),
                Bundle.getMessage("AckSensitivity"),
                Bundle.getMessage("Reserved")
            };
            csFlags[1] = new CmdStaFlags(1, Bundle.getMessage("OperationsFlags"), opFlagStrings, _nvArray[Sprog3PlusPaneProvider.OPERATIONS_FLAGS]);
            JPanel opFlags = csFlags[1].getContents();

            String debugFlagStrings [] = new String[] {Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved")
            };
            csFlags[2] = new CmdStaFlags(1, Bundle.getMessage("DebugFlags"), debugFlagStrings, _nvArray[Sprog3PlusPaneProvider.DEBUG_FLAGS]);
            JPanel debugFlags = csFlags[2].getContents();

            String powerModeStrings [] = new String[] {Bundle.getMessage("ProgOffMode"),
                Bundle.getMessage("ProgOnMode"),
                Bundle.getMessage("ProgArMode")
            };
            
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            // x = 1
            CbusModulesCommon.TitledSpinner cmdStaNoSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("CmdStaNo"), 0, cmdStaNoUpdateFn);
            cmdStaNoSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            cmdStaNoSpinner.init(0, 0, 255, 1);
            gridPane.add(cmdStaNoSpinner, c);
            c.gridy++;
            
            c.gridwidth = 3;
            JComboBox<String> powerModeList = new JComboBox<>(powerModeStrings);
            powerModeList.setSelectedIndex(_nvArray[Sprog3PlusPaneProvider.PROG_TRACK_POWER_MODE]);
            powerModeList.addActionListener((ActionEvent e) -> {
                pwrModeActionListener(e);
            });
            gridPane.add(powerModeList, c);
            c.gridwidth = 1;
            c.gridy++;
            
            JRadioButton meter = new JRadioButton(Bundle.getMessage("Multimeter"));
            meter.setSelected(false);
//            meter.setToolTipText("");
            meter.addActionListener((ActionEvent e) -> {
                meterActionListener(e);
            });
            gridPane.add(meter, c);
            c.gridy++;
            
            CbusModulesCommon.TitledSpinner mainSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("MainLimit"), 0, currentLimitUpdateFn);
//            mainSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            mainSpinner.init(_nvArray[Sprog3PlusPaneProvider.MAIN_TRACK_CURRENT_LIMIT]/100.0, 0.1, 2.5, 0.1);
            gridPane.add(mainSpinner, c);
            c.gridy++;
            
            gridPane.add(userFlags, c);
            c.gridx++;

            // x = 2
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            CbusModulesCommon.TitledSpinner progSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("ProgLimit"), 0, currentLimitUpdateFn);
//            mainSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            progSpinner.init(_nvArray[Sprog3PlusPaneProvider.PROG_TRACK_CURRENT_LIMIT]/100.0, 0.1, 2.5, 0.1);
            gridPane.add(progSpinner, c);
            c.gridy++;
            
            gridPane.add(opFlags, c);
            c.gridx++;

            // x = 3
            c.gridy = 0;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            c.gridy++;
            
            gridPane.add(debugFlags, c);
            c.gridx++;

            add(gridPane);
        }
        
        /**
         * Action listener for the command station power mode
         * 
         * @param e the action event
         */
        protected void pwrModeActionListener(ActionEvent e) {
            powerModeUpdateFn.setNewVal(((JComboBox)e.getSource()).getSelectedIndex());
        }
        
        /**
         * Action listener for multimeter nmode selection
         * 
         * @param e the action event
         */
        protected void meterActionListener(ActionEvent e) {
            meterUpdateFn.setNewVal(((JRadioButton)e.getSource()).isSelected() ? 1 : 0);
        }
    }
    
    /**
     * Panel to display DCC related NVs
     */
    public static class DccPane extends JPanel {
        public DccPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            
            CbusModulesCommon.TitledSpinner accyPktSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("AccyPktCnt"), 0, accyPktUpdateFn);
//            accyPktSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            accyPktSpinner.init(_nvArray[Sprog3PlusPaneProvider.ACCY_PACKET_REPEAT_COUNT], 1, 7, 1);
            gridPane.add(accyPktSpinner, c);
            c.gridy++;
            
            CbusModulesCommon.TitledSpinner nnMapDccSpinner = new CbusModulesCommon.TitledSpinner(Bundle.getMessage("NnMapDcc"), 0, nnMapUpdateFn);
//            nnMapDccSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            int nn = _nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_HI]*256 + _nvArray[Sprog3PlusPaneProvider.NN_MAP_DCC_LO];
            nnMapDccSpinner.init(nn, 0, 65535, 1);
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;
            
            gridPane.add(nnMapDccSpinner, c);
            c.gridy++;

            CbusModulesCommon.TitledSpinner PreambleSpinner = new TitledSpinner(Bundle.getMessage("DccPreambles"), 0, preambleUpdateFn);
//            PreambleSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            PreambleSpinner.setToolTip(Bundle.getMessage("CmdStaCanIdTt"));
            PreambleSpinner.init(_nvArray[Sprog3PlusPaneProvider.DCC_PREAMBLE], 14, 32, 1);
            gridPane.add(PreambleSpinner, c);
                    
            add(gridPane);
        }
    }
    
    /**
     * Panel to display CBUS operation related NVs
     */
    public static class CbusPane extends JPanel {
        public CbusPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            CbusModulesCommon.TitledSpinner canIdSpinner = new TitledSpinner(Bundle.getMessage("CanId"), 0, canIdUpdateFn);
//            canIdSpinner.setToolTip(Bundle.getMessage("CmdStaCanIdTt"));
            canIdSpinner.init(114, 100, 127, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            CbusModulesCommon.TitledSpinner nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), 0, nodeNumberUpdateFn);
//            nodeNumberSpinner.setToolTip(Bundle.getMessage("CmdStaNnTt"));
            nodeNumberSpinner.init(65534, 65520, 65534, 1);
            gridPane.add(nodeNumberSpinner, c);
            c.gridy++;
            
            JRadioButton setup = new JRadioButton("SetupMode");
            setup.setSelected(false);
//            setup.setToolTipText("");
            gridPane.add(setup, c);
            c.gridy++;
            
            JRadioButton disable = new JRadioButton("DisableCan");
            disable.setSelected(false);
//            disable.setToolTipText("");
            gridPane.add(disable, c);
            c.gridy++;
            
            add(gridPane);
        }
    }
    
    /**
     * Class to display CBUS command station flag settings
     */
    private static class CmdStaFlags extends JPanel {
        
//        protected int _type;
        protected String _title;
        protected String [] _fn;
        protected int _flags;
        protected JRadioButton [] buttons;

        public CmdStaFlags(int type, String title, String [] fn, int flags) {
            super();
            
//            _type = type;
            _title = title;
            _flags = flags;
            _fn = fn.clone();
            buttons = new JRadioButton[8];
        }
        
        protected JPanel getContents() {
            
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
            TitledBorder t = BorderFactory.createTitledBorder(border, _title);
            gridPane.setBorder(t);

            buttons = new JRadioButton[8];
            for (int i = 0; i < 8; i++) {
                buttons[i] = new JRadioButton(_fn[i]);
                gridPane.add(buttons[i], c);
                c.gridy++;
            }
            setButtons();
            
            return gridPane;
        }
        
        protected void setButtons() {
            for (int i = 0; i < 8; i++) {
                if ((_flags & (1<<i)) > 0) {
                    buttons[i].setSelected(true);
                } else {
                    buttons[i].setSelected(false);
                }
            }
        }
        
        protected void setButtons(int flags) {
            _flags = flags;
            setButtons();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Sprog3PlusEditNVPane.class);

}
