package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayDeque;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;

import static jmri.jmrit.symbolicprog.AbstractValue.COLOR_UNKNOWN;
import jmri.util.CvUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends VariableValue to represent a variable split across multiple CVs with
 * values from a pre-selected range each of which is associated with a text name
 * (aka, a drop down)
 * <br>
 * The {@code mask} attribute represents the part of the value that's present in
 * each CV; higher-order bits are loaded to subsequent CVs.<br>
 * It is possible to assign a specific mask for each CV by providing a space
 * separated list of masks, starting with the lowest, and matching the order of
 * CVs
 * <br><br>
 * The original use was for addresses of stationary (accessory) decoders.
 * <br>
 * The original version only allowed two CVs, with the second CV specified by
 * the attributes {@code highCV} and {@code upperMask}.
 * <br><br>
 * The preferred technique is now to specify all CVs in the {@code CV} attribute
 * alone, as documented at {@link CvUtil#expandCvList expandCvList(String)}.
 * <br><br>
 * Optional attributes {@code factor} and {@code offset} are applied when going
 * <i>from</i> the variable value <i>to</i> the CV values, or vice-versa:
 * <pre>
 * Value to put in CVs = ((value in text field) -{@code offset})/{@code factor}
 * Value to put in text field = ((value in CVs) *{@code factor}) +{@code offset}
 * </pre>
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2004, 2013
 * @author Dave Heap Copyright (C) 2016, 2019
 * @author Egbert Broerse Copyright (C) 2020
 * @author Jordan McBride Copyright (C) 2021
 */
public class SplitEnumVariableValue extends VariableValue
        implements ActionListener, FocusListener {

    private static final int RETRY_COUNT = 2;
    
    int atest = 1;
    private final List<JTree> trees = new ArrayList<>();
    
    private final List<ComboCheckBox> comboCBs = new ArrayList<>();
    private final List<SplitEnumVariableValue.VarComboBox> comboVars = new ArrayList<>();
    private final List<ComboRadioButtons> comboRBs = new ArrayList<>();
    

    public SplitEnumVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _minVal = 0;
        _maxVal = ~0;
        stepOneActions(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        _name = name;
        _mask = mask; // will be converted to MaskArray to apply separate mask for each CV
        if (mask != null && mask.contains(" ")) {
            _maskArray = mask.split(" "); // type accepts multiple masks for SplitVariableValue
        } else {
            _maskArray = new String[1];
            _maskArray[0] = mask;
        }
        _cvNum = cvNum;
        mFactor = pFactor;
        mOffset = pOffset;
        // legacy format variables
        mSecondCV = pSecondCV;
        _uppermask = uppermask;


        log.debug("Variable={};comment={};cvName={};cvNum={};stdname={}", _name, comment, cvName, _cvNum, stdname);

        // upper bit offset includes lower bit offset, and MSB bits missing from upper part
        log.debug("Variable={}; upper mask {} had offsetVal={} so upperbitoffset={}", _name, _uppermask, offsetVal(_uppermask), offsetVal(_uppermask));

        // set up array of used CVs
        cvList = new ArrayList<>();

        List<String> nameList = CvUtil.expandCvList(_cvNum); // see if cvName needs expanding
        if (nameList.isEmpty()) {
            // primary CV
            String tMask;
            if (_maskArray != null && _maskArray.length == 1) {
                log.debug("PrimaryCV mask={}", _maskArray[0]);
                tMask = _maskArray[0];
            } else {
                tMask = _mask; // mask supplied could be an empty string
            }
            cvList.add(new CvItem(_cvNum, tMask));

            if (pSecondCV != null && !pSecondCV.equals("")) {
                cvList.add(new CvItem(pSecondCV, _uppermask));
            }
        } else {
            for (int i = 0; i < nameList.size(); i++) {
                cvList.add(new CvItem(nameList.get(i), _maskArray[Math.min(i, _maskArray.length - 1)]));
                // use last mask for all following CVs if fewer masks than the number of CVs listed were provided
                log.debug("Added mask #{}: {}", i, _maskArray[Math.min(i, _maskArray.length - 1)]);
            }
        }

        cvCount = cvList.size();

        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).startOffset = currentOffset;
            String t = cvList.get(i).cvMask;
            if (t.contains("V")) {
                currentOffset = currentOffset + t.lastIndexOf("V") - t.indexOf("V") + 1;
            } else {
                log.error("Variable={};cvName={};cvMask={} is an invalid bitmask", _name, cvList.get(i).cvName, cvList.get(i).cvMask);
            }
            log.debug("Variable={};cvName={};cvMask={};startOffset={};currentOffset={}", _name, cvList.get(i).cvName, cvList.get(i).cvMask, cvList.get(i).startOffset, currentOffset);

            // connect CV for notification
            CvValue cv = _cvMap.get(cvList.get(i).cvName);
            cvList.get(i).thisCV = cv;
        }

        stepTwoActions();


        // have to do when list is complete
        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).thisCV.addPropertyChangeListener(this);
            cvList.get(i).thisCV.setState(CvValue.FROMFILE);
        }
        treeNodes.addLast(new DefaultMutableTreeNode(""));
    }

    /**
     * Subclasses can override this to pick up constructor-specific attributes
     * and perform other actions before cvList has been built.
     *
     * @param name      name.
     * @param comment   comment.
     * @param cvName    cv name.
     * @param readOnly  true for read only, else false.
     * @param infoOnly  true for info only, else false.
     * @param writeOnly true for write only, else false.
     * @param opsOnly   true for ops only, else false.
     * @param cvNum     cv number.
     * @param mask      cv mask.
     * @param minVal    minimum value.
     * @param maxVal    maximum value.
     * @param v         hashmap of string and cv value.
     * @param status    status.
     * @param stdname   std name.
     * @param pSecondCV second cv (no longer preferred, specify in cv)
     * @param pFactor   factor.
     * @param pOffset   offset.
     * @param uppermask upper mask (no longer preferred, specify in mask)
     * @param extra1    extra 1.
     * @param extra2    extra 2.
     * @param extra3    extra 3.
     * @param extra4    extra 4.
     */
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        if (extra3 != null) {
            _minVal = getValueFromText(extra3);
        }
        if (extra4 != null) {
            _maxVal = getValueFromText(extra4);
        }
    }
    
    public void nItems(int n) {
        _itemArray = new String[n];
        _pathArray = new TreePath[n];
        _valueArray = new int[n];
        _nstored = 0;
        log.debug("enumeration arrays size={}", n);
    }
    
        /**
     * Create a new item in the enumeration, with an associated value one more
     * than the last item (or zero if this is the first one added)
     *
     * @param s Name of the enumeration item
     */
    public void addItem(String s) {
        if (_nstored == 0) {
            addItem(s, 0);
        } else {
            addItem(s, _valueArray[_nstored - 1] + 1);
        }
    }

    public void addItem(String s, int value) {
        _valueArray[_nstored] = value;
        SplitEnumVariableValue.TreeLeafNode node = new SplitEnumVariableValue.TreeLeafNode(s, _nstored);
        treeNodes.getLast().add(node);
        _pathArray[_nstored] = new TreePath(node.getPath());
        _itemArray[_nstored++] = s;
        log.debug("_itemArray.length={},_nstored={},s='{}',value={}", _itemArray.length, _nstored, s, value);
    }

    public void startGroup(String name) {
        DefaultMutableTreeNode next = new DefaultMutableTreeNode(name);
        treeNodes.getLast().add(next);
        treeNodes.addLast(next);
    }

    public void endGroup() {
        treeNodes.removeLast();
    }
    
    public void lastItem() {
        _value = new JComboBox<>(java.util.Arrays.copyOf(_itemArray, _nstored));
        // finish initialization
        _value.setActionCommand("");
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        _value.setOpaque(true);
        // connect to the JComboBox model and the CV so we'll see changes.
        _value.addActionListener(this);
        CvValue cv1 = cvList.get(0).thisCV;
        CvValue cv2 = cvList.get(1).thisCV;
        if (cv1 == null || cv2 == null) {
            log.error("no CV defined in enumVal {}, skipping setState", getCvName());
            return;
        }
        cv1.addPropertyChangeListener(this);
        cv1.setState(CvValue.FROMFILE);
        cv2.addPropertyChangeListener(this);
        cv2.setState(CvValue.FROMFILE);
    }
    
    
    
    @Override
    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }
        // stored value
    JComboBox<String> _value = null;

    // place to keep the items & associated numbers
    private String[] _itemArray = null;
    private TreePath[] _pathArray = null;
    private int[] _valueArray = null;
    private int _nstored;

    Deque<DefaultMutableTreeNode> treeNodes = new ArrayDeque<>();
    
    /**
     * Subclasses can override this to invoke further actions after cvList has
     * been built.
     */
    public void stepTwoActions() {
        if (currentOffset > bitCount) {
            String eol = System.getProperty("line.separator");
            throw new Error(
                    "Decoder File parsing error:"
                    + eol + "The Decoder Definition File specified \"" + _cvNum
                    + "\" for variable \"" + _name + "\". This expands to:"
                    + eol + "\"" + getCvDescription() + "\""
                    + eol + "This requires " + currentOffset + " bits, which exceeds the " + bitCount
                    + " bit capacity of the long integer used to store the variable."
                    + eol + "The Decoder Definition File needs correction.");
        }
        _columns = cvCount * 2; //update column width now we have a better idea
    }

    @Override
    public void setAvailable(boolean a) {
        _value.setVisible(a);
        for (ComboCheckBox c : comboCBs) {
            c.setVisible(a);
        }
        for (SplitEnumVariableValue.VarComboBox c : comboVars) {
            c.setVisible(a);
        }
        for (ComboRadioButtons c : comboRBs) {
            c.setVisible(a);
        }
        super.setAvailable(a);
    }
    
    /**
     * Simple request getter for the CVs composing this variable
     * <br>
     * @return Array of CvValue for all of associated CVs
     */
    @Override
    public CvValue[] usesCVs() {
        CvValue[] theseCvs = new CvValue[cvCount];
        for (int i = 0; i < cvCount; i++) {
            theseCvs[i] = cvList.get(i).thisCV;
        }
        return theseCvs;
    }

    /**
     * Multiple masks can be defined for the CVs accessed by this variable.
     * <br>
     * Actual individual masks are returned in
     * {@link #getCvDescription getCvDescription()}.
     *
     * @return The legacy two-CV mask if {@code highCV} is specified.
     * <br>
     * The {@code mask} if {@code highCV} is not specified.
     */
    @Override
    public String getMask() {
        if (mSecondCV != null && !mSecondCV.equals("")) {
            return _uppermask + _mask;
        } else {
            return _mask; // a list of 1-n masks, separated by spaces
        }
    }

    /**
     * Access a specific mask, used in tests
     *
     * @param i index of CV in variable
     * @return a single mask as string in the form XXXXVVVV, or empty string if
     *         index out of bounds
     */
    protected String getMask(int i) {
        if (i < cvCount) {
            return cvList.get(i).cvMask;
        }
        return "";
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     * <br>
     * Actual individual masks are added to CVs if more are present.
     *
     * @return A user-friendly CV(s) and bitmask(s) description
     */
    @Override
    public String getCvDescription() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < cvCount; i++) {
            if (buf.length() > 0) {
                buf.append(" & ");
            }
            buf.append("CV");
            buf.append(cvList.get(i).cvName);
            String temp = CvUtil.getMaskDescription(cvList.get(i).cvMask);
            if (temp.length() > 0) {
                buf.append(" ");
                buf.append(temp);
            }
        }
        buf.append("."); // mark that mask descriptions are already inserted for CvUtil.addCvDescription
        return buf.toString();
    }

    String mSecondCV;
    String _uppermask;
    int mFactor;
    int mOffset;
    String _name;
    String _mask; // full string as provided, use _maskArray to access one of multiple masks
    String[] _maskArray = new String[0];
    String _cvNum;

    List<CvItem> cvList;

    int cvCount = 0;
    int currentOffset = 0;

    /**
     * Get the first CV from the set used to define this variable
     * <br>
     * @return The legacy two-CV mask if {@code highCV} is specified.
     */
    @Override
    public String getCvNum() {
        String retString = "";
        if (cvCount > 0) {
            retString = cvList.get(0).cvName;
        }
        return retString;
    }

    @Deprecated
    public String getSecondCvNum() {
        String retString = "";
        if (cvCount > 1) {
            retString = cvList.get(1).cvName;
        }
        return retString;
    }

    long _minVal;
    long _maxVal;

    @Override
    public Object rangeVal() {
        return "Split value";
    }

    String oldContents = "0";

    long getValueFromText(String s) {
        return (Long.parseUnsignedLong(s));
    }

    String getTextFromValue(long v) {
        return (Long.toUnsignedString(v));
    }

    /*
    int[] getCvValsFromTextField() {
        long newEntry;  // entered value
        try {
            newEntry = Long.parseLong((String)_value.getSelectedItem());
        } catch (java.lang.NumberFormatException ex) {
            newEntry = 0;
        }

        // calculate resulting number
        long newVal = (newEntry - mOffset) / mFactor;
        log.debug("Variable={};newEntry={};newVal={} with Offset={} + Factor={} applied", _name, newEntry, newVal, mOffset, mFactor);

        int[] retVals = new int[cvCount];

        // extract individual values via masks
        for (int i = 0; i < cvCount; i++) {
            retVals[i] = (((int) (newVal >>> cvList.get(i).startOffset))
                    & (maskValAsInt(cvList.get(i).cvMask) >>> offsetVal(cvList.get(i).cvMask)));
        }
        return retVals;
    }
    */
    
    /**
     * Contains numeric-value specific code.
     * <br><br>
     * Calculates new value for _enumField and invokes
     * {@link #setLongValue(long) setLongValue(newVal)} to make and notify the
     * change
     *
     * @param intVals array of new CV values
     */
    void updateVariableValue(int[] intVals) {
        if (intVals.length > 0){
            long newVal = 0;
            for (int i = 0; i < intVals.length; i++) {
                newVal = newVal | (((long) intVals[i]) << cvList.get(i).startOffset);
                log.debug("Variable={}; i={}; newVal={}", _name, i, getTextFromValue(newVal));
            }
            log.debug("Variable={}; set value to {}", _name, newVal);
            setLongValue(newVal);  // check for duplicate is done inside setLongValue
            log.debug("Variable={}; in property change after setValue call", _name);
        }
    }

    /**
     * Saves selected item from _value (enumField) to oldContents.
     */
    void enterField() {
        oldContents =  String.valueOf(_value.getSelectedItem());
        log.debug("enterField sets oldContents to {}", oldContents);
    }

    /**
     * Contains numeric-value specific code.
     * <br>
     * firePropertyChange for "Value" with new and old contents of _enumField
     */
    void exitField(){
        // there may be a lost focus event left in the queue when disposed so protect
        if (_value != null && !oldContents.equals(_value.getSelectedItem())) {
            long newFieldVal = 0;
            try {
                newFieldVal = Long.parseLong((String)_value.getSelectedItem());
            } catch (NumberFormatException e) {
                //_value.setText(oldContents);
            }
            log.debug("_minVal={};_maxVal={};newFieldVal={}",
                    Long.toUnsignedString(_minVal), Long.toUnsignedString(_maxVal), Long.toUnsignedString(newFieldVal));
            if (Long.compareUnsigned(newFieldVal, _minVal) < 0 || Long.compareUnsigned(newFieldVal, _maxVal) > 0) {
            
            } else {
                long newVal = (newFieldVal - mOffset) / mFactor;
                long oldVal = (getValueFromText(oldContents) - mOffset) / mFactor;
                prop.firePropertyChange("Value", oldVal, newVal);
            }
        }
    }

    boolean _fieldShrink = false;
    
    /*
    @Override
    void updatedTextField() {
        //log.debug("Variable='{}'; enter updatedTextField in {} with TextField='{}'", _name, (this.getClass().getSimpleName()), _textField.getText());
        // called for new values in text field - set the CVs as needed

        int[] retVals = getCvValsFromTextField();

        // combine with existing values via mask
        for (int j = 0; j < cvCount; j++) {
            int i = j;
            // special care needed if _textField is shrinking
            if (_fieldShrink) {
                i = (cvCount - 1) - j; // reverse CV updating order
            }
            log.debug("retVals[{}]={};cvList.get({}).cvMask{};offsetVal={}", i, retVals[i], i, cvList.get(i).cvMask, offsetVal(cvList.get(i).cvMask));
            int cvMask = maskValAsInt(cvList.get(i).cvMask);
            CvValue thisCV = cvList.get(i).thisCV;
            int oldCvVal = thisCV.getValue();
            int newCvVal = (oldCvVal & ~cvMask)
                    | ((retVals[i] << offsetVal(cvList.get(i).cvMask)) & cvMask);
            log.debug("{};cvMask={};oldCvVal={};retVals[{}]={};newCvVal={}", cvList.get(i).cvName, cvMask, oldCvVal, i, retVals[i], newCvVal);

            // cv updates here trigger updated property changes, which means
            // we're going to get notified sooner or later.
            if (newCvVal != oldCvVal) {
                thisCV.setValue(newCvVal);
            }
        }
        log.debug("Variable={}; exit updatedTextField", _name);
    }
    */
    
    void updatedDropDown() {
        log.debug("Variable='{}'; enter updatedDropDown in {} with DropDownValue='{}'", _name, (this.getClass().getSimpleName()), _value);
        // called for new values in text field - set the CVs as needed

        int[] retVals = getCvValsFromSingleInt(getIntValue());

        // combine with existing values via mask
        for (int j = 0; j < cvCount; j++) {
            int i = j;
            log.debug("retVals[{}]={};cvList.get({}).cvMask{};offsetVal={}", i, retVals[i], i, cvList.get(i).cvMask, offsetVal(cvList.get(i).cvMask));
            int cvMask = maskValAsInt(cvList.get(i).cvMask);
            CvValue thisCV = cvList.get(i).thisCV;
            int oldCvVal = thisCV.getValue();
            int newCvVal = (oldCvVal & ~cvMask)
                    | ((retVals[i] << offsetVal(cvList.get(i).cvMask)) & cvMask);
            log.debug("{};cvMask={};oldCvVal={};retVals[{}]={};newCvVal={}", cvList.get(i).cvName, cvMask, oldCvVal, i, retVals[i], newCvVal);

            // cv updates here trigger updated property changes, which means
            // we're going to get notified sooner or later.
            if (newCvVal != oldCvVal) {
                thisCV.setValue(newCvVal);
            }
        }
        log.debug("Variable={}; exit updatedDropDown", _name);
    }
    
    int[] getCvValsFromSingleInt(long newEntry) {
        // calculate resulting number
        long newVal = (newEntry - mOffset) / mFactor;
        log.debug("Variable={};newEntry={};newVal={} with Offset={} + Factor={} applied", _name, newEntry, newVal, mOffset, mFactor);

        int[] retVals = new int[cvCount];

        // extract individual values via masks
        for (int i = 0; i < cvCount; i++) {
            retVals[i] = (((int) (newVal >>> cvList.get(i).startOffset))
                    & (maskValAsInt(cvList.get(i).cvMask) >>> offsetVal(cvList.get(i).cvMask)));
        }
        return retVals;
    }
    
    /**
     * ActionListener implementation.
     * <p>
     * Invokes {@link #exitField exitField()}
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (e != null){
            if (log.isDebugEnabled()) {
                log.debug("{} start action event: {}", label(), e);
            }
            if (!(e.getActionCommand().equals(""))) {
                // is from alternate rep
                _value.setSelectedItem(e.getActionCommand());
                if (log.isDebugEnabled()) {
                    log.debug("{} action event was from alternate rep", label());
                }
                // match and select in tree
                if (_nstored > 0) {
                    for (int i = 0; i < _nstored; i++) {
                        if (e.getActionCommand().equals(_itemArray[i])) {
                            // now select in the tree
                            TreePath path = _pathArray[i];
                            for (JTree tree : trees) {
                                tree.setSelectionPath(path);
                                // ensure selection is in visible portion of JScrollPane
                                tree.scrollPathToVisible(path);
                            }
                            break; // first one is enough
                        }
                    }
                }
            }

            int oldVal = getIntValue();

            // called for new values - set the CV as needed
            CvValue cv = _cvMap.get(getCvNum());
            if (cv == null) {
                log.error("no CV defined in enumVal {}, skipping setValue", _cvMap.get(getCvName()));
                return;
            }

            int oldCv = cv.getValue();
            int newVal = getIntValue();
            int max = (int)_maxVal;
            int newCv = setValueInCV(oldCv, newVal, getMask(), max-1);
            if (newCv != oldCv) {
                cv.setValue(newCv);  // to prevent CV going EDITED during loading of decoder file

                // notify  (this used to be before setting the values)
                log.debug("{} about to firePropertyChange", label());
                prop.firePropertyChange("Value", null, oldVal);
                log.debug("{} returned to from firePropertyChange", label());
            }
            log.debug("{} end action event saw oldCv={} newVal={} newCv={}", label(), oldCv, newVal, newCv);
        }
        exitField();
    }

    /**
     * FocusListener implementations.
     */
    @Override
    public void focusGained(FocusEvent e) {
        log.debug("Variable={}; focusGained", _name);
        enterField();
    }

    @Override
    public void focusLost(FocusEvent e) {
        log.debug("Variable={}; focusLost", _name);
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        return Integer.toString(getIntValue());
    }

    /**
     * Set value from a String value.
     *
     * @param value a string representing the Long value to be set
     */
    public void setValue(int value) {
        if(value > 0){
            try {
                long longVal = (long)value;
                long val = longVal;
                setLongValue(val);
            } catch (NumberFormatException e) {
                log.warn("skipping set of non-long value \"{}\"", value);
            }
            selectValue(value);
        }
    }

    @Override
    public void setIntValue(int i) {
        setLongValue(i);
    }

    @Override
    public int getIntValue() {
        if (_value.getSelectedIndex() >= _valueArray.length || _value.getSelectedIndex() < 0) {
            log.error("trying to get value {} too large for array length {} in var {}", _value.getSelectedIndex(), _valueArray.length, label());
        }
        log.debug("SelectedIndex={}", _value.getSelectedIndex());
        return _valueArray[_value.getSelectedIndex()];
    }

    /**
     * Get the value as an unsigned long.
     *
     * @return the value as a long
     */
    @Override
    public long getLongValue() {
        return (long) _valueArray[_value.getSelectedIndex()];
    }

    @Override
    public String getTextValue() {
        if (_value.getSelectedItem() != null) {
            return _value.getSelectedItem().toString();
        } else {
            return "";
        }
    }

    @Override
    public Object getValueObject() {
        return getLongValue();
    }

    @Override
    public Component getCommonRep() {
        if (getReadOnly()) {
            JLabel r = new JLabel((String)_value.getSelectedItem());
            updateRepresentation(r);
            return r;
        } else {
            return _value;
        }
    }

    public void setLongValue(long value) {
        log.debug("Variable={}; enter setLongValue {}", _name, value);
        long oldVal;
        int indexOfSelected;
        try {
            oldVal = (Long.parseLong((String)_value.getSelectedItem()) - mOffset) / mFactor;
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        log.debug("Variable={}; setValue with new value {} old value {}", _name, value, oldVal);
            // Get number of items
        int num = _value.getItemCount();

        // Get items
        ComboBoxModel completeObject = _value.getModel();
        int lengthOfArray = this._valueArray.length;
        
        for (int i = 0; i < lengthOfArray; i++) {
          if(this._valueArray[i] == value){
              _value.setSelectedIndex(i);
          }
        }
       /*         
        for (int i = 0; i < num; i++) {
          Object item = _value.getItemAt(i);
          if(Long.parseLong((String)item) == value){
              _value.setSelectedIndex(i);
          }
        }
        */
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        // TODO PENDING: the code used to fire value * mFactor + mOffset, which is a text representation;
        // but 'oldValue' was converted back using mOffset / mFactor making those two (new / old)
        // using different scales. Probably a bug, but it has been there from well before
        // the extended splitVal. Because of the risk of breaking existing
        // behaviour somewhere, deferring correction until at least the next test release.
        prop.firePropertyChange("Value", oldVal, value * mFactor + mOffset);
        log.debug("Variable={}; exit setLongValue old={} new={}", _name, oldVal, value);
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        if (c != null && _value != null) {
            _value.setBackground(c);
            log.debug("Variable={}; Set Color to {}", _name, c.toString());
        } else if (_value != null) {
            log.debug("Variable={}; Set Color to defaultColor {}", _name, _defaultColor.toString());
            _value.setBackground(_defaultColor);
        }
        
        // prop.firePropertyChange("Value", null, null);
    }

    int _columns = 1;

    
       @Override
    public Component getNewRep(String format) {
        // sort on format type
        switch (format) {
            case "tree":
                DefaultTreeModel dModel = new DefaultTreeModel(treeNodes.getFirst());
                JTree dTree = new JTree(dModel);
                trees.add(dTree);
                JScrollPane dScroll = new JScrollPane(dTree);
                dTree.setRootVisible(false);
                dTree.setShowsRootHandles(true);
                dTree.setScrollsOnExpand(true);
                dTree.setExpandsSelectedPaths(true);
                dTree.getSelectionModel().setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
                // arrange for only leaf nodes can be selected
                dTree.addTreeSelectionListener(new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath[] paths = e.getPaths();
                        for (TreePath path : paths) {
                            DefaultMutableTreeNode o = (DefaultMutableTreeNode) path.getLastPathComponent();
                            if (o.getChildCount() > 0) {
                                ((JTree) e.getSource()).removeSelectionPath(path);
                            }
                        }
                        // now record selection
                        if (paths.length >= 1) {
                            if (paths[0].getLastPathComponent() instanceof SplitEnumVariableValue.TreeLeafNode) {
                                // update value of Variable
                                setValue(_valueArray[((SplitEnumVariableValue.TreeLeafNode) paths[0].getLastPathComponent()).index]);
                            }
                        }
                    }
                });
                // select initial value
                TreePath path = _pathArray[_value.getSelectedIndex()];
                dTree.setSelectionPath(path);
                // ensure selection is in visible portion of JScrollPane
                dTree.scrollPathToVisible(path);

                if (getReadOnly() || getInfoOnly()) {
                    log.error("read only variables cannot use tree format: {}", item());
                }
                updateRepresentation(dScroll);
                return dScroll;
            default: {
                // return a new JComboBox representing the same model
                SplitEnumVariableValue.VarComboBox b = new SplitEnumVariableValue.VarComboBox(_value.getModel(), this);
                comboVars.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
        }
    }   
    
    protected void selectValue(int value) {
        if (_nstored > 0 && value != 0) {
            for (int i = 0; i < _nstored; i++) {
                if (_valueArray[i] == value) {
                    //found it, select it
                    _value.setSelectedIndex(i);

                    // now select in the tree
                    TreePath path = _pathArray[i];
                    for (JTree tree : trees) {
                        tree.setSelectionPath(path);
                        // ensure selection is in visible portion of JScrollPane
                        tree.scrollPathToVisible(path);
                    }
                    return;
                }
            }
        }
    }

    java.util.List<Component> reps = new java.util.ArrayList<>();

    public int retry = 0;
    int _progState = 0;
    static final int IDLE = 0;
    static final int READING_FIRST = 1;
    static final int WRITING_FIRST = -1;
    static final int bitCount = Long.bitCount(~0);
    static final long intMask = Integer.toUnsignedLong(~0);

    /**
     * Notify the connected CVs of a state change from above
     *
     * @param state The new state
     */
    @Override
    public void setCvState(int state) {
        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).thisCV.setState(state);
        }
    }

    @Override
    public boolean isChanged() {
        boolean changed = false;
        for (int i = 0; i < cvCount; i++) {
            changed = (changed || considerChanged(cvList.get(i).thisCV));
        }
        return changed;
    }

    @Override
    public boolean isToRead() {
        boolean toRead = false;
        for (int i = 0; i < cvCount; i++) {
            toRead = (toRead || (cvList.get(i).thisCV).isToRead());
        }
        return toRead;
    }

    @Override
    public boolean isToWrite() {
        boolean toWrite = false;
        for (int i = 0; i < cvCount; i++) {
            toWrite = (toWrite || (cvList.get(i).thisCV).isToWrite());
        }
        return toWrite;
    }

    @Override
    public void readChanges() {
        if (isToRead() && !isChanged()) {
            log.debug("!!!!!!! unacceptable combination in readChanges: {}", label());
        }
        if (isChanged() || isToRead()) {
            readAll();
        }
    }

    @Override
    public void writeChanges() {
        if (isToWrite() && !isChanged()) {
            log.debug("!!!!!! unacceptable combination in writeChanges: {}", label());
        }
        if (isChanged() || isToWrite()) {
            writeAll();
        }
    }

    @Override
    public void readAll() {
        log.debug("Variable={}; splitVal read() invoked", _name);
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        //super.setState(READ);
        //_value.setSelectedIndex(0); // start with a clean slate
        for (int i = 0; i < cvCount; i++) { // mark all Cvs as unknown otherwise problems occur
            cvList.get(i).thisCV.setState(AbstractValue.UNKNOWN);
        }
        //super.setState(READING_FIRST);
        _progState = READING_FIRST;
        retry = 0;
        log.info("Variable={}; Start CV read", _name);
        log.info("Reading CV={}", cvList.get(0).cvName);
        (cvList.get(0).thisCV).read(_status); // kick off the read sequence
    }

    @Override
    public void writeAll() {
        log.debug("Variable={}; write() invoked", _name);
        if (getReadOnly()) {
            log.error("Variable={}; unexpected write operation when readOnly is set", _name);
        }
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Variable={}; Programming state {}, not IDLE, in write()", _name, _progState);
        }
        _progState = WRITING_FIRST;
        log.info("Variable={}; Start CV write", _name);
        log.info("Writing CV={}", cvList.get(0).cvName);
        (cvList.get(0).thisCV).write(_status); // kick off the write sequence
    }

    /**
     * Assigns a priority value to a given state.
     *
     * @param state State to be converted to a priority value
     * @return Priority value from state, with UNKNOWN numerically highest
     */
    @SuppressFBWarnings(value = {"SF_SWITCH_NO_DEFAULT", "SF_SWITCH_FALLTHROUGH"}, justification = "Intentional fallthrough to produce correct value")
    int priorityValue(int state) {
        int value = 0;
        switch (state) {
            case AbstractValue.UNKNOWN:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.DIFF:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.EDITED:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.FROMFILE:
                value++;
            //$FALL-THROUGH$
            default:
                //$FALL-THROUGH$
                return value;
        }
    }

       // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        switch (e.getPropertyName()) {
            case "Busy":
                if (((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                    if ( 0 >= _progState){
                        setToRead(false);
                        setToWrite(false);  // some programming operation just finished
                        setBusy(false);
                    }
                    if (_progState >= READING_FIRST){
                        int curState = (cvList.get(Math.abs(_progState) - 1).thisCV).getState();
                        if (curState == READ) {   // was the last read successful?
                            retry = 0;
                            if (Math.abs(_progState) < cvCount) {   // read next CV
                                _progState++;
                                log.info("Reading CV={}", cvList.get(Math.abs(_progState) - 1).cvName);
                                (cvList.get(Math.abs(_progState) - 1).thisCV).read(_status);
                            } else {  // finally done, set not busy
                                log.info("Variable={}; Busy goes false with success READING _progState {}", _name, _progState);
                                _progState = IDLE;
                                setToRead(false);
                                setBusy(false);
                            }
                        } else {   // read failed
                            log.info("Variable={}; Busy goes false with failure READING _progState {}", _name, _progState);
                            if (retry < RETRY_COUNT) { //have we exhausted retry count?
                                retry++;
                                _progState++;
                                (cvList.get(Math.abs(_progState) - 1).thisCV).read(_status);
                            } else {
                                _progState = IDLE;
                                setBusy(false);
                                if (RETRY_COUNT > 0) {
                                    for (int i = 0; i < cvCount; i++) { // mark all CVs as unknown otherwise problems may occur
                                        cvList.get(i).thisCV.setState(AbstractValue.UNKNOWN);
                                    }
                                }
                            }
                        }
                    } else  if (_progState <= WRITING_FIRST) {  // writing CVs
                        if ((cvList.get(Math.abs(_progState) - 1).thisCV).getState() == STORED) {   // was the last read successful?
                            if (Math.abs(_progState) < cvCount) {   // write next CV
                                _progState--;
                                log.info("Writing CV={}", cvList.get(Math.abs(_progState) - 1).cvName);
                                (cvList.get(Math.abs(_progState) - 1).thisCV).write(_status);
                            } else {  // finally done, set not busy
                                log.info("Variable={}; Busy goes false with success WRITING _progState {}", _name, _progState);
                                _progState = IDLE;
                                setBusy(false);
                                setToWrite(false);
                            }
                        } else {   // read failed we're done!
                            log.info("Variable={}; Busy goes false with failure WRITING _progState {}", _name, _progState);
                            _progState = IDLE;
                            setBusy(false);
                        }
                    }
                }
                break;
            case "State": {
                log.info("Possible {} variable state change due to CV state change, so propagate that", _name);
                int varState = getState(); // AbstractValue.SAME;
                log.info("{} variable state was {}", _name, stateNameFromValue(varState));
                for (int i = 0; i < cvCount; i++) {
                    int state = cvList.get(i).thisCV.getState();
                    if (i == 0) {
                        varState = state;
                    } else if (priorityValue(state) > priorityValue(varState)) {
                        varState = AbstractValue.UNKNOWN; // or should it be = state ?
//                        varState = state; // or should it be = state ?
                    }
                }
                setState(varState);
                for (JTree tree : trees) {
                    tree.setBackground(_value.getBackground());
                    //tree.setOpaque(true);
                }
                log.info("{} variable state set to {}", _name, stateNameFromValue(varState));
                break;
            }
            case "Value": {
                // update value of Variable

                //setLongValue(Long.parseLong((String)_value.getSelectedItem()));  // check for duplicate done inside setValue
                log.info("update value of Variable {}", _name);

                int[] intVals = new int[cvCount];

                for (int i = 0; i < cvCount; i++) {
                    intVals[i] = (cvList.get(i).thisCV.getValue() & maskValAsInt(cvList.get(i).cvMask)) >>> offsetVal(cvList.get(i).cvMask);
                }

                updateVariableValue(intVals);

                log.info("state change due to CV value change, so propagate that");
                int varState = AbstractValue.SAME;
                for (int i = 0; i < cvCount; i++) {
                    int state = cvList.get(i).thisCV.getState();
                    if (priorityValue(state) > priorityValue(varState)) {
                        varState = state;
                    }
                }
                setState(varState);
                
                int intMax = (int)_maxVal;
                CvValue cv = _cvMap.get(getCvNum());
                String currentCVNum = getCvNum();
                int cvVal = cv.getValue();
                int newVal = getValueInCV(cv.getValue(), getMask(), intMax-1); // _maxVal value is count of possibles, i.e. radix
                setValue(newVal);
                break;
            }
            default:
                break;
        }
    }

    /* Internal class extends a JComboBox so that its color is consistent with
     * an underlying variable
     *
     * @author Bob Jacobsen   Copyright (C) 2001
     * @author tweaked by Jordan McBride Copyright (C) 2021
     *
     */
    public static class VarComboBox extends JComboBox<String> {

        VarComboBox(ComboBoxModel<String> m, SplitEnumVariableValue var) {
            super(m);
            _var = var;
            _l = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                        log.debug("VarComboBox saw property change: {}", e);
                    originalPropertyChanged(e);
                }
            };
            // get the original color right
            setBackground(_var._value.getBackground());
            setOpaque(true);
            // listen for changes to original state
            _var.addPropertyChangeListener(_l);
        }

        SplitEnumVariableValue _var;
        transient java.beans.PropertyChangeListener _l = null;

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
                setOpaque(true);
            }
        }

        public void dispose() {
            if (_var != null && _l != null) {
                _var.removePropertyChangeListener(_l);
            }
            _l = null;
            _var = null;
        }
    }

    /**
     * Class to hold CV parameters for CVs used.
     */
    static class CvItem {

        // class fields
        String cvName;
        String cvMask;
        int startOffset;
        CvValue thisCV;

        CvItem(String cvNameVal, String cvMaskVal) {
            cvName = cvNameVal;
            cvMask = cvMaskVal;
        }
    }

// clean up connections when done
    @Override
    public void dispose() {
        log.debug("dispose");

        // remove connection to CV
        if (_cvMap.get(getCvNum()) == null) {
            log.error("no CV defined for variable {}, no listeners to remove", getCvNum());
        } else {
            _cvMap.get(getCvNum()).removePropertyChangeListener(this);
        }
        // remove connection to graphical representation
        disposeReps();
    }

        void disposeReps() {
        if (_value != null) {
            _value.removeActionListener(this);
        }
        for (int i = 0; i < comboCBs.size(); i++) {
            comboCBs.get(i).dispose();
        }
        for (int i = 0; i < comboVars.size(); i++) {
            comboVars.get(i).dispose();
        }
        for (int i = 0; i < comboRBs.size(); i++) {
            comboRBs.get(i).dispose();
        }
    }
        
    static class TreeLeafNode extends DefaultMutableTreeNode {

        TreeLeafNode(String name, int index) {
            super(name);
            this.index = index;
        }

        int index;
    }
    

    
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitEnumVariableValue.class
            .getName());

}