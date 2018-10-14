package jmri.jmrit.beantable.signalmast;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

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
 * @author Egbert Broerse Copyright (C) 2016
 * @since 4.11.2
 */
public class MatrixSignalMastAddPane extends SignalMastAddPane {

    public MatrixSignalMastAddPane() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // lit/unlit controls
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        p.add(new JLabel(Bundle.getMessage("AllowUnLitLabel") + ": "));
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
    
    String sigsysname;
    DefaultSignalAppearanceMap map;
    
    MatrixSignalMast currentMast;
    
    JCheckBox resetPreviousState = new JCheckBox(Bundle.getMessage("ResetPrevious"));

    jmri.UserPreferencesManager prefs = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

    String matrixBitNumSelectionCombo = this.getClass().getName() + ".matrixBitNumSelected";

    JCheckBox allowUnLit = new JCheckBox();

    JScrollPane matrixMastScroll;
    JPanel matrixMastBitnumPanel = new JPanel();
    JPanel matrixMastPanel = new JPanel();
    char[] bitString;
    char[] unLitPanelBits;
    int numberOfActiveAspects;

    String emptyChars = "000000"; // size of String = MAXMATRIXBITS; add 7th 0 in order to set > 6
    char[] emptyBits = emptyChars.toCharArray();
    JLabel bitNumLabel = new JLabel(Bundle.getMessage("MakeLabel", Bundle.getMessage("MatrixBitsLabel")));
    JComboBox<String> columnChoice = new JComboBox<>(choiceArray());

