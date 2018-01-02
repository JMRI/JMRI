package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import jmri.AddressedProgrammer;
import jmri.util.jdom.LocaleSelector;
import org.jdom2.Attribute;
import org.jdom2.Content;
import org.jdom2.Element;
import org.jdom2.util.IteratorIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of variables in symbolic programmer.
 * Also responsible for loading from the XML file.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2010
 * @author Howard G. Penny Copyright (C) 2005
 * @author Daniel Boudreau Copyright (C) 2007
 * @author Dave Heap Copyright (C) 2012 Added support for Marklin mfx style
 * speed table
 */
public class VariableTableModel extends AbstractTableModel implements ActionListener, PropertyChangeListener {

    private String headers[] = null;

    private Vector<VariableValue> rowVector = new Vector<VariableValue>();  // vector of Variable items
    private CvTableModel _cvModel = null;          // reference to external table model
    private Vector<JButton> _writeButtons = new Vector<JButton>();
    private Vector<JButton> _readButtons = new Vector<JButton>();
    private JLabel _status = null;

    /**
     * Define the columns; values understood are: "Name", "Value", "Range",
     * "Read", "Write", "Comment", "CV", "Mask", "State".
     * For each, a property key in SymbolicProgBundle by the same name allows i18n
     */
    public VariableTableModel(JLabel status, String h[], CvTableModel cvModel) {
        super();
        _status = status;
        _cvModel = cvModel;
        headers = Arrays.copyOf(h, h.length);
    }

    // basic methods for AbstractTableModel implementation

    @Override
    public int getRowCount() {
        return rowVector.size();
    }

    @Override
    public int getColumnCount() {
        return headers.length;
    }

    @Override
    public String getColumnName(int col) {
        if (log.isDebugEnabled()) {
            log.debug("getColumnName " + col);
        }
        return Bundle.getMessage(headers[col]); // I18N
    }

