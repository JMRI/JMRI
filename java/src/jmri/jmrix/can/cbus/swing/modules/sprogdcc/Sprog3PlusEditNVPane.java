package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.*;

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
    
    CmdStaFlags [] csFlags = new CmdStaFlags[3];
    
    // To allow instantiation of CbusModulesCommon inner classes
    private final CbusModulesCommon common = new CbusModulesCommon();
    
    private final UpdateCmdStaNo cmdStaNoUpdateFn = new UpdateCmdStaNo();
    private final UpdateCanId canIdUpdateFn = new UpdateCanId();
    private final UpdateNodeNumber nodeNumberUpdateFn = new UpdateNodeNumber();
        
    protected Sprog3PlusEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getContent() {
        
        JPanel newPane = new JPanel(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel cmdStaPane = new CmdStaPane();
        JPanel powerPane = new PowerPane();
        JPanel dccPane = new DccPane();
        JPanel cbusPane = new CbusPane();
        tabbedPane.addTab(Bundle.getMessage("CmdSta"), cmdStaPane);
        tabbedPane.addTab("Power", powerPane);
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
    protected class UpdateCmdStaNo implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
        
    /**
     * Update the command station number
     */
    protected class UpdateCanId implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
        
    /**
     * Update the command station number
     */
    protected class UpdateNodeNumber implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
        }
    }
        
    public class CmdStaPane extends JPanel {
        
        TitledSpinner cmdStaNoSpinner;
        TitledSpinner canIdSpinner;
        TitledSpinner nodeNumberSpinner;
        
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
                Bundle.getMessage("AutoPower")};
            csFlags[0] = new CmdStaFlags(0, Bundle.getMessage("UserFlags"), userFlagStrings, _nvArray[2]);
            JPanel userFlags = csFlags[0].getContents();

            String opFlagStrings [] = new String[] {Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("ZtcMode"),
                Bundle.getMessage("AllStopTrackOff"),
                Bundle.getMessage("BluelineMode"),
                Bundle.getMessage("AckSensitivity"),
                Bundle.getMessage("Reserved")};
            csFlags[1] = new CmdStaFlags(1, Bundle.getMessage("OperationsFlags"), opFlagStrings, _nvArray[3]);
            JPanel opFlags = csFlags[1].getContents();

            String debugFlagStrings [] = new String[] {Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved"),
                Bundle.getMessage("Reserved")};
            csFlags[2] = new CmdStaFlags(1, Bundle.getMessage("DebugFlags"), debugFlagStrings, _nvArray[4]);
            JPanel debugFlags = csFlags[2].getContents();
            
            c.weightx = 1;
            c.weighty = 1;
//            c.gridheight = 4;
            c.gridx = 0;
            c.gridy = 0;
            
            // x = 1
            cmdStaNoSpinner = common.new TitledSpinner(Bundle.getMessage("CmdStaNo"), 0, cmdStaNoUpdateFn);
            cmdStaNoSpinner.setToolTip(Bundle.getMessage("CmdStaNoTt"));
            cmdStaNoSpinner.init(0, 0, 255, 1);
            gridPane.add(cmdStaNoSpinner, c);
            c.gridy++;
            gridPane.add(userFlags, c);
            c.gridx++;

            // x = 2
            c.gridy = 0;
            canIdSpinner = common.new TitledSpinner(Bundle.getMessage("CanId"), 0, canIdUpdateFn);
            canIdSpinner.setToolTip(Bundle.getMessage("CmdStaCanIdTt"));
            canIdSpinner.init(114, 100, 127, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;
            gridPane.add(opFlags, c);
            c.gridx++;

            // x = 2
            c.gridy = 0;
            nodeNumberSpinner = common.new TitledSpinner(Bundle.getMessage("NodeNumber"), 0, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("CmdStaNnTt"));
            nodeNumberSpinner.init(65534, 65520, 65534, 1);
            gridPane.add(nodeNumberSpinner, c);
            c.gridy++;
            gridPane.add(debugFlags, c);
            c.gridx++;
                    
            add(gridPane);
        }
    }
    
    public class PowerPane extends JPanel {
        public PowerPane() {
            super();
        }
        
    }
    
    public class DccPane extends JPanel {
        public DccPane() {
            super();
        }
    }
    
    public class CbusPane extends JPanel {
        public CbusPane() {
            super();
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
