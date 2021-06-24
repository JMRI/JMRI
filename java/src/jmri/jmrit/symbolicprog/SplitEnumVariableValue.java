package jmri.jmrit.symbolicprog;


import java.io.UnsupportedEncodingException;

import javax.swing.*;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SplitVariableValue}, except that the string representation is
 * text.
 * <br><br>
 * Most attributes of {@link SplitVariableValue} are inherited.
 * <br><br>
 * Specific attributes for this class are:
 * <ul>
 * <li>
 * A {@code match} attribute (which must be a {@code regular expression}) can be
 * used to impose constraints on entered text.
 * </li>
 * <li>
 * A {@code termByteStr} attribute can be used to change the default string
 * terminator byte value. Valid values are 0-255 or "" to specify no terminator
 * byte. The default is "0" (a null byte).
 * </li>
 * <li>
 * A {@code padByteStr} attribute can be used to change the default string
 * padding byte value. Valid values are 0-255 or "" to specify no pad byte. The
 * default is "0" (a null byte).
 * </li>
 * <li>
 * A {@code charSet} attribute can be used to change the character set used to
 * encode or decode the text string. Valid values are any Java-supported
 * {@link java.nio.charset.Charset} name. If not specified, the default
 * character set of this Java virtual machine is used.
 * </li>
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2013, 2014
 * @author Dave Heap Copyright (C) 2016
 */
public class SplitEnumVariableValue extends SplitVariableValue {

    
    private static final int RETRY_COUNT = 2;
    
    int atest;
    private final List<JTree> trees = new ArrayList<>();
    
