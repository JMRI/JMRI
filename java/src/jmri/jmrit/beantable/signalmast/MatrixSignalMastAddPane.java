package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring MatrixSignalMast objects
 * <P>
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @since 4.11.2
 */
public class MatrixSignalMastAddPane extends SignalMastAddPane {

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("MatrixCtlMast");
    }

    /**
     * Set the maximum number of outputs for Matrix Signal Masts Used in
     * combobox and for loops
     */
    public static final int MAXMATRIXBITS = 6; // Don't set above 6
    // 6 Seems the maximum to be able to show in a panel a coded and code below should be extended where marked

    /**
     * The number of columns in logic matrix
     */
    int bitNum;
    // ToDo: add boxes to set DCC Packets (with drop down selection "Output Type": Turnouts/Direct DCC Packets)

    char[] unLitPanelBits;
    JTextField unLitBitsField = new JTextField(MAXMATRIXBITS); // for debug


    public JPanel getLitPanel() {
        JPanel matrixUnLitPanel = new JPanel();
        JCheckBox unlitCheck1 = new JCheckBox();
        JCheckBox unlitCheck2 = new JCheckBox();
        JCheckBox unlitCheck3 = new JCheckBox();
        JCheckBox unlitCheck4 = new JCheckBox();
        JCheckBox unlitCheck5 = new JCheckBox();
        JCheckBox unlitCheck6 = new JCheckBox();
        // repeat in order to set MAXMATRIXBITS > 6
        
        if (bitNum < 1 || bitNum > MAXMATRIXBITS) {
            bitNum = 4; // default to 4 col for (first) new mast
        }
        /*        if (unLitPanelBits == null) {
            char[] unLitPanelBits = emptyBits;
            // if needed, assign panel var to enable setting separate items by clicking a UnLitCheck check box
        }*/
        JPanel matrixUnLitDetails = new JPanel();
        matrixUnLitDetails.setLayout(new jmri.util.javaworld.GridLayout2(1, 1)); // stretch to full width
        //matrixUnLitDetails.setAlignmentX(matrixUnLitDetails.RIGHT_ALIGNMENT);
        matrixUnLitDetails.add(unlitCheck1);
        matrixUnLitDetails.add(unlitCheck2);
        matrixUnLitDetails.add(unlitCheck3);
        matrixUnLitDetails.add(unlitCheck4);
        matrixUnLitDetails.add(unlitCheck5);
        matrixUnLitDetails.add(unlitCheck6);
        // repeat in order to set MAXMATRIXBITS > 6

        //matrixUnLitDetails.add(unLitBitsField);
        //unLitBitsField.setEnabled(false); // not editable, just for debugging
        //unLitBitsField.setVisible(false); // set to true to check/debug unLitBits
        matrixUnLitPanel.add(matrixUnLitDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("MatrixUnLitDetails"));
        matrixUnLitPanel.setBorder(border);
        matrixUnLitPanel.setToolTipText(Bundle.getMessage("MatrixUnlitTooltip"));

        unlitCheck1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(1, unlitCheck1.isSelected());
            }
        });
        unlitCheck2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(2, unlitCheck2.isSelected());
            }
        });
        unlitCheck3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(3, unlitCheck3.isSelected());
            }
        });
        unlitCheck4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(4, unlitCheck4.isSelected());
            }
        });
        unlitCheck5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(5, unlitCheck5.isSelected());
            }
        });
        unlitCheck6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(6, unlitCheck6.isSelected());
            }
        });
        // repeat in order to set MAXMATRIXBITS > 6
        
        return matrixUnLitPanel;
    }

    /**
     * Update the on/off positions for the unLitPanelBits char[].
     * <p>
     * Invoked from bit checkboxes 1 to MAXMATRIXBITS on unLitPanel.
     *
     * @param column int as index for an output (between 1 and 6)
     * @param state  boolean for the output On (Closed) or Off (Thrown)
     */
    public void setUnLitBit(int column, boolean state) {
        if (state == true) {
            unLitPanelBits[column - 1] = '1';
        }
        if (state == false) {
            unLitPanelBits[column - 1] = '0';
        }
        String value = String.valueOf(unLitPanelBits); // convert back from char[] to String
        unLitBitsField.setText(value);
    }

    /**
     * JPanel to define properties of an Aspect for a Matrix Signal Mast.
     * <p>
     * Invoked from the AddSignalMastPanel class when a Matrix Signal Mast is
     * selected.
     *
     * @author Egbert Broerse
     */
    class MatrixAspectPanel {

        JCheckBox disabledCheck = new JCheckBox(Bundle.getMessage("DisableAspect"));
        JCheckBox bitCheck1 = new JCheckBox();
        JCheckBox bitCheck2 = new JCheckBox();
        JCheckBox bitCheck3 = new JCheckBox();
        JCheckBox bitCheck4 = new JCheckBox();
        JCheckBox bitCheck5 = new JCheckBox();
        JCheckBox bitCheck6 = new JCheckBox();
        // repeat in order to set MAXMATRIXBITS > 6
        JTextField aspectBitsField = new JTextField(MAXMATRIXBITS); // for debug
        String aspect = "";
        String emptyChars = "000000"; // size of String = MAXMATRIXBITS; add 0 in order to set > 6
        char[] emptyBits = emptyChars.toCharArray();
        char[] aspectBits = emptyBits;

        /**
         * Build new aspect matrix panel called when Add Signal Mast Pane is
         * built
         *
         * @param aspect String like "Clear"
         */
        MatrixAspectPanel(String aspect) {
            this.aspect = aspect;
        }

        /**
         * Rebuild an aspect matrix panel using char[] previously entered called
         * from line 1870 when number of columns is changed (new mast creeation
         * only)
         *
         * @param aspect    String like "Clear"
         * @param panelBits char[] of up to 5 1's and 0's
         */
        MatrixAspectPanel(String aspect, char[] panelBits) {
            if (panelBits == null || panelBits.length == 0) {
                return;
            }
            this.aspect = aspect;
            // aspectBits is char[] of length(bitNum) describing state of on/off checkboxes
            // i.e. "0101" provided as char[] array
            // easy to manipulate by index
            // copy to checkbox states:
            aspectBits = panelBits;
            setAspectBoxes(aspectBits);
        }

        void updateAspectBits(char[] newBits) {
            aspectBits = newBits;
        }

        boolean isAspectDisabled() {
            return disabledCheck.isSelected();
        }

        /**
         * Set an Aspect Panels elements inactive.
         * <p>
         * Invoked from Disabled (aspect) checkbox and from Edit mast pane.
         *
         * @param boo true (On) or false (Off)
         */
        void setAspectDisabled(boolean boo) {
            disabledCheck.setSelected(boo);
            if (boo) { // disable all (output bit) checkboxes on this aspect panel
                // aspectBitsField always Disabled
                bitCheck1.setEnabled(false);
                if (bitNum > 1) {
                    bitCheck2.setEnabled(false);
                }
                if (bitNum > 2) {
                    bitCheck3.setEnabled(false);
                }
                if (bitNum > 3) {
                    bitCheck4.setEnabled(false);
                }
                if (bitNum > 4) {
                    bitCheck5.setEnabled(false);
                }
                if (bitNum > 5) {
                    bitCheck6.setEnabled(false);
                }
                // repeat in order to set MAXMATRIXBITS > 6
            } else { // enable all (output bit) checkboxes on this aspect panel
                // aspectBitsField always Disabled
                bitCheck1.setEnabled(true);
                if (bitNum > 1) {
                    bitCheck2.setEnabled(true);
                }
                if (bitNum > 2) {
                    bitCheck3.setEnabled(true);
                }
                if (bitNum > 3) {
                    bitCheck4.setEnabled(true);
                }
                if (bitNum > 4) {
                    bitCheck5.setEnabled(true);
                }
                if (bitNum > 5) {
                    bitCheck6.setEnabled(true);
                }
                // repeat in order to set MAXMATRIXBITS > 6
            }
        }

        /**
         * Update the on/off positions for an Aspect in the aspectBits char[].
         * <p>
         * Invoked from bit checkboxes 1 to MAXMATRIXBITS on aspectPanels.
         *
         * @param column int of the output (between 1 and MAXMATRIXBITS)
         * @param state  boolean for the output On (Closed) or Off (Thrown)
         * @see #aspectBits
         */
        public void setBit(int column, boolean state) {
            if (state == true) {
                aspectBits[column - 1] = '1';
            }
            if (state == false) {
                aspectBits[column - 1] = '0';
            }
            String value = String.valueOf(aspectBits); // convert back from char[] to String
            aspectBitsField.setText(value);
        }

        /**
         * Send the on/off positions for an Aspect to mast.
         *
         * @return A char[] of '1' and '0' elements with a length between 1 and
         *         5 corresponding with the number of outputs for this mast
         * @see jmri.implementation.MatrixSignalMast
         */
        char[] trimAspectBits() {
            try {
                //return aspectBits;
                return Arrays.copyOf(aspectBits, bitNum); // copy to new char[] of length bitNum
            } catch (Exception ex) {
                log.error("failed to read and copy aspectBits");
                return null;
            }
        }

        /**
         * Activate the corresponding checkboxes on a MatrixApectPanel.
         *
         * @param aspectBits A char[] of '1' and '0' elements with a length
         *                   between 1 and 5 corresponding with the number of
         *                   outputs for this mast
         */
        private void setAspectBoxes(char[] aspectBits) {
            bitCheck1.setSelected(aspectBits[0] == '1');
            if (bitNum > 1) {
                bitCheck2.setSelected(aspectBits[1] == '1');
            }
            if (bitNum > 2) {
                bitCheck3.setSelected(aspectBits[2] == '1');
            }
            if (bitNum > 3) {
                bitCheck4.setSelected(aspectBits[3] == '1');
            }
            if (bitNum > 4) {
                bitCheck5.setSelected(aspectBits[4] == '1');
            }
            if (bitNum > 5) {
                bitCheck6.setSelected(aspectBits[5] == '1');
            }
            // repeat in order to set MAXMATRIXBITS > 6
            String value = String.valueOf(aspectBits); // convert back from char[] to String
            aspectBitsField.setText(value);
        }

        JPanel panel;

        /**
         * Build a JPanel for an Aspect Matrix row.
         *
         * @return JPanel to be displayed on the Add/Edit Signal Mast panel
         */
        JPanel getPanel() {
            if (panel == null) {
                panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JPanel matrixDetails = new JPanel();
                matrixDetails.add(disabledCheck);
                matrixDetails.add(bitCheck1);
                matrixDetails.add(bitCheck2);
                matrixDetails.add(bitCheck3);
                matrixDetails.add(bitCheck4);
                matrixDetails.add(bitCheck5);
                matrixDetails.add(bitCheck6);
                // repeat in order to set MAXMATRIXBITS > 6
                // ToDo refresh aspectSetting, can be in OKPressed() to store/warn for duplicates (with button 'Ignore')
                // hide if id > bitNum (var in panel)
                bitCheck2.setVisible(bitNum > 1);
                bitCheck3.setVisible(bitNum > 2);
                bitCheck4.setVisible(bitNum > 3);
                bitCheck5.setVisible(bitNum > 4);
                bitCheck6.setVisible(bitNum > 5);
                // repeat in order to set MAXMATRIXBITS > 6
                matrixDetails.add(aspectBitsField);
                aspectBitsField.setEnabled(false); // not editable, just for debugging
                aspectBitsField.setVisible(false); // set to true to check/debug

                panel.add(matrixDetails);
                TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
                border.setTitle(aspect);
                panel.setBorder(border);

                disabledCheck.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setAspectDisabled(disabledCheck.isSelected());
                    }
                });

                bitCheck1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(1, bitCheck1.isSelected());
                    }
                });
                bitCheck2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(2, bitCheck2.isSelected());
                    }
                });
                bitCheck3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(3, bitCheck3.isSelected());
                    }
                });
                bitCheck4.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(4, bitCheck4.isSelected());
                    }
                });
                bitCheck5.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(5, bitCheck5.isSelected());
                    }
                });
                bitCheck6.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(6, bitCheck6.isSelected());
                    }
                });
                // repeat in order to set MAXMATRIXBITS > 6
            }
            return panel;
        }

    }

    @ServiceProvider(service = SignalMastAddPane.SignalMastAddPaneProvider.class)
    static public class SignalMastAddPaneProvider extends SignalMastAddPane.SignalMastAddPaneProvider {
        /** {@inheritDoc} */
        @Override
        @Nonnull public String getPaneName() {
            return Bundle.getMessage("MatrixCtlMast");
        }
        /** {@inheritDoc} */
        @Override
        @Nonnull public SignalMastAddPane getNewPane() {
            return new MatrixSignalMastAddPane();
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MatrixSignalMastAddPane.class);
}