    @Override
    public Class<?> getColumnClass(int col) {
        // if (log.isDebugEnabled()) log.debug("getColumnClass "+col);
        if (headers[col].equals("Value")) {
            return JTextField.class;
        } else if (headers[col].equals("Read")) {
            return JButton.class;
        } else if (headers[col].equals("Write")) {
            return JButton.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        if (log.isDebugEnabled()) {
            log.debug("isCellEditable " + col);
        }
        if (headers[col].equals("Value")) {
            return true;
        } else if (headers[col].equals("Read")) {
            return true;
        } else if (headers[col].equals("Write")
                && !((rowVector.elementAt(row))).getReadOnly()) {
            return true;
        } else {
            return false;
        }
    }

    public VariableValue getVariable(int row) {
        return (rowVector.elementAt(row));
    }

    public String getLabel(int row) {
        return (rowVector.elementAt(row)).label();
    }

    public String getItem(int row) {
        return (rowVector.elementAt(row)).item();
    }

    public String getCvName(int row) {
        return (rowVector.elementAt(row)).cvName();
    }

    public String getValString(int row) {
        return (rowVector.elementAt(row)).getValueString();
    }

    public void setIntValue(int row, int val) {
        (rowVector.elementAt(row)).setIntValue(val);
    }

    public void setState(int row, int val) {
        if (log.isDebugEnabled()) {
            log.debug("setState row: " + row + " val: " + val);
        }
        (rowVector.elementAt(row)).setState(val);
    }

    public int getState(int row) {
        return (rowVector.elementAt(row)).getState();
    }

    /*
     * Request a "unique representation", e.g. something we can show
     * for the row-th variable.
     */
    public Object getRep(int row, String format) {
        VariableValue v = rowVector.elementAt(row);
        return v.getNewRep(format);
    }

    @Override
    public Object getValueAt(int row, int col) {
        // if (log.isDebugEnabled()) log.debug("getValueAt "+row+" "+col);
        if (row >= rowVector.size()) {
            log.debug("row index greater than row vector size");
            return "Error";
        }
        VariableValue v = rowVector.elementAt(row);
        if (v == null) {
            log.debug("v is null!");
            return "Error value";
        }
        if (headers[col].equals("Value")) {
            return v.getCommonRep();
        } else if (headers[col].equals("Read")) { // NOI18N
            return _readButtons.elementAt(row);
        } else if (headers[col].equals("Write")) { // NOI18N
            return _writeButtons.elementAt(row);
        } else if (headers[col].equals("CV")) { // NOI18N
            return "" + v.getCvNum();
        } else if (headers[col].equals("Name")) { // NOI18N
            return "" + v.label();
        } else if (headers[col].equals("Comment")) { // NOI18N
            return v.getComment();
        } else if (headers[col].equals("Mask")) { // NOI18N
            return v.getMask();
        } else if (headers[col].equals("State")) { // NOI18N
            int state = v.getState();
            switch (state) {
                case CvValue.UNKNOWN:
                    return "Unknown";
                case CvValue.READ:
                    return "Read";
                case CvValue.EDITED:
                    return "Edited";
                case CvValue.STORED:
                    return "Stored";
                case CvValue.FROMFILE:
                    return "From file";
                default:
                    return "inconsistent";
            }
        } else if (headers[col].equals("Range")) {
            return v.rangeVal();
        } else {
            return "Later, dude";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (log.isDebugEnabled()) {
            log.debug("setvalueAt " + row + " " + col + " " + value);
        }
        setFileDirty(true);
    }

    /**
     * Load one row in the VariableTableModel, by reading in the Element
     * containing its definition.
     * <p>
     * Invoked from DecoderFile
     *
     * @param row number of row to fill
     * @param e   Element of type "variable"
     */
    public void setRow(int row, Element e) {
        // get the values for the VariableValue ctor
        String name = LocaleSelector.getAttribute(e, "label");  // Note the name variable is actually the label attribute
        if (log.isDebugEnabled()) {
            log.debug("Starting to setRow \"" + name + "\"");
        }
        String item = (e.getAttribute("item") != null
                ? e.getAttribute("item").getValue()
                : null);
        // as a special case, if no item, use label
        if (item == null) {
            item = name;
            log.debug("no item attribute, used label \"" + name + "\"");
        }
        // as a special case, if no label, use item
        if (name == null) {
            name = item;
            log.debug("no label attribute, used item attribute \"" + item + "\"");
        }

        String comment = LocaleSelector.getAttribute(e, "comment");

        String CV = "";
        if (e.getAttribute("CV") != null) {
            CV = e.getAttribute("CV").getValue();
        }
        String mask = null;
        if (e.getAttribute("mask") != null) {
            mask = e.getAttribute("mask").getValue();
        } else {
            mask = "VVVVVVVV";
        }

        boolean readOnly = e.getAttribute("readOnly") != null
                ? e.getAttribute("readOnly").getValue().equals("yes") : false;
        boolean infoOnly = e.getAttribute("infoOnly") != null
                ? e.getAttribute("infoOnly").getValue().equals("yes") : false;
        boolean writeOnly = e.getAttribute("writeOnly") != null
                ? e.getAttribute("writeOnly").getValue().equals("yes") : false;
        boolean opsOnly = e.getAttribute("opsOnly") != null
                ? e.getAttribute("opsOnly").getValue().equals("yes") : false;

        // Ops mode doesn't allow reads, therefore we must disable read buttons
        if (_cvModel.getProgrammer() != null) {
            if (opsOnly && !AddressedProgrammer.class.isAssignableFrom(_cvModel.getProgrammer().getClass())) {
                // opsOnly but not Ops mode, so adjust
                readOnly = false;
                writeOnly = false;
                infoOnly = true;
            } else if (!_cvModel.getProgrammer().getCanRead()) {
                // can't read, so adjust
                if (readOnly) {
                    readOnly = false;
                    infoOnly = true;
                }
                if (!infoOnly) {
                    writeOnly = true;
                }
            }
        }

        JButton bw = new JButton(Bundle.getMessage("ButtonWrite"));
        _writeButtons.addElement(bw);
        JButton br = new JButton(Bundle.getMessage("ButtonRead"));
        _readButtons.addElement(br);
        setButtonsReadWrite(readOnly, infoOnly, writeOnly, bw, br, row);

        if (_cvModel == null) {
            log.error("CvModel reference is null; cannot add variables");
            return;
        }
        if (!CV.equals("")) // some variables have no CV per se
        {
            _cvModel.addCV(CV, readOnly, infoOnly, writeOnly);
        }

        // decode and handle specific types
        Element child;
        VariableValue v = null;
        if ((child = e.getChild("decVal")) != null) {
            v = processDecVal(child, name, comment, readOnly, infoOnly, writeOnly, opsOnly, CV, mask, item);

        } else if ((child = e.getChild("hexVal")) != null) {
            v = processHexVal(child, name, comment, readOnly, infoOnly, writeOnly, opsOnly, CV, mask, item);

        } else if ((child = e.getChild("enumVal")) != null) {
            v = processEnumVal(child, name, comment, readOnly, infoOnly, writeOnly, opsOnly, CV, mask, item);

        } else if ((child = e.getChild("compositeVal")) != null) {
            // loop over the choices
            v = processCompositeVal(child, name, comment, readOnly, infoOnly, writeOnly, opsOnly, CV, mask, item);

        } else if ((child = e.getChild("speedTableVal")) != null) {

            v = processSpeedTableVal(child, CV, readOnly, infoOnly, writeOnly, name, comment, opsOnly, mask, item);

        } else if ((child = e.getChild("longAddressVal")) != null) {
            v = processLongAddressVal(CV, readOnly, infoOnly, writeOnly, name, comment, opsOnly, mask, item);

        } else if ((child = e.getChild("shortAddressVal")) != null) {
            v = processShortAddressVal(name, comment, readOnly, infoOnly, writeOnly, opsOnly, CV, mask, item, child);

        } else if ((child = e.getChild("splitVal")) != null) {
            v = processSplitVal(child, CV, readOnly, infoOnly, writeOnly, name, comment, opsOnly, mask, item);

        } else {
            reportBogus();
            return;
        }

        processModifierElements(e, v);

        setToolTip(e, v);

        // record new variable, update state, hook up listeners
        rowVector.addElement(v);
        v.setState(VariableValue.FROMFILE);
        v.addPropertyChangeListener(this);

        // set to default value if specified (CV load may later override this)
        if (setDefaultValue(e, v)) {
            _cvModel.getCvByNumber(CV).setState(VariableValue.FROMFILE);  // correct for transition to "edited"
        }
    }

    /**
     * If there are any modifier elements, process them by e.g. setting
     * attributes on the VariableValue
     */
    protected void processModifierElements(final Element e, final VariableValue v) {
        QualifierAdder qa = new QualifierAdder() {
            @Override
            protected Qualifier createQualifier(VariableValue var, String relation, String value) {
                return new ValueQualifier(v, var, Integer.parseInt(value), relation);
            }

            @Override
            protected void addListener(java.beans.PropertyChangeListener qc) {
                v.addPropertyChangeListener(qc);
            }
        };

        qa.processModifierElements(e, this);
    }

    /**
     * If there's a "default" attribute, set that value to start
     *
     * @return true if the value was set
     */
    protected boolean setDefaultValue(Element e, VariableValue v) {
        Attribute a;
        if ((a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            v.setIntValue(Integer.valueOf(val).intValue());
            return true;
        }
        return false;
    }

    protected VariableValue processCompositeVal(Element child, String name, String comment, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, String CV, String mask, String item) {
        VariableValue v;
        List<Element> lChoice = child.getChildren("compositeChoice");
        CompositeVariableValue v1 = new CompositeVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, 0, lChoice.size() - 1, _cvModel.allCvMap(), _status, item);
        v = v1; // v1 is of CompositeVariableType, so doesn't need casts
        // loop over the choices
        for (int k = 0; k < lChoice.size(); k++) {
            // Create the choice
            Element choiceElement = lChoice.get(k);
            String choice = LocaleSelector.getAttribute(choiceElement, "choice");
            v1.addChoice(choice);
            // for each choice, capture the settings
            @SuppressWarnings("unchecked")
            List<Element> lSetting = choiceElement.getChildren("compositeSetting");
            for (int n = 0; n < lSetting.size(); n++) {
                Element settingElement = lSetting.get(n);
                String varName = LocaleSelector.getAttribute(settingElement, "label");
                String value = settingElement.getAttribute("value").getValue();
                v1.addSetting(choice, varName, findVar(varName), value);
            }
        }
        v1.lastItem();
        return v;
    }

    protected VariableValue processDecVal(Element child, String name, String comment, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, String CV, String mask, String item) throws NumberFormatException {
        VariableValue v;
        Attribute a;
        int minVal = 0;
        int maxVal = 255;
        if ((a = child.getAttribute("min")) != null) {
            minVal = Integer.valueOf(a.getValue()).intValue();
        }
        if ((a = child.getAttribute("max")) != null) {
            maxVal = Integer.valueOf(a.getValue()).intValue();
        }
        v = new DecVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, item);
        return v;
    }

    protected VariableValue processEnumVal(Element child, String name, String comment, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, String CV, String mask, String item) throws NumberFormatException {

        int count = 0;
        IteratorIterable<Content> iterator = child.getDescendants();
        while (iterator.hasNext()) {
            Object ex = iterator.next();
            if (ex instanceof Element) {
                if (((Element) ex).getName().equals("enumChoice")) {
                    count++;
                }
            }
        }

        VariableValue v;
        EnumVariableValue v1 = new EnumVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, 0, count, _cvModel.allCvMap(), _status, item);
        v = v1; // v1 is of EnunVariableValue type, so doesn't need casts

        v1.nItems(count);
        handleENumValChildren(child, v1);
        v1.lastItem();
        return v;
    }

