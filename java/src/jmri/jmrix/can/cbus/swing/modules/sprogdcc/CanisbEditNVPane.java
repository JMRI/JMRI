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

    private JRadioButton setup;
    private TitledSpinner canIdSpinner;
    private TitledSpinner nodeNumberSpinner;
    private JRadioButton disable;

    // Read/Clear error status
    String[] rcTitle = {Bundle.getMessage("CanErrStatus"),
        Bundle.getMessage("CanTxFailureCount"),
        Bundle.getMessage("CanRxOverflowCount"),
        Bundle.getMessage("CanNomBitRateRxCount"),
        Bundle.getMessage("CanNomBitRateTxCount"),
        Bundle.getMessage("CanDiagnosticsLo"),
        Bundle.getMessage("CanDiagnosticsHi"),
        Bundle.getMessage("CanErrFreeCount")
    };
    int clearableErrors = rcTitle.length;
    JLabel[] rcText = new JLabel[clearableErrors];
    JTextField[] rcCount = new JTextField[clearableErrors];
    JTextField[] rcOldCount = new JTextField[clearableErrors];
    JButton[] rcButton = new JButton[clearableErrors];
    // Read only error status
    String[] rTitle = {Bundle.getMessage("CanRxErrorCount"),
        Bundle.getMessage("CanTxErrorCount")
    };
    int rErrors = rTitle.length;
    JLabel[] rText = new JLabel[rErrors];
    JTextField[] rCount = new JTextField[rErrors];
    JTextField[] rOldCount = new JTextField[rErrors];
    JButton[] rButton = new JButton[rErrors];

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
        tabbedPane.addTab("CBUS", cbusPane);
        tabbedPane.addTab("CBUS Diagnostics", diagnosticsPane);

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
            int value = getSelectValue(nv);
            log.debug("canisb gui table changed NV: {} Value: {}", nv, value);
            if ((nv==15) && (value==4)) {
                log.debug("blah");
            }
            if (value >= -1) {
                switch (nv) {
                    case CanisbPaneProvider.SETUP:
                        setup.setSelected(value != 0);
                        break;

                    case CanisbPaneProvider.CANID:
                        canIdSpinner.setValue(getSelectValue(CanisbPaneProvider.CANID,
                                CanisbPaneProvider.MIN_CANID, CanisbPaneProvider.MAX_CANID));
                        break;

                    case CanisbPaneProvider.NN_HI:
                    case CanisbPaneProvider.NN_LO:
                        nodeNumberSpinner.setValue(getSelectValue(CanisbPaneProvider.NN_HI,
                                CanisbPaneProvider.NN_LO, CanisbPaneProvider.MIN_NN, CanisbPaneProvider.MAX_NN));
                        break;

                    case CanisbPaneProvider.RX_ERR_CNT:
                        rCount[0].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.TX_ERR_CNT:
                        rCount[1].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_ERR_STATUS:
                        rcCount[0].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.TX_FAIL_CNT:
                        rcCount[1].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.RX_OVFLW_COUNT:
                        rcCount[2].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_NOM_BIT_RATE_RX_COUNT:
                        rcCount[3].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_NOM_BIT_RATE_TX_COUNT:
                        rcCount[4].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_DIAGNOSTICS_LO:
                        rcCount[5].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_DIAGNOSTICS_HI:
                        rcCount[6].setText(Integer.toString(value));
                        break;

                    case CanisbPaneProvider.CAN_ERR_FREE_COUNT_LO:
//                        log.debug("canisb gui table changed NV: {} Value: {}", nv, value);
                        // Do not update display until Hi byte is handled
                        break;

                    case CanisbPaneProvider.CAN_ERR_FREE_COUNT_HI:
//                        log.debug("canisb gui table changed NV: {} Value: {}", nv, value);
                        rcCount[7].setText(Integer.toString(value * 256
                                + (int) _dataModel.getValueAt(CanisbPaneProvider.CAN_ERR_FREE_COUNT_LO - 1, NV_SELECT_COLUMN)));
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

    public void rcButtonActionPerformed(int button) {
        // TODO: for now just set text to 0
        rcCount[button].setText("0");
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
            canIdSpinner.init(getSelectValue(CanisbPaneProvider.CANID, 100, 127), CanisbPaneProvider.MIN_CANID, CanisbPaneProvider.MAX_CANID, 1);
            gridPane.add(canIdSpinner, c);
            c.gridy++;

            nodeNumberSpinner = new TitledSpinner(Bundle.getMessage("NodeNumber"), CanisbPaneProvider.NN_HI, nodeNumberUpdateFn);
            nodeNumberSpinner.setToolTip(Bundle.getMessage("NodeNumberTt"));
            int nn = getSelectValue(CanisbPaneProvider.NN_HI, CanisbPaneProvider.NN_LO, CanisbPaneProvider.MIN_NN, CanisbPaneProvider.MAX_NN);
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

            JLabel statusCol = new JLabel("Status");
            statusCol.setHorizontalAlignment(SwingConstants.RIGHT);
            gridPane.add(statusCol, c);
            c.gridx++;

            JLabel newCol = new JLabel("New");
            newCol.setHorizontalAlignment(SwingConstants.RIGHT);
            gridPane.add(newCol, c);
            c.gridx++;

            JLabel oldCol = new JLabel("Old");
            oldCol.setHorizontalAlignment(SwingConstants.RIGHT);
            gridPane.add(oldCol, c);
            c.gridx = 0;
            c.gridy++;

            for (int i = 0; i < clearableErrors; i++) {
                rcText[i] = new JLabel();
                rcText[i].setText(rcTitle[i]);
                gridPane.add(rcText[i], c);
                c.gridx++;

                rcCount[i] = new JTextField("0", 5);
                rcCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                gridPane.add(rcCount[i], c);
                c.gridx++;

                rcOldCount[i] = new JTextField("0", 5);
                rcOldCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                gridPane.add(rcOldCount[i], c);
                c.gridx++;

                rcButton[i] = new JButton(Bundle.getMessage("Clear"));
                final int button = i;
                rcButton[i].addActionListener((java.awt.event.ActionEvent e) -> {
                    rcButtonActionPerformed(button);
                });

                gridPane.add(rcButton[i], c);
                c.gridx = 0;
                c.gridy++;
            }

            for (int i = 0; i < rErrors; i++) {
                rText[i] = new JLabel();
                rText[i].setText(rTitle[i]);
                gridPane.add(rText[i], c);
                c.gridx++;

                rCount[i] = new JTextField("0", 5);
                rCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                gridPane.add(rCount[i], c);
                c.gridx++;

                rOldCount[i] = new JTextField("0", 5);
                rOldCount[i].setHorizontalAlignment(SwingConstants.RIGHT);
                gridPane.add(rOldCount[i], c);
                c.gridx = 0;
                c.gridy++;
            }

            add(gridPane);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CanisbEditNVPane.class);

}
