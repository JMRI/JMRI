package jmri.jmrit.ctc.editor.code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.SignalHeadManager;
import jmri.SignalMastManager;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;
import jmri.jmrit.ctc.editor.gui.FrmMainForm;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 *
 * The purpose of this object is to take a passed class and by using reflection
 * see if any of the public class Strings in the class has specific patterns in
 * their name(s).
 *
 * If so, that variable's contents then is passed to the JMRIConnection object
 * to see if it is valid.
 *
 */
public class CheckJMRIObject {

//  Putting these strings ANYWHERE in a string variable definition (with EXACT case!)
//  will cause this routine to try to validate it against JMRI Simple Server:
    public static final String EXTERNAL_TURNOUT = "ExternalTurnout";    // NOI18N
    public static final String EXTERNAL_SENSOR = "ExternalSensor";      // NOI18N
    public static final String EXTERNAL_BLOCK =  "ExternalBlock";       // NOI18N
    public static final String EXTERNAL_SIGNAL = "ExternalSignal";      // NOI18N

    public static enum OBJECT_TYPE { SENSOR, TURNOUT, SIGNAL, BLOCK }

    public static class VerifyClassReturnValue {
        public final String  _mFieldContents;                                // The contents
        public final OBJECT_TYPE _mObjectType;   // What it is.

        public VerifyClassReturnValue(String fieldContents, OBJECT_TYPE objectType) {
            _mFieldContents = fieldContents;
            _mObjectType = objectType;
        }

        @Override
        public String toString() {
            switch(_mObjectType) {
                case SENSOR:
                    return Bundle.getMessage("CJMRIO_Sensor") + " " + _mFieldContents + " " + Bundle.getMessage("CJMRIO_DoesntExist");  // NOI18N
                case TURNOUT:
                    return Bundle.getMessage("CJMRIO_Turnout") + " " + _mFieldContents + " " + Bundle.getMessage("CJMRIO_DoesntExist"); // NOI18N
                case SIGNAL:
                    return Bundle.getMessage("CJMRIO_Signal") + " " + _mFieldContents + " " + Bundle.getMessage("CJMRIO_DoesntExist");  // NOI18N
                case BLOCK:
                    return Bundle.getMessage("CJMRIO_Block") + " " + _mFieldContents + " " + Bundle.getMessage("CJMRIO_DoesntExist");   // NOI18N
                default:
                    break;
            }
            return "";
        }
    }

//  Quick and dirty routine for signals only:
    public boolean checkSignal(String signalName) {
        return lowLevelCheck(OBJECT_TYPE.SIGNAL, signalName);
    }

//  NOTE below on function prefix naming conventions:
//  "valid" just returns boolean if the entire object is valid (true) or not (false).  It stops scanning on first error.
//  "verify" returns a "VerifyClassReturnValue" (invalid) or null (valid) against the entire object.  It stops scanning on first error.
//  "analyze" adds entry(s) to the end of a passed errors array for ALL invalid entries.  No return value.
//  All of these work with String fields ONLY.  If the field is blank or null, it is ignored, it is up to other code
//  to determine whether that is valid or not.

    public boolean validClass(Object object) {
        return verifyClassCommon("", object) == null;
    }

    public boolean validClassWithPrefix(String prefix, Object object) {
        return verifyClassCommon(prefix, object) == null;
    }

    public VerifyClassReturnValue verifyClass(Object object) {
        return verifyClassCommon("", object);
    }

    public void analyzeClass(Object object, ArrayList<String> errors) {
        Field[] objFields = object.getClass().getDeclaredFields();
        for (Field field : objFields) { // For all fields in the class
            if (field.getType() == String.class) { // Strings only: need to check variable name:
                String fieldContent;
                try {
                    fieldContent = (String)field.get(object);
                    if (ProjectsCommonSubs.isNullOrEmptyString(fieldContent)) continue;    // Skip blank fields
                } catch (IllegalAccessException e) { continue; }    // Should never happen, if it does, just skip this field.
                VerifyClassReturnValue verifyClassReturnValue = processField(field.getName(), fieldContent);
                if (verifyClassReturnValue != null) errors.add(verifyClassReturnValue.toString());
            }
        }
    }