    /**
     * Recursively walk the child enumChoice elements, working through the
     * enumChoiceGroup elements as needed.
     */
    protected void handleENumValChildren(Element e, EnumVariableValue var) {
        List<Element> local = e.getChildren();
        for (int k = 0; k < local.size(); k++) {
            Element el = local.get(k);
            if (el.getName().equals("enumChoice")) {
                Attribute valAttr = el.getAttribute("value");
                if (valAttr == null) {
                    var.addItem(LocaleSelector.getAttribute(el, "choice"));
                } else {
                    var.addItem(LocaleSelector.getAttribute(el, "choice"),
                            Integer.parseInt(valAttr.getValue()));
                }
            } else if (el.getName().equals("enumChoiceGroup")) {
                var.startGroup(LocaleSelector.getAttribute(el, "name"));
                handleENumValChildren(el, var);
                var.endGroup();
            }

        }
    }

    protected VariableValue processHexVal(Element child, String name, String comment, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, String CV, String mask, String item) throws NumberFormatException {
        VariableValue v;
        Attribute a;
        int minVal = 0;
        int maxVal = 255;
        if ((a = child.getAttribute("min")) != null) {
            minVal = Integer.valueOf(a.getValue(), 16).intValue();
        }
        if ((a = child.getAttribute("max")) != null) {
            maxVal = Integer.valueOf(a.getValue(), 16).intValue();
        }
        v = new HexVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, item);
        return v;
    }

    protected VariableValue processLongAddressVal(String CV, boolean readOnly, boolean infoOnly, boolean writeOnly, String name, String comment, boolean opsOnly, String mask, String item) {
        VariableValue v;
        int minVal = 0;
        int maxVal = 255;
        _cvModel.addCV("18", readOnly, infoOnly, writeOnly); // ensure 2nd CV exists
        v = new LongAddrVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, item, _cvModel.allCvMap().get("18"));
        return v;
    }

    protected VariableValue processShortAddressVal(String name, String comment, boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly, String CV, String mask, String item, Element child) {
        VariableValue v;
        ShortAddrVariableValue v1 = new ShortAddrVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, _cvModel.allCvMap(), _status, item);
        v = v1;
        // get specifics if any
        List<Element> l = child.getChildren("shortAddressChanges");
        for (int k = 0; k < l.size(); k++) {
            v1.setModifiedCV(l.get(k).getAttribute("cv").getValue());
        }
        return v;
    }

    protected VariableValue processSpeedTableVal(Element child, String CV, boolean readOnly, boolean infoOnly, boolean writeOnly, String name, String comment, boolean opsOnly, String mask, String item) throws NumberFormatException {
        VariableValue v;
        Attribute a;
        int minVal = 0;
        int maxVal = 255;
        if ((a = child.getAttribute("min")) != null) {
            minVal = Integer.valueOf(a.getValue()).intValue();
        }
        if ((a = child.getAttribute("max")) != null) {
            maxVal = Integer.valueOf(a.getValue()).intValue();
        }
        Attribute entriesAttr = child.getAttribute("entries");
        int entries = 28;
        try {
            if (entriesAttr != null) {
                entries = entriesAttr.getIntValue();
            }
        } catch (org.jdom2.DataConversionException e1) {
        }
        Attribute ESUAttr = child.getAttribute("mfx");
        boolean mfxFlag = false;
        try {
            if (ESUAttr != null) {
                mfxFlag = ESUAttr.getBooleanValue();
            }
        } catch (org.jdom2.DataConversionException e1) {
        }
        // ensure all CVs exist
        for (int i = 0; i < entries; i++) {
            _cvModel.addCV(Integer.toString(Integer.parseInt(CV) + i), readOnly, infoOnly, writeOnly);
        }
        if (mfxFlag) {
            _cvModel.addCV("2", readOnly, infoOnly, writeOnly);
            _cvModel.addCV("5", readOnly, infoOnly, writeOnly);
        }
        v = new SpeedTableVarValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, item, entries, mfxFlag);
        return v;
    }

    protected VariableValue processSplitVal(Element child, String CV, boolean readOnly, boolean infoOnly, boolean writeOnly, String name, String comment, boolean opsOnly, String mask, String item) throws NumberFormatException {
        VariableValue v;
        Attribute a;
        int minVal = 0;
        int maxVal = 255;
        if ((a = child.getAttribute("min")) != null) {
            minVal = Integer.valueOf(a.getValue()).intValue();
        }
        if ((a = child.getAttribute("max")) != null) {
            maxVal = Integer.valueOf(a.getValue()).intValue();
        }

        String highCV = child.getAttribute("highCV").getValue(); // no default, must be present

        int factor = 1;
        if ((a = child.getAttribute("factor")) != null) {
            factor = Integer.valueOf(a.getValue()).intValue();
        }
        int offset = 0;
        if ((a = child.getAttribute("offset")) != null) {
            offset = Integer.valueOf(a.getValue()).intValue();
        }
        String uppermask = "VVVVVVVV";
        if ((a = child.getAttribute("upperMask")) != null) {
            uppermask = a.getValue();
        }
        _cvModel.addCV("" + (highCV), readOnly, infoOnly, writeOnly); // ensure 2nd CV exists
        v = new SplitVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly, CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, item, highCV, factor, offset, uppermask);
        return v;
    }

    protected void setButtonsReadWrite(boolean readOnly, boolean infoOnly, boolean writeOnly, JButton bw, JButton br, int row) {
        if (readOnly || infoOnly) {
            // readOnly or infoOnly, config write, read buttons
            if (writeOnly) {
                bw.setEnabled(true);
                bw.setActionCommand("W" + row);
                bw.addActionListener(this);
            } else {
                bw.setEnabled(false);
            }
            if (infoOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R" + row);
                br.addActionListener(this);
            }
        } else {
            // not readOnly or infoOnly, config write, read buttons
            bw.setActionCommand("W" + row);
            bw.addActionListener(this);
            if (writeOnly) {
                br.setEnabled(false);
            } else {
                br.setActionCommand("R" + row);
                br.addActionListener(this);
            }
        }
    }

    protected void setToolTip(Element e, VariableValue v) {
        // back to general processing
        // add tooltip text if present
        {
            String t;
            if ((t = LocaleSelector.getAttribute(e, "tooltip")) != null) {
                v.setToolTipText(t);
            }
        }
    }

    void reportBogus() {
        log.error("Did not find a valid variable type");
    }

    /**
     * Configure from a constant. This is like setRow (which processes a
     * variable Element).
     */
    @SuppressFBWarnings(value = "NP_LOAD_OF_KNOWN_NULL_VALUE",
            justification = "null mask parameter to ConstantValue constructor expected.")
    public void setConstant(Element e) {
        // get the values for the VariableValue ctor
        String stdname = e.getAttribute("item").getValue();
        if (log.isDebugEnabled()) {
            log.debug("Starting to setConstant \"" + stdname + "\"");
        }

        String name = LocaleSelector.getAttribute(e, "label");
        if (name == null || name.equals("")) {
            name = stdname;
        }

        String comment = LocaleSelector.getAttribute(e, "comment");

        String mask = null;

        // intrinsically readOnly, so use just that branch
        JButton bw = new JButton();
        _writeButtons.addElement(bw);

        // config read button as a dummy - there's really nothing to read
        JButton br = new JButton("Read");
        _readButtons.addElement(br);

        // no CV references are added here
        // have to handle various value types, see "snippet"
        Attribute a;

        // set to default value if specified (CV load will later override this)
        int defaultVal = 0;
        if ((a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Found default value: " + val + " for " + stdname);
            }
            defaultVal = Integer.valueOf(val).intValue();
        }

        // create the specific object
        ConstantValue v = new ConstantValue(name, comment, "", true, true, false, false,
                "", mask, defaultVal, defaultVal,
                _cvModel.allCvMap(), _status, stdname);

        // record new variable, update state, hook up listeners
        rowVector.addElement(v);
        v.setState(VariableValue.FROMFILE);
        v.addPropertyChangeListener(this);

        // set to default value if specified (CV load will later override this)
        if ((a = e.getAttribute("default")) != null) {
            String val = a.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Found default value: " + val + " for " + name);
            }
            v.setIntValue(defaultVal);
        }
    }

    /**
     * Programmatically create a new DecVariableValue from parameters
     */
    public void newDecVariableValue(String name, String CV, String comment, String mask,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly) {
        setFileDirty(true);

        int minVal = 0;
        int maxVal = 255;
        _cvModel.addCV("" + CV, readOnly, infoOnly, writeOnly);

        int row = getRowCount();

        // config write button
        JButton bw = new JButton("Write");
        bw.setActionCommand("W" + row);
        bw.addActionListener(this);
        _writeButtons.addElement(bw);

        // config read button
        JButton br = new JButton("Read");
        br.setActionCommand("R" + row);
        br.addActionListener(this);
        _readButtons.addElement(br);

        VariableValue v = new DecVariableValue(name, comment, "", readOnly, infoOnly, writeOnly, opsOnly,
                CV, mask, minVal, maxVal, _cvModel.allCvMap(), _status, null);
        rowVector.addElement(v);
        v.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("action performed,  command: " + e.getActionCommand());
        }
        setFileDirty(true);
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) {
            log.debug("event on " + b + " row " + row);
        }
        if (b == 'R') {
            // read command
            read(row);
        } else {
            // write command
            write(row);
        }
    }

    /**
     * Command reading of a particular variable
     *
     * @param i row number
     */
    public void read(int i) {
        VariableValue v = rowVector.elementAt(i);
        v.readAll();
    }

    /**
     * Command writing of a particular variable
     *
     * @param i row number
     */
    public void write(int i) {
        VariableValue v = rowVector.elementAt(i);
        v.writeAll();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("prop changed " + e.getPropertyName()
                    + " new value: " + e.getNewValue()
                    + (e.getPropertyName().equals("State") ? (" (" + VariableValue.stateNameFromValue(((Integer) e.getNewValue()).intValue()) + ") ") : " ")
                    + " Source " + e.getSource());
        }
        if (e.getNewValue() == null) {
            log.error("new value of " + e.getPropertyName() + " should not be null!");
            (new Exception()).printStackTrace();
        }
        // set dirty only if edited or read
        if (e.getPropertyName().equals("State")
                && ((Integer) e.getNewValue()).intValue() == CvValue.READ
                || e.getPropertyName().equals("State")
                && ((Integer) e.getNewValue()).intValue() == CvValue.EDITED) {
            setFileDirty(true);

        }
        fireTableDataChanged();
    }

    public void configDone() {
        fireTableDataChanged();
    }

    /**
     * Represents any change to values, etc, hence rewriting the file is
     * desirable.
     */
    public boolean fileDirty() {
        return _fileDirty;
    }

    public void setFileDirty(boolean b) {
        _fileDirty = b;
    }
    private boolean _fileDirty;

    /**
     * Check for change to values, etc, hence rewriting the decoder is
     * desirable.
     */
    public boolean decoderDirty() {
        int len = rowVector.size();
        for (int i = 0; i < len; i++) {
            if (((rowVector.elementAt(i))).getState() == CvValue.EDITED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the (first) variable that matches a given name string. Searches
     * first for "item", the true name, but if none found will attempt to find a
     * matching "label". In that case, only the default language is checked.
     */
    public VariableValue findVar(String name) {
        for (int i = 0; i < getRowCount(); i++) {
            if (name.equals(getItem(i))) {
                return getVariable(i);
            }
            if (name.equals(getLabel(i))) {
                return getVariable(i);
            }
        }
        return null;
    }

    public int findVarIndex(String name) {
        for (int i = 0; i < getRowCount(); i++) {
            if (name.equals(getItem(i))) {
                return i;
            }
            if (name.equals(getLabel(i))) {
                return i;
            }
            if (name.equals("CV" + getCvName(i))) {
                return i;
            }
        }
        return -1;
    }

    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        // remove buttons
        for (int i = 0; i < _writeButtons.size(); i++) {
            _writeButtons.elementAt(i).removeActionListener(this);
        }
        for (int i = 0; i < _readButtons.size(); i++) {
            _readButtons.elementAt(i).removeActionListener(this);
        }

        // remove variables listeners
        for (int i = 0; i < rowVector.size(); i++) {
            VariableValue v = rowVector.elementAt(i);
            v.removePropertyChangeListener(this);
            v.dispose();
        }

        headers = null;

        rowVector.removeAllElements();
        rowVector = null;

        _cvModel = null;

        _writeButtons.removeAllElements();
        _writeButtons = null;

        _readButtons.removeAllElements();
        _readButtons = null;

        _status = null;
    }

    private final static Logger log = LoggerFactory.getLogger(VariableTableModel.class);

}
