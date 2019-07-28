package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends EnumVariableValue to represent a composition of variable values.
 * <p>
 * Internally, each "choice" is stored as a list of "setting" items. Numerical
 * values for this type of variable (itself) are strictly sequential, because
 * they are arbitrary.
 * <p>
 * This version of the class has certain limitations:
 * <ol>
 * <li>Variables referenced in the definition of one of these must have already
 * been declared earlier in the decoder file. This prevents circular references,
 * and makes it much easier to find the target variables.
 * <li>
 * This version of the variable never changes "State" (color), though it does
 * track its value from changes to other variables.
 * <li>There should be a final choice (entry) that doesn't define any settings.
 * This will then form the default value when the target variables change.
 * <li>Programming operations on a variable of this type doesn't do anything,
 * because there doesn't seem to be a consistent model of what "read changes"
 * and "write changes" should do. This has two implications:
 * <ul>
 * <li>Variables referenced as targets must appear on some programming pane, or
 * they won't be updated by programming operations.
 * <li>If this variable references variables that are not on this pane, the user
 * needs to do a read/write all panes operation to record the changes made to
 * this variable.
 * </ul>
 * It's therefore recommended that a CompositeVariableValue just make changes to
 * target variables on the same programming page.
 * </ol>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005, 2013
 */
public class CompositeVariableValue extends EnumVariableValue {