    private VerifyClassReturnValue verifyClassCommon(String prefix, Object object) {
        String fieldName;
        Field[] objFields = object.getClass().getDeclaredFields();
        for (Field field : objFields) { // For all fields in the class
            if (field.getType() == String.class) { // Strings only: need to check variable name:
                if ((fieldName = field.getName()).startsWith(prefix)) {
                    String fieldContent;
                    try {
                        fieldContent = (String)field.get(object);
                        if (ProjectsCommonSubs.isNullOrEmptyString(fieldContent)) continue;    // Skip blank fields
                    } catch (IllegalAccessException e) { continue; }    // Should never happen, if it does, just skip this field.
                    VerifyClassReturnValue verifyClassReturnValue = processField(fieldName, fieldContent);
                    if (verifyClassReturnValue != null) return verifyClassReturnValue;  // Error, stop and return error!
                }
            }
        }
        return null;    // All fields pass.
    }

//  Function similar to the above, EXCEPT that it is used for form processing.
//  Only JTextField's and JTable's are checked.
//  A LIST of errors is returned, i.e. it checks ALL fields.
//  Gotcha: All JTextField's in a dialog are declared "private" by the IDE, ergo the need for "field.setAccessible(true);"
    public void analyzeForm(String prefix, javax.swing.JFrame dialog, ArrayList<String> errors) {
        Field[] objFields = dialog.getClass().getDeclaredFields();
        for (Field field : objFields) { // For all fields in the class
            Class<?> fieldType = field.getType();
            if (fieldType == javax.swing.JTextField.class) { // JTextField: need to check variable name:
                String fieldName;
                if ((fieldName = field.getName()).startsWith(prefix)) {
                    String fieldContent;
                    try {
                        field.setAccessible(true);
                        fieldContent = ((javax.swing.JTextField)field.get(dialog)).getText();
                        if (ProjectsCommonSubs.isNullOrEmptyString(fieldContent)) continue;    // Skip blank fields
                    } catch (IllegalAccessException e) { continue; }    // Should never happen, if it does, just skip this field.
                    VerifyClassReturnValue verifyClassReturnValue = processField(fieldName, fieldContent);
                    if (verifyClassReturnValue != null) { // Error:
                        errors.add(verifyClassReturnValue.toString());
                    }
                }
            }
            else if (fieldType == javax.swing.JTable.class) { // JTable: need to check variable name:
                String fieldName;
                if ((fieldName = field.getName()).startsWith(prefix)) {
                    OBJECT_TYPE objectType;
                    if (fieldName.contains(EXTERNAL_TURNOUT)) objectType = OBJECT_TYPE.TURNOUT;
                    else if (fieldName.contains(EXTERNAL_SENSOR)) objectType = OBJECT_TYPE.SENSOR;
                    else if (fieldName.contains(EXTERNAL_BLOCK)) objectType = OBJECT_TYPE.BLOCK;
                    else if (fieldName.contains(EXTERNAL_SIGNAL)) objectType = OBJECT_TYPE.SIGNAL;
                    else continue;   // Nothing to check in this field, skip it.
                    DefaultTableModel defaultTableModel;
                    try {
                        field.setAccessible(true);
                        defaultTableModel = (DefaultTableModel)((javax.swing.JTable)field.get(dialog)).getModel();
                    } catch (IllegalAccessException e) { continue; }    // Should never happen, if it does, just skip this field.
                    for (int sourceIndex = 0; sourceIndex < defaultTableModel.getRowCount(); sourceIndex++) {
                        Object object = defaultTableModel.getValueAt(sourceIndex, 0);
                        if (object != null) {
                            if (ProjectsCommonSubs.isNullOrEmptyString(object.toString())) continue;    // Skip blank fields
                            String jmriObjectName = object.toString().trim();
                            if (!lowLevelCheck(objectType, jmriObjectName)) { // Invalid:
                                errors.add(new VerifyClassReturnValue(jmriObjectName, objectType).toString());
                            }
                        }
                    }
                }
            }
        }
    }

    private VerifyClassReturnValue processField(String fieldName, String fieldContent) {
        OBJECT_TYPE objectType;
        if (fieldName.contains(EXTERNAL_TURNOUT)) objectType = OBJECT_TYPE.TURNOUT;
        else if (fieldName.contains(EXTERNAL_SENSOR)) objectType = OBJECT_TYPE.SENSOR;
        else if (fieldName.contains(EXTERNAL_BLOCK)) objectType = OBJECT_TYPE.BLOCK;
        else if (fieldName.contains(EXTERNAL_SIGNAL)) objectType = OBJECT_TYPE.SIGNAL;
        else return null;   // Nothing to check in this field, OK.
        if (lowLevelCheck(objectType, fieldContent)) return null;  // Valid, OK.
//  OOPPSS, JMRI don't know about it (at this time):
        return new VerifyClassReturnValue(fieldContent, objectType);
    }

    private boolean lowLevelCheck(OBJECT_TYPE objectType, String JMRIObjectName) {
        if (!InstanceManager.getDefault(FrmMainForm.class)._mPanelLoaded) return true;
        switch(objectType) {
            case SENSOR:
                if (InstanceManager.sensorManagerInstance().getSensor(JMRIObjectName) != null) return true;
                break;
            case TURNOUT:
                if (InstanceManager.turnoutManagerInstance().getTurnout(JMRIObjectName) != null) return true;
                break;
            case SIGNAL:
                if (InstanceManager.getDefault(SignalHeadManager.class).getSignalHead(JMRIObjectName) != null) return true; // Try BOTH:
                if (InstanceManager.getDefault(SignalMastManager.class).getSignalMast(JMRIObjectName) != null) return true;
                break;
            case BLOCK:
                if (InstanceManager.getDefault(BlockManager.class).getBlock(JMRIObjectName) != null) return true;
                break;
            default:
                break;
        }
        return false;   // Either bad objectType or object doesn't exist in JMRI
    }
}