    LinkedHashMap<String, MatrixAspectPanel> matrixAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT); // LinkedHT type keeps things sorted // only used once, see updateMatrixAspectPanel()

    DecimalFormat paddedNumber = new DecimalFormat("0000");

    private String[] choiceArray() {
        String[] numberOfOutputs = new String[MAXMATRIXBITS];
        for (int i = 0; i < MAXMATRIXBITS; i++) {
            numberOfOutputs[i] = (i + 1) + "";
        }
        log.debug("Created output combo box: {}", Arrays.toString(numberOfOutputs));
        return numberOfOutputs;
    }

    /**
     * Set the maximum number of outputs for Matrix Signal Masts Used in
     * combobox and for loops
     */
    public static final int MAXMATRIXBITS = 6; // Don't set above 6
    // 6 Seems the maximum to be able to show in a panel a coded and code below should be extended where marked

    /**
     * on = thrown, off = closed, no turnout states asked
     */
    BeanSelectCreatePanel<Turnout> turnoutBox1 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    BeanSelectCreatePanel<Turnout> turnoutBox2 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    BeanSelectCreatePanel<Turnout> turnoutBox3 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    BeanSelectCreatePanel<Turnout> turnoutBox4 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    BeanSelectCreatePanel<Turnout> turnoutBox5 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    BeanSelectCreatePanel<Turnout> turnoutBox6 = new BeanSelectCreatePanel<>(InstanceManager.turnoutManagerInstance(), null);
    // repeat in order to set MAXMATRIXBITS > 6

    /**
     * The number of columns in logic matrix
     */
    int bitNum;
    // ToDo: add boxes to set DCC Packets (with drop down selection "Output Type": Turnouts/Direct DCC Packets)

    /** {@inheritDoc} */
    @Override
    @Nonnull public String getPaneName() {
        return Bundle.getMessage("MatrixCtlMast");
    }

    /** {@inheritDoc} */
    @Override
    public void setAspectNames(@Nonnull
            SignalAppearanceMap newMap, SignalSystem sigSystem) {
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
        if ( ! currentMast.getOutputName(1).equals("")) {
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
        // repeat in order to set MAXMATRIXBITS > 6
        if (currentMast.resetPreviousStates()) {
            resetPreviousState.setSelected(true);
        }
 
        unLitPanelBits = Arrays.copyOf(currentMast.getUnLitBits(), MAXMATRIXBITS); // store as MAXMATRIXBITS character array
        unlitCheck1.setSelected(unLitPanelBits[0] == '1'); // set checkboxes
        if (bitNum > 1) {
            unlitCheck2.setSelected(unLitPanelBits[1] == '1');
        }
        if (bitNum > 2) {
            unlitCheck3.setSelected(unLitPanelBits[2] == '1');
        }
        if (bitNum > 3) {
            unlitCheck4.setSelected(unLitPanelBits[3] == '1');
        }
        if (bitNum > 4) {
            unlitCheck5.setSelected(unLitPanelBits[4] == '1');
        }
        if (bitNum > 5) {
            unlitCheck6.setSelected(unLitPanelBits[5] == '1');
        }
        // repeat in order to set MAXMATRIXBITS > 6
        
        allowUnLit.setSelected(currentMast.allowUnLit());
 
        log.trace("setMast {} end", mast);
    }

    /** {@inheritDoc} */
    @Override
    public boolean createMast(@Nonnull String sigsysname, @Nonnull String mastname, @Nonnull String username) {
        log.debug("createMast({},{})", sigsysname, mastname);

        // check all boxes are filled
        if (       (              ( turnoutBox1.getDisplayName() == null || turnoutBox1.getDisplayName().isEmpty() ) )
                || (bitNum > 1 && ( turnoutBox2.getDisplayName() == null || turnoutBox2.getDisplayName().isEmpty() ) )
                || (bitNum > 2 && ( turnoutBox3.getDisplayName() == null || turnoutBox3.getDisplayName().isEmpty() ) )
                || (bitNum > 3 && ( turnoutBox4.getDisplayName() == null || turnoutBox4.getDisplayName().isEmpty() ) )
                || (bitNum > 4 && ( turnoutBox5.getDisplayName() == null || turnoutBox5.getDisplayName().isEmpty() ) )
                || (bitNum > 5 && ( turnoutBox6.getDisplayName() == null || turnoutBox6.getDisplayName().isEmpty() ) )
        ) {
            // add extra OR in order to set MAXMATRIXBITS > 6
            // error dialog
            JOptionPane.showMessageDialog(null, Bundle.getMessage("MatrixOutputEmpty", mastname),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            log.warn("Empty output on panel");
            return false;
        }

        // check if bit sets are identical
        if (identicalBits()) {
            // error dialog
            JOptionPane.showMessageDialog(null, Bundle.getMessage("AspectMastBitsWarning", (int) Math.sqrt(numberOfActiveAspects), numberOfActiveAspects),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.ERROR_MESSAGE);
            log.warn("Identical bits on panel");
            return false;
        }

        if (currentMast == null) {
            // Create was pressed for new mast:
            // create new MatrixMast with props from panel
            String name = "IF$xsm:"
                    + sigsysname
                    + ":" + mastname.substring(11, mastname.length() - 4);
            name += "($" + (paddedNumber.format(MatrixSignalMast.getLastRef() + 1));
            name += ")" + "-" + bitNum + "t"; // for the number of t = "turnout-outputs", add option for direct packets
            currentMast = new MatrixSignalMast(name);
            InstanceManager.getDefault(jmri.SignalMastManager.class).register(currentMast);
        }
        
        String name = currentMast.getSystemName();
        
        currentMast.setBitNum(bitNum); // store number of columns in aspect - outputs matrix in mast

        //store outputs from turnoutBoxes; method in line 976
        currentMast.setOutput("output1", turnoutBox1.getDisplayName()); // store choice from turnoutBox1
        setMatrixReference(turnoutBox1, name + ":output1"); // write mast name to output1 bean comment
        if (bitNum > 1) {
            currentMast.setOutput("output2", turnoutBox2.getDisplayName()); // store choice from turnoutBox2
            setMatrixReference(turnoutBox2, name + ":output2"); // write mast name to output2 bean comment
            if (bitNum > 2) {
                currentMast.setOutput("output3", turnoutBox3.getDisplayName()); // store choice from turnoutBox3
                setMatrixReference(turnoutBox3, name + ":output3"); // write mast name to output3 bean comment
                if (bitNum > 3) {
                    currentMast.setOutput("output4", turnoutBox4.getDisplayName()); // store choice from turnoutBox4
                    setMatrixReference(turnoutBox4, name + ":output4"); // write mast name to output4 bean comment
                    if (bitNum > 4) {
                        currentMast.setOutput("output5", turnoutBox5.getDisplayName()); // store choice from turnoutBox5
                        setMatrixReference(turnoutBox5, name + ":output5"); // write mast name to output5 bean comment
                        if (bitNum > 5) {
                            currentMast.setOutput("output6", turnoutBox6.getDisplayName()); // store choice from turnoutBox6
                            setMatrixReference(turnoutBox6, name + ":output6"); // write mast name to output6 bean comment
                            // repeat in order to set MAXMATRIXBITS > 6
                        }
                    }
                }
            }
        }

        for (String aspect : matrixAspect.keySet()) {
            // store matrix in mast per aspect, compare with line 991
            matrixMastPanel.add(matrixAspect.get(aspect).getPanel()); // read from aspect panel to mast
            if (matrixAspect.get(aspect).isAspectDisabled()) {
                currentMast.setAspectDisabled(aspect); // don't store bits when this aspect is disabled
            } else {
                currentMast.setAspectEnabled(aspect);
                currentMast.setBitsForAspect(aspect, matrixAspect.get(aspect).trimAspectBits()); // return as char[]
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
    JPanel makeMatrixMastBitnumPanel() {
        JPanel bitnumpanel = new JPanel();
        bitnumpanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        // select number of columns in logic matrix
        bitnumpanel.add(bitNumLabel);
        bitnumpanel.add(columnChoice); // drop down list 1 - 5
        if (bitNum < 1 || bitNum > MAXMATRIXBITS) {
            bitNum = 4; // default to 4 col for (first) new mast
        }
        columnChoice.setSelectedIndex(bitNum - 1);
        columnChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newBitnumString = (String) columnChoice.getSelectedItem();
                bitNumChanged(Integer.valueOf(newBitnumString));
            }
        });
        return bitnumpanel;
    }

    /**
     * @return char[] of length bitNum copied from unLitPanelBits
     */
    protected char[] trimUnLitBits() {
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
    void updateMatrixMastPanel() {
        matrixAspect = new LinkedHashMap<>(NOTIONAL_ASPECT_COUNT);

        // nothing to if no map yet
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
        JPanel output1panel = new JPanel();
        output1panel.add(turnoutBox1);
        TitledBorder border1 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border1.setTitle(Bundle.getMessage("MatrixOutputLabel", 1));
        output1panel.setBorder(border1);
        turnoutpanel.add(output1panel);

        JPanel output2panel = new JPanel();
        output2panel.add(turnoutBox2);
        TitledBorder border2 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border2.setTitle(Bundle.getMessage("MatrixOutputLabel", 2));
        output2panel.setBorder(border2);
        turnoutpanel.add(output2panel);

        JPanel output3panel = new JPanel();
        output3panel.add(turnoutBox3);
        TitledBorder border3 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border3.setTitle(Bundle.getMessage("MatrixOutputLabel", 3));
        output3panel.setBorder(border3);
        turnoutpanel.add(output3panel);

        JPanel output4panel = new JPanel();
        output4panel.add(turnoutBox4);
        TitledBorder border4 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border4.setTitle(Bundle.getMessage("MatrixOutputLabel", 4));
        output4panel.setBorder(border4);
        turnoutpanel.add(output4panel);

        JPanel output5panel = new JPanel();
        output5panel.add(turnoutBox5);
        TitledBorder border5 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border5.setTitle(Bundle.getMessage("MatrixOutputLabel", 5));
        output5panel.setBorder(border5);
        turnoutpanel.add(output5panel);

        JPanel output6panel = new JPanel();
        output6panel.add(turnoutBox6);
        TitledBorder border6 = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black));
        border6.setTitle(Bundle.getMessage("MatrixOutputLabel", 6));
        output6panel.setBorder(border6);
        turnoutpanel.add(output6panel);

        // repeat in order to set MAXMATRIXBITS > 6
        // output1panel always on
        output2panel.setVisible(bitNum > 1);
        output3panel.setVisible(bitNum > 2);
        output4panel.setVisible(bitNum > 3);
        output5panel.setVisible(bitNum > 4);
        output6panel.setVisible(bitNum > 5);
        // repeat in order to set MAXMATRIXBITS > 6

        matrixMastPanel.add(turnoutpanel);

        unlitCheck2.setVisible(bitNum > 1);
        unlitCheck3.setVisible(bitNum > 2);
        unlitCheck4.setVisible(bitNum > 3);
        unlitCheck5.setVisible(bitNum > 4);
        unlitCheck6.setVisible(bitNum > 5);
        // repeat in order to set MAXMATRIXBITS > 6

        JPanel matrixHeader = new JPanel();
        JLabel matrixHeaderLabel = new JLabel(Bundle.getMessage("AspectMatrixHeaderLabel", bitNum), JLabel.CENTER);
        matrixHeader.add(matrixHeaderLabel);
        matrixHeaderLabel.setToolTipText(Bundle.getMessage("AspectMatrixHeaderTooltip"));
        matrixMastPanel.add(matrixHeader);

        for (String aspect : matrixAspect.keySet()) {
            matrixMastPanel.add(matrixAspect.get(aspect).getPanel()); // load Aspect sub panels to matrixMastPanel from hashmap
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
        matrixCopyPanel.add(new JLabel(Bundle.getMessage("MatrixMastCopyAspectBits") + ":"));
        matrixCopyPanel.add(copyFromMastSelection());
        matrixMastPanel.add(matrixCopyPanel);

        matrixMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
        matrixMastPanel.revalidate();
    }
    
     /**
     * When the user changes the number of columns in matrix from the drop down:
     * store the new value.
     *
     * @param newColNum int with the new value = the number of columns in the
     *                  Matrix Table
     */
    void bitNumChanged(Integer newColNum) {
        if (newColNum < 1 || newColNum > MAXMATRIXBITS || newColNum == bitNum) {
            return;
        }
        bitNum = newColNum;
        // hide/show output choices per Aspect
        updateMatrixMastPanel();

        validate();
        if (getTopLevelAncestor() != null) {
            ((jmri.util.JmriJFrame) getTopLevelAncestor()).setSize(((jmri.util.JmriJFrame) getTopLevelAncestor()).getPreferredSize());
            ((jmri.util.JmriJFrame) getTopLevelAncestor()).pack();
        }
        repaint();
    }
   
    
    /**
     * Write matrix mast name + output no. to output bean comment.
     *
     * @param bp           the bean panel containing the Turnout (output)
     * @param functionName Description of turnout function on mast
     */
    void setMatrixReference(BeanSelectCreatePanel bp, String functionName) {
        //System.out.println("box: " + bp.getDisplayName()); // debug
        //System.out.println("name: " + functionName); // debug
        bp.setReference(functionName);
    }

    void copyFromAnotherMatrixMastAspect(String strMast) {
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
        for (String key : matrixAspect.keySet()) {
            // select the correct checkboxes
            MatrixAspectPanel matrixPanel = matrixAspect.get(key); // load aspectpanel from hashmap
            matrixPanel.setAspectDisabled(mast.isAspectDisabled(key)); // sets a disabled aspect
            if (!mast.isAspectDisabled(key)) {
                char[] mastBits = mast.getBitsForAspect(key); // same as loading an existing MatrixMast
                char[] panelAspectBits = Arrays.copyOf(mastBits, MAXMATRIXBITS); // store as 6 character array in panel
                matrixPanel.updateAspectBits(panelAspectBits);
                matrixPanel.setAspectBoxes(panelAspectBits);
                // sets boxes 1 - MAXMATRIXBITS on aspect sub panel from values in hashmap char[] like: 1001
            }
        }
    }

    /**
     * Call for sub panel per aspect from hashmap matrixAspect with check boxes
     * to set properties.
     * <p>
     * Invoked when updating MatrixMastPanel
     *
     * @see #updateMatrixMastPanel()
     */
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
        for (String aspect : matrixAspect.keySet()) {
            matrixMastPanel.add(matrixAspect.get(aspect).getPanel());
            // Matrix checkbox states are set by getPanel()
        }

        matrixMastPanel.setLayout(new jmri.util.javaworld.GridLayout2(0, 1)); // 0 means enough
        matrixMastPanel.revalidate();
    }

    JPanel matrixUnLitPanel = new JPanel();
    JCheckBox unlitCheck1 = new JCheckBox();
    JCheckBox unlitCheck2 = new JCheckBox();
    JCheckBox unlitCheck3 = new JCheckBox();
    JCheckBox unlitCheck4 = new JCheckBox();
    JCheckBox unlitCheck5 = new JCheckBox();
    JCheckBox unlitCheck6 = new JCheckBox();
    // repeat in order to set MAXMATRIXBITS > 6

    /**
     * JPanel to set outputs for an unlit (Dark) Matrix Signal Mast.
     */
    void matrixUnLitPanel() {
        if (bitNum < 1 || bitNum > MAXMATRIXBITS) {
            bitNum = 4; // default to 4 col for (first) new mast
        }

        JPanel matrixUnLitDetails = new JPanel();
        matrixUnLitDetails.setLayout(new jmri.util.javaworld.GridLayout2(1, 1)); // stretch to full width

        matrixUnLitDetails.add(unlitCheck1);
        matrixUnLitDetails.add(unlitCheck2);
        matrixUnLitDetails.add(unlitCheck3);
        matrixUnLitDetails.add(unlitCheck4);
        matrixUnLitDetails.add(unlitCheck5);
        matrixUnLitDetails.add(unlitCheck6);
        // repeat in order to set MAXMATRIXBITS > 6

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
    }


    JComboBox<String> copyFromMastSelection() {
        JComboBox<String> mastSelect = new JComboBox<>();
        List<String> names = InstanceManager.getDefault(jmri.SignalMastManager.class).getSystemNameList();
        for (String name : names) {
            // only accept MatrixSignalMast masts
            if (InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(name) instanceof MatrixSignalMast) {
                SignalMast m = InstanceManager.getDefault(jmri.SignalMastManager.class).getNamedBean(name);
                if (m!=null) {
                    mastSelect.addItem(m.getDisplayName());
                } else {
                    log.error("Can't copy from mast {} as it doesn't exist", name);
                }
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
    public void setUnLitBit(int column, boolean state) {
        if (state == true) {
            unLitPanelBits[column - 1] = '1';
        }
        if (state == false) {
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
        for (String aspect : matrixAspect.keySet()) {
            // check per aspect
            if (matrixAspect.get(aspect).isAspectDisabled()) {
                continue; // skip disabled aspects
            } else if (seenBits.contains(String.valueOf(matrixAspect.get(aspect).trimAspectBits()))) {
                identical = true;
                log.debug("-found duplicate {}", String.valueOf(matrixAspect.get(aspect).trimAspectBits()));
                // break; // don't break, so we can count number of enabled aspects for this mast
            } else {
                seenBits.add(String.valueOf(matrixAspect.get(aspect).trimAspectBits())); // convert back from char[] to String
                log.debug("-added new {}; seenBits = {}", String.valueOf(matrixAspect.get(aspect).trimAspectBits()), seenBits.toString());
            }
            ++numberOfActiveAspects;
        }
        return identical;
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
