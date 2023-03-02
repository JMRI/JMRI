package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.*;
import javax.swing.event.TableModelEvent;

import jmri.jmrix.can.cbus.node.CbusNode;
import jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel;
import static jmri.jmrix.can.cbus.node.CbusNodeNVTableDataModel.NV_SELECT_COLUMN;
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
    private final UpdateNV arbDelayUpdateFn = new UpdateArbDelay();

    private JRadioButton setup;
    private TitledSpinner canIdSpinner;
    private TitledSpinner nodeNumberSpinner;
    private JRadioButton disable;
    private TitledSpinner arbDelaySpinner;
    
    // Read/Clear error status
    String[] rcTitle = {Bundle.getMessage("CanErrStatus")+" 1",
        Bundle.getMessage("CanErrStatus")+" 2",
        Bundle.getMessage("CanTxFailureCount"),
        Bundle.getMessage("CanRxOverflowCount"),
        Bundle.getMessage("CanNomBitRateRxCount"),
        Bundle.getMessage("CanNomBitRateTxCount"),
        Bundle.getMessage("CanDiagnostics")+" Hi",
        Bundle.getMessage("CanDiagnostics")+" Lo",
        Bundle.getMessage("CanErrFreeCount")
    };
    String[] rcTt = {Bundle.getMessage("CanErrStatus1Tt"),
        Bundle.getMessage("CanErrStatus2Tt"),
        Bundle.getMessage("CanTxFailureCountTt"),
        Bundle.getMessage("CanRxOverflowCountTt"),
        Bundle.getMessage("CanNomBitRateRxCountTt"),
        Bundle.getMessage("CanNomBitRateTxCountTt"),
        Bundle.getMessage("CanDiagnosticsHiTt"),
        Bundle.getMessage("CanDiagnosticsLoTt"),
        Bundle.getMessage("CanErrFreeCountTt")
    };
    int clearableErrors = rcTitle.length;
    JLabel[] rcText = new JLabel[clearableErrors];
    JTextField[] rcCount = new JTextField[clearableErrors];
    JButton[] rcButton = new JButton[clearableErrors];
    JButton rcAllButton = new JButton();
    JButton[] rcUpButton = new JButton[clearableErrors];
    JButton rcAllUpButton = new JButton();
    // Translate clear button index to NV index
    int[] rcNvOffset = {CanisbPaneProvider.CAN_ERR_STATUS_1,
        CanisbPaneProvider.CAN_ERR_STATUS_2,
        CanisbPaneProvider.TX_FAIL_CNT,
        CanisbPaneProvider.RX_OVFLW_COUNT,
        CanisbPaneProvider.CAN_NOM_BIT_RATE_RX_COUNT,
        CanisbPaneProvider.CAN_NOM_BIT_RATE_TX_COUNT,
        CanisbPaneProvider.CAN_DIAGNOSTICS_HI,
        CanisbPaneProvider.CAN_DIAGNOSTICS_LO,
        CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI
    };
    
    // Read only error status
    String[] rTitle = {Bundle.getMessage("CanRxErrorCount"),
        Bundle.getMessage("CanTxErrorCount")
    };
    String[] rTt = {Bundle.getMessage("CanRxErrorCountTt"),
        Bundle.getMessage("CanTxErrorCountTt")
    };
    int rErrors = rTitle.length;
    JLabel[] rText = new JLabel[rErrors];
    JTextField[] rCount = new JTextField[rErrors];
    JButton[] rUpButton = new JButton[rErrors];

    String[] commsTitle = {Bundle.getMessage("HostTxCnt"),
        Bundle.getMessage("HostRxCnt"),
        Bundle.getMessage("CanTxCnt"),
        Bundle.getMessage("CanRxCnt")
    };
    String[] commsToolTips = {Bundle.getMessage("HostTxCntTt"),
        Bundle.getMessage("HostRxCntTt"),
        Bundle.getMessage("CanTxCntTt"),
        Bundle.getMessage("CanRxCntTt")
    };
    int commsItems = commsTitle.length;
    JLabel[] commsText = new JLabel[commsItems];
    JTextField[] commsCount = new JTextField[commsItems];
    JButton[] commsButton = new JButton[commsItems];
    JButton commsAllButton = new JButton();
    JButton[] commsUpButton = new JButton[commsItems];
    JButton commsAllUpButton = new JButton();
    // Translate comms button index to NV index
    int[] commsNvOffset = {CanisbPaneProvider.HOST_TX_CNT_T,
        CanisbPaneProvider.HOST_RX_CNT_T,
        CanisbPaneProvider.CAN_TX_CNT_T,
        CanisbPaneProvider.CAN_RX_CNT_T
    };
    
    protected CanisbEditNVPane(CbusNodeNVTableDataModel dataModel, CbusNode node) {
        super(dataModel, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractEditNVPane getContent() {
        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel cbusPane = new CbusPane();
        JPanel diagnosticsPane = new DiagnosticsPane();
        JPanel commsPane = new CommsPane();
        tabbedPane.addTab("CBUS", cbusPane);
        tabbedPane.addTab(Bundle.getMessage("CanDiagnostics"), diagnosticsPane);
        tabbedPane.addTab(Bundle.getMessage("PacketCounts"), commsPane);

        JScrollPane scroll = new JScrollPane(tabbedPane);
        add(scroll);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tableChanged(TableModelEvent e) {
//        log.debug("canisb table changed");
        if (e.getType() == TableModelEvent.UPDATE) {
            int row = e.getFirstRow();
            int nv = row + 1;
            int value = getSelectValue8(nv);
            log.debug("canisb gui table changed NV: {} Value: {}", nv, value);
            if (value >= -1) {
                switch (nv) {
                    case CanisbPaneProvider.SETUP:
                        setup.setSelected(value != 0);
                        break;

                    case CanisbPaneProvider.CANID:
                        canIdSpinner.setValue(getSelectValue8(CanisbPaneProvider.CANID,
                                CanisbPaneProvider.MIN_CANID, CanisbPaneProvider.MAX_CANID));
                        break;

                    case CanisbPaneProvider.NN_HI:
                    case CanisbPaneProvider.NN_LO:
                        nodeNumberSpinner.setValue(getSelectValue16(CanisbPaneProvider.NN_HI,
                                CanisbPaneProvider.NN_LO, CanisbPaneProvider.MIN_NN, CanisbPaneProvider.MAX_NN));
                        break;

                    case CanisbPaneProvider.RX_ERR_CNT:
                        rCount[0].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.TX_ERR_CNT:
                        rCount[1].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_ERR_STATUS_1:
                        rcCount[0].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_ERR_STATUS_2:
                        rcCount[1].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.TX_FAIL_CNT:
                        rcCount[2].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.RX_OVFLW_COUNT:
                        rcCount[3].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_NOM_BIT_RATE_RX_COUNT:
                        rcCount[4].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_NOM_BIT_RATE_TX_COUNT:
                        rcCount[5].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_DIAGNOSTICS_HI:
                        rcCount[6].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_DIAGNOSTICS_LO:
                        rcCount[7].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI:
                        // Do not update display until LO byte is handled
                        break;

                    case CanisbPaneProvider.CAN_ERR_FREE_COUNT_LO:
                        rcCount[8].setText(Integer.toString(value
                                + (int)_dataModel.getValueAt(CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI - 1, NV_SELECT_COLUMN)*256));
                        break;

                    case CanisbPaneProvider.HOST_TX_CNT_T:
                    case CanisbPaneProvider.HOST_TX_CNT_U:
                    case CanisbPaneProvider.HOST_TX_CNT_H:
                    case CanisbPaneProvider.HOST_RX_CNT_T:
                    case CanisbPaneProvider.HOST_RX_CNT_U:
                    case CanisbPaneProvider.HOST_RX_CNT_H:
                    case CanisbPaneProvider.CAN_TX_CNT_T:
                    case CanisbPaneProvider.CAN_TX_CNT_U:
                    case CanisbPaneProvider.CAN_TX_CNT_H:
                    case CanisbPaneProvider.CAN_RX_CNT_T:
                    case CanisbPaneProvider.CAN_RX_CNT_U:
                    case CanisbPaneProvider.CAN_RX_CNT_H:
                        // Ignore until Lo byte is seen
                        break;
                        
                    case CanisbPaneProvider.HOST_TX_CNT_L:
                    case CanisbPaneProvider.HOST_RX_CNT_L:
                    case CanisbPaneProvider.CAN_TX_CNT_L:
                    case CanisbPaneProvider.CAN_RX_CNT_L:
                        // Get index of top byte NV
                        int top = nv - CanisbPaneProvider.HOST_TX_CNT_T;
                        top -= top%4;           // 0, 4, 8,...
                        int topNv = top + CanisbPaneProvider.HOST_TX_CNT_T;
                        commsCount[top/4].setText(Integer.toString(getSelectValue32(topNv)));
                        break;
                        
                    case CanisbPaneProvider.CAN_TX_ARB_DELAY:
                        arbDelaySpinner.setValue(getSelectValue8(CanisbPaneProvider.CAN_TX_ARB_DELAY,
                                0, 15));
                        break;
                        
                    default:
                    // Not used, or row was -1
                    //                    log.debug("Update unknown NV {}", nv);
                }
            }
        }
    }

    /**
     * Update the CAN ID
     * <p>
     * For debug only, CAN ID is not normally set this way
     */
    protected class UpdateCanId implements UpdateNV {

        /**
         * {@inheritDoc}
         */
        @Override
        public void setNewVal(int index) {
            int canId = canIdSpinner.getIntegerValue();
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(canId, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }

    /**
     * Update the node number
     * <p>
     * For debug only, CAN ID is not normally set this way
     */
    protected class UpdateNodeNumber implements UpdateNV {

        /**
         * {@inheritDoc}
         */
        @Override
        public void setNewVal(int index) {
            int nn = nodeNumberSpinner.getIntegerValue();
            int nnHi = nn / 256;
            int nnLo = nn % 256;
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(nnHi, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
            _dataModel.setValueAt(nnLo, index, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }

    /**
     * Update the number of times a DCC accessory packet is repeated
     */
    protected class UpdateArbDelay implements UpdateNV {
        
        /** {@inheritDoc} */
        @Override
        public void setNewVal(int index) {
            int cnt = arbDelaySpinner.getIntegerValue();
            // Note that changing the data model will result in tableChanged() being called, which can manipulate the buttons, etc
            _dataModel.setValueAt(cnt, index - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }

    /**
     * Clearable errors clear button action
     * 
     * Translate the button index to the NV index and clear the NV.
     * 
     * Multi byte NV clearing is handled by the hardware node, but we have to
     * clear them all to ensure the table update mechanism works.
     * 
     * @param button the button index
     */
    public void rcButtonActionPerformed(int button) {
        _dataModel.setValueAt(0, rcNvOffset[button] - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        if (rcNvOffset[button] == CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI) {
            _dataModel.setValueAt(0, rcNvOffset[button], CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        }
    }

    /**
     * Clear all clearable errors
     */
    public void allButtonActionPerformed() {
        for (int i = 0; i < clearableErrors; i++) {
            rcButtonActionPerformed(i);
        }
    }
    
    /**
     * Clearable errors update button action
     * 
     * Translate the button index to the NV index and clear the NV.
     * 
     * @param button the button index
     */
    public void rcUpButtonActionPerformed(int button) {
        _node.send.nVRD(_node.getNodeNumber(), rcNvOffset[button]);
        if (rcNvOffset[button] == CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI) {
            _node.send.nVRD(_node.getNodeNumber()+1, rcNvOffset[button]);
        }
    }

    /**
     * Read only errors update button action
     * 
     * Translate the button index to the NV index and clear the NV.
     * 
     * @param button the button index
     */
    public void rUpButtonActionPerformed(int button) {
        _node.send.nVRD(_node.getNodeNumber(), rcNvOffset[button]);
    }

    /**
     * Update all clearable and read only errors
     */
    public void rcAllUpButtonActionPerformed() {
        for (int i = 0; i < clearableErrors; i++) {
            rcUpButtonActionPerformed(i);
        }
        for (int i = 0; i < rErrors; i++) {
            rUpButtonActionPerformed(i);
        }
    }
    
    /**
     * Communications clear button action
     * 
     * Translate the button index to the NV index and clear the NV
     * 
     * Multi byte NV clearing is handled by the hardware node, but we have to
     * clear them all to ensure the table update mechanism works.
     * 
     * @param button the button index
     */
    public void commsButtonActionPerformed(int button) {
        _dataModel.setValueAt(0, commsNvOffset[button] - 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        _dataModel.setValueAt(0, commsNvOffset[button], CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        _dataModel.setValueAt(0, commsNvOffset[button] + 1, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
        _dataModel.setValueAt(0, commsNvOffset[button] + 2, CbusNodeNVTableDataModel.NV_SELECT_COLUMN);
    }

    /**
     * Communications update button action
     */
    public void commsAllButtonActionPerformed() {
        for (int i = 0; i < commsItems; i++) {
            commsButtonActionPerformed(i);
        }
    }
    
    /**
     * Communications button update action
     * 
     * Translate the button index to the NV index and update the NV
     * 
     * THese are all 4-byte NVs
     * 
     * @param button the button index
     */
    public void commsUpButtonActionPerformed(int button) {
       _node.send.nVRD(_node.getNodeNumber(), commsNvOffset[button]);
       _node.send.nVRD(_node.getNodeNumber()+1, commsNvOffset[button]);
       _node.send.nVRD(_node.getNodeNumber()+2, commsNvOffset[button]);
       _node.send.nVRD(_node.getNodeNumber()+3, commsNvOffset[button]);
    }

    /**
     * Update all comms status
     */
    public void commsAllUpButtonActionPerformed() {
        for (int i = 0; i < commsItems; i++) {
            commsUpButtonActionPerformed(i);
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
            canIdSpinner.init(getSelectValue8(CanisbPaneProvider.CANID, 100, 127), CanisbPaneProvider.MIN_CANID, CanisbPaneProvider.MAX_CANID, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), CanisbPaneProvider.NN_HI, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("NodeNumberTt"));
            int nn = getSelectValue16(CanisbPaneProvider.NN_HI, CanisbPaneProvider.NN_LO, CanisbPaneProvider.MIN_NN, CanisbPaneProvider.MAX_NN);
            nodeNumberSpinner.init(nn, CanisbPaneProvider.MIN_NN, CanisbPaneProvider.MAX_NN, 1);
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

            arbDelaySpinner = new TitledSpinner(Bundle.getMessage("TxArbDelay"), CanisbPaneProvider.CAN_TX_ARB_DELAY, arbDelayUpdateFn);
            arbDelaySpinner.setToolTip(Bundle.getMessage("TxArbDelayTt"));
            arbDelaySpinner.init(getSelectValue8(CanisbPaneProvider.CAN_TX_ARB_DELAY, 0, 15), 0, 15, 1);
            gridPane.add(arbDelaySpinner, c);
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

            JLabel errControl = new JLabel(Bundle.getMessage("CanErrControl"));
            gridPane.add(errControl, c);
            c.gridx++;
            
            JTextField errControlBits = new JTextField("0", 5);
            errControlBits.setToolTipText(Bundle.getMessage("CanErrControlTt"));
            errControlBits.setHorizontalAlignment(SwingConstants.RIGHT);
            gridPane.add(errControlBits, c);

            c.gridx = 0;
            c.gridy++;
            c.gridy++;
            
            JLabel statusCol = new JLabel(Bundle.getMessage("CanErrStatus"));
            gridPane.add(statusCol, c);
            c.gridx++;
            c.gridx++;

            rcAllButton = new JButton(Bundle.getMessage("ClearAll"));
            rcAllButton.addActionListener((java.awt.event.ActionEvent e) -> {
                allButtonActionPerformed();
            });
            gridPane.add(rcAllButton, c);
            c.gridx++;
            
            rcAllUpButton = new JButton(Bundle.getMessage("UpdateAll"));
            rcAllUpButton.addActionListener((java.awt.event.ActionEvent e) -> {
                rcAllUpButtonActionPerformed();
            });
            gridPane.add(rcAllUpButton, c);
            c.gridx = 0;
            c.gridy++;

            for (int i = 0; i < clearableErrors; i++) {
                rcText[i] = new JLabel();
                rcText[i].setText(rcTitle[i]);
                gridPane.add(rcText[i], c);
                c.gridx++;

                rcCount[i] = new JTextField("0", 5);
                rcCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                rcCount[i].setToolTipText(rcTt[i]);
                rcCount[i].setEditable(false);
                gridPane.add(rcCount[i], c);
                c.gridx++;

                final int button = i;
                
                rcButton[i] = new JButton(Bundle.getMessage("Clear"));
                rcButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    rcButtonActionPerformed(button);
                });
                gridPane.add(rcButton[i], c);
                c.gridx++;
                
                rcUpButton[i] = new JButton(Bundle.getMessage("Update"));
                rcUpButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    rcUpButtonActionPerformed(button);
                });
                gridPane.add(rcUpButton[i], c);

                c.gridx = 0;
                c.gridy++;
            }

            JLabel roHeader = new JLabel(Bundle.getMessage("ReadOnly"));
            gridPane.add(roHeader, c);
            c.gridy++;
            
            for (int i = 0; i < rErrors; i++) {
                rText[i] = new JLabel();
                rText[i].setText(rTitle[i]);
                gridPane.add(rText[i], c);
                c.gridx++;

                rCount[i] = new JTextField("0", 5);
                rCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                rCount[i].setToolTipText(rTt[i]);
                rCount[i].setEditable(false);
                gridPane.add(rCount[i], c);
                c.gridx++;
                
                c.gridx++;
                
                final int button = i;
                
                rUpButton[i] = new JButton(Bundle.getMessage("Update"));
                rUpButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    rUpButtonActionPerformed(button);
                });
                gridPane.add(rUpButton[i], c);
                c.gridx = 0;
                c.gridy++;
            }

            add(gridPane);
        }
    }

    /**
     * Panel to display communuication stats
     */
    public class CommsPane extends JPanel {

        public CommsPane() {
            super();

            JPanel gridPane = new JPanel(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            JLabel statusCol = new JLabel("");
            gridPane.add(statusCol, c);
            c.gridx++;

            JLabel col = new JLabel("");
            gridPane.add(col, c);
            c.gridx++;
            
            commsAllButton = new JButton(Bundle.getMessage("ClearAll"));
            commsAllButton.addActionListener((java.awt.event.ActionEvent e) -> {
                commsAllButtonActionPerformed();
            });
            gridPane.add(commsAllButton, c);
            c.gridx++;
            
            commsAllUpButton = new JButton(Bundle.getMessage("UpdateAll"));
            commsAllUpButton.addActionListener((java.awt.event.ActionEvent e) -> {
                commsAllUpButtonActionPerformed();
            });
            gridPane.add(commsAllUpButton, c);
            c.gridx = 0;
            c.gridy++;

            for (int i = 0; i < commsItems; i++) {
                commsText[i] = new JLabel();
                commsText[i].setText(commsTitle[i]);
                gridPane.add(commsText[i], c);
                c.gridx++;

                commsCount[i] = new JTextField("0", 10);
                commsCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                commsCount[i].setToolTipText(commsToolTips[i]);
                commsCount[i].setEditable(false);
                gridPane.add(commsCount[i], c);
                c.gridx++;

                final int button = i;
                
                commsButton[i] = new JButton(Bundle.getMessage("Clear"));
                commsButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    commsButtonActionPerformed(button);
                });
                gridPane.add(commsButton[i], c);
                c.gridx++;

                commsUpButton[i] = new JButton(Bundle.getMessage("Update"));
                commsUpButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    commsUpButtonActionPerformed(button);
                });
                gridPane.add(commsUpButton[i], c);
                c.gridx = 0;
                c.gridy++;
            }

            add(gridPane);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CanisbEditNVPane.class);

}
