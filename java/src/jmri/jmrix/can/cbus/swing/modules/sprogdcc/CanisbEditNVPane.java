package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import jmri.jmrix.can.cbus.swing.modules.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Node Variable edit frame for a SPROG DCC CANSERVOIO module
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CanisbEditNVPane extends AbstractEditNVPane {
    
    private final UpdateNV canIdUpdateFn = new UpdateCanId();
    private final UpdateNV nodeNumberUpdateFn = new UpdateNodeNumber();

    private JRadioButton setup;
    private TitledSpinner canIdSpinner;
    private TitledSpinner nodeNumberSpinner;
    private JRadioButton disable;
    private JTextArea canRxErrCount;
    private JButton clearRxErrCount;
    private JTextArea canTxErrCount;
    private JButton clearTxErrCount;
    
    protected CanisbEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }
    
    /** {@inheritDoc} */
    @Override
    public AbstractEditNVPane getContent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        
        JPanel cbusPane = new CbusPane();
        JPanel diagnosticsPane = new DiagnosticsPane();
        tabbedPane.addTab("CBUS", cbusPane);
        tabbedPane.addTab("CBUS Diagnostics", diagnosticsPane);
        
        JScrollPane scroll = new JScrollPane(tabbedPane);
        add(scroll);
        
        return this;
    }
    
    /** {@inheritDoc} */
    @Override
    public void tableChanged(TableModelEvent e) {
//        log.debug("canisb table changed");
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int nv = row + 1;
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

            canIdSpinner = new TitledSpinner(Bundle.getMessage("CanId"), CanisbPaneProvider.CANID, canIdUpdateFn);
            canIdSpinner.setToolTip(Bundle.getMessage("CanIdTt"));
            canIdSpinner.init(getSelectValue(CanisbPaneProvider.CANID, 100), 100, 127, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), CanisbPaneProvider.NN_HI, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("NodeNumberTt"));
            int nn = getSelectValue(CanisbPaneProvider.NN_HI, CanisbPaneProvider.NN_LO, 65520);
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
    
    /**
     * Panel to display CBUS operation related NVs
     */
    public class DiagnosticsPane extends JPanel {
        public DiagnosticsPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            String [] errCountTitle = {Bundle.getMessage("CanRxErrorCount"),
                Bundle.getMessage("CanTxErrorCount"),
                Bundle.getMessage("CanTxFailureCount"),
                Bundle.getMessage("CanRxOverflowCount")
            };
                    
            JTextArea [] errText = new JTextArea[4];
            JTextArea [] errCount = new JTextArea[4];
            JButton [] clearErr = new JButton[4];
            
            for(int i=0; i<4; i++) {
                errText[i] = new JTextArea();
                errText[i].setText(errCountTitle[i]);
                gridPane.add(errText[i], c);
                c.gridx++;
                
                errCount[i] = new JTextArea();
                errCount[i].setText("0");
                gridPane.add(errCount[i], c);
                c.gridx++;
                
                clearErr[i] = new JButton(Bundle.getMessage("Clear"));
                gridPane.add(clearErr[i], c);
                c.gridx = 0;
                c.gridy++;
            }
            
            add(gridPane);
        }
    }
    
    private final static Logger log = LoggerFactory.getLogger(CanisbEditNVPane.class);

}
