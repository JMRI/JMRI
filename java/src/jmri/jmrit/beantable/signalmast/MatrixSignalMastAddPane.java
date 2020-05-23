package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import jmri.*;
import jmri.implementation.*;
import jmri.util.*;
import jmri.util.swing.*;

import org.openide.util.lookup.ServiceProvider;

/**
 * A pane for configuring MatrixSignalMast objects.
 *
 * @see jmri.jmrit.beantable.signalmast.SignalMastAddPane
 * @author Bob Jacobsen Copyright (C) 2018
 * @author Egbert Broerse Copyright (C) 2016, 2019
 * @since 4.11.2
 */
public class MatrixSignalMastAddPane extends SignalMastAddPane {

    public MatrixSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("AllowUnLitLabel"))));
        p.add(allowUnLit);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(p);

        add(matrixUnLitPanel);
        matrixUnLitPanel();
        
        matrixMastBitnumPanel = makeMatrixMastBitnumPanel(); // create panel
        add(matrixMastBitnumPanel);
        if (prefs.getComboBoxLastSelection(matrixBitNumSelectionCombo) != null) {
            columnChoice.setSelectedItem(prefs.getComboBoxLastSelection(matrixBitNumSelectionCombo)); // setting for bitNum
        }

        matrixMastScroll = new JScrollPane(matrixMastPanel);
        matrixMastScroll.setBorder(BorderFactory.createEmptyBorder());
        add(matrixMastScroll);
    }

    private DefaultSignalAppearanceMap map;
    private MatrixSignalMast currentMast; // mast being edited, null for new mast
    private JCheckBox resetPreviousState = new JCheckBox(Bundle.getMessage("ResetPrevious"));
    private jmri.UserPreferencesManager prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    private String matrixBitNumSelectionCombo = this.getClass().getName() + ".matrixBitNumSelected";
    private JCheckBox allowUnLit = new JCheckBox();

    private JScrollPane matrixMastScroll;
    private JPanel matrixMastBitnumPanel;
    private JPanel matrixMastPanel = new JPanel();
    // private char[] bitString;
    private char[] unLitPanelBits;
    private int numberOfActiveAspects;

    private JLabel bitNumLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("MatrixBitsLabel")));
    private JComboBox<String> columnChoice = new JComboBox<>(choiceArray());
    private JSpinner timeDelay = new JSpinner();

    private LinkedHashMap<String, MatrixAspectPanel> matrixAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);
    // LinkedHM type keeps things sorted // only used once, see updateMatrixAspectPanel()

    private DecimalFormat paddedNumber = new DecimalFormat("0000");

    private String[] choiceArray() {
        String[] numberOfOutputs = new String[MAXMATRIXBITS];
        for (int i = 0; i < MAXMATRIXBITS; i++) {
            numberOfOutputs[i] = (i + 1) + "";
        }
        log.debug("Created output combo box: {}", Arrays.toString(numberOfOutputs));
        return numberOfOutputs;
    }

    /**
     * Set the maximum number of outputs for Matrix Signal Masts.
     * Used in combobox and for loops.
     */
    public static final int MAXMATRIXBITS = 10; // requires code changes to modify this value

    private String emptyChars = "0000000000"; // size of String = MAXMATRIXBITS; extend if MAXMATRIXBITS changed
    private char[] emptyBits = emptyChars.toCharArray();

    /**
     * on = thrown, off = closed, no turnout states asked
     */
    private BeanSelectCreatePanel<Turnout> turnoutBox1 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox2 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox3 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox4 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox5 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox6 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox7 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox8 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox9 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    private BeanSelectCreatePanel<Turnout> turnoutBox10= new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    // repeat in order to extend MAXMATRIXBITS 

    /**
     * The number of columns in logic matrix. 
     * This is set when the user resizes the mast
     */
    private int bitNum;
    
    // ToDo: add boxes to set DCC Packets (with drop down selection "Output Type": Turnouts/Direct DCC Packets)

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("MatrixCtlMast");
    }

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull SignalAppearanceMap newMap, @Nonnull SignalSystem sigSystem) {
        log.debug("setAspectNames(...)");

        unLitPanelBits = Arrays.copyOf(emptyBits, MAXMATRIXBITS);
        map = (DefaultSignalAppearanceMap)newMap;

        // set up rest of panel
        updateMatrixMastPanel(); // show only the correct amount of columns for existing matrixMast

        columnChoice.setSelectedIndex(bitNum - 1); // index of items in list starts counting at 0 while "1" is displayed
    }

    /** {@inheritDoc} */
    @Override
    public boolean canHandleMast(@Nonnull SignalMast mast) {
        return mast instanceof MatrixSignalMast;
    }

    /** {@inheritDoc} */
    @Override
    public void setMast(SignalMast mast) { 
        log.debug("setMast({})", mast);
        if (mast == null) { 
            currentMast = null;
            return; 
        }
        
        if (! (mast instanceof MatrixSignalMast) ) {
            log.error("mast was wrong type: {} {}", mast.getSystemName(), mast.getClass().getName());
            return;
        }

        currentMast = (MatrixSignalMast) mast;

        bitNum = currentMast.getBitNum(); // number of matrix columns = logic outputs = number of bits per Aspect
        updateMatrixMastPanel(); // show only the correct amount of columns for existing matrixMast
        // @see copyFromAnotherMatrixMastAspect(mast)
        if (map != null) {
            Enumeration<String> aspects = map.getAspects();
            // in matrixPanel LinkedHashtable, fill in mast settings per aspect
            while (aspects.hasMoreElements()) {
                String key = aspects.nextElement(); // for each aspect
                MatrixAspectPanel matrixPanel = matrixAspect.get(key); // load aspectpanel from hashmap
                matrixPanel.setAspectDisabled(currentMast.isAspectDisabled(key)); // sets a disabled aspect
                if ( ! currentMast.isAspectDisabled(key)) { // bits not saved in mast when disabled, so we should not load them back in
                    char[] mastBits = currentMast.getBitsForAspect(key); // same as loading an existing MatrixMast
                    char[] panelAspectBits = Arrays.copyOf(mastBits, MAXMATRIXBITS); // store as [6] character array in panel
                    matrixPanel.updateAspectBits(panelAspectBits);
                    matrixPanel.setAspectBoxes(panelAspectBits);
                    // sets boxes 1 - MAXMATRIXBITS on aspect sub panel from values in hashmap char[] like: 1001
                }
            }
        }

        columnChoice.setSelectedIndex(bitNum - 1); // index of items in list starts counting at 0 while "1" is displayed
        columnChoice.setEnabled(false);
        // fill in the names of the outputs from mast:
        if ( !currentMast.getOutputName(1).equals("")) {
            turnoutBox1.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(1))); // load input into turnoutBox1
        }
        if (bitNum > 1 && !currentMast.getOutputName(2).equals("")) {
            turnoutBox2.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(2))); // load input into turnoutBox2
        }
        if (bitNum > 2 && !currentMast.getOutputName(3).equals("")) {
            turnoutBox3.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(3))); // load input into turnoutBox3
        }
        if (bitNum > 3 && !currentMast.getOutputName(4).equals("")) {
            turnoutBox4.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(4))); // load input into turnoutBox4
        }
        if (bitNum > 4 && !currentMast.getOutputName(5).equals("")) {
            turnoutBox5.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(5))); // load input into turnoutBox5
        }
        if (bitNum > 5 && !currentMast.getOutputName(6).equals("")) {
            turnoutBox6.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(6))); // load input into turnoutBox6
        }
        if (bitNum > 6 && !currentMast.getOutputName(6).equals("")) {
            turnoutBox7.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(7))); // load input into turnoutBox6
        }
        if (bitNum > 7 && !currentMast.getOutputName(6).equals("")) {
            turnoutBox8.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(8))); // load input into turnoutBox6
        }
        if (bitNum > 8 && !currentMast.getOutputName(6).equals("")) {
            turnoutBox9.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(9))); // load input into turnoutBox6
        }
        if (bitNum > 9 && !currentMast.getOutputName(6).equals("")) {
            turnoutBox10.setDefaultNamedBean(InstanceManager.turnoutManagerInstance().getTurnout(currentMast.getOutputName(10))); // load input into turnoutBox6
        }
        // repeat in order to extend MAXMATRIXBITS
        if (currentMast.resetPreviousStates()) {
            resetPreviousState.setSelected(true);
        }
 
        unLitPanelBits = Arrays.copyOf(currentMast.getUnLitBits(), MAXMATRIXBITS); // store as MAXMATRIXBITS character array
        
        // transfer the lit/unlit status to checkboxes
        for (int i = 0; i < MAXMATRIXBITS; i++) {
            unlitCheckArray[i].setSelected(unLitPanelBits[i] == '1');
        }
        
        allowUnLit.setSelected(currentMast.allowUnLit());
        // set up additional mast specific Delay
        timeDelay.setValue(currentMast.getMatrixMastCommandDelay());

        log.trace("setMast {} end", mast);
    }

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        log.debug("createMast({},{})", sigsysname, mastname);
        String newMastName; // UserName for mast

        // check all output boxes are filled (calling getDisplayName() would immediately create a new bean (with an empty comment)
        if (       (              ( turnoutBox1.isEmpty() ) )
                || (bitNum > 1 && ( turnoutBox2.isEmpty() ) )
                || (bitNum > 2 && ( turnoutBox3.isEmpty() ) )
                || (bitNum > 3 && ( turnoutBox4.isEmpty() ) )
                || (bitNum > 4 && ( turnoutBox5.isEmpty() ) )
                || (bitNum > 5 && ( turnoutBox6.isEmpty() ) )
                || (bitNum > 6 && ( turnoutBox7.isEmpty() ) )
                || (bitNum > 7 && ( turnoutBox8.isEmpty() ) )
                || (bitNum > 8 && ( turnoutBox9.isEmpty() ) )
                || (bitNum > 9 && ( turnoutBox10.isEmpty() ) )
        ) {
            // add an extra OR in list above in order to extend MAXMATRIXBITS
            // error dialog
            JOptionPane.showMessageDialog(null, Bundle.getMessage("MatrixOutputEmpty", mastname),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            log.warn("Empty output on panel");
            return false;
        }

        // check/warn if bit sets are identical
        if (identicalBits()) {
            // error dialog
            JOptionPane.showMessageDialog(null, Bundle.getMessage("AspectMastBitsWarning", (int) Math.sqrt(numberOfActiveAspects), numberOfActiveAspects),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            log.warn("Identical bits on panel");
            return false;
        }

        if (currentMast == null) {
            // Create was pressed for new mast: create new MatrixMast with props from panel
            newMastName = "IF$xsm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            newMastName += "($" + (paddedNumber.format(MatrixSignalMast.getLastRef() + 1));
            newMastName += ")" + "-" + bitNum + "t"; // for the number of t = "turnout-outputs"
            // TODO: add d = option for direct packets
            currentMast = new MatrixSignalMast(newMastName); // timedDelay is stored later on
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        } else {
            newMastName = currentMast.getSystemName();
        }

        currentMast.setBitNum(bitNum); // store number of columns in aspect - outputs matrix in mast

        // store outputs from turnoutBoxes; see method MatrixSignalMast#setOutput(colname, turnoutname) line 357
        log.debug("newMastName = {}", newMastName);
        currentMast.setOutput("output1", turnoutBox1.getDisplayName()); // store choice from turnoutBox1, creates bean if needed
        try {
            turnoutBox1.updateComment(turnoutBox1.getNamedBean(), newMastName + ":output1"); // write mast name to output1 bean comment
            if (bitNum > 1) {
                currentMast.setOutput("output2", turnoutBox2.getDisplayName()); // store choice from turnoutBox2
                turnoutBox2.updateComment(turnoutBox2.getNamedBean(), newMastName + ":output2");
                if (bitNum > 2) {
                    currentMast.setOutput("output3", turnoutBox3.getDisplayName()); // store choice from turnoutBox3
                    turnoutBox3.updateComment(turnoutBox3.getNamedBean(), newMastName + ":output3");
                    if (bitNum > 3) {
                        currentMast.setOutput("output4", turnoutBox4.getDisplayName()); // store choice from turnoutBox4
                        turnoutBox4.updateComment(turnoutBox4.getNamedBean(), newMastName + ":output4");
                        if (bitNum > 4) {
                            currentMast.setOutput("output5", turnoutBox5.getDisplayName()); // store choice from turnoutBox5
                            turnoutBox5.updateComment(turnoutBox5.getNamedBean(), newMastName + ":output5");
                            if (bitNum > 5) {
                                currentMast.setOutput("output6", turnoutBox6.getDisplayName()); // store choice from turnoutBox6
                                turnoutBox6.updateComment(turnoutBox6.getNamedBean(), newMastName + ":output6");
                                if (bitNum > 6) {
                                    currentMast.setOutput("output7", turnoutBox7.getDisplayName()); // store choice from turnoutBox7
                                    turnoutBox7.updateComment(turnoutBox7.getNamedBean(), newMastName + ":output7");
                                    if (bitNum > 7) {
                                        currentMast.setOutput("output8", turnoutBox8.getDisplayName()); // store choice from turnoutBox8
                                        turnoutBox8.updateComment(turnoutBox8.getNamedBean(), newMastName + ":output8");
                                        if (bitNum > 8) {
                                            currentMast.setOutput("output9", turnoutBox9.getDisplayName()); // store choice from turnoutBox9
                                            turnoutBox9.updateComment(turnoutBox9.getNamedBean(), newMastName + ":output9");
                                            if (bitNum > 9) {
                                                currentMast.setOutput("output10", turnoutBox10.getDisplayName()); // store choice from turnoutBox10
                                                turnoutBox10.updateComment(turnoutBox10.getNamedBean(), newMastName + ":output10");
                                                // repeat in order to extend MAXMATRIXBITS
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (JmriException e) {
            log.warn("bean not found");
        }

        for (Map.Entry<String, MatrixAspectPanel> entry : matrixAspect.entrySet()) {
            // store matrix in mast per aspect, compare with line 991
            matrixMastPanel.add(entry.getValue().getPanel()); // read from aspect panel to mast
            if (matrixAspect.get(entry.getKey()).isAspectDisabled()) {
                currentMast.setAspectDisabled(entry.getKey()); // don't store bits when this aspect is disabled
            } else {
                currentMast.setAspectEnabled(entry.getKey());
                currentMast.setBitsForAspect(entry.getKey(), matrixAspect.get(entry.getKey()).trimAspectBits()); // return as char[]
            }
        }
        currentMast.resetPreviousStates(resetPreviousState.isSelected()); // read from panel, not displayed?

        currentMast.setAllowUnLit(allowUnLit.isSelected());

        // copy bits from UnLitPanel var unLitPanelBits
        try {
            currentMast.setUnLitBits(trimUnLitBits());
        } catch (Exception ex) {
            log.error("failed to read and copy unLitPanelBits");
        }
        
        if (!username.equals("")) {
            currentMast.setUserName(username);
        }

        prefs.setComboBoxLastSelection(matrixBitNumSelectionCombo, (String) columnChoice.getSelectedItem()); // store bitNum pref
        
        currentMast.setAllowUnLit(allowUnLit.isSelected());

        // set MatrixMast specific Delay information, see jmri.implementation.MatrixSignalMast
        int addDelay = (Integer) timeDelay.getValue(); // from a JSpinner with 0 set as minimum
        currentMast.setMatrixMastCommandDelay(addDelay);
        log.debug("mast create completed successfully");
        return true;
    }

    /**
     * Create bitNumPanel with drop down to set number of columns, separate from
     * the rest for redraw.
     * <p>
     * Auto refresh to show/hide input (turnout) selection boxes. Hide/show
     * checkboxes in matrix (per aspect).
     *
     * @return a JPanel with a comboBox to select number of outputs, set at
     *         current value
     */
    private JPanel makeMatrixMastBitnumPanel() {
        JPanel bitnumpanel = new JPanel();
        bitnumpanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        // select number of columns in logic matrix
        bitnumpanel.add(bitNumLabel);
        bitnumpanel.add(columnChoice);
        if (bitNum < 1 || bitNum > MAXMATRIXBITS) {
            bitNum = 4; // default to 4 col if out-of-range for some reason
        }
        columnChoice.setSelectedIndex(bitNum - 1);
        columnChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newBitnumString = (String) columnChoice.getSelectedItem();
                if (newBitnumString == null) {
                    newBitnumString = "4"; // error, fall back to default
                    log.debug("null newBitnumString in makeMatrixMastBitnumPanel()");
                }
                bitNumChanged(Integer.valueOf(newBitnumString));
            }
        });
        return bitnumpanel;
    }

    /**
     * @return char[] of length bitNum copied from unLitPanelBits
     */
    private char[] trimUnLitBits() {
        if (unLitPanelBits != null) {
            return Arrays.copyOf(unLitPanelBits, bitNum);
        } else {
            return Arrays.copyOf(emptyBits, bitNum);
        }
    }

    /**
     * Build lower half of Add Signal Mast panel, specifically for Matrix Mast.
     * <p>
     * Called when Mast Type drop down changes.
     */
    private void updateMatrixMastPanel() {
        matrixAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);

        // nothing to do if no map yet present
        if (map == null) return;
        
        Enumeration<String> aspects = map.getAspects();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            MatrixAspectPanel aspectpanel = new MatrixAspectPanel(aspect);
            matrixAspect.put(aspect, aspectpanel); // store in LinkedHashMap
            // values are filled in later
        }
        matrixMastPanel.removeAll();

        // sub panels (so we can hide all turnouts with Output Type drop down box later)
        JPanel turnoutpanel = new JPanel();
        
        // binary matrix outputs follow:
        
        JPanel output1panel = makeOutputPanel(turnoutBox1,1);
        turnoutpanel.add(output1panel);
        // output1panel always on
        output1panel.setVisible(true);
        
        turnoutpanel.add(makeOutputPanel(turnoutBox2,2));
        turnoutpanel.add(makeOutputPanel(turnoutBox3,3));
        turnoutpanel.add(makeOutputPanel(turnoutBox4,4));
        
        // Four in a row is really enough, start another row
        matrixMastPanel.add(turnoutpanel);
        turnoutpanel = new JPanel();
        
        turnoutpanel.add(makeOutputPanel(turnoutBox5,5));
        turnoutpanel.add(makeOutputPanel(turnoutBox6,6));
        turnoutpanel.add(makeOutputPanel(turnoutBox7,7));
        turnoutpanel.add(makeOutputPanel(turnoutBox8,8));

        // Four in a row is really enough, start another row
        matrixMastPanel.add(turnoutpanel);
        turnoutpanel = new JPanel();

        turnoutpanel.add(makeOutputPanel(turnoutBox9,9));
        turnoutpanel.add(makeOutputPanel(turnoutBox10,10));

        // repeat in order to extend MAXMATRIXBITS

        matrixMastPanel.add(turnoutpanel);

        // show the active unlit check boxes
        for (int i = 0; i < MAXMATRIXBITS; i++) {
            unlitCheckArray[i].setVisible(i < bitNum);
        }

        JPanel matrixHeader = new JPanel();
        JLabel matrixHeaderLabel = new JLabel(Bundle.getMessage("AspectMatrixHeaderLabel", bitNum), JLabel.CENTER);
        matrixHeader.add(matrixHeaderLabel);
        matrixHeaderLabel.setToolTipText(Bundle.getMessage("AspectMatrixHeaderTooltip"));
        matrixMastPanel.add(matrixHeader);

        for (Map.Entry<String, MatrixAspectPanel> entry : matrixAspect.entrySet()) {
            matrixMastPanel.add(entry.getValue().getPanel()); // load Aspect sub panels to matrixMastPanel from hashmap
            // build aspect sub panels
        }
        if ((matrixAspect.size() & 1) == 1) {
            // spacer before "Reset previous aspect"
            matrixMastPanel.add(new JLabel());
        }

        matrixMastPanel.add(resetPreviousState); // checkbox
        
        resetPreviousState.setToolTipText(Bundle.getMessage("ResetPreviousToolTip"));
        // copy option matrixMast bitstrings = settings
        JPanel matrixCopyPanel = new JPanel();
        matrixCopyPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        matrixCopyPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("MatrixMastCopyAspectBits"))));
        matrixCopyPanel.add(copyFromMastSelection());
        matrixMastPanel.add(matrixCopyPanel);

        // add additional MatrixMast-specific delay
        JPanel delayPanel = new JPanel();
        delayPanel.add(new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("LabelTurnoutDelay"))));
        timeDelay.setModel(new SpinnerNumberModel(0, 0, 1000, 1));
        // timeDelay.setValue(0); // reset from possible previous use
        timeDelay.setPreferredSize(new JTextField(5).getPreferredSize());
        delayPanel.add(timeDelay);
        timeDelay.setToolTipText(Bundle.getMessage("TooltipTurnoutDelay"));
        delayPanel.add(new JLabel(Bundle.getMessage("LabelMilliseconds")));
        matrixMastPanel.add(delayPanel);

        matrixMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
        matrixMastPanel.revalidate();
    }
    
    /**
     * Index is 1-based here:  1..MAXMATRIXBITS
     * @param turnoutPanel The panel to convert to an output panel
     * @param index The 1..MAXMATRIXBITS index of the one to be set visible
     * @return An output panel containing the selected turnout panel
     */
    JPanel makeOutputPanel(JPanel turnoutPanel, int index) {
        JPanel outputpanel = new JPanel();
        outputpanel.add(turnoutPanel);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("MatrixOutputLabel", index));
        outputpanel.setBorder(border);
        
        outputpanel.setVisible(index <= bitNum);
        
        return outputpanel;
    }
    
    /**
     * When the user changes the number of columns in matrix from the drop down:
     * store the new value and redraw pane.
     *
     * @param newColNum int with the new value = the number of columns in the
     *                  Matrix Table
     */
     private void bitNumChanged(Integer newColNum) {
        if (newColNum < 1 || newColNum > MAXMATRIXBITS || newColNum == bitNum) {
            return;
        }
        bitNum = newColNum;
        // hide/show output choices per Aspect
        updateMatrixMastPanel();

        validate();
        java.awt.Container ancestor = getTopLevelAncestor();
        if ((ancestor instanceof JmriJFrame)) {
            ancestor.setSize(ancestor.getPreferredSize());
            ((JmriJFrame) ancestor).pack();
        }
        repaint();
    }

    private void copyFromAnotherMatrixMastAspect(String strMast) {
        MatrixSignalMast mast = (MatrixSignalMast) InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(strMast);
        if (mast == null) {
            log.error("Cannot copy from mast {} which doesn't exist", strMast);
            return;
        }
        if (bitNum != mast.getBitNum()) {
            int i = JOptionPane.showConfirmDialog(null, Bundle.getMessage("MatrixColWarning", mast.getBitNum(), bitNum),
                    Bundle.getMessage("MatrixColWarningTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (i != 0) {
                return;
            }
        }
        // cf. line 405 loading an existing mast for edit
        for (Map.Entry<String, MatrixAspectPanel> entry : matrixAspect.entrySet()) {
            // select the correct checkboxes
            MatrixAspectPanel matrixPanel = entry.getValue(); // load aspectpanel from hashmap
            matrixPanel.setAspectDisabled(mast.isAspectDisabled(entry.getKey())); // sets a disabled aspect
            if (!mast.isAspectDisabled(entry.getKey())) {
                char[] mastBits = mast.getBitsForAspect(entry.getKey()); // same as loading an existing MatrixMast
                char[] panelAspectBits = Arrays.copyOf(mastBits, MAXMATRIXBITS); // store as 6 character array in panel
                matrixPanel.updateAspectBits(panelAspectBits);
                matrixPanel.setAspectBoxes(panelAspectBits);
                // sets boxes 1 - MAXMATRIXBITS on aspect sub panel from values in hashmap char[] like: 1001
            }
        }
    }

/*    *//**
     * Call for sub panel per aspect from hashmap matrixAspect with check boxes
     * to set properties.
     * <p>
     * Invoked when updating MatrixMastPanel.
     *
     * @see #updateMatrixMastPanel()
     *//*
    void updateMatrixAspectPanel() {
        Enumeration<String> aspects = map.getAspects();
        while (aspects.hasMoreElements()) {
            String aspect = aspects.nextElement();
            MatrixAspectPanel aspectpanel = new MatrixAspectPanel(aspect, bitString); // build 1 line, picking up bitString
            matrixAspect.put(aspect, aspectpanel); // store that line
        }
        // refresh aspects list
        // TODO sort matrixAspect HashTable, which at this point is not sorted
        matrixMastPanel.removeAll();
        for (Map.Entry<String, MatrixAspectPanel> entry : matrixAspect.entrySet()) {
            matrixMastPanel.add(entry.getValue().getPanel());
            // Matrix checkbox states are set by getPanel()
        }
        matrixMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
        matrixMastPanel.revalidate();
    }*/

    private JPanel matrixUnLitPanel = new JPanel();
    
    private JCheckBox[] unlitCheckArray = new JCheckBox[MAXMATRIXBITS];
    {
        for (int i = 0; i < MAXMATRIXBITS; i++) {
            unlitCheckArray[i] = new JCheckBox();
        }
    }
    
    // add more JCheckBoxes in order to extend MAXMATRIXBITS

    /**
     * JPanel to set outputs for an unlit (Dark) Matrix Signal Mast.
     */
    private void matrixUnLitPanel() {
        if (bitNum < 1 || bitNum > MAXMATRIXBITS) {
            bitNum = 4; // default to 4 col for (first) new mast
        }

        JPanel matrixUnLitDetails = new JPanel();
        matrixUnLitDetails.setLayout(new jmri.util.javaworld.GridLayout2(1, 1)); // stretch to full width

        for (int i = 0; i < MAXMATRIXBITS; i++) matrixUnLitDetails.add(unlitCheckArray[i]);

        matrixUnLitPanel.add(matrixUnLitDetails);
        TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border.setTitle(Bundle.getMessage("MatrixUnLitDetails"));
        matrixUnLitPanel.setBorder(border);
        matrixUnLitPanel.setToolTipText(Bundle.getMessage("MatrixUnlitTooltip"));

        unlitCheckArray[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(1, unlitCheckArray[0].isSelected());
            }
        });
        unlitCheckArray[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(2, unlitCheckArray[1].isSelected());
            }
        });
        unlitCheckArray[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(3, unlitCheckArray[2].isSelected());
            }
        });
        unlitCheckArray[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(4, unlitCheckArray[3].isSelected());
            }
        });
        unlitCheckArray[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(5, unlitCheckArray[4].isSelected());
            }
        });
        unlitCheckArray[5].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(6, unlitCheckArray[5].isSelected());
            }
        });
        unlitCheckArray[6].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(7, unlitCheckArray[6].isSelected());
            }
        });
        unlitCheckArray[7].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(8, unlitCheckArray[7].isSelected());
            }
        });
        unlitCheckArray[8].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(9, unlitCheckArray[8].isSelected());
            }
        });
        unlitCheckArray[9].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setUnLitBit(10, unlitCheckArray[9].isSelected());
            }
        });
        // repeat in order to extend MAXMATRIXBITS
    }


    private JComboBox<String> copyFromMastSelection() {
        JComboBox<String> mastSelect = new JComboBox<>();
        for (SignalMast mast : InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBeanSet()) {
            if (mast instanceof MatrixSignalMast){
                mastSelect.addItem(mast.getDisplayName());
            }
        }
        if (mastSelect.getItemCount() == 0) {
            mastSelect.setEnabled(false);
        } else {
            mastSelect.insertItemAt("", 0);
            mastSelect.setSelectedIndex(0);
            mastSelect.addActionListener(new ActionListener() {
                @SuppressWarnings("unchecked") // e.getSource() cast from mastSelect source
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox<String> eb = (JComboBox<String>) e.getSource();
                    String sourceMast = (String) eb.getSelectedItem();
                    if (sourceMast != null && !sourceMast.equals("")) {
                        copyFromAnotherMatrixMastAspect(sourceMast);
                    }
                }
            });
        }
        return mastSelect;
    }

    /**
     * Update the on/off positions for the unLitPanelBits char[].
     * <p>
     * Invoked from bit checkboxes 1 to MAXMATRIXBITS on unLitPanel.
     *
     * @param column int as index for an output (between 1 and 6)
     * @param state  boolean for the output On (Closed) or Off (Thrown)
     */
    private void setUnLitBit(int column, boolean state) {
        if (state) {
            unLitPanelBits[column - 1] = '1';
        } else {
            unLitPanelBits[column - 1] = '0';
        }
    }

    /**
     * Check all aspects for duplicate bit combos.
     *
     * @return true if at least 1 duplicate row of bits is found
     */
    private boolean identicalBits() {
        boolean identical = false;
        numberOfActiveAspects = 0;
        Collection<String> seenBits = new HashSet<String>(); // a fast access, no duplicates Collection of bit combinations
        for (Map.Entry<String, MatrixAspectPanel> entry : matrixAspect.entrySet()) {
            // check per aspect
            if (entry.getValue().isAspectDisabled()) {
                continue; // skip disabled aspects
            } else if (seenBits.contains(String.valueOf(entry.getValue().trimAspectBits()))) {
                identical = true;
                log.debug("-found duplicate {}", String.valueOf(entry.getValue().trimAspectBits()));
                // don't break, so we can count number of enabled aspects for this mast
            } else {
                seenBits.add(String.valueOf(entry.getValue().trimAspectBits())); // convert back from char[] to String
                log.debug("-added new {}; seenBits = {}", String.valueOf(entry.getValue().trimAspectBits()), seenBits.toString());
            }
            ++numberOfActiveAspects;
        }
        return identical;
    }

    /**
     * JPanel to define properties of an Aspect for a Matrix Signal Mast.
     * <p>
     * Invoked from the AddSignalMastPanel class when Output Matrix Signal Mast is
     * selected as mast type.
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
        JCheckBox bitCheck7 = new JCheckBox();
        JCheckBox bitCheck8 = new JCheckBox();
        JCheckBox bitCheck9 = new JCheckBox();
        JCheckBox bitCheck10= new JCheckBox();
        // repeat in order to extend MAXMATRIXBITS
        
        JTextField aspectBitsField = new JTextField(MAXMATRIXBITS); // for debug
        String aspect = "";
        String emptyChars = "0000000000"; // size of String = MAXMATRIXBITS; add another 0 in order to extend MAXMATRIXBITS
        char[] aspectBits = emptyChars.toCharArray();

        /**
         * Build new aspect matrix panel.
         * Called when Add Signal Mast Pane is built.
         *
         * @param aspect String like "Clear"
         */
        MatrixAspectPanel(String aspect) {
            this.aspect = aspect;
        }

        /**
         * Build an aspect matrix panel using char[] previously entered. Called
         * when number of outputs = columns is changed (possible during new mast
         * creation only).
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
                if (bitNum > 6) {
                    bitCheck7.setEnabled(false);
                }
                if (bitNum > 7) {
                    bitCheck8.setEnabled(false);
                }
                if (bitNum > 8) {
                    bitCheck9.setEnabled(false);
                }
                if (bitNum > 9) {
                    bitCheck10.setEnabled(false);
                }
                // repeat in order to extend MAXMATRIXBITS
                
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
                if (bitNum > 6) {
                    bitCheck7.setEnabled(true);
                }
                if (bitNum > 7) {
                    bitCheck8.setEnabled(true);
                }
                if (bitNum > 8) {
                    bitCheck9.setEnabled(true);
                }
                if (bitNum > 9) {
                    bitCheck10.setEnabled(true);
                }
                // repeat in order to set extend MAXMATRIXBITS
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
        void setBit(int column, boolean state) {
            if (state) {
                aspectBits[column - 1] = '1';
            } else {
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
                return Arrays.copyOf(aspectBits, bitNum); // copy to new char[] of length bitNum
            } catch (Exception ex) {
                log.error("failed to read and copy aspectBits");
                return new char[] {};
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
            if (bitNum > 6) {
                bitCheck7.setSelected(aspectBits[6] == '1');
            }
            if (bitNum > 7) {
                bitCheck8.setSelected(aspectBits[7] == '1');
            }
            if (bitNum > 8) {
                bitCheck9.setSelected(aspectBits[8] == '1');
            }
            if (bitNum > 9) {
                bitCheck10.setSelected(aspectBits[9] == '1');
            }
            // repeat in order to set extend MAXMATRIXBITS
            
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
                matrixDetails.add(bitCheck7);
                matrixDetails.add(bitCheck8);
                matrixDetails.add(bitCheck9);
                matrixDetails.add(bitCheck10);
                // repeat in order to extend MAXMATRIXBITS
                
                // TODO refresh aspectSetting, can be in OKPressed() to store/warn for duplicates (with button 'Ignore')
                
                // hide if id > bitNum (var in panel)
                bitCheck2.setVisible(bitNum > 1);
                bitCheck3.setVisible(bitNum > 2);
                bitCheck4.setVisible(bitNum > 3);
                bitCheck5.setVisible(bitNum > 4);
                bitCheck6.setVisible(bitNum > 5);
                bitCheck7.setVisible(bitNum > 6);
                bitCheck8.setVisible(bitNum > 7);
                bitCheck9.setVisible(bitNum > 8);
                bitCheck10.setVisible(bitNum> 9);
                // repeat in order to extend MAXMATRIXBITS

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
                bitCheck7.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(7, bitCheck7.isSelected());
                    }
                });
                bitCheck8.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(8, bitCheck8.isSelected());
                    }
                });
                bitCheck9.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(9, bitCheck9.isSelected());
                    }
                });
                bitCheck10.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setBit(10, bitCheck10.isSelected());
                    }
                });
                // repeat in order to extend MAXMATRIXBITS
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