    public CompositeVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JComboBox<String>();
        log.debug("New Composite named {}", name);
    }

    /**
     * Create a null object. Normally only used for tests and to pre-load
     * classes.
     */
    public CompositeVariableValue() {
        _value = new JComboBox<String>();
    }

    @Override
    public CvValue[] usesCVs() {
        HashSet<CvValue> cvSet = new HashSet<CvValue>(20);  // 20 is arbitrary
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            CvValue[] cvs = v.usesCVs();
            for (int k = 0; k < cvs.length; k++) {
                cvSet.add(cvs[k]);
            }
        }
        CvValue[] retval = new CvValue[cvSet.size()];
        Iterator<CvValue> j = cvSet.iterator();
        int index = 0;
        while (j.hasNext()) {
            retval[index++] = j.next();
        }
        return retval;
    }

    /**
     * Define objects to save and manipulate a particular setting.
     */
    static class Setting {

        String varName;
        VariableValue variable;
        int value;

        Setting(String varName, VariableValue variable, String value) {
            this.varName = varName;
            this.variable = variable;
            this.value = Integer.parseInt(value);
            log.debug("    cTor Setting {} = {}", varName, value);

        }

        void setValue() {
            log.debug("    Setting.setValue of {} to {}", varName, value);
            variable.setIntValue(value);
        }

        boolean match() {
            if (log.isDebugEnabled()) {
                log.debug("         Match checks {} == {}", variable.getIntValue(), value);
            }
            return (variable.getIntValue() == value);
        }
    }

    /**
     * Defines a list of Setting objects.
     * <p>
     * Serves as a home for various service methods
     */
    static class SettingList extends ArrayList<Setting> {

        public SettingList() {
            super();
            log.debug("New setting list");
        }

        void addSetting(String varName, VariableValue variable, String value) {
            Setting s = new Setting(varName, variable, value);
            add(s);
        }

        void setValues() {
            if (log.isDebugEnabled()) {
                log.debug(" setValues in length {}", size());
            }
            for (int i = 0; i < this.size(); i++) {
                Setting s = this.get(i);
                s.setValue();
            }
        }

        boolean match() {
            for (int i = 0; i < size(); i++) {
                if (!this.get(i).match()) {
                    if (log.isDebugEnabled()) {
                        log.debug("      No match in setting list of length {} at position {}", size(), i);
                    }
                    return false;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("      Match in setting list of length {}", size());
            }
            return true;
        }
    }

    Hashtable<String, SettingList> choiceHash = new Hashtable<String, SettingList>();
    HashSet<VariableValue> variables = new HashSet<VariableValue>(20);  // VariableValue; 20 is an arbitrary guess

    /**
     * Create a new possible selection.
     *
     * @param name Name of the choice being added
     */
    public void addChoice(String name) {
        SettingList l = new SettingList();
        choiceHash.put(name, l);
        _value.addItem(name);
    }

    /**
     * Add a setting to an existing choice.
     */
    public void addSetting(String choice, String varName, VariableValue variable, String value) {
        SettingList s = choiceHash.get(choice);
        s.addSetting(varName, variable, value);

        if (variable != null) {
            variables.add(variable);
            if (!variable.label().equals(varName)) {
                log.warn("Unexpected label /{}/ for varName /{}/ during addSetting", variable.label(), varName);
            }
        } else {
            log.error("Variable pointer null when varName={} in choice {}; ignored", varName, choice);
        }
    }

    /**
     * Do end of initialization processing.
     */
    @SuppressWarnings("null")
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH",
            justification = "we want to force an exception")
    @Override
    public void lastItem() {
        // configure the representation object
        _defaultColor = _value.getBackground();
        super.setState(READ);

        // note that we don't set this to COLOR_UNKNOWN!  Rather, 
        // we check the current value
        findValue();

        // connect to all variables to hear changes
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v == null) {
                log.error("Variable found as null in last item");
            }
            // connect, force an exception if v == null
            v.addPropertyChangeListener(this);
        }

        // connect to the JComboBox model so we'll see changes.
        _value.setActionCommand("");            // so we can tell where change comes from
        _value.addActionListener(this);
    }

    @Override
    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    @Override
    public Object rangeVal() {
        return "composite: " + _minVal + " - " + _maxVal;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (!(e.getActionCommand().equals(""))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
        }
        log.debug("action event: {}", e);

        // notify
        prop.firePropertyChange("Value", null, Integer.valueOf(getIntValue()));
        // Here for new values; set as needed
        selectValue(getIntValue());
    }

    /**
     * This variable doesn't change state, hence doesn't change color.
     */
    @Override
    public void setState(int state) {
        log.debug("Ignore setState({})", state);
    }

    /**
     * Set to a specific value.
     * <p>
     * Does this by delegating to the SettingList
     */
    @Override
    protected void selectValue(int value) {
        log.debug("selectValue({})", value);
        if (value > _value.getItemCount() - 1) {
            log.error("Saw unreasonable internal value: {}", value);
            return;
        }

        // locate SettingList for that number
        String choice = _value.getItemAt(value);
        SettingList sl = choiceHash.get(choice);
        sl.setValues();

    }

    @Override
    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    @Override
    public Component getCommonRep() {
        return _value;
    }

    @Override
    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);

        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            prop.firePropertyChange("Value", null, Integer.valueOf(value));
        }
    }

    /**
     * Notify the connected CVs of a state change from above by way of the
     * variables (e.g. not direct to CVs).
     */
    @Override
    public void setCvState(int state) {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setCvState(state);
        }
    }

    @Override
    public boolean isChanged() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isChanged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setToRead(boolean state) {

        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setToRead(state);
        }
    }

    /**
     * This variable needs to be read if any of its subsidiary variables needs
     * to be read.
     */
    @Override
    public boolean isToRead() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToRead()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setToWrite(boolean state) {
        log.debug("Start setToWrite with {}", state);

        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.setToWrite(state);
        }
        log.debug("End setToWrite");
    }

    /**
     * This variable needs to be written if any of its subsidiary variables
     * needs to be written.
     */
    @Override
    public boolean isToWrite() {
        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToWrite()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void readChanges() {
        if (isChanged()) {
            readingChanges = true;
            amReading = true;
            continueRead();
        }
    }

    @Override
    public void writeChanges() {
        if (isChanged()) {
            writingChanges = true;
            amWriting = true;
            continueWrite();
        }
    }

    @Override
    public void readAll() {
        readingChanges = false;
        amReading = true;
        continueRead();
    }
    boolean amReading = false;
    boolean readingChanges = false;

    /**
     * See if there's anything to read, and if so do it.
     */
    protected void continueRead() {
        // search for something to do
        log.debug("Start continueRead");

        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToRead() && (!readingChanges || v.isChanged())) {
                // something to do!
                amReading = true; // should be set already
                setBusy(true);
                if (readingChanges) {
                    v.readChanges();
                } else {
                    v.readAll();
                }
                return;  // wait for busy change event to continue
            }
        }
        // found nothing, ensure cleaned up
        amReading = false;
        super.setState(READ);
        setBusy(false);
        log.debug("End continueRead, nothing to do");
    }

    @Override
    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        writingChanges = false;
        amWriting = true;
        continueWrite();
    }
    boolean amWriting = false;
    boolean writingChanges = false;

    /**
     * See if there's anything to write, and if so do it.
     */
    protected void continueWrite() {
        // search for something to do
        log.debug("Start continueWrite");

        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            if (v.isToWrite() && (!writingChanges || v.isChanged())) {
                // something to do!
                amWriting = true; // should be set already
                setBusy(true);
                log.debug("request write of {} writing changes {}", v.label(), writingChanges);
                if (writingChanges) {
                    v.writeChanges();
                } else {
                    v.writeAll();
                }
                log.debug("return from starting write request");
                return;  // wait for busy change event to continue
            }
        }
        // found nothing, ensure cleaned up
        amWriting = false;
        super.setState(STORED);
        setBusy(false);
        log.debug("End continueWrite, nothing to do");
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        if (log.isDebugEnabled()) {
            log.debug("propertyChange in {} type {} new value {}", label(), e.getPropertyName(), e.getNewValue());
        }
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
                log.debug("busy change continues programming");
                // some programming operation just finished
                if (amReading) {
                    continueRead();
                    return;
                } else if (amWriting) {
                    continueWrite();
                    return;
                }
                // if we're not reading or writing, no problem, that's just something else happening
            }
        } else if (e.getPropertyName().equals("Value")) {
            findValue();
        }
    }

    /**
     * Suspect underlying variables have changed value; check. First match will
     * succeed, so there should not be multiple matches possible. ("First match"
     * is defined in choice-sequence).
     */
    void findValue() {
        if (log.isDebugEnabled()) {
            log.debug("findValue invoked on {}", label());
        }
        for (int i = 0; i < _value.getItemCount(); i++) {
            String choice = _value.getItemAt(i);
            SettingList sl = choiceHash.get(choice);
            if (sl.match()) {
                log.debug("  match in {}", i);
                _value.setSelectedItem(choice);
                return;
            }
        }
        log.debug("   no match");
    }

    // clean up connections when done
    @Override
    public void dispose() {
        log.debug("dispose");

        Iterator<VariableValue> i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = i.next();
            v.removePropertyChangeListener(this);
        }

        // remove the graphical representation
        disposeReps();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CompositeVariableValue.class);

}
