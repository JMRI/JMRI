package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * Extends VariableValue to represent a enumerated variable.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2013, 2014
 *
 */
public class EnumVariableValue extends VariableValue implements ActionListener {

    public EnumVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;

        treeNodes.addLast(new DefaultMutableTreeNode("")); // root
    }

    /**
     * Create a null object. Normally only used for tests and to pre-load
     * classes.
     */
    public EnumVariableValue() {
    }

    @Override
    public CvValue[] usesCVs() {
        return new CvValue[]{_cvMap.get(getCvNum())};
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

    /**
     * Create a new item in the enumeration, with a specified associated value.
     *
     * @param s Name of the enumeration item
     */
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

    int _maxVal;
    int _minVal;
    Color _defaultColor;

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
            log.debug(label() + " start action event: " + e);
        }
        if (!(e.getActionCommand().equals(""))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
            if (log.isDebugEnabled()) {
                log.debug(label() + " action event was from alternate rep");
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
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal = getIntValue();
        int newCv = setValueInCV(oldCv, newVal, getMask(), _maxVal-1);
        if (newCv != oldCv) {
            cv.setValue(newCv);  // to prevent CV going EDITED during loading of decoder file

            // notify  (this used to be before setting the values)
            if (log.isDebugEnabled()) {
                log.debug(label() + " about to firePropertyChange");
            }
            prop.firePropertyChange("Value", null, oldVal);
            if (log.isDebugEnabled()) {
                log.debug(label() + " returned to from firePropertyChange");
            }
        }
        if (log.isDebugEnabled()) {
            log.debug(label() + " end action event saw oldCv=" + oldCv + " newVal=" + newVal + " newCv=" + newCv);
        }
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        return "" + _value.getSelectedIndex();
    }

    @Override
    public void setIntValue(int i) {
        selectValue(i);
    }

    @Override
    public String getTextValue() {
        return _value.getSelectedItem().toString();
    }

    @Override
    public Object getValueObject() {
        return _value.getSelectedIndex();
    }

    /**
     * Set to a specific value.
     * <p>
     * This searches for the displayed value, and sets the enum to that
     * particular one. It used to work off an index, but now it looks for the
     * value.
     * <p>
     * If the value is larger than any defined, a new one is created.
     */
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
        log.debug("Create new item with value " + value + " count was " + _value.getItemCount()
                + " in " + label());
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

    @Override
    public int getIntValue() {
        if (_value.getSelectedIndex() >= _valueArray.length || _value.getSelectedIndex() < 0) {
            log.error("trying to get value " + _value.getSelectedIndex() + " too large"
                    + " for array length " + _valueArray.length + " in var " + label());
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
    }

    @Override
    public Component getNewRep(String format) {
        // sort on format type
        switch (format) {
            case "checkbox": {
                // this only makes sense if there are exactly two options
                ComboCheckBox b = new ComboCheckBox(_value, this);
                comboCBs.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
            case "radiobuttons": {
                ComboRadioButtons b = new ComboRadioButtons(_value, this);
                comboRBs.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
            case "onradiobutton": {
                ComboRadioButtons b = new ComboOnRadioButton(_value, this);
                comboRBs.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
            case "offradiobutton": {
                ComboRadioButtons b = new ComboOffRadioButton(_value, this);
                comboRBs.add(b);
                if (getReadOnly() || getInfoOnly()) {
                    b.setEnabled(false);
                }
                updateRepresentation(b);
                return b;
            }
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

    private final List<ComboCheckBox> comboCBs = new ArrayList<>();
    private final List<VarComboBox> comboVars = new ArrayList<>();
    private final List<ComboRadioButtons> comboRBs = new ArrayList<>();
    private final List<JTree> trees = new ArrayList<>();

    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        if (c != null) {
            _value.setBackground(c);
        } else {
            _value.setBackground(_defaultColor);
        }
        _value.setOpaque(true);
    }

    /**
     * Notify the connected CVs of a state change from above
     */
    @Override
    public void setCvState(int state) {
        _cvMap.get(getCvNum()).setState(state);
    }

    @Override
    public boolean isChanged() {
        CvValue cv = _cvMap.get(getCvNum());
        return considerChanged(cv);
    }

    @Override
    public void readChanges() {
        if (isToRead() && !isChanged()) {
            log.debug("!!!!!!! unacceptable combination in readChanges: " + label());
        }
        if (isChanged() || isToRead()) {
            readAll();
        }
    }

    @Override
    public void writeChanges() {
        if (isToWrite() && !isChanged()) {
            log.debug("!!!!!! unacceptable combination in writeChanges: " + label());
        }
        if (isChanged() || isToWrite()) {
            writeAll();
        }
    }

    @Override
    public void readAll() {
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        _cvMap.get(getCvNum()).read(_status);
    }

    @Override
    public void writeAll() {
        setToWrite(false);
        if (getReadOnly() || getInfoOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        _cvMap.get(getCvNum()).write(_status);
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        switch (e.getPropertyName()) {
            case "Busy":
                if (((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                    setToRead(false);
                    setToWrite(false);  // some programming operation just finished
                    setBusy(false);
                }
                break;
            case "State": {
                CvValue cv = _cvMap.get(getCvNum());
                if (cv.getState() == STORED) {
                    setToWrite(false);
                }
                if (cv.getState() == READ) {
                    setToRead(false);
                }
                setState(cv.getState());
                for (JTree tree : trees) {
                    tree.setBackground(_value.getBackground());
                    //tree.setOpaque(true);
                }
                break;
            }
            case "Value": {
                // update value of Variable
                CvValue cv = _cvMap.get(getCvNum());
                int newVal = getValueInCV(cv.getValue(), getMask(), _maxVal-1); // _maxVal value is count of possibles, i.e. radix
                setValue(newVal);  // check for duplicate done inside setVal
                break;
            }
            default:
                break;
        }
    }

    /* Internal class extends a JComboBox so that its color is consistent with
     * an underlying variable; we return one of these in getNewRep.
     * <p>
     * Unlike similar cases elsewhere, this doesn't have to listen to
     * value changes.  Those are handled automagically since we're sharing the same
     * model between this object and the real JComboBox value.
     *
     * @author   Bob Jacobsen   Copyright (C) 2001
     */
    public static class VarComboBox extends JComboBox<String> {

        VarComboBox(ComboBoxModel<String> m, EnumVariableValue var) {
            super(m);
            _var = var;
            _l = new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("VarComboBox saw property change: " + e);
                    }
                    originalPropertyChanged(e);
                }
            };
            // get the original color right
            setBackground(_var._value.getBackground());
            setOpaque(true);
            // listen for changes to original state
            _var.addPropertyChangeListener(_l);
        }

        EnumVariableValue _var;
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
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        // remove connection to CV
        _cvMap.get(getCvNum()).removePropertyChangeListener(this);

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
    private final static Logger log = LoggerFactory.getLogger(EnumVariableValue.class);

}
