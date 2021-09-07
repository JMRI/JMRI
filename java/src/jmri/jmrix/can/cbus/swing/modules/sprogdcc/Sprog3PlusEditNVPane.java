package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.AbstractEditNVPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a MERG CANACC8 CBUS module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class Sprog3PlusEditNVPane extends AbstractEditNVPane {
    
    private CmdStaFlags [] flags = new CmdStaFlags[3];
        
    protected Sprog3PlusEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public JPanel getContent() {
        
        JPanel newPane = new JPanel(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel cmdStaPane = new CmdStaPane().getContent();
        JPanel powerPane = new PowerPane();
        JPanel dccPane = new DccPane();
        JPanel cbusPane = new CbusPane();
        tabbedPane.addTab("Command Station", cmdStaPane);
        tabbedPane.addTab("Power", powerPane);
        tabbedPane.addTab("DCC", dccPane);
        tabbedPane.addTab("CBUS", cbusPane);
        
        JScrollPane scroll = new JScrollPane(tabbedPane);
        
        newPane.add(scroll, BorderLayout.CENTER);
        newPane.validate();
        newPane.repaint();
        
        return newPane;
    }
    
    public class CmdStaPane extends JPanel {
        
        CmdStaFlags [] flags = new CmdStaFlags[3];
        
        public CmdStaPane() {
            super();
        }
        
        protected JPanel getContent() {
            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            String userFlagStrings [] = new String[] {"Reserved",
                "Permit steal",
                "Permit share",
                "Reserved",
                "map events",
                "Stop on timeout",
                "Start of day",
                "Auto power"};
            flags[0] = new CmdStaFlags(0, "User Flags", userFlagStrings, _nvArray[2]);
            JPanel userFlags = flags[0].getContents();

            String opFlagStrings [] = new String[] {"Reserved",
                "Reserved",
                "Reserved",
                "ZTC mode",
                "All stop track off",
                "Blueline mode",
                "ACK sensitivity",
                "Reserved"};
            flags[1] = new CmdStaFlags(1, "Operations Flags", opFlagStrings, _nvArray[3]);
            JPanel opFlags = flags[1].getContents();

            String debugFlagStrings [] = new String[] {"Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved",
                "Reserved"};
            flags[2] = new CmdStaFlags(1, "Debug Flags", debugFlagStrings, _nvArray[4]);
            JPanel debugFlags = flags[2].getContents();
            
            gridPane.add(userFlags, c);
            c.gridx++;
            gridPane.add(opFlags, c);
            c.gridx++;
            gridPane.add(debugFlags, c);
            
            return gridPane;
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
                flags[nv - 2].setButtons(value);
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
     * Class to display CBUS command station flag settings
     */
    private class CmdStaFlags extends JPanel {
        
        protected int _type;
        protected String _title;
        protected String [] _fn;
        protected int _flags;
        protected JRadioButton [] buttons;

        public CmdStaFlags(int type, String title, String [] fn, int flags) {
            super();
            
            _type = type;
            _title = title;
            _flags = flags;
            _fn = fn.clone();
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
                if ((_flags & (1<<i)) > 0) {
                    buttons[i].setSelected(true);
                } else {
                    buttons[i].setSelected(true);
                }
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
                    buttons[i].setSelected(true);
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
