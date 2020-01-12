package jmri.jmrit.symbolicprog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.util.CvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent an entire speed table as a single Variable.
 * <p>
 * This presents as a set of vertically oriented sliders, with numeric values
 * above them. That it turn is done using VarSlider and DecVariableValue objects
 * respectively. VarSlider is an interior class to color a JSlider by state. The
 * respective VarSlider and DecVariableValue communicate through their
 * underlying CV objects. Changes to CV Values are listened to by this class,
 * which updates the model objects for the VarSliders; the DecVariableValues
 * listen directly.
 * <p>
 * Color (hence state) of individual sliders (hence CVs) are directly coupled to
 * the state of those CVs.
 * <p>
 * The state of the entire variable has to be a composite of all the sliders,
 * hence CVs. The mapping is (in order):
 * <ul>
 * <li>If any CVs are UNKNOWN, its UNKNOWN..
 * <li>If not, and any are EDITED, its EDITED.
 * <li>If not, and any are FROMFILE, its FROMFILE.
 * <li>If not, and any are READ, its READ.
 * <li>If not, and any are STORED, its STORED.
 * <li>And if we get to here, something awful has happened.
 * </ul>
 * <p>
 * A similar pattern is used for a read or write request. Write writes them all;
 * Read reads any that aren't READ or WRITTEN.
 * <p>
 * Speed tables can have different numbers of entries; 28 is the default, and
 * also the maximum.
 * <p>
 * The NMRA specification says that speed table entries cannot be non-monotonic
 * (e.g. cannot decrease when moving from lower to higher CV numbers). In
 * earlier versions of the code, this was enforced any time a value was changed
 * (for any reason). This caused a problem when CVs were read that were
 * non-monotonic: That value was read, causing lower CVs to be made consistent,
 * a change in their value which changed their state, so they were read again.
 * To avoid this, the class now only enforces non-monotonicity when the slider
 * is adjusted.
 * <p>
 * _value is a holdover from the LongAddrVariableValue, which this was copied
 * from; it should be removed.
 *
 * @author Bob Jacobsen, Alex Shepherd Copyright (C) 2001, 2004, 2013
 * @author Dave Heap Copyright (C) 2012 Added support for Marklin mfx style speed table
 * @author Dave Heap Copyright (C) 2013 Changes to fix mfx speed table issue (Vstart {@literal &} Vhigh not written)
 * @author Dave Heap - generate cvList array to incorporate Vstart {@literal &} Vhigh
 *
 */
public class SpeedTableVarValue extends VariableValue implements ChangeListener {

    int nValues;
    int numCvs;
    String[] cvList;
    BoundedRangeModel[] models;
    int _min;
    int _max;
    int _range;
    boolean mfx;

    List<JCheckBox> stepCheckBoxes;

