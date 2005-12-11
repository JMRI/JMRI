// CompositeVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;

import javax.swing.*;

import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Hashtable;
import com.sun.java.util.collections.HashSet;
import com.sun.java.util.collections.Iterator;

/**
 * Extends EnumVariableValue to represent a composition of variable values.
 * <P>
 * Internally, each "choice" is stored as a list of "setting" items.
 * Numerical values for this type of variable (itself) are strictly sequential,
 * because they are arbitrary.
 * <p>
 * This version of the class has certain limitations:
 *<OL>
 * <LI>Variables referenced in the definition of one of these must have
 * already been declared earlier in the decoder file.  This prevents
 * circular references, and makes it much easier to find the target variables.
 * <LI>
 * This version of the variable never changes "State" (color), though it does
 * track it's value from changes to other variables.
 * <LI>The should be a final choice (entry) that doesn't define any 
 * settings.  This will then form the default value when the target variables
 * change.
 * <LI>Programming operations on one of these variables don't do anything
 * now.  This has two implications:
 *  <UL>
 *  <LI>Variables referenced as targets must appear on some programming pane,
 *      or they won't be updated by programming operations.
 *  <LI>If this variable references variables that are not on this pane,
 *      the user needs to do a read/write all panes operation to record
 *      the changes made to this variable.  
 *  </UL>
 *  It's therefore recommended that a CompositeVariableValue just make changes
 *  to target variables on the same programming page.
 *</ol>
 * <P>
 * @author	Bob Jacobsen   Copyright (C) 2001, 2005
 * @version	$Revision: 1.3 $
 *
 */
public class CompositeVariableValue extends EnumVariableValue implements ActionListener, PropertyChangeListener {

    public CompositeVariableValue(String name, String comment, String cvName,
                             boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                             int cvNum, String mask, int minVal, int maxVal,
                             Vector v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JComboBox();
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public CompositeVariableValue() {
        _value = new JComboBox();
    }

    public CvValue[] usesCVs() {
        HashSet cvSet = new HashSet(20);  // 20 is arbitrary
        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            CvValue[] cvs = v.usesCVs();
            for (int k=0; k<cvs.length; k++)
                cvSet.add(cvs[k]);
        }
        CvValue[] retval = new CvValue[cvSet.size()];
        Iterator j = cvSet.iterator();
        int index = 0;
        while (j.hasNext()) {
            retval[index++] = (CvValue)j.next();
        }
        return retval;
    }

    /**
     * Define objects to save and manipulate a particular setting
     */
    class Setting {
        String varName;
        VariableValue variable;
        int value;
        
        Setting(String varName, VariableValue variable, String value) {
            this.varName = varName;
            this.variable = variable;
            this.value = Integer.valueOf(value).intValue();
        }
        void setValue() {
            if (log.isDebugEnabled()) log.debug("    Setting.setValue of "+varName+" to "+value);
            variable.setIntValue(value);
        }
        boolean match() {
            return (variable.getIntValue() == value);
        }
    }
    
    /**
     * Defines a list of Setting objects.
     * <P> Serves as a home for various service methods
     */
    class SettingList extends ArrayList {
        void addSetting(String varName, VariableValue variable, String value) {
            Setting s = new Setting(varName, variable, value);
            add(s);
        }
        void setValues() {
            if (log.isDebugEnabled()) log.debug(" setValues in length "+size());
            for (int i = 0; i<this.size(); i++) {
                Setting s = (Setting)this.get(i);
                s.setValue();
            }
        }
        boolean match() {
            for (int i = 0; i<size(); i++) {
                if ( ! ((Setting)this.get(i)).match()) return false;
            }
            return true;
        }
    }
    
    Hashtable choiceHash = new Hashtable(); // String, String
    HashSet   variables = new HashSet(20);  // VariableValue; 20 is an arbitrary guess
    
    /**
     * Create a new possible selection.
     * @param name  Name of the choice being added
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
        SettingList s = (SettingList)choiceHash.get(choice);
        s.addSetting(varName, variable, value);

        if (variable!=null) {
            variables.add(variable);
        } else log.error("Variable pointer null when varName="+varName+" in choice "+choice+"; ignored");

    }
    
    /** 
     * Do end of initialization processing.
     */
    public void lastItem() {
        // configure the representation object
        _defaultColor = _value.getBackground();
        super.setState(READ);
        
        // note that we don't set this to COLOR_UNKNOWN!  Rather, 
        // we check the current value
        findValue();
                
        // connect to all variables to hear changes
        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            if (v==null) log.error("Variable found as null in last item");
            // connect
            v.addPropertyChangeListener(this);
        }

