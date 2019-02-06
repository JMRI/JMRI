package code;

import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import jmri.jmrit.ctcserialdata.CodeButtonHandlerData;
import jmri.jmrit.ctcserialdata.ProjectsCommonSubs;

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
 * Therefore this module is the "interface" between "JMRIConnection" and the
 * rest of the Editor's external JMRI variable name checking "system".
 * 
 * NOTE:
 * If the JMRI Simple Server is NOT running, then method "JMRIConnection/JMRIQueryAndResponse"
 * will ALWAYS return "true" to us, thus faking us out into thinking that the
 * object exists. This is the proper behavior when we can't verify it.
 * 
 */
public class CheckJMRIObject {
//  Putting these strings ANYWHERE in a string variable definition (with EXACT case!)
//  will cause this routine to try to validate it against JMRI Simple Server:
    public static final String EXTERNAL_TURNOUT = "ExternalTurnout";
    public static final String EXTERNAL_SENSOR = "ExternalSensor";
    public static final String EXTERNAL_BLOCK =  "ExternalBlock";
    public static final String EXTERNAL_SIGNAL = "ExternalSignal";
    
    private final JMRIConnection _mJMRIConnection;

    public class VerifyClassReturnValue {
        public final String  _mFieldContents;                                // The contents
        public final CodeButtonHandlerData.objectTypeToCheck _mObjectType;   // What it is.
        
        public VerifyClassReturnValue(String fieldContents, CodeButtonHandlerData.objectTypeToCheck objectType) {
            _mFieldContents = fieldContents;
            _mObjectType = objectType;
        }
        
        public String toString() {
            switch(_mObjectType) {
                case SENSOR:
                    return "JMRI Sensor " + _mFieldContents + " doesn't exist.";
                case TURNOUT:
                    return "JMRI Turnout " + _mFieldContents + " doesn't exist.";
                case SIGNAL:
                    return "JMRI Signal " + _mFieldContents + " doesn't exist.";
                case BLOCK:
                    return "JMRI Block " + _mFieldContents + " doesn't exist.";
            }
            return "";
        }
    }
    
    public CheckJMRIObject(JMRIConnection jmriConnection) {
        _mJMRIConnection = jmriConnection;
    }
    
//  Quick and dirty routine for signals only:    
    public boolean checkSignal(String signalName) {
        return _mJMRIConnection.JMRIQueryAndResponse(CodeButtonHandlerData.objectTypeToCheck.SIGNAL, signalName);  // Valid, OK.
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
    public void analyzeForm(String prefix, javax.swing.JDialog dialog, ArrayList<String> errors) {
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
                    CodeButtonHandlerData.objectTypeToCheck itemTypeToCheck;
                    if (fieldName.contains(EXTERNAL_TURNOUT)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.TURNOUT;
                    else if (fieldName.contains(EXTERNAL_SENSOR)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.SENSOR;
                    else if (fieldName.contains(EXTERNAL_BLOCK)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.BLOCK;
                    else if (fieldName.contains(EXTERNAL_SIGNAL)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.SIGNAL;
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
                            if (!_mJMRIConnection.JMRIQueryAndResponse(itemTypeToCheck, jmriObjectName)) { // Invalid:
                                errors.add(new VerifyClassReturnValue(jmriObjectName, itemTypeToCheck).toString());
                            }
                        }
                    }
                }
            }
        }
    }
    
    private VerifyClassReturnValue processField(String fieldName, String fieldContent) {
        CodeButtonHandlerData.objectTypeToCheck itemTypeToCheck;
        if (fieldName.contains(EXTERNAL_TURNOUT)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.TURNOUT;
        else if (fieldName.contains(EXTERNAL_SENSOR)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.SENSOR;
        else if (fieldName.contains(EXTERNAL_BLOCK)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.BLOCK;
        else if (fieldName.contains(EXTERNAL_SIGNAL)) itemTypeToCheck = CodeButtonHandlerData.objectTypeToCheck.SIGNAL;
        else return null;   // Nothing to check in this field, OK.
        if (_mJMRIConnection.JMRIQueryAndResponse(itemTypeToCheck, fieldContent)) return null;  // Valid, OK.
//  OOPPSS, JMRI don't know about it (at this time):
        return new VerifyClassReturnValue(fieldContent, itemTypeToCheck);
    }
}