    /**
     * Create the object with a "standard format ctor".
     */
    public SpeedTableVarValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname, int entries, boolean mfxFlag) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);

        nValues = entries;
        _min = minVal;
        _max = maxVal;
        _range = maxVal - minVal;
        mfx = mfxFlag;

        numCvs = nValues;
        cvList = new String[numCvs];

        models = new BoundedRangeModel[nValues];

        // create the set of models
        for (int i = 0; i < nValues; i++) {
            // populate cvList
            cvList[i] = Integer.toString(Integer.parseInt(getCvNum()) + i);
            // create each model
            DefaultBoundedRangeModel j = new DefaultBoundedRangeModel(_range * i / (nValues - 1) + _min, 0, _min, _max);
            models[i] = j;
            // connect each model to CV for notification
            // the connection is to cvNum through cvNum+nValues (28 values total typically)
            // The invoking code (e.g. VariableTableModel) must ensure the CVs exist
            // Note that the default values in the CVs are zero, but are the ramp
            // values here.  We leave that as work item 177, and move on to set the
            // CV states to "FromFile"
            CvValue c = _cvMap.get(cvList[i]);
            c.setValue(_range * i / (nValues - 1) + _min);
            c.addPropertyChangeListener(this);
            c.setState(CvValue.FROMFILE);
        }

        _defaultColor = (new JSlider()).getBackground();
    }

    /**
     * Create a null object. Normally only used for tests and to pre-load
     * classes.
     */
    public SpeedTableVarValue() {
    }

    @Override
    public Object rangeVal() {
        log.warn("rangeVal doesn't make sense for a speed table");
        return "Speed table";
    }

    @Override
    public CvValue[] usesCVs() {
        CvValue[] retval = new CvValue[numCvs];
        int i;
        for (i = 0; i < numCvs; i++) {
            retval[i] = _cvMap.get(cvList[i]);
        }
        return retval;
    }

    /**
     * Called for new values of a slider.
     * <p>
     * Sets the CV(s) as needed.
     *
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        // e.getSource() points to the JSlider object - find it in the list
        JSlider j = (JSlider) e.getSource();
        BoundedRangeModel r = j.getModel();

        for (int i = 0; i < nValues; i++) {
            if (r == models[i]) {
                // found it, and i is useful!
                setModel(i, r.getValue());
                break; // no need to continue loop
            }
        }
        // notify that Value property changed
        prop.firePropertyChange("Value", null, j);
    }

    void setModel(int i, int value) {  // value is _min to _max
        if (value < _min || (mfx && (i == 0))) {
            value = _min;
        }
        if (value > _max || (mfx && (i == nValues - 1))) {
            value = _max;
        }
        if (i < nValues && models[i].getValue() != value) {
            models[i].setValue(value);
        }
        // update the CV
        _cvMap.get(cvList[i]).setValue(value);
        // if programming, that's it
        if (isReading || isWriting) {
            return;
        } else if (i < nValues && !(mfx && (i == 0 || i == (nValues - 1)))) {
            forceMonotonic(i, value);
            matchPoints(i);
        }
    }

    /**
     * Check entries on either side to see if they are set monotonically. If
     * not, adjust.
     *
     * @param modifiedStepIndex number (index) of the entry
     * @param value             new value
     */
    void forceMonotonic(int modifiedStepIndex, int value) {
        // check the neighbors, and force them if needed
        if (modifiedStepIndex > 0) {
            // left neighbour
            if (models[modifiedStepIndex - 1].getValue() > value) {
                setModel(modifiedStepIndex - 1, value);
            }
        }
        if (modifiedStepIndex < nValues - 1) {
            // right neighbour
            if (value > models[modifiedStepIndex + 1].getValue()) {
                setModel(modifiedStepIndex + 1, value);
            }
        }
    }

    /**
     * If there are fixed points specified, set linear step settings to them.
     *
     */
    void matchPoints(int modifiedStepIndex) {
        if (stepCheckBoxes == null) {
            // if no stepCheckBoxes, then GUI not present, and
            // no need to use the matchPoints algorithm
            return;
        }
        if (modifiedStepIndex < 0) {
            log.error("matchPoints called with index too small: " + modifiedStepIndex);
        }
        if (modifiedStepIndex >= stepCheckBoxes.size()) {
            log.error("matchPoints called with index too large: " + modifiedStepIndex
                    + " >= " + stepCheckBoxes.size());
        }
        if (stepCheckBoxes.get(modifiedStepIndex) == null) {
            log.error("matchPoints found null checkbox " + modifiedStepIndex);
        }

        // don't do the match if this step isn't checked,
        // which is necessary to keep from an infinite 
        // recursion
        if (!stepCheckBoxes.get(modifiedStepIndex).isSelected()) {
            return;
        }
        matchPointsLeft(modifiedStepIndex);
        matchPointsRight(modifiedStepIndex);
    }

    void matchPointsLeft(int modifiedStepIndex) {
        // search for checkbox if any
        for (int i = modifiedStepIndex - 1; i >= 0; i--) {
            if (stepCheckBoxes.get(i).isSelected()) {
                // now have two ends to adjust
                int leftval = _cvMap.get(cvList[i]).getValue();
                int rightval = _cvMap.get(cvList[modifiedStepIndex]).getValue();
                int steps = modifiedStepIndex - i;
                log.debug("left found " + leftval + " " + rightval + " " + steps);
                // loop to set values
                for (int j = i + 1; j < modifiedStepIndex; j++) {
                    int newValue = leftval + (rightval - leftval) * (j - i) / steps;
                    log.debug("left set " + j + " to " + newValue);
                    if (_cvMap.get(cvList[j]).getValue() != newValue) {
                        _cvMap.get(cvList[j]).setValue(newValue);
                    }
                }
                return;
            }
        }
        // no match, so don't adjust
        return;
    }

    void matchPointsRight(int modifiedStepIndex) {
        // search for checkbox if any
        for (int i = modifiedStepIndex + 1; i < nValues; i++) { // need at least one intervening point
            if (stepCheckBoxes.get(i).isSelected()) {
                // now have two ends to adjust
                int rightval = _cvMap.get(cvList[i]).getValue();
                int leftval = _cvMap.get(cvList[modifiedStepIndex]).getValue();
                int steps = i - modifiedStepIndex;
                log.debug("right found " + leftval + " " + rightval + " " + steps);
                // loop to set values
                for (int j = modifiedStepIndex + 1; j < i; j++) {
                    int newValue = leftval + (rightval - leftval) * (j - modifiedStepIndex) / steps;
                    log.debug("right set " + j + " to " + newValue);
                    if (_cvMap.get(cvList[j]).getValue() != newValue) {
                        _cvMap.get(cvList[j]).setValue(newValue);
                    }
                }
                return;
            }
        }
        // no match, so don't adjust
        return;
    }

    @Override
    public int getState() {
        int i;
        for (i = 0; i < numCvs; i++) {
            if (_cvMap.get(cvList[i]).getState() == UNKNOWN) {
                return UNKNOWN;
            }
        }
        for (i = 0; i < numCvs; i++) {
            if (_cvMap.get(cvList[i]).getState() == EDITED) {
                return EDITED;
            }
        }
        for (i = 0; i < numCvs; i++) {
            if (_cvMap.get(cvList[i]).getState() == FROMFILE) {
                return FROMFILE;
            }
        }
        for (i = 0; i < numCvs; i++) {
            if (_cvMap.get(cvList[i]).getState() == READ) {
                return READ;
            }
        }
        for (i = 0; i < numCvs; i++) {
            if (_cvMap.get(cvList[i]).getState() == STORED) {
                return STORED;
            }
        }
        log.error("getState did not decode a possible state");
        return UNKNOWN;
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < models.length; i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append(Integer.toString(models[i].getValue()));
        }
        return buf.toString();
    }

    /** 
     * Set value from a String value.
     * <p>
     * Requires the format written by getValueString, not implemented yet
     */
    @Override
    public void setValue(String value) {
        log.debug("skipping setValue in SpeedTableVarValue");
    }

    @Override
    public void setIntValue(int i) {
        log.warn("setIntValue doesn't make sense for a speed table: " + i);
    }

    @Override
    public int getIntValue() {
        log.warn("getValue doesn't make sense for a speed table");
        return 0;
    }

    @Override
    public Object getValueObject() {
        return null;
    }

    @Override
    public Component getCommonRep() {
        log.warn("getValue not implemented yet");
        return new JLabel("speed table");
    }

    public void setValue(int value) {
        log.warn("setValue doesn't make sense for a speed table: " + value);
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        // prop.firePropertyChange("Value", null, null);
    }

    @Override
    public Component getNewRep(String format) {
        final int GRID_Y_BUTTONS = 3;
        // put together a new panel in scroll pane
        JPanel j = new JPanel();

        GridBagLayout g = new GridBagLayout();
        GridBagConstraints cs = new GridBagConstraints();
        j.setLayout(g);

        initStepCheckBoxes();

        for (int i = 0; i < nValues; i++) {
            cs.gridy = 0;
            cs.gridx = i;

            CvValue cv = _cvMap.get(cvList[i]);
            JSlider s = new VarSlider(models[i], cv, i + 1);
            s.setOrientation(JSlider.VERTICAL);
            s.addChangeListener(this);

            int currentState = cv.getState();
            int currentValue = cv.getValue();

            DecVariableValue decVal = new DecVariableValue("val" + i, "", "", false, false, false, false,
                    cvList[i], "VVVVVVVV", _min, _max,
                    _cvMap, _status, "");
            decVal.setValue(currentValue);
            decVal.setState(currentState);

            Component v = decVal.getCommonRep();
            String start = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TextStep")
                    + " " + (i + 1);
            ((JTextField) v).setToolTipText(CvUtil.addCvDescription(start, "CV " + cvList[i], null));
            ((JComponent) v).setBorder(null);  // pack tighter

            if (mfx && (i == 0 || i == (nValues - 1))) {
                ((JTextField) v).setEditable(false); // disable field editing
                s.setEnabled(false);    // disable slider adjustment
            }

            g.setConstraints(v, cs);

            if (i == 0 && log.isDebugEnabled()) {
                log.debug("Font size " + v.getFont().getSize());
            }
            float newSize = v.getFont().getSize() * 0.8f;
            v.setFont(v.getFont().deriveFont(newSize));
            j.add(v);

            cs.gridy++;
            g.setConstraints(s, cs);

            j.add(s);

            cs.gridy++;
            JCheckBox b = stepCheckBoxes.get(i);

            g.setConstraints(b, cs);
            j.add(b, cs);

        }

        // add control buttons
        JPanel k = new JPanel();
        JButton b;
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonForceStraight")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipForceStraight"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doForceStraight(e);
            }
        });
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonMatchEnds")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipMatchEnds"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doMatchEnds(e);
            }
        });
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonConstantRatio")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipConstantRatio"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doRatioCurve(e);
            }
        });
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonLogCurve")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipLogCurve"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doLogCurve(e);
            }
        });
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonShiftLeft")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipShiftLeft"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doShiftLeft(e);
            }
        });
        k.add(b = new JButton(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("ButtonShiftRight")));
        b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipShiftRight"));
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doShiftRight(e);
            }
        });

        cs.gridy = GRID_Y_BUTTONS;
        cs.gridx = 0;
        cs.gridwidth = GridBagConstraints.RELATIVE;
        g.setConstraints(k, cs);

        // add Vstart & Vhigh if applicable
        JPanel l = new JPanel();

        JPanel val = new JPanel();
        val.setLayout(new BorderLayout());
        val.add(j, BorderLayout.NORTH);
        val.add(k, BorderLayout.CENTER);
        if (mfx) {
            val.add(l, BorderLayout.SOUTH);
        }

        updateRepresentation(val);
        return val;

    }

    void initStepCheckBoxes() {
        stepCheckBoxes = new ArrayList<JCheckBox>();
        for (int i = 0; i < nValues; i++) {
            JCheckBox b = new JCheckBox();
            b.setToolTipText(ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TooltipCheckToFix"));
            stepCheckBoxes.add(b);
        }
    }

    /**
     * Set the values to a straight line from _min to _max
     */
    void doForceStraight(java.awt.event.ActionEvent e) {
        _cvMap.get(cvList[0]).setValue(_min);
        _cvMap.get(cvList[nValues - 1]).setValue(_max);
        doMatchEnds(e);
    }

    /**
     * Set the values to a straight line from existing ends
     */
    void doMatchEnds(java.awt.event.ActionEvent e) {
        int first = _cvMap.get(cvList[0]).getValue();
        int last = _cvMap.get(cvList[nValues - 1]).getValue();
        log.debug(" first=" + first + " last=" + last);
        // to avoid repeatedly bumping up later values, push the first one
        // all the way up now
        _cvMap.get(cvList[0]).setValue(last);
        // and push each one down
        for (int i = 0; i < nValues; i++) {
            int value = first + i * (last - first) / (nValues - 1);
            _cvMap.get(cvList[i]).setValue(value);
        }
//         enforceEndPointsMfx();
    }

    /**
     * Set a constant ratio curve
     */
    void doRatioCurve(java.awt.event.ActionEvent e) {
        double first = _cvMap.get(cvList[0]).getValue();
        if (first < 1.) {
            first = 1.;
        }
        double last = _cvMap.get(cvList[nValues - 1]).getValue();
        if (last < first + 1) {
            last = first + 1.;
        }
        double step = Math.log(last / first) / (nValues - 1);
        log.debug("log ratio step is " + step);
        // to avoid repeatedly bumping up later values, push the first one
        // all the way up now
        _cvMap.get(cvList[0]).setValue((int) Math.round(last));
        // and push each one down
        for (int i = 0; i < nValues; i++) {
            int value = (int) (Math.floor(first * Math.exp(step * i)));
            _cvMap.get(cvList[i]).setValue(value);
        }
//         enforceEndPointsMfx();
    }

    /**
     * Set a log curve
     */
    void doLogCurve(java.awt.event.ActionEvent e) {
        double first = _cvMap.get(cvList[0]).getValue();
        double last = _cvMap.get(cvList[nValues - 1]).getValue();
        if (last < first + 1.) {
            last = first + 1.;
        }
        double factor = 1. / 10.;
        // to avoid repeatedly bumping up later values, push the second one
        // all the way up now
        _cvMap.get(cvList[1]).setValue((int) Math.round(last));
        // and push each one down (except the first, left as it was)
        double ratio = Math.pow(1. - factor, nValues - 1.);
        double limit = last + (last - first) * ratio;
        for (int i = 1; i < nValues; i++) {
            double previous = limit - (limit - first) * ratio / Math.pow(1. - factor, nValues - 1. - i);
            int value = (int) (Math.floor(previous));
            _cvMap.get(cvList[i]).setValue(value);
        }
//         enforceEndPointsMfx();
    }

    /**
     * Shift the curve one CV to left. The last entry is left unchanged.
     */
    void doShiftLeft(java.awt.event.ActionEvent e) {
        for (int i = 0; i < nValues - 1; i++) {
            int value = _cvMap.get(cvList[i + 1]).getValue();
            _cvMap.get(cvList[i]).setValue(value);
        }
//         enforceEndPointsMfx();
    }

    /**
     * Shift the curve one CV to right. The first entry is left unchanged.
     */
    void doShiftRight(java.awt.event.ActionEvent e) {
        for (int i = nValues - 1; i > 0; i--) {
            int value = _cvMap.get(cvList[i - 1]).getValue();
            _cvMap.get(cvList[i]).setValue(value);
        }
//         enforceEndPointsMfx();
    }

    /**
     * IDLE if a read/write operation is not in progress. During an operation,
     * it indicates the index of the CV to handle when the current programming
     * operation finishes.
     */
    private int _progState = IDLE;

    private static final int IDLE = -1;
    boolean isReading;
    boolean isWriting;

    /**
     * Count number of retries done
     */
    private int retries = 0;

    /**
     * Define maximum number of retries of read/write operations before moving
     * on
     */
    private static final int RETRY_MAX = 2;

    boolean onlyChanges = false;

    /**
     * Notify the connected CVs of a state change from above
     *
     */
    @Override
    public void setCvState(int state) {
        _cvMap.get(cvList[0]).setState(state);
    }

    @Override
    public boolean isChanged() {
        for (int i = 0; i < numCvs; i++) {
            if (considerChanged(_cvMap.get(cvList[i]))) {
                // this one is changed, return true
                return true;
            }
        }
        return false;
    }

    @Override
    public void readChanges() {
        if (log.isDebugEnabled()) {
            log.debug("readChanges() invoked");
        }
        if (!isChanged()) {
            return;
        }
        onlyChanges = true;
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in read()");
        }
        isReading = true;
        isWriting = false;
        _progState = -1;
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("start series of read operations");
        }
        readNext();
    }

    @Override
    public void writeChanges() {
        if (log.isDebugEnabled()) {
            log.debug("writeChanges() invoked");
        }
        if (!isChanged()) {
            return;
        }
        onlyChanges = true;
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        super.setState(STORED);
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in write()");
        }
        isReading = false;
        isWriting = true;
        _progState = -1;
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("start series of write operations");
        }
        writeNext();
    }

    @Override
    public void readAll() {
        if (log.isDebugEnabled()) {
            log.debug("readAll() invoked");
        }
        onlyChanges = false;
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in read()");
        }
        isReading = true;
        isWriting = false;
        _progState = -1;
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("start series of read operations");
        }
        readNext();
    }

    @Override
    public void writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("writeAll() invoked");
        }
        onlyChanges = false;
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        super.setState(STORED);
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in write()");
        }
        isReading = false;
        isWriting = true;
        _progState = -1;
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("start series of write operations");
        }
        writeNext();
    }

    void readNext() {
        // read operation start/continue
        // check for retry if needed
        if ((_progState >= 0) && (retries < RETRY_MAX)
                && (_cvMap.get(cvList[_progState]).getState() != CvValue.READ)) {
            // need to retry an error; leave progState (CV number) as it was
            retries++;
        } else {
            // normal read operation of next CV
            retries = 0;
            _progState++;  // progState is the index of the CV to handle now
        }

        if (_progState >= numCvs) {
            // done, clean up and return to invoker
            _progState = IDLE;
            isReading = false;
            isWriting = false;
            setBusy(false);
            return;
        }
        // not done, proceed to do the next
        CvValue cv = _cvMap.get(cvList[_progState]);
        int state = cv.getState();
        if (log.isDebugEnabled()) {
            log.debug("invoke CV read index " + _progState + " cv state " + state);
        }
        if (!onlyChanges || considerChanged(cv)) {
            cv.read(_status);
        } else {
            readNext(); // repeat until end
        }
    }

    void writeNext() {
        // write operation start/continue
        // check for retry if needed
        if ((_progState >= 0) && (retries < RETRY_MAX)
                && (_cvMap.get(cvList[_progState]).getState() != CvValue.STORED)) {
            // need to retry an error; leave progState (CV number) as it was
            retries++;
        } else {
            // normal read operation of next CV
            retries = 0;
            _progState++;  // progState is the index of the CV to handle now
        }

        if (_progState >= numCvs) {
            _progState = IDLE;
            isReading = false;
            isWriting = false;
            setBusy(false);
            return;
        }
        CvValue cv = _cvMap.get(cvList[_progState]);
        int state = cv.getState();
        if (log.isDebugEnabled()) {
            log.debug("invoke CV write index " + _progState + " cv state " + state);
        }
        if (!onlyChanges || considerChanged(cv)) {
            cv.write(_status);
        } else {
            writeNext();
        }
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property changed event - name: "
                    + e.getPropertyName());
        }
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive an ongoing programming operation
            // see if actually done

            if (isReading) {
                readNext();
            } else if (isWriting) {
                writeNext();
            } else {
                return;
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = _cvMap.get(cvList[0]);
            if (log.isDebugEnabled()) {
                log.debug("CV State changed to " + cv.getState());
            }
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            // find the CV that sent this
            CvValue cv = (CvValue) e.getSource();
            int value = cv.getValue();
            // find the index of that CV
            for (int i = 0; i < numCvs; i++) {
                if (_cvMap.get(cvList[i]) == cv) {
                    // this is the one, so use this i
                    setModel(i, value);
                    break;
                }
            }
//         enforceEndPointsMfx();
        }
    }

    /* Internal class extends a JSlider so that its color is consistent with
     * an underlying CV; we return one of these in getNewRep.
     * <p>
     * Unlike similar cases elsewhere, this doesn't have to listen to
     * value changes.  Those are handled automagically since we're sharing the same
     * model between this object and others.  And this is listening to
     * a CV state, not a variable.
     *
     * @author   Bob Jacobsen   Copyright (C) 2001
     */
    public class VarSlider extends JSlider {

        VarSlider(BoundedRangeModel m, CvValue var, int step) {
            super(m);
            _var = var;
            // get the original color right
            setBackground(_var.getColor());
            if (_var.getColor() == _var.getDefaultColor()) {
                setOpaque(false);
            } else {
                setOpaque(true);
            }
            // tooltip label
            String start = ResourceBundle.getBundle("jmri.jmrit.symbolicprog.SymbolicProgBundle").getString("TextStep")
                    + " " + step;
            setToolTipText(CvUtil.addCvDescription(start, "CV " + var.number(), null));
            // listen for changes to original state
            _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    originalPropertyChanged(e);
                }
            });
        }

        CvValue _var;

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            if (log.isDebugEnabled()) {
                log.debug("VarSlider saw property change: " + e);
            }
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var.getColor());
                if (_var.getColor() == _var.getDefaultColor()) {
                    setOpaque(false);
                } else {
                    setOpaque(true);
                }
            }
        }

    }  // end class definition

    // clean up connections when done
    @Override
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        // the connection is to cvNum through cvNum+numCvs (28 values typical)
        for (int i = 0; i < numCvs; i++) {
            _cvMap.get(cvList[i]).removePropertyChangeListener(this);
        }

        // do something about the VarSlider objects
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SpeedTableVarValue.class);

}