        // connect to the JComboBox model so we'll see changes.
        _value.setActionCommand("");            // so we can tell where change comes from
        _value.addActionListener(this);
    }

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    public Object rangeVal() {
        return new String("composite: "+_minVal+" - "+_maxVal);
    }

    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (!(e.getActionCommand().equals(""))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
        }
        if (log.isDebugEnabled()) log.debug("action event: "+e);

        // notify
        prop.firePropertyChange("Value", null, new Integer(getIntValue()));
        // Here for new values; set as needed
        selectValue(getIntValue());
    }

    /**
     * This variable doesn't change state, hence doesn't change color.
     */
    public void setState(int state) {
        if (log.isDebugEnabled()) log.debug("Ignore setState("+state+")");
    }

    /**
     * Set to a specific value.
     *<P>
     * Does this by delegating to the SettingList
     * @param value
     */
    protected void selectValue(int value) {
        if (log.isDebugEnabled()) log.debug("selectValue("+value+")");
        if (value>_value.getItemCount()-1) {
            log.error("Saw unreasonable internal value: "+value);
            return;
        }
        
        // locate SettingList for that number
        String choice = (String)_value.getItemAt(value);
        SettingList sl = (SettingList)choiceHash.get(choice);
        sl.setValues();
        
    }

    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    public Component getValue()  { return _value; }

    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);

        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            prop.firePropertyChange("Value", null, new Integer(value));
    }

    /**
     * Notify the connected CVs of a state change from above
     * by way of the variables (e.g. not direct to CVs)
     * @param state
     */
    public void setCvState(int state) {
        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            v.setCvState(state);
        }
    }

    public boolean isChanged() {
/*         Iterator i = variables.iterator(); */
/*         while (i.hasNext()) { */
/*             VariableValue v = (VariableValue) i.next(); */
/*             if (v.isChanged()) return true; */
/*         } */
        return false;
    }

    public void setToRead(boolean state) {
        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            v.setToRead(state);
        }
    }
    
    public boolean isToRead() {
/*         Iterator i = variables.iterator(); */
/*         while (i.hasNext()) { */
/*             VariableValue v = (VariableValue) i.next(); */
/*             if (v.isToRead()) return true; */
/*         } */
        return false;
    }

    public void setToWrite(boolean state) {
        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            v.setToWrite(state);
        }
    }
    
    public boolean isToWrite() {
/*         Iterator i = variables.iterator(); */
/*         while (i.hasNext()) { */
/*             VariableValue v = (VariableValue) i.next(); */
/*             if (v.isToWrite()) return true; */
/*         } */
        return false;
    }

    public void readChanges() {
         if (isChanged()) readAll();
    }

    public void writeChanges() {
         if (isChanged()) writeAll();
    }

    public void readAll() {
        setToRead(false);
        // doesn't actually do anything; variables will be naturally read
    }

    public void writeAll() {
        setToWrite(false);
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        // doesn't actually do anything; variables will be naturally written
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
                setToRead(false);
                setToWrite(false);  // some programming operation just finished
                setBusy(false);
            }
        } else if (e.getPropertyName().equals("Value")) {
            findValue();
        }
    }

    /**
     * Suspect underlying variables have changed value; check.
     * First match will succeed, so there should not be multiple 
     * matches possible. ("First match" is defined in 
     * choice-sequence)
     */
    void findValue() {
        if (log.isDebugEnabled()) log.debug("findValue invoked");
        for (int i=0; i<_value.getItemCount(); i++) {
            String choice = (String)_value.getItemAt(i);
            SettingList sl = (SettingList) choiceHash.get(choice);
            if (sl.match()) {
                if (log.isDebugEnabled()) log.debug("  match in "+i);
                _value.setSelectedItem(choice);
                return;
            }
        }
        if (log.isDebugEnabled()) log.debug("   no match");
    }
    
    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        Iterator i = variables.iterator();
        while (i.hasNext()) {
            VariableValue v = (VariableValue) i.next();
            v.removePropertyChangeListener(this);
        }

        // remove the graphical representation
        disposeReps();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CompositeVariableValue.class.getName());

}