    private final List<ComboCheckBox> comboCBs = new ArrayList<>();
    private final List<SplitEnumVariableValue.VarComboBox> comboVars = new ArrayList<>();
    private final List<ComboRadioButtons> comboRBs = new ArrayList<>();
    
    
    public SplitEnumVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        _maxVal = maxVal;
        _minVal = minVal;
        treeNodes.addLast(new DefaultMutableTreeNode(""));
    }

    @Override
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        atest = 77;
        
        
        log.debug("stepOneActions");
        log.debug("atest={}", atest);
        
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
        TreeLeafNode node = new TreeLeafNode(s, _nstored);
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
        CvValue cv = _cvMap.get(getCvNum());
        if (cv == null) {
            log.error("no CV defined in enumVal {}, skipping setState", getCvName());
            return;
        }
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
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
    
    @Override
    public void stepTwoActions() {
        log.debug("stepTwoActions");
        log.debug("atest={}", atest);
        //log.debug("termByteStr=\"{}\",padByteStr=\"{}\"", termByteStr, padByteStr);
        //log.debug("termByteVal={},padByteVal={}", termByteVal, padByteVal);
        _columns = cvCount + 2; //update column width now we have a better idea
        _value = new JComboBox<String>();
        _defaultColor = _value.getBackground();
    }


        @Override
    public void setAvailable(boolean a) {
        _value.setVisible(a);
        for (ComboCheckBox c : comboCBs) {
            c.setVisible(a);
        }
        for (VarComboBox c : comboVars) {
            c.setVisible(a);
        }
        for (ComboRadioButtons c : comboRBs) {
            c.setVisible(a);
        }
        super.setAvailable(a);
    }

    @Override
    public Object rangeVal() {
        return "enum: " + _minVal + " - " + _maxVal;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (log.isDebugEnabled()) {
            log.debug("{} start action event: {}", label(), e);
        }
        
        if(e != null){
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
            else if(e.getActionCommand().equals("")){
                exitField();
            }
        }
    }

    // not currently being invoked!!
    /**
     * Saves contents of _textField to oldContents.
     */
    void enterField() {
        oldContents = getTextValue();
        log.debug("enterField sets oldContents to {}", oldContents);
    }

    @Override
    void exitField(){
        int selVal = getIntValue();
        // there may be a lost focus event left in the queue when disposed so protect
        long newFieldVal = selVal;
        log.debug("_minVal={};_maxVal={};newFieldVal={}",
                Long.toUnsignedString(_minVal), Long.toUnsignedString(_maxVal), Long.toUnsignedString(newFieldVal));
        
        long newVal = (newFieldVal - mOffset) / mFactor;
        long oldVal = (getValueFromText(oldContents) - mOffset) / mFactor;
        log.debug("Enter updatedDropDown from exitField oldVal={}; newVal={}", oldVal, newVal);
        updatedDropDown();
        prop.firePropertyChange("Value", oldVal, newVal);
    }
    
    void updatedDropDown() {
        log.debug("Variable='{}'; enter updatedDropDown in {} with TextField='{}'", _name, (this.getClass().getSimpleName()), _value);
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
        log.debug("Variable={}; exit updatedTextField", _name);
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
    
     @Override
    public String getValueString() {
        return Integer.toString(getIntValue());
    }
    
        @Override
    public void setIntValue(int i) {
        setLongValue(i);
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
        return _value.getSelectedIndex();
    }
    
    @Override
    public int getIntValue() {
        if (_value.getSelectedIndex() >= _valueArray.length || _value.getSelectedIndex() < 0) {
            log.error("trying to get value {} too large for array length {} in var {}", _value.getSelectedIndex(), _valueArray.length, label());
        }
        log.debug("SelectedIndex={}", _value.getSelectedIndex());
        return _valueArray[_value.getSelectedIndex()];
    }
    
    @Override
    public Component getCommonRep() {
        return _value;
    }

    
    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            prop.firePropertyChange("Value", null, value);
        }
        try {
            long val = Long.parseUnsignedLong(String.valueOf(value));
            setLongValue(val);
        } catch (NumberFormatException e) {
            log.warn("skipping set of non-long value \"{}\"", value);
        }
        
    }
    
    @Override
    public void setValue(String value){
        try {
            long val = Long.parseUnsignedLong(value);
            setLongValue(val);
        } catch (NumberFormatException e) {
            log.warn("skipping set of non-long value \"{}\"", value);
        }
    }
    
    protected void selectValue(int value) {
        if (_nstored > 0) {
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

        // We can be commanded to a number that hasn't been defined.
        // But that's OK for certain applications.  Instead, we add them as needed
        log.debug("Create new item with value {} count was {} in {}", value, _value.getItemCount(), label());
        // lengthen arrays
        _valueArray = java.util.Arrays.copyOf(_valueArray, _valueArray.length + 1);

        _itemArray = java.util.Arrays.copyOf(_itemArray, _itemArray.length + 1);

        _pathArray = java.util.Arrays.copyOf(_pathArray, _pathArray.length + 1);

        addItem("Reserved value " + value, value);

        // update the JComboBox
        _value.addItem(_itemArray[_nstored - 1]);
        _value.setSelectedItem(_itemArray[_nstored - 1]);

        // tell trees to redisplay & select
        for (JTree tree : trees) {
            ((DefaultTreeModel) tree.getModel()).reload();
            tree.setSelectionPath(_pathArray[_nstored - 1]);
            // ensure selection is in visible portion of JScrollPane
            tree.scrollPathToVisible(_pathArray[_nstored - 1]);
        }
    }
    
    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        if (c != null) {
            _value.setBackground(c);
            log.debug("Variable={}; Set Color to {}", _name, c.toString());
        } else {
            log.debug("Variable={}; Set Color to defaultColor {}", _name, _defaultColor.toString());
            _value.setBackground(_defaultColor);
        }
        // prop.firePropertyChange("Value", null, null);
    }

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
                            if (paths[0].getLastPathComponent() instanceof TreeLeafNode) {
                                // update value of Variable
                                setValue(_valueArray[((TreeLeafNode) paths[0].getLastPathComponent()).index]);
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
                VarComboBox b = new VarComboBox(_value.getModel(), this);
                comboVars.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
        }
    }

    /** Internal class extends a JComboBox so that its color is consistent with
     * an underlying variable; we return one of these in getNewRep.
     * <p>
     * Unlike similar cases elsewhere, this doesn't have to listen to
     * value changes.  Those are handled automagically since we're sharing the same
     * model between this object and the real JComboBox value.
     *
     * @author   Bob Jacobsen   Copyright (C) 2001
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
